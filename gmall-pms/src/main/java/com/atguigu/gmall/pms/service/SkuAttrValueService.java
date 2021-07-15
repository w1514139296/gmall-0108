package com.atguigu.gmall.pms.service;

import com.atguigu.gmall.pms.vo.SaleAttrValueVo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.pms.entity.SkuAttrValueEntity;

import java.util.List;

/**
 * sku销售属性&值
 *
 * @author gouge
 * @email gouge@atguigu.com
 * @date 2021-06-22 17:55:57
 */
public interface SkuAttrValueService extends IService<SkuAttrValueEntity> {

    PageResultVo queryPage(PageParamVo paramVo);

    List<SkuAttrValueEntity> querySearchAttrValuesBySkuId(Long cid, Long skuId);
    //根据spuId查询spu下所有销售属性的可取值
    List<SaleAttrValueVo> querySaleAttrValuesBySpuId(Long spuId);
    //根据spuId所有销售属性组合和skuId的映射关系
    String queryMappingBySpuId(Long spuId);
}

