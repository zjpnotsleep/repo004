package com.leyou.search.repository;

import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.pojo.PageResult;
import com.leyou.item.pojo.Brand;
import com.leyou.item.pojo.Spu;
import com.leyou.search.client.BrandClient;
import com.leyou.search.client.GoodsClient;
import com.leyou.search.pojo.Goods;
import com.leyou.search.service.SearchService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

@RunWith(SpringRunner.class)
@SpringBootTest
//@EnableFeignClients
public class GoodsRepositoryTest {
    @Autowired
    private ElasticsearchTemplate template;
    @Autowired
    private BrandClient brandClient;
    @Autowired
    private GoodsClient goodsClient;
    @Autowired
    private SearchService searchService;
    @Autowired
    private GoodsRepository goodsRepository;

    @Test
    public void testCreateIndex(){
        template.createIndex(Goods.class);
        template.putMapping(Goods.class);
    }

    @Test
    public void loadData(){
        /*Brand brand = brandClient.queryBrandByBid(15127L);
        System.out.println(brand);*/
        int page = 1;
        int rows = 100;
        int size = 0;
        do{
            PageResult<Spu> result = goodsClient.findGoodsByPage(null, true, page, rows);
            List<Spu> items = result.getItems();
            if(CollectionUtils.isEmpty(items)){
                break;
            }
            //List<Goods> goodsList = items.stream().map(s -> searchService.buildGoods(s)).collect(Collectors.toList());
            List<Goods> goodsList = items.stream().map(searchService::buildGoods).collect(Collectors.toList());
            goodsRepository.saveAll(goodsList);
            page++;
            size = items.size();
        }while (size == 100);
    }
}
