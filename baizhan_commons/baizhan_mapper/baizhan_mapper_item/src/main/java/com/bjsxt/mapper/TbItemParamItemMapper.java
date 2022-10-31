package com.bjsxt.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bjsxt.pojo.TbItemParamItem;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

/**
 * <p>
 * 商品规格和商品的关系表 Mapper 接口
 * </p>
 *
 * @author ${author}
 * @since 2020-07-09
 */
@Mapper
@Component
public interface TbItemParamItemMapper extends BaseMapper<TbItemParamItem> {

}
