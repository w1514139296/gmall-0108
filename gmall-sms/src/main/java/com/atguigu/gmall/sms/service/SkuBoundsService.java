package com.atguigu.gmall.sms.service;

import com.atguigu.gmall.sms.vo.ItemSalesVo;
import com.atguigu.gmall.sms.vo.SkuSaleVo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.sms.entity.SkuBoundsEntity;

import java.util.List;
import java.util.Map;

/**
 * 商品spu积分设置
 *
 * @author gouge
 * @email gouge@atguigu.com
 * @date 2021-06-22 18:12:00
 */
public interface SkuBoundsService extends IService<SkuBoundsEntity> {

    PageResultVo queryPage(PageParamVo paramVo);

    void saveSales(SkuSaleVo skuSaleVo);
    //根据skuId查询营销信息
    List<ItemSalesVo> queryItemSalesBySkuId(Long skuId);
}

