package com.atguigu.gmall.pms.service.impl;

import com.atguigu.gmall.pms.entity.AttrEntity;
import com.atguigu.gmall.pms.entity.SkuAttrValueEntity;
import com.atguigu.gmall.pms.entity.SpuAttrValueEntity;
import com.atguigu.gmall.pms.mapper.AttrMapper;
import com.atguigu.gmall.pms.mapper.SkuAttrValueMapper;
import com.atguigu.gmall.pms.mapper.SpuAttrValueMapper;
import com.atguigu.gmall.pms.vo.AttrValueVo;
import com.atguigu.gmall.pms.vo.GroupVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;

import com.atguigu.gmall.pms.mapper.AttrGroupMapper;
import com.atguigu.gmall.pms.entity.AttrGroupEntity;
import com.atguigu.gmall.pms.service.AttrGroupService;
import org.springframework.util.CollectionUtils;


@Service("attrGroupService")
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupMapper, AttrGroupEntity> implements AttrGroupService {
    @Autowired
    private AttrMapper attrMapper;

    @Autowired
    private SpuAttrValueMapper spuAttrValueMapper;

    @Autowired
    private SkuAttrValueMapper skuAttrValueMapper;

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<AttrGroupEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<AttrGroupEntity>()
        );

        return new PageResultVo(page);
    }

    @Override
    public List<AttrGroupEntity> queryGropupsWithAttrsByCid(Long catId) {
        //先根据分类的Id查询组列表
        List<AttrGroupEntity> groupEntities = this.list(new QueryWrapper<AttrGroupEntity>().eq("category_id", catId));
        //遍历所有的组查询组下的规格参数
        if (CollectionUtils.isEmpty(groupEntities)) {
            return null;
        }
        groupEntities.forEach(attrGroupEntity -> {
            List<AttrEntity> attrEntities = this.attrMapper.selectList(new QueryWrapper<AttrEntity>()
                    .eq("group_id", attrGroupEntity.getId())
                    .eq("type", 1));
            attrGroupEntity.setAttrEntities(attrEntities);
        });
        return groupEntities;

    }

    //根据分类id、spuId、skuId查询出所有的规格参数组及组下的规格参数和值
    @Override
    public List<GroupVo> queryGroupsWithAttrValuesByCidAndSpuIdAndSkuId(Long cid, Long spuId, Long skuId) {
        //根据cid查询分组集合
        List<AttrGroupEntity> groupEntities = this.list(new QueryWrapper<AttrGroupEntity>().eq("category_id", cid));
        if (CollectionUtils.isEmpty(groupEntities)) {
            return null;
        }
        //遍历分组查询组下的规格参数
        return groupEntities.stream().map(attrGroupEntity -> {
            GroupVo groupVo = new GroupVo();
            groupVo.setGroupName(attrGroupEntity.getName());
            //查询组下的规格参数
            List<AttrEntity> attrEntities = this.attrMapper.selectList(new QueryWrapper<AttrEntity>().eq("group_id", attrGroupEntity.getId()));
            if (!CollectionUtils.isEmpty(attrEntities)) {
                List<AttrValueVo> attrValueVos = attrEntities.stream().map(attrEntity -> {
                    AttrValueVo attrValueVo = new AttrValueVo();
                    //设置规格参数的id和名称
                    attrValueVo.setAttrId(attrEntity.getId());
                    attrValueVo.setAttrName(attrEntity.getName());

                    if (attrEntity.getType() == 1) {
                        SpuAttrValueEntity spuAttrValueEntity = this.spuAttrValueMapper.selectOne(new QueryWrapper<SpuAttrValueEntity>().eq("spu_id", spuId).eq("attr_id", attrEntity.getId()));
                        if (spuAttrValueEntity != null) {
                            attrValueVo.setAttrValue(spuAttrValueEntity.getAttrValue());
                        }
                    } else {
                        SkuAttrValueEntity skuAttrValueEntity = this.skuAttrValueMapper.selectOne(new QueryWrapper<SkuAttrValueEntity>().eq("sku_id", skuId).eq("attr_id", attrEntity.getId()));
                        if (skuAttrValueEntity != null) {
                            attrValueVo.setAttrValue(skuAttrValueEntity.getAttrValue());
                        }
                    }
                    return attrValueVo;
                }).collect(Collectors.toList());
                groupVo.setAttrs(attrValueVos);
            }
            return groupVo;
        }).collect(Collectors.toList());
    }
}