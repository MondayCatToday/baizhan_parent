package com.bjsxt.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bjsxt.pojo.TbContentCategory;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

/**
 * <p>
 * 内容分类 Mapper 接口
 * </p>
 *
 * @author ${author}
 * @since 2020-08-05
 */
@Mapper
@Component
public interface TbContentCategoryMapper extends BaseMapper<TbContentCategory> {

}
