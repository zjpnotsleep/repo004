package com.leyou.item.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.leyou.common.dto.CartDTO;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.pojo.PageResult;
import com.leyou.item.mapper.*;
import com.leyou.item.pojo.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GoodsService {
    @Autowired
    private SpuMapper spuMapper;
    @Autowired
    private CategoryMapper categoryMapper;
    @Autowired
    private BrandMapper brandMapper;
    @Autowired
    private SpuDetailMapper spuDetailMapper;
    @Autowired
    private SkuMapper skuMapper;
    @Autowired
    private StockMapper stockMapper;
    @Autowired
    private AmqpTemplate amqpTemplate;

    public PageResult<Spu> findGoodsByPage(String key,Boolean saleable,Integer page,Integer rows) {
        PageHelper.startPage(page,rows);
        Example example = new Example(Spu.class);
        Example.Criteria criteria = example.createCriteria();
        if(StringUtils.isNotBlank(key)){
            criteria.andLike("title", "%" + key + "%");
        }
        if(saleable != null){
            criteria.andEqualTo("saleable", saleable);
        }
        example.setOrderByClause("last_update_time desc");
        List<Spu> spuList = spuMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(spuList)) {
            throw new LyException(ExceptionEnum.GOODS_NOT_FOND);
        }
        loadCategoryAndBrandName(spuList);
        PageInfo<Spu> pageInfo = new PageInfo<>(spuList);

        return new PageResult<>(pageInfo.getTotal(),spuList);
    }

    private void loadCategoryAndBrandName(List<Spu> spuList) {
        for (Spu spu : spuList) {
            //查询商品所属分类
            List<Long> cids = Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3());
            List<Category> categories = categoryMapper.selectByIdList(cids);
            if (CollectionUtils.isEmpty(categories)) {
                throw new LyException(ExceptionEnum.CATEGORY_NOT_FOND);
            }
            String categoryNames = categories.stream().map(Category::getName).collect(Collectors.joining("/"));
            spu.setCname(categoryNames);
            //查询商品品牌名称
            Brand brand = brandMapper.selectByPrimaryKey(spu.getBrandId());
            if(brand != null){
                spu.setBname(brand.getName());
            }
        }
    }

    @Transactional
    public void saveGoods(Spu spu) {
       //新增spu
       spu.setId(null);
       spu.setCreateTime(new Date());
       spu.setLastUpdateTime(spu.getCreateTime());
       spu.setSaleable(true);
       spu.setValid(true);
        int count = spuMapper.insert(spu);
        if(count != 1){
            throw new LyException(ExceptionEnum.GOODS_SAVE_ERROR);
        }
        //新增spuDetail
        SpuDetail spuDetail = spu.getSpuDetail();
        spuDetail.setSpuId(spu.getId());
        count = spuDetailMapper.insert(spuDetail);
        if(count != 1){
            throw new LyException(ExceptionEnum.GOODS_SAVE_ERROR);
        }
        //新增Sku和Stock
        saveSkuAndStock(spu);
        amqpTemplate.convertAndSend("item.insert",spu.getId());
    }

    private void saveSkuAndStock(Spu spu) {
        int count;
        List<Stock> stockList = new ArrayList<>();
        for (Sku sku : spu.getSkus()) {
            sku.setSpuId(spu.getId());
            sku.setCreateTime(new Date());
            sku.setLastUpdateTime(sku.getCreateTime());
            count = skuMapper.insert(sku);
            if(count != 1){
                throw new LyException(ExceptionEnum.GOODS_SAVE_ERROR);
            }
            Stock stock = new Stock();
            stock.setSkuId(sku.getId());
            stock.setStock(sku.getStock());
            stockList.add(stock);
           /* count = stockMapper.insertSelective(stock);
            if(count != 1){
                throw new LyException(ExceptionEnum.GOODS_SAVE_ERROR);
            }*/
        }
        count = stockMapper.insertList(stockList);
        if(count != stockList.size()){
            throw new LyException(ExceptionEnum.GOODS_SAVE_ERROR);
        }
    }

    public SpuDetail querySpuDetailBySpuId(Long spuId) {
       /* Spu spu = spuMapper.selectByPrimaryKey(spuId);
        if(spu == null){
            throw new LyException(ExceptionEnum.GOODS_NOT_FOND);
        }*/
        SpuDetail spuDetail = spuDetailMapper.selectByPrimaryKey(spuId);
        if(spuDetail == null){
            throw new LyException(ExceptionEnum.GOODS_NOT_FOND);
        }
        //spu.setSpuDetail(spuDetail);
        /*Sku sku = new Sku();
        sku.setSpuId(spuId);
        List<Sku> skuList = skuMapper.select(sku);
        if(CollectionUtils.isEmpty(skuList)){
            throw new LyException(ExceptionEnum.GOODS_SKU_NOT_FOND);
        }

        for (Sku sku1 : skuList) {
            Stock stock = stockMapper.selectByPrimaryKey(sku1.getId());
            if(stock == null){
                throw new LyException(ExceptionEnum.GOODS_STOCK_NOT_FOND);
            }
            sku1.setStock(stock.getStock());
        }
        spu.setSkus(skuList);*/
        return spuDetail;
    }

    public List<Sku> querySkuBySpuId(Long spuId) {
        Sku sku = new Sku();
        sku.setSpuId(spuId);
        List<Sku> skuList = skuMapper.select(sku);
        if(CollectionUtils.isEmpty(skuList)){
            throw new LyException(ExceptionEnum.GOODS_SKU_NOT_FOND);
        }

        for (Sku sku1 : skuList) {
            Stock stock = stockMapper.selectByPrimaryKey(sku1.getId());
            if(stock == null){
                throw new LyException(ExceptionEnum.GOODS_STOCK_NOT_FOND);
            }
            sku1.setStock(stock.getStock());
        }
        return skuList;
    }

    @Transactional
    public void updateGoods(Spu spu) {
        Long spuId = spu.getId();
        if(spuId == null){
           throw new LyException(ExceptionEnum.GOODS_ID_CANNOT_BE_NULL);
        }
        SpuDetail spuDetail = spu.getSpuDetail();
        int count = spuDetailMapper.updateByPrimaryKeySelective(spuDetail);
        if(count != 1){
            throw new LyException(ExceptionEnum.GOODS_UPDATE_ERROR);
        }
        spu.setCreateTime(null);
        spu.setLastUpdateTime(spu.getCreateTime());
        spu.setSaleable(null);
        spu.setValid(null);
        count = spuMapper.updateByPrimaryKeySelective(spu);
        if(count != 1){
            throw new LyException(ExceptionEnum.GOODS_UPDATE_ERROR);
        }

        Sku sku = new Sku();
        sku.setSpuId(spuId);
        List<Sku> skuList = skuMapper.select(sku);
        if(!CollectionUtils.isEmpty(skuList)){
            skuMapper.delete(sku);
            List<Long> stockIds = skuList.stream().map(Sku::getId).collect(Collectors.toList());
            stockMapper.deleteByIdList(stockIds);
        }
        saveSkuAndStock(spu);
        amqpTemplate.convertAndSend("item.update",spu.getId());
    }

    public Spu querySpuBySpuId(Long spuId) {
        Spu spu = spuMapper.selectByPrimaryKey(spuId);
        SpuDetail spuDetail = querySpuDetailBySpuId(spuId);
        List<Sku> skuList = querySkuBySpuId(spuId);
        spu.setSpuDetail(spuDetail);
        spu.setSkus(skuList);
        if(spu == null){
            throw new LyException(ExceptionEnum.GOODS_NOT_FOND);
        }
        return spu;
    }

    public List<Sku> querySkuBySkuIds(List<Long> ids) {
        List<Sku> skuList = skuMapper.selectByIdList(ids);
        if(CollectionUtils.isEmpty(skuList)){
            throw new LyException(ExceptionEnum.GOODS_SKU_NOT_FOND);
        }
        return skuList;
    }

    @Transactional
    public void deceaseStock(List<CartDTO> carts) {
        for (CartDTO cart : carts) {
            int count = stockMapper.deceaseStock(cart.getNum(), cart.getSkuId());
            if(count != 1){
                throw new LyException(ExceptionEnum.STOCK_NOT_ENOUGH);
            }
            //如果按以下代码执行，容易出现多线程问题
            /*Stock stock = stockMapper.selectByPrimaryKey(cart.getSkuId());
            Integer difference = stock.getStock()-cart.getNum();
            if(difference >= 0){
                stock.setStock(difference);
                stockMapper.updateByPrimaryKey(stock);
            }*/
        }
    }
}
