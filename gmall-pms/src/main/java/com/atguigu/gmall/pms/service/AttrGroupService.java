package com.atguigu.gmall.pms.service;

import com.atguigu.gmall.pms.vo.GroupVo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.pms.entity.AttrGroupEntity;

import java.util.List;

/**
 * 属性分组
 *
 * @author gouge
 * @email gouge@atguigu.com
 * @date 2021-06-22 17:55:57
 */
public interface AttrGroupService extends IService<AttrGroupEntity> {

    PageResultVo queryPage(PageParamVo paramVo);

    List<AttrGroupEntity> queryGropupsWithAttrsByCid(Long catId);
    //根据分类id、spuId、skuId查询出所有的规格参数组及组下的规格参数和值
    List<GroupVo> queryGroupsWithAttrValuesByCidAndSpuIdAndSkuId(Long cid, Long spuId, Long skuId);
}

