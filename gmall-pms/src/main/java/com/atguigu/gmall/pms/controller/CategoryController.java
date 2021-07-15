package com.atguigu.gmall.pms.controller;

import java.util.List;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.atguigu.gmall.pms.entity.CategoryEntity;
import com.atguigu.gmall.pms.service.CategoryService;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.common.bean.PageParamVo;

/**
 * 商品三级分类
 *
 * @author gouge
 * @email gouge@atguigu.com
 * @date 2021-06-22 17:55:57
 */
@Api(tags = "商品三级分类 管理")
@RestController
@RequestMapping("pms/category")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;


    //根据三积分类的id查询123级的分类
    @GetMapping("sub/{cid3}")
    public ResponseVo<List<CategoryEntity>> queryLv123CategoriesByCid3(@PathVariable("cid3")Long cid){
        List<CategoryEntity> categoryEntities = this.categoryService.queryLv123CategoriesByCid3(cid);
        return ResponseVo.ok(categoryEntities);
    }


    //根据一级分类查询二级三级分类
    @GetMapping("subs/{pid}")
    public ResponseVo<List<CategoryEntity>> queryLvl2WithSubsByPid(@PathVariable("pid") Long pid){
        List<CategoryEntity> categoryEntities = this.categoryService.queryLvl2WithSubsByPid(pid);
        return ResponseVo.ok(categoryEntities);
    }

    /**
     * 列表
     */
    @GetMapping
    @ApiOperation("分页查询")
    public ResponseVo<PageResultVo> queryCategoryByPage(PageParamVo paramVo){
        PageResultVo pageResultVo = categoryService.queryPage(paramVo);

        return ResponseVo.ok(pageResultVo);
    }


    /**
     * 信息
     */
    @GetMapping("{id}")
    @ApiOperation("详情查询")
    public ResponseVo<CategoryEntity> queryCategoryById(@PathVariable("id") Long id){
		CategoryEntity category = categoryService.getById(id);

        return ResponseVo.ok(category);
    }

    /**
     * 保存
     */
    @PostMapping
    @ApiOperation("保存")
    public ResponseVo<Object> save(@RequestBody CategoryEntity category){
		categoryService.save(category);

        return ResponseVo.ok();
    }

    /**
     * 修改
     */
    @PostMapping("/update")
    @ApiOperation("修改")
    public ResponseVo update(@RequestBody CategoryEntity category){
		categoryService.updateById(category);

        return ResponseVo.ok();
    }

    /**
     * 删除
     */
    @PostMapping("/delete")
    @ApiOperation("删除")
    public ResponseVo delete(@RequestBody List<Long> ids){
		categoryService.removeByIds(ids);

        return ResponseVo.ok();
    }

    @GetMapping("/parent/{parentId}")
    public ResponseVo<List<CategoryEntity>>  queryCategoriesByPid(@PathVariable("parentId") Long parentId){
        List<CategoryEntity> categoryEntities = categoryService.queryCategoriesByPid(parentId);
        return ResponseVo.ok(categoryEntities);
    }

}
