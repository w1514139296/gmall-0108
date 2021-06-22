package com.atguigu.gmall.pms.mapper;

import com.atguigu.gmall.pms.entity.CategoryBrandEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 品牌分类关联
 * 
 * @author gouge
 * @email gouge@atguigu.com
 * @date 2021-06-22 17:55:57
 */
@Mapper
public interface CategoryBrandMapper extends BaseMapper<CategoryBrandEntity> {
	
}
