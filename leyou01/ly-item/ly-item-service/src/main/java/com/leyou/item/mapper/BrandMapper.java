package com.leyou.item.mapper;

import com.leyou.common.mapper.BaseMapper;
import com.leyou.item.pojo.Brand;
import com.leyou.item.pojo.Category;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface BrandMapper extends BaseMapper<Brand> {
    @Insert("insert into tb_category_brand (category_id,brand_id) values (#{cid},#{bid})")
    int insertCategoryBrand(@Param("cid") Long cid,@Param("bid") Long bid);

    @Select("select c.* from tb_category c,tb_category_brand cb where c.id = cb.category_id and cb.brand_id = #{bid}")
    List<Category>  queryBrandCategoriesByBid(@Param("bid") Long bid);

    @Select("select b.* from tb_brand b,tb_category_brand cb where b.id = cb.brand_id and cb.category_id = #{cid};")
    List<Brand> queryBrandByCid(@Param("cid") Long cid);
}
