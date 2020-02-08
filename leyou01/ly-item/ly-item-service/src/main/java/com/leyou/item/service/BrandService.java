package com.leyou.item.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.pojo.PageResult;
import com.leyou.item.mapper.BrandMapper;
import com.leyou.item.pojo.Brand;
import com.leyou.item.pojo.Category;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

@Service
public class BrandService {
    @Autowired
    private BrandMapper brandMapper;

    public PageResult<Brand> findBrandsByPage(String key, Integer page, Integer rows, String sortBy, Boolean desc) {
        PageHelper.startPage(page,rows);
        Example example = new Example(Brand.class);
        if(StringUtils.isNotBlank(key)){
            Example.Criteria criteria = example.createCriteria();
            criteria.orLike("name","%"+key+"%").orEqualTo("letter",key.toUpperCase());
        }
        if(StringUtils.isNotBlank(sortBy)){
            String orderByClause = sortBy +  (desc ? " DESC" : " ASC");
            example.setOrderByClause(orderByClause);
        }
        List<Brand> brands = brandMapper.selectByExample(example);
        if(CollectionUtils.isEmpty(brands)){
            throw new LyException(ExceptionEnum.BRAND_NOT_FOND);
        }
        PageInfo<Brand> info = new PageInfo<>(brands);
        return new PageResult<>(info.getTotal(),brands);
    }

    @Transactional
    public void saveBrand(Brand brand, List<Long> cids) {
        //新增品牌表
        brand.setId(null);
        int count = brandMapper.insert(brand);
        if(count != 1){
            throw new LyException(ExceptionEnum.BRAND_SAVE_ERROR);
        }
        //cids.stream().map(cid -> brandMapper.insertCategoryBrand(cid,brand.getId()));
        //新增中间表
        for (Long cid : cids) {
            count = brandMapper.insertCategoryBrand(cid,brand.getId());
            if(count != 1){
                throw new LyException(ExceptionEnum.BRAND_SAVE_ERROR);
            }
        }

    }

    public List<Category> queryBrandCategoriesByBid(Long bid) {
        /*Brand brand = brandMapper.selectByPrimaryKey(bid);
        if (brand == null) {
            throw new LyException(ExceptionEnum.BRAND_NOT_FOND);
        }*/
        List<Category> categories = brandMapper.queryBrandCategoriesByBid(bid);
        if(CollectionUtils.isEmpty(categories)){
            throw new LyException(ExceptionEnum.BRAND_CATEGORIES_NOT_FOND);
        }
        return categories;
    }

    public List<Brand> queryBrandByCid(Long cid) {
        List<Brand> brands = brandMapper.queryBrandByCid(cid);
        if(CollectionUtils.isEmpty(brands)){
            throw new LyException(ExceptionEnum.BRAND_NOT_FOND);
        }
        return brands;
    }

    public Brand queryBrandByBid(Long bid) {
        Brand brand = brandMapper.selectByPrimaryKey(bid);
        if(brand == null){
            throw new LyException(ExceptionEnum.BRAND_NOT_FOND);
        }
        return brand;
    }

    public List<Brand> queryBrandByBids(List<Long> bids) {
        return brandMapper.selectByIdList(bids);
    }
}
