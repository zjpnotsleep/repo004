package com.leyou.search.client;

import com.leyou.search.pojo.Goods;
import com.leyou.search.repository.GoodsRepository;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AbstractAggregationBuilder;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.LongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.*;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.Map;
import java.util.Set;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GoodsClientTest {
    @Autowired
    private GoodsRepository goodsRepository;
    @Autowired
    private ElasticsearchTemplate template;

    @Test
    public void testGoodsClient01(){
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        queryBuilder.withSourceFilter(new FetchSourceFilter(new String[]{"id","subTitle","skus"},null));
        queryBuilder.withPageable(PageRequest.of(1,20));
        queryBuilder.withQuery(QueryBuilders.matchQuery("all","锤子"));
        queryBuilder.addAggregation(AggregationBuilders.terms("category_agg").field("cid3"));
        AggregatedPage<Goods> goods = template.queryForPage(queryBuilder.build(), Goods.class);
        System.out.println(goods.getTotalElements());
        System.out.println(goods.getTotalPages());
        for (Goods goods1 : goods.getContent()) {

            System.out.println(goods1);
        }

        Aggregations aggregations = goods.getAggregations();
        LongTerms aggregation = aggregations.get("category_agg");
        List<LongTerms.Bucket> buckets = aggregation.getBuckets();
        /*for (LongTerms.Bucket bucket : buckets) {
            long l = bucket.getKeyAsNumber().longValue();
        }*/
        System.out.println(aggregation.getName());
        System.out.println(aggregation.getType());
        Map<String, Object> metaData = aggregation.getMetaData();
        System.out.println(metaData);
        /*Set<Map.Entry<String, Object>> entries = metaData.entrySet();
        for (Map.Entry<String, Object> entry : entries) {
            System.out.println(entry.getKey()+": "+entry.getValue());
        }*/
    }

    @Test
    public void testGoodsClient02(){
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        queryBuilder.withSourceFilter(new FetchSourceFilter(new String[]{"id","subTitle","skus"},null));
        queryBuilder.withPageable(PageRequest.of(1,20));
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.must(QueryBuilders.matchQuery("all","锤子"));
        //boolQueryBuilder.filter(QueryBuilders.t)
        queryBuilder.withQuery(QueryBuilders.matchQuery("all","锤子"));
        queryBuilder.addAggregation(AggregationBuilders.terms("category_agg").field("cid3"));
        AggregatedPage<Goods> goods = template.queryForPage(queryBuilder.build(), Goods.class);
        System.out.println(goods.getTotalElements());
        System.out.println(goods.getTotalPages());
        for (Goods goods1 : goods.getContent()) {

            System.out.println(goods1);
        }

        Aggregations aggregations = goods.getAggregations();
        Aggregation aggregation = aggregations.get("category_agg");
        System.out.println(aggregation.getName());
        System.out.println(aggregation.getType());
        Map<String, Object> metaData = aggregation.getMetaData();
        System.out.println(metaData);
        /*Set<Map.Entry<String, Object>> entries = metaData.entrySet();
        for (Map.Entry<String, Object> entry : entries) {
            System.out.println(entry.getKey()+": "+entry.getValue());
        }*/
    }
}
