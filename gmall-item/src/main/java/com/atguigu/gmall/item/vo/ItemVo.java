package com.atguigu.gmall.item.vo;

import com.atguigu.gmall.pms.entity.CategoryEntity;
import com.atguigu.gmall.pms.entity.SkuImagesEntity;
import com.atguigu.gmall.pms.vo.GroupVo;
import com.atguigu.gmall.pms.vo.SaleAttrValueVo;
import com.atguigu.gmall.sms.vo.ItemSalesVo;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
public class ItemVo {

    //面包屑所需要的参数
    //123级分类
    private List<CategoryEntity> categories;
    //品牌信息
    private Long brandId;
    private String brandName;
    //spu信息
    private String  spuName;
    private Long spuId;

    //商品详情页中间的详情信息
    private Long skuId;
    private String title;
    private String subTitle;
    private BigDecimal price;
    private Integer weight;
    private String defaultImage;

    //营销信息
    private List<ItemSalesVo> sales;

    //是否有货
    private Boolean store = false;

    //sku图片列表
    private List<SkuImagesEntity> images;

    //销售属性列表
    //"[{attrId:3,attrName:'颜色',attrValues:['白色','黑色']},
//    {attrId:4,attrName:'内存',attrValues:['8G','12G']},
//    {attrId:5,attrName:'存储',attrValues:['256G','512G']}
    // ]"
    private List<SaleAttrValueVo> saleAttrs;


    //查询当前sku 的销售属性：{3：'白色'，4：'12G'，5：'256G'}
    private Map<Long,String> saleAttr;

    //为了方便页面跳转  需要销售属性组合与skuId的映射关系
    //{'白色','8G'，'256G':100;'白色','8G'，'512G':101}
    private String skuJsons;

    //商品描述
    private List<String> spuImages;

    //规格参数分组列表
    private List<GroupVo> groups;
}
