package com.bjsxt.backend.contentcategory.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bjsxt.backend.contentcategory.service.ContentCategoryService;
import com.bjsxt.backend.contentcategory.vo.TbContentCategoryVO;
import com.bjsxt.commons.exception.DaoException;
import com.bjsxt.commons.pojo.BaizhanResult;
import com.bjsxt.mapper.TbContentCategoryMapper;
import com.bjsxt.pojo.TbContentCategory;
import com.bjsxt.utils.IDUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 后台后端系统 - CMS系统 - 内容分类管理服务实现
 */
@Service
public class ContentCategoryServiceImpl implements ContentCategoryService {
    @Autowired
    private TbContentCategoryMapper contentCategoryMapper;

    /**
     * 修改内容分类
     * 要求： 修改的内容分类命名，不能有同级别，有效的其他同名内容分类。
     * @param contentCategory
     * @return
     */
    @Override
    @Transactional(rollbackFor = {DaoException.class})
    public BaizhanResult updateContentCategory(TbContentCategory contentCategory) {
        try {
            // 查询要修改的内容分类
            TbContentCategory oldContentCategory =
                    contentCategoryMapper.selectById(contentCategory.getId());
            // 查询要修改的内容分类的有效兄弟节点。
            QueryWrapper<TbContentCategory> queryWrapper =
                    new QueryWrapper<>();
            queryWrapper.eq("parent_id", oldContentCategory.getParentId())
                    .eq("status", 1);
            List<TbContentCategory> siblings =
                    contentCategoryMapper.selectList(queryWrapper);
            // 判断是否有同名的兄弟节点
            for (TbContentCategory sibling : siblings) {
                if (contentCategory.getName().equals(sibling.getName())) {
                    // 有同名的兄弟节点
                    return BaizhanResult.error("修改后的内容分类命名重复，请重新修改");
                }
            }
            // 没有同名内容分类，更新
            contentCategory.setUpdated(new Date()); // 设置更新时间
            int rows = contentCategoryMapper.updateById(contentCategory);
            if (rows != 1) {
                throw new DaoException("更新内容分类错误， 主键是：" + contentCategory.getId());
            }
            return BaizhanResult.ok();
        }catch (DaoException e){
            throw e;
        }catch (Exception e){
            throw new DaoException("更新内容分类时，发生错误：" + e.getMessage(), e);
        }
    }

    /**
     * 根据主键，删除内容分类
     * 标记删除，更新状态status=2
     * 注意：
     *  1. 删除父内容分类的时候，子内容分类如何处理？ 使用递归处理，递归删除父内容分类的所有
     *     子孙内容分类。
     *  2. 删除内容分类后，其父内容分类的isParent是否需要更新？
     * @param id
     * @return
     */
    @Override
    @Transactional(rollbackFor = {DaoException.class})
    public BaizhanResult deleteContentCategoryById(Long id) {
        try {
            // 递归删除内容分类
            Date now = new Date();
            deleteContentCategoryAndSubById(id, now);

            // 检查父内容分类，还有没其他的有效子内容分类，如果没有，则修改父内容分类isParent=false
            TbContentCategory parent =
                    contentCategoryMapper.selectById( // 查询删除节点的父内容分类
                            contentCategoryMapper.selectById(id).getParentId() // 查询当前内容分类
                    );
            // 查询父内容分类的，所有有效子内容分类
            QueryWrapper<TbContentCategory> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("parent_id", parent.getId()).eq("status", 1);
            List<TbContentCategory> children = contentCategoryMapper.selectList(queryWrapper);
            if (children == null || children.size() == 0) {
                // 没有子内容分类集合，更新父内容分类的isParent = false
                parent.setIsParent(false);
                parent.setUpdated(now);
                int rows = contentCategoryMapper.updateById(parent);
                if (rows != 1) {
                    throw new DaoException("删除内容分类后，更新父内容分类isParent错误");
                }
            }

            return BaizhanResult.ok();
        }catch (DaoException e){
            throw e;
        }catch (Exception e){
            throw new DaoException("删除内容分类时，发生错误：" + e.getMessage(), e);
        }
    }

    /**
     * 根据主键，递归删除子节点及当前节点。
     * @param id
     */
    private void deleteContentCategoryAndSubById(Long id, Date now){
        // 查询当前节点的所有有效子节点。
        QueryWrapper<TbContentCategory> queryWrapper = new QueryWrapper<>();
        // 查询条件是： 父内容分类主键和状态
        queryWrapper.eq("parent_id", id).eq("status", 1);
        List<TbContentCategory> children =
                contentCategoryMapper.selectList(queryWrapper);
        if(children != null && children.size() > 0){
            // 有子节点
            for(TbContentCategory child : children){
                // 删除迭代的当前子节点，及其子孙节点
                deleteContentCategoryAndSubById(child.getId(), now);
            }
        }
        // 已删除所有的子孙节点，删除当前节点
        TbContentCategory current = new TbContentCategory();
        current.setId(id); // 当前内容分类主键
        current.setStatus(2); // 删除状态
        current.setUpdated(now); // 删除的时间
        int rows = contentCategoryMapper.updateById(current);
        if(rows != 1){
            throw new DaoException("递归删除内容分类错误");
        }
    }

    /**
     * 新增内容分类
     * 要求：新增的内容分类，不能有同名的，同级的，有效状态的内容分类。
     * @param contentCategory
     * @return
     */
    @Override
    @Transactional(rollbackFor = {DaoException.class})
    public BaizhanResult insertContentCategory(TbContentCategory contentCategory) {
        try {
            // 检查要新增的内容分类是否可用。
            // 查询同一个父节点，有效的内容分类， 是否有和当前内容分类同名的数据。
            QueryWrapper<TbContentCategory> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("parent_id", contentCategory.getParentId())
                    .eq("status", 1)
                    .eq("name", contentCategory.getName());
            // 查询有效同名兄弟节点
            List<TbContentCategory> siblings = contentCategoryMapper.selectList(queryWrapper);
            if (siblings != null && siblings.size() > 0) {
                // 有同名有效的兄弟节点。
                return BaizhanResult.error("有同级别的同名内容分类，请重新输入");
            }

            // 新增数据有效。
            // 完善要新增的内容分类
            contentCategory.setId(IDUtils.genItemId());
            Date now = new Date();
            contentCategory.setCreated(now);
            contentCategory.setUpdated(now);
            contentCategory.setIsParent(false);
            contentCategory.setStatus(1);
            contentCategory.setSortOrder(1);

            // 新增内容分类
            int rows = contentCategoryMapper.insert(contentCategory);
            if (rows != 1) {
                throw new DaoException("新增内容分类错误，名称是：" + contentCategory.getName());
            }

            // 更新父节点的isParent。
            // 1. 先查询父节点，判断isParent是不是false，如果是，则更新。
            // 2. 直接更新
            TbContentCategory parent =
                    contentCategoryMapper.selectById(contentCategory.getParentId());
            if (!parent.getIsParent()) {
                // 父内容分类的isParent属性需要更新
                parent.setIsParent(true);
                parent.setUpdated(now);
                rows = contentCategoryMapper.updateById(parent);
                if (rows != 1) {
                    throw new DaoException("新增内容分类时，更新父节点isParent字段错误，" +
                            "父内容分类主键=" + parent.getId());
                }
            }

            return BaizhanResult.ok();
        }catch (DaoException e){
            throw e;
        }catch (Exception e){
            throw new DaoException("新增内容分类时发生错误：" + e.getMessage(), e);
        }
    }

    /**
     * 根据父内容分类主键，查询子内容分类集合。
     * @param id
     * @return
     */
    @Override
    public BaizhanResult selectContentCategoriesByParentId(Long id) {
        try {
            // 设置查询条件,父内容分类主键等值查询 and 状态 = 1
            QueryWrapper<TbContentCategory> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("parent_id", id).eq("status", 1);
            // 增加查询排序逻辑
            // orderByAsc|Desc(String... columns)， 根据多个字段，做升降序排列。
            queryWrapper.orderByAsc("sort_order", "name");
            List<TbContentCategory> list = contentCategoryMapper.selectList(queryWrapper);
            // 转换查询结果类型为值对象类型。
            List<TbContentCategoryVO> result = new ArrayList<>(list.size());
            for (TbContentCategory category : list) {
                // 创建目标对象
                TbContentCategoryVO vo = new TbContentCategoryVO();
                // 复制源对象属性到目标对象
                BeanUtils.copyProperties(category, vo);
                // 保存到结果集合中
                result.add(vo);
            }
            return BaizhanResult.ok(result);
        }catch (Exception e){
            throw new DaoException("根据父内容分类主键，查询子内容分类集合发生错误：" + e.getMessage(), e);
        }
    }
}
