package com.atguigu.gmall.pms.controller;

import java.util.List;

import com.atguigu.gmall.pms.vo.GroupVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.atguigu.gmall.pms.entity.AttrGroupEntity;
import com.atguigu.gmall.pms.service.AttrGroupService;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.common.bean.PageParamVo;

/**
 * 属性分组
 *
 * @author gouge
 * @email gouge@atguigu.com
 * @date 2021-06-22 17:55:57
 */
@Api(tags = "属性分组 管理")
@RestController
@RequestMapping("pms/attrgroup")
public class AttrGroupController {
    @Autowired
    private AttrGroupService attrGroupService;


    //根据分类id、spuId、skuId查询出所有的规格参数组及组下的规格参数和值
    @GetMapping("with/attr/value/{cid}")
    public ResponseVo<List<GroupVo>> queryGroupsWithAttrValuesByCidAndSpuIdAndSkuId(
            @PathVariable("cid") Long cid,
            @RequestParam("spuId") Long spuId,
            @RequestParam("skuId") Long skuId
    ){
        List<GroupVo> groupVos = this.attrGroupService.queryGroupsWithAttrValuesByCidAndSpuIdAndSkuId(cid,spuId,skuId);
        return ResponseVo.ok(groupVos);
    }


    @GetMapping("withattrs/{catId}")
    public ResponseVo<List<AttrGroupEntity>> queryGropupsWithAttrsByCid(@PathVariable("catId")Long catId){
       List<AttrGroupEntity> groupEntities = attrGroupService.queryGropupsWithAttrsByCid(catId);
       return ResponseVo.ok(groupEntities);
    }

    @GetMapping("category/{cid}")
    public ResponseVo<List<AttrGroupEntity>> queryAttrGroupEntityByCid(@PathVariable("cid") Long cid){
        List<AttrGroupEntity> attrGroupEntities = attrGroupService.list(new QueryWrapper<AttrGroupEntity>().eq("category_id", cid));
        return ResponseVo.ok(attrGroupEntities);
    }

    /**
     * 列表
     */
    @GetMapping
    @ApiOperation("分页查询")
    public ResponseVo<PageResultVo> queryAttrGroupByPage(PageParamVo paramVo){
        PageResultVo pageResultVo = attrGroupService.queryPage(paramVo);

        return ResponseVo.ok(pageResultVo);
    }


    /**
     * 信息
     */
    @GetMapping("{id}")
    @ApiOperation("详情查询")
    public ResponseVo<AttrGroupEntity> queryAttrGroupById(@PathVariable("id") Long id){
		AttrGroupEntity attrGroup = attrGroupService.getById(id);

        return ResponseVo.ok(attrGroup);
    }

    /**
     * 保存
     */
    @PostMapping
    @ApiOperation("保存")
    public ResponseVo<Object> save(@RequestBody AttrGroupEntity attrGroup){
		attrGroupService.save(attrGroup);

        return ResponseVo.ok();
    }

    /**
     * 修改
     */
    @PostMapping("/update")
    @ApiOperation("修改")
    public ResponseVo update(@RequestBody AttrGroupEntity attrGroup){
		attrGroupService.updateById(attrGroup);

        return ResponseVo.ok();
    }

    /**
     * 删除
     */
    @PostMapping("/delete")
    @ApiOperation("删除")
    public ResponseVo delete(@RequestBody List<Long> ids){
		attrGroupService.removeByIds(ids);

        return ResponseVo.ok();
    }




}
