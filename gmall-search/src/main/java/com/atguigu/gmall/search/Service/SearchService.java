package com.atguigu.gmall.search.Service;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.pms.entity.BrandEntity;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import com.atguigu.gmall.search.pojo.Goods;
import com.atguigu.gmall.search.pojo.SearchParamVo;
import com.atguigu.gmall.search.pojo.SearchResponseAttrValueVo;
import com.atguigu.gmall.search.pojo.SearchResponseVo;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SearchService {
    @Autowired
    private RestHighLevelClient restHighLevelClient;

    public SearchResponseVo search(SearchParamVo paramVo) {

        try {
            SearchResponse response = this.restHighLevelClient.search(new SearchRequest(new String[]{"goods"}, buildDsl(paramVo)), RequestOptions.DEFAULT);
            System.out.println(response);

            //分页参数在请求参数中
            SearchResponseVo responseVo = this.parseResult(response);
            responseVo.setPageNum(paramVo.getPageNum());
            responseVo.setPageSize(paramVo.getPageSize());

            return responseVo;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    private SearchResponseVo parseResult(SearchResponse response){
        SearchResponseVo responseVo = new SearchResponseVo();
        //获取hits结果集
        SearchHits hits = response.getHits();
        responseVo.setTotal(hits.getTotalHits());
        SearchHit[] hitsHits = hits.getHits();
        List<Goods> goodsList = Arrays.stream(hitsHits).map(hitsHit->{
            String json = hitsHit.getSourceAsString();//获取_source
            Goods goods = JSON.parseObject(json, Goods.class);//反序列化为goods对象
            //获取高亮标题  覆盖普通标题
            Map<String, HighlightField> highlightFields = hitsHit.getHighlightFields();
            HighlightField highlightField = highlightFields.get("title");
//            System.out.println(highlightField);
            goods.setTitle(highlightField.fragments()[0].string());
//            System.out.println(highlightField.fragments()[0].string());
            return goods;
        }).collect(Collectors.toList());
        responseVo.setGoodsList(goodsList);


        //获取聚合结果集
        Aggregations aggregations = response.getAggregations();
        //获取聚合结果集中的品牌ID聚合
        ParsedLongTerms brandIdAgg = (ParsedLongTerms)aggregations.get("brandIdAgg");
        //获取品牌id聚合结果集中的桶
        List<? extends Terms.Bucket> brandBuckets = brandIdAgg.getBuckets();
        if (!CollectionUtils.isEmpty(brandBuckets)){
            //把每个桶转化为品牌
           List<BrandEntity> brandEntities = brandBuckets.stream().map(bucket->{
               BrandEntity brandEntity = new BrandEntity();
               //桶中的Key就是品牌Id
               brandEntity.setId(bucket.getKeyAsNumber().longValue());
               //获取品牌ID聚合中的子聚合
               Aggregations subBrandAggs = bucket.getAggregations();
               //获取品牌名称的子聚合
               ParsedStringTerms brandNameAgg = (ParsedStringTerms)subBrandAggs.get("brandNameAgg");
               List<? extends Terms.Bucket> nameAggBuckets = brandNameAgg.getBuckets();
               if (!CollectionUtils.isEmpty(nameAggBuckets)){
                   brandEntity.setName(nameAggBuckets.get(0).getKeyAsString());
               }
               //获取品牌id聚合中的logo子聚合
               ParsedStringTerms brandLogoAgg = (ParsedStringTerms)subBrandAggs.get("brandLogoAgg");
               List<? extends Terms.Bucket> logoAggBuckets = brandLogoAgg.getBuckets();
               if (!CollectionUtils.isEmpty(logoAggBuckets)){
                   brandEntity.setLogo(logoAggBuckets.get(0).getKeyAsString());
               }
               return brandEntity;
           }).collect(Collectors.toList());
            responseVo.setBrands(brandEntities);
        }


        //获取分类的聚合  转化为分类列表
        ParsedLongTerms categoryIdAgg = (ParsedLongTerms)aggregations.get("categoryIdAgg");
        //获取分类id聚合集合中的桶
        List<? extends Terms.Bucket> categoryBuckets = categoryIdAgg.getBuckets();
        if (!CollectionUtils.isEmpty(categoryBuckets)){
            //吧分类id聚合中的桶转化为分类集合
            List<CategoryEntity> categoryEntities = categoryBuckets.stream().map(bucket -> {
                CategoryEntity categoryEntity = new CategoryEntity();
                //分类id聚合中桶的key就是分类的id
                categoryEntity.setId(bucket.getKeyAsNumber().longValue());
                //获取分类ID聚合中的子聚合
                Aggregations subCategoryAgg = bucket.getAggregations();
                ParsedStringTerms categoryNameAgg = (ParsedStringTerms)subCategoryAgg.get("categoryNameAgg");
                List<? extends Terms.Bucket> nameAggBuckets = categoryNameAgg.getBuckets();
                if (!CollectionUtils.isEmpty(nameAggBuckets)){
                    categoryEntity.setName(nameAggBuckets.get(0).getKeyAsString());
                }
                return categoryEntity;
            }).collect(Collectors.toList());
            responseVo.setCategories(categoryEntities);
        }


        //获取规格参数的聚合  解析出规格参数列表
        ParsedNested attrAgg = (ParsedNested)aggregations.get("attrAgg");//获取规格参数的嵌套聚合
        //获取嵌套聚合中的规格参数Id的子聚合
        ParsedLongTerms attrIdAgg = (ParsedLongTerms)attrAgg.getAggregations().get("attrIdAgg");
        //获取规格参数id聚合中的桶
        List<? extends Terms.Bucket> attrIdBuckets = attrIdAgg.getBuckets();
        //吧attrId的桶集合转化为SearchResponseAttrValueVo集合
        if (!CollectionUtils.isEmpty(attrIdBuckets)){
            List<SearchResponseAttrValueVo> searchResponseAttrValueVos = attrIdBuckets.stream().map(bucket -> {
                SearchResponseAttrValueVo attrValueVo = new SearchResponseAttrValueVo();
                //桶中的key就是attrId
                attrValueVo.setAttrId(bucket.getKeyAsNumber().longValue());
                //获取attrId聚合中的子聚合
                Aggregations aggs = bucket.getAggregations();
                //获取子聚合中的attrNameAgg
                ParsedStringTerms attrNameAgg = (ParsedStringTerms)aggs.get("attrNameAgg");
                //获取attrNameAgg中的桶
                List<? extends Terms.Bucket> nameBuckets = attrNameAgg.getBuckets();
                if (!CollectionUtils.isEmpty(nameBuckets)){
                    //attrNameAgg中的桶 有且仅有一个元素
                    attrValueVo.setAttrName(nameBuckets.get(0).getKeyAsString());
                }
                //获取子聚合中的attrValueAgg
                ParsedStringTerms attrValueAgg = (ParsedStringTerms)aggs.get("attrValueAgg");
                //获取attrValueAgg中的桶
                List<? extends Terms.Bucket> valueBuckets = attrValueAgg.getBuckets();
                if (!CollectionUtils.isEmpty(valueBuckets)){

                    List<String> attrValues = valueBuckets.stream().map(Terms.Bucket::getKeyAsString).collect(Collectors.toList());
                    attrValueVo.setAttrValues(attrValues);
                }
                return attrValueVo;
            }).collect(Collectors.toList());
            responseVo.setFilters(searchResponseAttrValueVos);
        }


        return responseVo;
    }

    public SearchSourceBuilder buildDsl(SearchParamVo paramVo) {
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

        String keyword = paramVo.getKeyword();
        if (StringUtils.isBlank(keyword)) {
            //TODO 打广告
            return sourceBuilder;
        }
        //1.构建查询及过滤
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        sourceBuilder.query(boolQueryBuilder);
        //1.1.匹配查询
        boolQueryBuilder.must(QueryBuilders.matchQuery("title", keyword).operator(Operator.OR));

        //1.2过滤
        //1.2.1.品牌过滤
        List<Long> brandId = paramVo.getBrandId();//品牌过滤条件
        if (!CollectionUtils.isEmpty(brandId)) {
            boolQueryBuilder.filter(QueryBuilders.termsQuery("brandId", brandId));
        }
        //1.2.2.分类过滤
        List<Long> categoryId = paramVo.getCategoryId();//分类过滤条件
        if (!CollectionUtils.isEmpty(categoryId)) {
            boolQueryBuilder.filter(QueryBuilders.termsQuery("categoryId", categoryId));
        }
        //1.2.3.价格区间过滤
        Double priceFrom = paramVo.getPriceFrom();
        Double priceTo = paramVo.getPriceTo();
        if (priceFrom != null || priceTo != null) {
            //构建范围查询
            RangeQueryBuilder rangeQuery = QueryBuilders.rangeQuery("price");
            boolQueryBuilder.filter(rangeQuery);
            if (priceFrom != null) {
                rangeQuery.gte(priceFrom);
            }
            if (priceTo != null) {
                rangeQuery.lte(priceTo);
            }
        }
        //1.2.4.仅显示有货过滤
        Boolean store = paramVo.getStore();
        if (store != null) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("store", store));
        }
        //1.2.5.规格参数过滤
        List<String> props = paramVo.getProps();
        if (!CollectionUtils.isEmpty(props)) {
            props.forEach(prop -> { //4:8G-12G

                //对规格参数过滤的条件字符串进行截取  过去attrId 和attrValue
                String[] attr = StringUtils.split(prop, ":");
                if (attr != null && attr.length == 2) {//分割后的数组长度必须是2时才去处理
                    //每一个规格参数过滤条件中的元素 对应一个嵌套查询
                    BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
                    boolQueryBuilder.filter(QueryBuilders.nestedQuery("searchAttrs", boolQuery, ScoreMode.None));

                    //bool查询中有两个查询  他们之间是must关系
                    boolQuery.must(QueryBuilders.termQuery("searchAttrs.attrId", attr[0]));
                    boolQuery.must(QueryBuilders.termsQuery("searchAttrs.attrValue", StringUtils.split(attr[1], "-")));
                }
            });
        }
        //2.构建排序
        Integer sort = paramVo.getSort();
        switch (sort) {
            case 0:
                sourceBuilder.sort("_score", SortOrder.DESC);
                break;
            case 1:
                sourceBuilder.sort("price", SortOrder.DESC);
                break;
            case 2:
                sourceBuilder.sort("price", SortOrder.ASC);
                break;
            case 3:
                sourceBuilder.sort("sales", SortOrder.DESC);
                break;
            case 4:
                sourceBuilder.sort("createTime", SortOrder.DESC);
                break;
            default:
                throw new RuntimeException("你的搜索条件不合法");
        }
        //3.分页
        Integer pageNum = paramVo.getPageNum();
        Integer pageSize = paramVo.getPageSize();
        sourceBuilder.from((pageNum - 1) * pageSize);
        sourceBuilder.size(pageSize);

        //4.高亮
        sourceBuilder.highlighter(
                new HighlightBuilder()
                        .field("title")
                        .preTags("<font style='color:red;'>")
                        .postTags("</font>")
        );

        //5.聚合
        //5.1.品牌聚合
        sourceBuilder.aggregation(
                AggregationBuilders.terms("brandIdAgg").field("brandId")
                        .subAggregation(AggregationBuilders.terms("brandNameAgg").field("brandName"))
                        .subAggregation(AggregationBuilders.terms("brandLogoAgg").field("logo"))
        );
        //5.2.分类聚合
        sourceBuilder.aggregation(
                AggregationBuilders.terms("categoryIdAgg").field("categoryId")
                        .subAggregation(AggregationBuilders.terms("categoryNameAgg").field("categoryName"))
        );
        //5.3.规格参数聚合
        sourceBuilder.aggregation(
                AggregationBuilders.nested("attrAgg", "searchAttrs")
                        .subAggregation(AggregationBuilders.terms("attrIdAgg").field("searchAttrs.attrId")
                                .subAggregation(AggregationBuilders.terms("attrNameAgg").field("searchAttrs.attrName"))
                                .subAggregation(AggregationBuilders.terms("attrValueAgg").field("searchAttrs.attrValue"))
                        )
        );

        //6.结果集过滤
        sourceBuilder.fetchSource(new String[]{"skuId","title","subTitle","price","defaultImage"}, null);
        System.out.println(sourceBuilder);
        return sourceBuilder;
    }
}
