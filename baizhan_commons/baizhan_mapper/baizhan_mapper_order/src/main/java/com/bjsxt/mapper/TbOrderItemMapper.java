package com.bjsxt.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bjsxt.pojo.TbOrderItem;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author ${author}
 * @since 2020-08-10
 */
@Mapper
@Component
public interface TbOrderItemMapper extends BaseMapper<TbOrderItem> {

}
