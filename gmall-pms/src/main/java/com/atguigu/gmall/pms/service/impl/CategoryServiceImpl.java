package com.atguigu.gmall.pms.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;

import com.atguigu.gmall.pms.mapper.CategoryMapper;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import com.atguigu.gmall.pms.service.CategoryService;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, CategoryEntity> implements CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<CategoryEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageResultVo(page);
    }

    @Override
    public List<CategoryEntity> queryCategoriesByPid(Long parentId) {
        QueryWrapper<CategoryEntity> wrapper = new QueryWrapper<>();

        if (parentId!=-1){
            wrapper.eq("parent_id", parentId);
        }
        return this.list(wrapper);
    }

    //根据一级分类查询二级三级分类
    @Override
    public List<CategoryEntity> queryLvl2WithSubsByPid(Long pid) {
        return this.categoryMapper.queryLvl2WithSubsByPid(pid);

    }
    //根据3级分类查询一级二级三级分类
    @Override
    public List<CategoryEntity> queryLv123CategoriesByCid3(Long cid) {
        //查询三级分类
        CategoryEntity categoryEntity3 = this.getById(cid);
        if (categoryEntity3==null){
            return null;
        }
        //查询二级分类
        CategoryEntity categoryEntity2 = this.getById(categoryEntity3.getParentId());
        //查询一级分类
        CategoryEntity categoryEntity1 = this.getById(categoryEntity2.getParentId());

        return Arrays.asList(categoryEntity1,categoryEntity2,categoryEntity3);
    }

}