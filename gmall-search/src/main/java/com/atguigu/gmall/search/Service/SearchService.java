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

            //??????????????????????????????
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
        //??????hits?????????
        SearchHits hits = response.getHits();
        responseVo.setTotal(hits.getTotalHits());
        SearchHit[] hitsHits = hits.getHits();
        List<Goods> goodsList = Arrays.stream(hitsHits).map(hitsHit->{
            String json = hitsHit.getSourceAsString();//??????_source
            Goods goods = JSON.parseObject(json, Goods.class);//???????????????goods??????
            //??????????????????  ??????????????????
            Map<String, HighlightField> highlightFields = hitsHit.getHighlightFields();
            HighlightField highlightField = highlightFields.get("title");
//            System.out.println(highlightField);
            goods.setTitle(highlightField.fragments()[0].string());
//            System.out.println(highlightField.fragments()[0].string());
            return goods;
        }).collect(Collectors.toList());
        responseVo.setGoodsList(goodsList);


        //?????????????????????
        Aggregations aggregations = response.getAggregations();
        //?????????????????????????????????ID??????
        ParsedLongTerms brandIdAgg = (ParsedLongTerms)aggregations.get("brandIdAgg");
        //????????????id????????????????????????
        List<? extends Terms.Bucket> brandBuckets = brandIdAgg.getBuckets();
        if (!CollectionUtils.isEmpty(brandBuckets)){
            //???????????????????????????
           List<BrandEntity> brandEntities = brandBuckets.stream().map(bucket->{
               BrandEntity brandEntity = new BrandEntity();
               //?????????Key????????????Id
               brandEntity.setId(bucket.getKeyAsNumber().longValue());
               //????????????ID?????????????????????
               Aggregations subBrandAggs = bucket.getAggregations();
               //??????????????????????????????
               ParsedStringTerms brandNameAgg = (ParsedStringTerms)subBrandAggs.get("brandNameAgg");
               List<? extends Terms.Bucket> nameAggBuckets = brandNameAgg.getBuckets();
               if (!CollectionUtils.isEmpty(nameAggBuckets)){
                   brandEntity.setName(nameAggBuckets.get(0).getKeyAsString());
               }
               //????????????id????????????logo?????????
               ParsedStringTerms brandLogoAgg = (ParsedStringTerms)subBrandAggs.get("brandLogoAgg");
               List<? extends Terms.Bucket> logoAggBuckets = brandLogoAgg.getBuckets();
               if (!CollectionUtils.isEmpty(logoAggBuckets)){
                   brandEntity.setLogo(logoAggBuckets.get(0).getKeyAsString());
               }
               return brandEntity;
           }).collect(Collectors.toList());
            responseVo.setBrands(brandEntities);
        }


        //?????????????????????  ?????????????????????
        ParsedLongTerms categoryIdAgg = (ParsedLongTerms)aggregations.get("categoryIdAgg");
        //????????????id?????????????????????
        List<? extends Terms.Bucket> categoryBuckets = categoryIdAgg.getBuckets();
        if (!CollectionUtils.isEmpty(categoryBuckets)){
            //?????????id????????????????????????????????????
            List<CategoryEntity> categoryEntities = categoryBuckets.stream().map(bucket -> {
                CategoryEntity categoryEntity = new CategoryEntity();
                //??????id???????????????key???????????????id
                categoryEntity.setId(bucket.getKeyAsNumber().longValue());
                //????????????ID?????????????????????
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


        //???????????????????????????  ???????????????????????????
        ParsedNested attrAgg = (ParsedNested)aggregations.get("attrAgg");//?????????????????????????????????
        //????????????????????????????????????Id????????????
        ParsedLongTerms attrIdAgg = (ParsedLongTerms)attrAgg.getAggregations().get("attrIdAgg");
        //??????????????????id???????????????
        List<? extends Terms.Bucket> attrIdBuckets = attrIdAgg.getBuckets();
        //???attrId?????????????????????SearchResponseAttrValueVo??????
        if (!CollectionUtils.isEmpty(attrIdBuckets)){
            List<SearchResponseAttrValueVo> searchResponseAttrValueVos = attrIdBuckets.stream().map(bucket -> {
                SearchResponseAttrValueVo attrValueVo = new SearchResponseAttrValueVo();
                //?????????key??????attrId
                attrValueVo.setAttrId(bucket.getKeyAsNumber().longValue());
                //??????attrId?????????????????????
                Aggregations aggs = bucket.getAggregations();
                //?????????????????????attrNameAgg
                ParsedStringTerms attrNameAgg = (ParsedStringTerms)aggs.get("attrNameAgg");
                //??????attrNameAgg?????????
                List<? extends Terms.Bucket> nameBuckets = attrNameAgg.getBuckets();
                if (!CollectionUtils.isEmpty(nameBuckets)){
                    //attrNameAgg????????? ????????????????????????
                    attrValueVo.setAttrName(nameBuckets.get(0).getKeyAsString());
                }
                //?????????????????????attrValueAgg
                ParsedStringTerms attrValueAgg = (ParsedStringTerms)aggs.get("attrValueAgg");
                //??????attrValueAgg?????????
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
            //TODO ?????????
            return sourceBuilder;
        }
        //1.?????????????????????
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        sourceBuilder.query(boolQueryBuilder);
        //1.1.????????????
        boolQueryBuilder.must(QueryBuilders.matchQuery("title", keyword).operator(Operator.OR));

        //1.2??????
        //1.2.1.????????????
        List<Long> brandId = paramVo.getBrandId();//??????????????????
        if (!CollectionUtils.isEmpty(brandId)) {
            boolQueryBuilder.filter(QueryBuilders.termsQuery("brandId", brandId));
        }
        //1.2.2.????????????
        List<Long> categoryId = paramVo.getCategoryId();//??????????????????
        if (!CollectionUtils.isEmpty(categoryId)) {
            boolQueryBuilder.filter(QueryBuilders.termsQuery("categoryId", categoryId));
        }
        //1.2.3.??????????????????
        Double priceFrom = paramVo.getPriceFrom();
        Double priceTo = paramVo.getPriceTo();
        if (priceFrom != null || priceTo != null) {
            //??????????????????
            RangeQueryBuilder rangeQuery = QueryBuilders.rangeQuery("price");
            boolQueryBuilder.filter(rangeQuery);
            if (priceFrom != null) {
                rangeQuery.gte(priceFrom);
            }
            if (priceTo != null) {
                rangeQuery.lte(priceTo);
            }
        }
        //1.2.4.?????????????????????
        Boolean store = paramVo.getStore();
        if (store != null) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("store", store));
        }
        //1.2.5.??????????????????
        List<String> props = paramVo.getProps();
        if (!CollectionUtils.isEmpty(props)) {
            props.forEach(prop -> { //4:8G-12G

                //???????????????????????????????????????????????????  ??????attrId ???attrValue
                String[] attr = StringUtils.split(prop, ":");
                if (attr != null && attr.length == 2) {//?????????????????????????????????2???????????????
                    //????????????????????????????????????????????? ????????????????????????
                    BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
                    boolQueryBuilder.filter(QueryBuilders.nestedQuery("searchAttrs", boolQuery, ScoreMode.None));

                    //bool????????????????????????  ???????????????must??????
                    boolQuery.must(QueryBuilders.termQuery("searchAttrs.attrId", attr[0]));
                    boolQuery.must(QueryBuilders.termsQuery("searchAttrs.attrValue", StringUtils.split(attr[1], "-")));
                }
            });
        }
        //2.????????????
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
                throw new RuntimeException("???????????????????????????");
        }
        //3.??????
        Integer pageNum = paramVo.getPageNum();
        Integer pageSize = paramVo.getPageSize();
        sourceBuilder.from((pageNum - 1) * pageSize);
        sourceBuilder.size(pageSize);

        //4.??????
        sourceBuilder.highlighter(
                new HighlightBuilder()
                        .field("title")
                        .preTags("<font style='color:red;'>")
                        .postTags("</font>")
        );

        //5.??????
        //5.1.????????????
        sourceBuilder.aggregation(
                AggregationBuilders.terms("brandIdAgg").field("brandId")
                        .subAggregation(AggregationBuilders.terms("brandNameAgg").field("brandName"))
                        .subAggregation(AggregationBuilders.terms("brandLogoAgg").field("logo"))
        );
        //5.2.????????????
        sourceBuilder.aggregation(
                AggregationBuilders.terms("categoryIdAgg").field("categoryId")
                        .subAggregation(AggregationBuilders.terms("categoryNameAgg").field("categoryName"))
        );
        //5.3.??????????????????
        sourceBuilder.aggregation(
                AggregationBuilders.nested("attrAgg", "searchAttrs")
                        .subAggregation(AggregationBuilders.terms("attrIdAgg").field("searchAttrs.attrId")
                                .subAggregation(AggregationBuilders.terms("attrNameAgg").field("searchAttrs.attrName"))
                                .subAggregation(AggregationBuilders.terms("attrValueAgg").field("searchAttrs.attrValue"))
                        )
        );

        //6.???????????????
        sourceBuilder.fetchSource(new String[]{"skuId","title","subTitle","price","defaultImage"}, null);
        System.out.println(sourceBuilder);
        return sourceBuilder;
    }
}
