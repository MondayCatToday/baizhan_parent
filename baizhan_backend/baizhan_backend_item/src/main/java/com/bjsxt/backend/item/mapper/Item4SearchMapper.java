package com.bjsxt.backend.item.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface Item4SearchMapper {
    @Select("select it.id, it.title, it.sell_point as sellPoint, it.price, it.image, it.updated, ifnull(itd.item_desc, '') as itemDesc, itc.name as categoryName from tb_item it left join tb_item_desc itd on it.id = itd.item_id left join tb_item_cat itc on it.cid = itc.id where it.status = 1")
    List<Map<String, Object>> selectAll4SearchInit();

    @Select("select it.id, it.title, it.sell_point as sellPoint, it.price, it.image, date_format(it.updated, '%Y-%m-%d %H:%i:%s') as updated, ifnull(itd.item_desc, '') as itemDesc, itc.name as categoryName from tb_item it left join tb_item_desc itd on it.id = itd.item_id left join tb_item_cat itc on it.cid = itc.id where it.id = #{id}")
    Map<String, Object> selectItem4SearchById(Long id);
}
