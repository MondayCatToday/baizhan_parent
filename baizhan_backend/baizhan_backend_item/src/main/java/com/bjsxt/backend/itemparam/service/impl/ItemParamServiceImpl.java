package com.bjsxt.backend.itemparam.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bjsxt.backend.itemparam.service.ItemParamService;
import com.bjsxt.commons.exception.DaoException;
import com.bjsxt.commons.pojo.BaizhanResult;
import com.bjsxt.mapper.TbItemParamMapper;
import com.bjsxt.pojo.TbItemCat;
import com.bjsxt.pojo.TbItemParam;
import com.bjsxt.utils.IDUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * 后台后端系统 - 规格参数服务实现类型。
 */
@Service
public class ItemParamServiceImpl implements ItemParamService {
    /**
     * 规格参数数据库访问接口。
     * 对应tb_item_param表格。实体使用TbItemParam
     * mybatis-plus生成的Mapper接口。方法命名有固定的规范。
     * 方法名称前缀：
     *  新增 - insert
     *  更新 - update
     *  删除 - delete
     *  查询 - select
     */
    @Autowired
    private TbItemParamMapper itemParamMapper;

    /**
     * 根据商品类型主键，查询规格参数
     * @param itemCatId
     * @return
     */
    @Override
    public BaizhanResult selectItemParamByItemCatId(Long itemCatId) {
        try {
            QueryWrapper<TbItemParam> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("item_cat_id", itemCatId);
            TbItemParam itemParam = itemParamMapper.selectOne(queryWrapper);
            return BaizhanResult.ok(itemParam);
        }catch (Exception e){
            throw new DaoException("根据商品类型主键-"+itemCatId+"，查询规格参数错误：" + e.getMessage(), e);
        }
    }

    /**
     * 根据主键,删除规格参数.
     * @param id
     * @return
     */
    @Override
    @Transactional(rollbackFor = {DaoException.class})
    public BaizhanResult deleteItemParamById(Long id) {
        try {
            // 删除使用的方法是delete系列方法
            int rows = itemParamMapper.deleteById(id);
            if (rows != 1) {
                throw new DaoException("删除规格参数错误");
            }
            return BaizhanResult.ok();
        }catch (DaoException e){
            throw e;
        }catch (Exception e){
            throw new DaoException("从数据库删除规格参数时发生错误:" + e.getMessage(), e);
        }
    }

    /**
     * 新增规格参数到数据库
     * @param itemParam
     * @return
     */
    @Override
    @Transactional(rollbackFor = {DaoException.class})
    public BaizhanResult insertItemParam(TbItemParam itemParam) {
        try {
            // 维护要新增数据的完整性
            itemParam.setId(IDUtils.genItemId()); // 生成主键
            Date now = new Date();
            itemParam.setCreated(now); // 创建时间
            itemParam.setUpdated(now); // 更新时间,新增数据的更新时间就是创建时间.
            // 新增数据到数据库，返回新增数据的行数
            int rows = itemParamMapper.insert(itemParam);
            if (rows != 1) {
                // 新增错误
                throw new DaoException("新增规格参数错误");
            }
            return BaizhanResult.ok();
        }catch (DaoException e){
            // 自定义抛出的异常.
            throw e;
        }catch (Exception e){
            // mapper访问数据库发生的其他异常.
            throw new DaoException("新增规格参数到数据库时,发生异常:" + e.getMessage(), e);
        }
    }

    /**
     * 根据商品分类主键，查询规格
     * @param itemCatId
     * @return
     */
    @Override
    public BaizhanResult selectHaveByItemCatId(Long itemCatId) {
        try {
            // 根据条件查询一行数据。
            // 查询条件还是QueryWrapper
            QueryWrapper<TbItemParam> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("item_cat_id", itemCatId);
            // 查询有结果，返回具体对象，查询无结果，返回null。查询结果行数>1，抛出异常。
            TbItemParam itemParam = itemParamMapper.selectOne(queryWrapper);
            if (null == itemParam) {
                // 无对应的规格，可以新增规格参数
                return BaizhanResult.ok();
            }
            return BaizhanResult.error("商品分类有对应规格参数，请选择其他商品分类");
        }catch (Exception e){
            throw new DaoException("根据商品分类主键，检查是否存在规格参数时，错误", e);
        }
    }

    /**
     * 查询所有规格参数
     *
     * 注意： 异常处理问题。
     *
     * @return
     */
    @Override
    public BaizhanResult selectAllItemParams() {
        try {
            // selectList 有条件查询多行数据。参数是条件。
            // 需要的是无条件查询所有。
            List<TbItemParam> result = itemParamMapper.selectList(null);
            return BaizhanResult.ok(result);
        }catch (Exception e){
            throw new DaoException("查询规格参数错误", e);
        }
    }
}
