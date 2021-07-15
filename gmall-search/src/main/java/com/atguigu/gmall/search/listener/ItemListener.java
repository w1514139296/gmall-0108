package com.atguigu.gmall.search.listener;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.search.feign.GmallPmsClient;
import com.atguigu.gmall.search.feign.GmallWmsClient;
import com.atguigu.gmall.search.pojo.Goods;
import com.atguigu.gmall.search.pojo.SearchAttrVo;
import com.atguigu.gmall.search.repository.GoodsRepository;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ItemListener {

    @Autowired
    private GmallPmsClient gmallPmsClient;

    @Autowired
    private GmallWmsClient gmallWmsClient;

    @Autowired
    private GoodsRepository goodsRepository;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue("SEARCH_ITEM_QUEUE"),
            exchange = @Exchange(value = "PMS_ITEM_EXCHANGE",ignoreDeclarationExceptions = "true",type = ExchangeTypes.TOPIC),
            key = {"item.insert"}
    ))
    public void listen(Long spuId, Channel channel, Message message) throws IOException {
        //判断是否是无效消息或者垃圾消息
        if (spuId==null){
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            return;
        }

        //根据spuId查询spu
        ResponseVo<SpuEntity> spuEntityResponseVo = this.gmallPmsClient.querySpuById(spuId);
        SpuEntity spuEntity = spuEntityResponseVo.getData();
        if (spuEntity==null){//如果根据spuId查询spu  结果为空  说明该spuId是无效的消息
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            return;
        }


        ResponseVo<List<SkuEntity>> skuResponseVo = this.gmallPmsClient.querySkuEntityBySpuId(spuId);
        List<SkuEntity> skuEntities = skuResponseVo.getData();
        if (!CollectionUtils.isEmpty(skuEntities)) {

            //查询品牌和分类  由于同一个spu品牌和分类是一样的  所以在spu的遍历中查询品牌和分类
            ResponseVo<BrandEntity> brandEntityResponseVo = this.gmallPmsClient.queryBrandById(spuEntity.getBrandId());
            BrandEntity brandEntity = brandEntityResponseVo.getData();
            ResponseVo<CategoryEntity> categoryEntityResponseVo = this.gmallPmsClient.queryCategoryById(spuEntity.getCategoryId());
            CategoryEntity categoryEntity = categoryEntityResponseVo.getData();


            //把spu下所有的sku转化成goods 导入到es
            List<Goods> goodsList = skuEntities.stream().map(skuEntity -> {
                Goods goods = new Goods();
                //sku的相关信息
                goods.setSkuId(skuEntity.getId());
                goods.setDefaultImage(skuEntity.getDefaultImage());
                goods.setTitle(skuEntity.getTitle());
                goods.setSubTitle(skuEntity.getSubtitle());
                goods.setPrice(skuEntity.getPrice().doubleValue());

                //创建时间
                goods.setCreateTime(spuEntity.getCreateTime());

                //销量和库存
                ResponseVo<List<WareSkuEntity>> wareResponseVo = this.gmallWmsClient.queryWareSkuEntityBySkuId(skuEntity.getId());
                List<WareSkuEntity> wareSkuEntities = wareResponseVo.getData();
                if (!CollectionUtils.isEmpty(wareSkuEntities)) {
                    goods.setSales(wareSkuEntities.stream().mapToLong(WareSkuEntity::getSales).reduce((a, b) -> a + b).getAsLong());
                    goods.setStore(wareSkuEntities.stream().anyMatch(wareSkuEntity -> wareSkuEntity.getStock() - wareSkuEntity.getStockLocked()>0));
                }
                //品牌
                if (brandEntity!=null){
                    goods.setBrandId(brandEntity.getId());
                    goods.setBrandName(brandEntity.getName());
                    goods.setLogo(brandEntity.getLogo());
                }
                //分类
                if (categoryEntity!=null){
                    goods.setCategoryId(categoryEntity. getId());
                    goods.setCategoryName(categoryEntity.getName());
                }

                //检索类型的规格参数
                List<SearchAttrVo> attrValueVos = new ArrayList<>();
                //sku中的检索类型的规格参数
                ResponseVo<List<SkuAttrValueEntity>> saleAttrResponseVo = this.gmallPmsClient.querySearchAttrValuesBySkuId(skuEntity.getCategoryId(), skuEntity.getId());
                List<SkuAttrValueEntity> skuAttrValueEntities = saleAttrResponseVo.getData();
                if (!CollectionUtils.isEmpty(skuAttrValueEntities)){
                    attrValueVos.addAll(skuAttrValueEntities.stream().map(skuAttrValueEntity -> {
                        SearchAttrVo searchAttrVo = new SearchAttrVo();
                        BeanUtils.copyProperties(skuAttrValueEntity, searchAttrVo);
                        return searchAttrVo;
                    }).collect(Collectors.toList()));
                }
                //spu中的检索类型的规格参数
                ResponseVo<List<SpuAttrValueEntity>> baseAttrResponseVo = this.gmallPmsClient.querySearchAttrValuesBySpuId(skuEntity.getCategoryId(), spuEntity.getId());
                List<SpuAttrValueEntity> spuAttrValueEntities = baseAttrResponseVo.getData();
                if (!CollectionUtils.isEmpty(spuAttrValueEntities)){
                    attrValueVos.addAll(spuAttrValueEntities.stream().map(spuAttrValueEntity -> {
                        SearchAttrVo searchAttrVo = new SearchAttrVo();
                        BeanUtils.copyProperties(spuAttrValueEntity, searchAttrVo);
                        return searchAttrVo;
                    }).collect(Collectors.toList()));
                }

                goods.setSearchAttrs(attrValueVos);

                return goods;
            }).collect(Collectors.toList());

            this.goodsRepository.saveAll(goodsList);
        }

        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }
}
