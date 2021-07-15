package com.atguigu.gmall.pms.api;

import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.pms.vo.GroupVo;
import com.atguigu.gmall.pms.vo.SaleAttrValueVo;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import java.util.List;

public interface GmallPmsApi {
    @GetMapping("pms/spu")
    public ResponseVo<PageResultVo> querySpuByPage(PageParamVo paramVo);

    @PostMapping("pms/spu/page")
    public ResponseVo<List<SpuEntity>> querySpuByPageJson(@RequestBody PageParamVo paramVo);

    @GetMapping("pms/spu/{id}")
    @ApiOperation("详情查询")
    public ResponseVo<SpuEntity> querySpuById(@PathVariable("id") Long id);

    //根据spuId查询spu的描述信息
    @GetMapping("pms/spudesc/{spuId}")
    @ApiOperation("详情查询")
    public ResponseVo<SpuDescEntity> querySpuDescById(@PathVariable("spuId") Long spuId);



    @GetMapping("pms/sku/spu/{spuId}")
    public ResponseVo<List<SkuEntity>> querySkuEntityBySpuId(@PathVariable("spuId")Long spuId);

    @GetMapping("pms/sku/{id}")
    public ResponseVo<SkuEntity> querySkuById(@PathVariable("id") Long id);

    @GetMapping("pms/brand/{id}")
    public ResponseVo<BrandEntity> queryBrandById(@PathVariable("id") Long id);

    @GetMapping("pms/category/{id}")
    public ResponseVo<CategoryEntity> queryCategoryById(@PathVariable("id") Long id);

    @GetMapping("pms/category/parent/{parentId}")
    public ResponseVo<List<CategoryEntity>>  queryCategoriesByPid(@PathVariable("parentId") Long parentId);
        //根据一级分类查询二级三级分类
    @GetMapping("pms/category/subs/{pid}")
    public ResponseVo<List<CategoryEntity>> queryLvl2WithSubsByPid(@PathVariable("pid") Long pid);

    //根据三积分类的id查询123级的分类
    @GetMapping("pms/category/sub/{cid3}")
    public ResponseVo<List<CategoryEntity>> queryLv123CategoriesByCid3(@PathVariable("cid3")Long cid);

    @GetMapping("pms/skuattrvalue/search/{cid}")
    public ResponseVo<List<SkuAttrValueEntity>> querySearchAttrValuesBySkuId(
            @PathVariable("cid") Long  cid,
            @RequestParam("skuId") Long skuId
    );

    //根据spuId所有销售属性组合和skuId的映射关系
    @GetMapping("pms/skuattrvalue/mapping/{spuId}")
    public ResponseVo<String> queryMappingBySpuId(@PathVariable("spuId")Long spuId);

    //根据spuId查询spu下所有销售属性的可取值
    @GetMapping("pms/skuattrvalue/spu/{spuId}")
    public ResponseVo<List<SaleAttrValueVo>> querySaleAttrValuesBySpuId(@PathVariable("spuId")Long spuId);

    // 根据skuId查询当前sku的销售属性
    @GetMapping("pms/skuattrvalue/sku/{skuId}")
    public ResponseVo<List<SkuAttrValueEntity>> querySkuAttrValuesBySkuId(@PathVariable("skuId")Long skuId);

    @GetMapping("pms/spuattrvalue/search/{cid}")
    public ResponseVo<List<SpuAttrValueEntity>> querySearchAttrValuesBySpuId(
            @PathVariable("cid") Long cid,
            @RequestParam("spuId") Long spuId
    );

    //根据skuId查询sku的图片列表
    @GetMapping("pms/skuimages/sku/{skuId}")
    public ResponseVo<List<SkuImagesEntity>> querySkuImagesBySkuId(@PathVariable("skuId")Long skuId);

    //根据分类id、spuId、skuId查询出所有的规格参数组及组下的规格参数和值
    @GetMapping("pms/attrgroup/with/attr/value/{cid}")
    public ResponseVo<List<GroupVo>> queryGroupsWithAttrValuesByCidAndSpuIdAndSkuId(
            @PathVariable("cid") Long cid,
            @RequestParam("spuId") Long spuId,
            @RequestParam("skuId") Long skuId
    );

}
