package com.atguigu.gmall.pms.service.impl;

import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.pms.feign.GmallSmsClient;
import com.atguigu.gmall.pms.mapper.SkuMapper;
import com.atguigu.gmall.pms.mapper.SpuDescMapper;
import com.atguigu.gmall.pms.service.*;

import com.atguigu.gmall.pms.vo.SkuVo;
import com.atguigu.gmall.pms.vo.SpuAttrValueVo;
import com.atguigu.gmall.pms.vo.SpuVo;
import com.atguigu.gmall.sms.vo.SkuSaleVo;
import io.seata.spring.annotation.GlobalTransactional;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;

import com.atguigu.gmall.pms.mapper.SpuMapper;
import org.springframework.util.CollectionUtils;


@Service("spuService")
public class SpuServiceImpl extends ServiceImpl<SpuMapper, SpuEntity> implements SpuService {
    @Autowired
    private SpuDescMapper spuDescMapper;

    @Autowired
    private SpuAttrValueService spuAttrValueService;

    @Autowired
    private SkuMapper skuMapper;

    @Autowired
    private SkuImagesService skuImagesService;

    @Autowired
    private SkuAttrValueService skuAttrValueService;

    @Autowired
    private GmallSmsClient gmallSmsClient;

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<SpuEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<SpuEntity>()
        );

        return new PageResultVo(page);
    }

    @Override
    public PageResultVo querySpuByCidAndPage(PageParamVo paramVo, Long categoryId) {

        QueryWrapper<SpuEntity> wrapper = new QueryWrapper<>();
        //如果categoryId不为空 就需要查询本类
        if (categoryId != 0) {
            wrapper.eq("category_id", categoryId);
        }
        //关键字
        String key = paramVo.getKey();
        if (StringUtils.isNotBlank(key)) {
            //默认情况下 wrapper后面直接写条件  默认条件是and关系 并且没有括号
            wrapper.and(t -> t.eq("id", key).or().like("name", key));
        }
        IPage<SpuEntity> page = this.page(
                paramVo.getPage(),
                wrapper
        );

        return new PageResultVo(page);
    }

    @Override
    @GlobalTransactional
    public void bigSave(SpuVo spu) {
        //1.先保存spu相关的三张表
        //1.1保存pms_spu
        spu.setCreateTime(new Date());
        spu.setUpdateTime(spu.getCreateTime());
        this.save(spu);
        Long spuId = spu.getId();
        //1.2保存pms_spu_desc
        List<String> spuImages = spu.getSpuImages();
        if (!CollectionUtils.isEmpty(spuImages)) {
            SpuDescEntity spuDescEntity = new SpuDescEntity();
            spuDescEntity.setSpuId(spuId);
            spuDescEntity.setDecript(StringUtils.join(spuImages, ","));
            spuDescMapper.insert(spuDescEntity);
        }

        //1.3保存pms_spu_attr_value
        List<SpuAttrValueVo> baseAttrs = spu.getBaseAttrs();
        if (!CollectionUtils.isEmpty(baseAttrs)) {
            //把SpuAttrValueVo转化为SpuAttrValueEntity集合
            List<SpuAttrValueEntity> spuAttrValueEntites = baseAttrs.stream()
                    .filter(spuAttrValueVo -> spuAttrValueVo.getAttrValue() != null)
                    .map(spuAttrValueVo -> {
                        SpuAttrValueEntity spuAttrValueEntity = new SpuAttrValueEntity();
                        BeanUtils.copyProperties(spuAttrValueVo, spuAttrValueEntity);
                        spuAttrValueEntity.setSpuId(spuId);
                        return spuAttrValueEntity;
                    }).collect(Collectors.toList());
            spuAttrValueService.saveBatch(spuAttrValueEntites);
        }

        //2.保存sku相关的三张表
        List<SkuVo> skus = spu.getSkus();

        if (CollectionUtils.isEmpty(skus)){
            return;
        }
        //遍历sku保存到pms_sku
        skus.forEach(skuVo -> {
            //2.1保存pms_sku
            skuVo.setSpuId(spuId);
            skuVo.setCategoryId(spu.getCategoryId());
            skuVo.setBrandId(spu.getBrandId());
            //获取页面图片列表
            List<String> images = skuVo.getImages();
            if (!CollectionUtils.isEmpty(images)){
                //获取第一张图片作为默认图片
                skuVo.setDefaultImage(StringUtils.isBlank(skuVo.getDefaultImage())? images.get(0):skuVo.getDefaultImage());
            }
            this.skuMapper.insert(skuVo);
            Long skuId = skuVo.getId();
            //2.2保存pms_sku_images
            if (!CollectionUtils.isEmpty(images)){
              this.skuImagesService.saveBatch(images.stream().map(image->{
                    SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
                    skuImagesEntity.setSkuId(skuId);
                    skuImagesEntity.setUrl(image);
                    //如果当前图片的地址和sku的默认图片的地址相同 则设置为1否则设置为0
                    skuImagesEntity.setDefaultStatus(StringUtils.equals(skuVo.getDefaultImage(), image)? 1:0);
                    return skuImagesEntity;
                }).collect(Collectors.toList()));
            }

            //2.3保存pms_sku_attr_value
            List<SkuAttrValueEntity> saleAttrs = skuVo.getSaleAttrs();
            if (!CollectionUtils.isEmpty(saleAttrs)){
                saleAttrs.forEach(skuAttrValueEntity -> {
                    skuAttrValueEntity.setSkuId(skuId);
                });
                skuAttrValueService.saveBatch(saleAttrs);
            }

//            int i = 1 /0;

            //保存营销信息相关的三张表
            SkuSaleVo skuSaleVo = new SkuSaleVo();
            BeanUtils.copyProperties(skuVo, skuSaleVo);
            skuSaleVo.setSkuId(skuId);
            this.gmallSmsClient.saveSales(skuSaleVo);
        });


    }
}
//    public static void main(String[] args) {
//        List<User> users = Arrays.asList(
//                new User("liuyan",20,false),
//                new User("marong",21,false),
//                new User("xiaolu",22,false),
//                new User("laowang",23,true),
//                new User("xiaoliang",24,true),
//                new User("zhengshuang",25,false),
//                new User("xiaozhu",26,true)
//        );
//map方法 可以吧一个集合转化为另一个集合
//        users.stream().map(user -> user.getUsername()).collect(Collectors.toList()).forEach(System.out::println);
//        users.stream().map(user -> {
//            Person person = new Person();
//            person.setAge(user.getAge());
//            person.setName(user.getUsername());
//            return person;
//        }).collect(Collectors.toList()).forEach(System.out::println);


//filter  可以过滤出需要的元素  组成一个新的集合
//        users.stream().filter(user -> user.getAge()>22).collect(Collectors.toList()).forEach(System.out::println);
//        users.stream().filter(user -> !user.getSex()).collect(Collectors.toList()).forEach(System.out::println);
//reduce: 求和
//        List<Integer> arrs = Arrays.asList(21,22,23,24,25);
//        System.out.println(arrs.stream().reduce((a, b) -> a + b).get());
//        List<Integer> userAges = users.stream().map(user -> user.getAge()).collect(Collectors.toList());
//        System.out.println(userAges.stream().reduce((a, b) -> a + b).get());
//    }
//}
//
//@Data
//@AllArgsConstructor
//@NoArgsConstructor
//class User{
//    private String username;
//    private Integer age;
//    private Boolean sex;
//}
//
//@Data
//class Person{
//    private String name;
//    private Integer age;
//}