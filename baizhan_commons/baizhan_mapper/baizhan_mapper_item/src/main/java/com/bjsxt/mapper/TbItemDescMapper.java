package com.bjsxt.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bjsxt.pojo.TbItemDesc;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

/**
 * <p>
 * 商品描述表 Mapper 接口
 * </p>
 *
 * @author ${author}
 * @since 2020-07-09
 */
@Mapper
@Component
public interface TbItemDescMapper extends BaseMapper<TbItemDesc> {

}
