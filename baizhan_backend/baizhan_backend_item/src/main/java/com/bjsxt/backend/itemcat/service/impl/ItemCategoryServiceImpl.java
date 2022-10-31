package com.bjsxt.backend.itemcat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bjsxt.backend.itemcat.service.ItemCategoryService;
import com.bjsxt.backend.itemcat.vo.TbItemCatVO;
import com.bjsxt.commons.exception.DaoException;
import com.bjsxt.commons.pojo.BaizhanResult;
import com.bjsxt.mapper.TbItemCatMapper;
import com.bjsxt.pojo.TbItemCat;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 后台后端系统 - 商品分类管理服务实现类型。
 */
@Service
public class ItemCategoryServiceImpl implements ItemCategoryService {
    @Autowired
    private TbItemCatMapper itemCatMapper;

    /**
     * 根据父商品类型主键，查询子商品类型集合。
     * 访问表格tb_item_cat
     * 开发要求。不是你写的代码，不要做任何修改。
     * 可以在自己的工程范围内，写其子类型，加特性修改。局部使用。
     * @param id
     * @return
     */
    @Override
    public BaizhanResult selectItemCategoriesByParentId(Long id) {
        try {
            // 有条件查询多行数据。条件统一位QueryWrapper类型，
            // 条件的泛型就是要查询的实体类型。
            QueryWrapper<TbItemCat> queryWrapper =
                    new QueryWrapper<>();
            // QueryWrapper 用于封装所有要查询的条件。创建对象不做任何配置
            // 相当于无条件全数据查询。
            // 所有的条件都是基于QueryWrapper中的方法实现的。
            // 方法名称，和具体条件相关。如： 等值 eq。 大于 gt。 大于等于 ge。小于 lt。小于等于 le
            // 所有的方法参数都是2个。第一个是字符串类型，代表具体的数据库表格字段名。不是属性名。
            // 第二个参数类型是Object。是查询条件值
            // 多条件and并列，则直接多条件设置即可。
            queryWrapper.eq("parent_id", id)
                    .eq("status", 1);

            List<TbItemCat> result = itemCatMapper.selectList(queryWrapper);
            // 创建一个转换后的结果集合
            List<TbItemCatVO> localResult = new ArrayList<>(result.size());
            for(TbItemCat itemCat : result ){
                // 创建子类型对象。
                TbItemCatVO vo = new TbItemCatVO();

                // 把查询结果对象中的每个属性的值，赋值到vo对象中。
                // vo.setId(itemCat.getId());
                // Spring提供的属性赋值工具。根据getter和setter，赋值属性。
                // 把源itemCat中的属性，赋值到目标vo中。
                // 同名赋值,同名属性类型也相同。相当于 vo.setId(itemCat.getId());
                BeanUtils.copyProperties(itemCat, vo);

                localResult.add(vo);
            }
            return BaizhanResult.ok(localResult);
        }catch (Exception e){
            throw new DaoException("根据父商品类型主键，查询子商品类型集合错误", e);
        }
    }
}
