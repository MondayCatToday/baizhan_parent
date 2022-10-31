package com.bjsxt.backend.item.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bjsxt.backend.item.feign.SearchFeignInterface;
import com.bjsxt.backend.item.mapper.Item4SearchMapper;
import com.bjsxt.backend.item.service.ItemService;
import com.bjsxt.commons.exception.DaoException;
import com.bjsxt.commons.pojo.BaizhanResult;
import com.bjsxt.mapper.TbItemCatMapper;
import com.bjsxt.mapper.TbItemDescMapper;
import com.bjsxt.mapper.TbItemMapper;
import com.bjsxt.mapper.TbItemParamItemMapper;
import com.bjsxt.pojo.TbItem;
import com.bjsxt.pojo.TbItemCat;
import com.bjsxt.pojo.TbItemDesc;
import com.bjsxt.pojo.TbItemParamItem;
import com.bjsxt.redis.dao.RedisDao;
import com.bjsxt.utils.IDUtils;
import com.codingapi.txlcn.tc.annotation.LcnTransaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 后台后端系统 - 商品管理服务实现
 */
@Service
public class ItemServiceImpl implements ItemService {
    @Autowired
    private TbItemMapper itemMapper;
    @Autowired
    private TbItemDescMapper itemDescMapper;
    @Autowired
    private TbItemParamItemMapper itemParamItemMapper;
    @Autowired
    private TbItemCatMapper itemCatMapper;
    @Autowired
    private Item4SearchMapper item4SearchMapper;
    @Autowired
    private SearchFeignInterface searchFeignInterface;
    @Value("${baizhan.frontend.details.itemKeyPrefix}")
    private String itemKeyPrefix;
    @Value("${baizhan.frontend.details.itemDescKeyPrefix}")
    private String itemDescKeyPrefix;
    @Value("${baizhan.frontend.details.itemParamItemKeyPrefix}")
    private String itemParamItemKeyPrefix;
    @Autowired
    private RedisDao redisDao;

    /**
     * 查询商品（部分字段），商品详情（itemDesc），商品分类（name）
     * @return
     */
    @Override
    public BaizhanResult selectAll4SearchInit() {
        try {
            List<Map<String, Object>> result =
                    item4SearchMapper.selectAll4SearchInit();
            return BaizhanResult.ok(result);
        }catch (Exception e){
            throw new DaoException("初始化搜索引擎查询错误");
        }
    }

    /**
     * 更新商品
     * @param item
     * @param itemDesc
     * @param itemParamItem
     * @return
     */
    @Override
    @Transactional(rollbackFor = {DaoException.class})
    @LcnTransaction
    public BaizhanResult updateTbItem(TbItem item, TbItemDesc itemDesc, TbItemParamItem itemParamItem) {
        try {
            // 查询更新前的旧数据
            Map<String, Object> oldData =
                    item4SearchMapper.selectItem4SearchById(item.getId());

            // 完善要更新的商品的数据
            Date now = new Date(); // 当前时间，即更新时间。
            item.setUpdated(now);

            itemDesc.setItemId(item.getId()); // 商品主键即商品详情主键
            itemDesc.setUpdated(now); // 更新时间

            itemParamItem.setUpdated(now);
            itemParamItem.setItemId(item.getId());

            // 更新商品
            int rows = itemMapper.updateById(item);
            if (rows != 1) {
                throw new DaoException("更新商品 - 更新商品到数据库错误");
            }

            // 更新商品详情
            rows = itemDescMapper.updateById(itemDesc);
            if (rows != 1) {
                throw new DaoException("更新商品 - 更新商品详情到数据库错误");
            }

            // 更新商品规格，条件更新。当商品主键等于要更新的商品主键时，更新商品规格
            QueryWrapper<TbItemParamItem> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("item_id", item.getId());
            rows = itemParamItemMapper.update(itemParamItem, queryWrapper);
            if (rows != 1) {
                throw new DaoException("更新商品 - 更新商品规格到数据库错误");
            }

            // 查询更新后的新数据
            Map<String, Object> newData =
                    item4SearchMapper.selectItem4SearchById(item.getId());

            // 开始调用远程，实现双写一致。
            List<Map<String, Object>> datas = new ArrayList<>(2);
            datas.add(newData);
            datas.add(oldData);
            searchFeignInterface.update2ES(datas);

            try {
                // 保存新的商品相关数据到Redis
                redisDao.set(itemKeyPrefix + item.getId(), item);
                redisDao.set(itemDescKeyPrefix + item.getId(), itemDesc);
                redisDao.set(itemParamItemKeyPrefix + item.getId(), itemParamItem);
            }catch (Exception e){
                e.printStackTrace();
                // 如果发生了异常，则代表缓存同步失败。删除缓存，保证无脏数据
                redisDao.del(itemKeyPrefix + item.getId());
                redisDao.del(itemDescKeyPrefix + item.getId());
                redisDao.del(itemParamItemKeyPrefix + item.getId());
                // 如，也可以发送一个消息到RabbitMQ，让消费者去访问Redis，淘汰数据或同步数据。
            }

            return BaizhanResult.ok();
        }catch (DaoException e){
            throw e;
        }catch (Exception e){
            throw new DaoException("更新商品错误， " + e.getMessage(), e);
        }
    }

    /**
     * 预更新商品
     * @param id 要更新的商品主键
     * @return
     */
    @Override
    public BaizhanResult preUpdateItem(Long id) {
        try {
            // 查询商品
            TbItem item = itemMapper.selectById(id);

            // 查询商品类型
            if (item == null) {
                throw new DaoException("预更新商品错误， 查询商品失败， id = " + id);
            }
            TbItemCat itemCat = itemCatMapper.selectById(item.getCid());

            // 查询商品详情
            TbItemDesc itemDesc = itemDescMapper.selectById(item.getId());

            // 查询商品规格
            QueryWrapper<TbItemParamItem> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("item_id", item.getId());
            TbItemParamItem itemParamItem = itemParamItemMapper.selectOne(queryWrapper);

            // 处理返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("itemCat", itemCat);
            result.put("item", item);
            result.put("itemDesc", itemDesc);
            result.put("itemParamItem", itemParamItem);

            return BaizhanResult.ok(result);
        }catch (DaoException e){
            throw e;
        }catch (Exception e){
            throw new DaoException("预更新商品错误：" + e.getMessage(), e);
        }
    }

    /**
     * 商品下架，修改状态 status = 2
     *
     * 业务方法要和业务逻辑紧密关联。
     * 禁止做多业务合并耦合。
     * 万一某一个业务发生变更，不回影响其他业务。
     * 如：假设删除商品修改为备份删除，不再是标记删除。
     * 即，把要删除的商品数据查询，并保存到一个和tb_item表格同结构的备份表格中，tb_item_backup
     * 然后删除tb_item表格中的商品数据。
     *
     * 可以在当前服务实现中，定义一个private方法。做整合式处理。更新状态。
     * 不同的服务方法，调用私有方法，传递不同参数。
     *
     * @param id
     * @return
     */
    @Override
    @Transactional(rollbackFor = {DaoException.class})
    @LcnTransaction
    public BaizhanResult offshelfItemById(Long id) {
        try {
            TbItem item = itemMapper.selectById(id);
            // 设置要更新的数据
            item.setStatus(2); // 下架状态
            item.setUpdated(new Date()); // 更新时间
            int rows = itemMapper.updateById(item);
            if (rows != 1) {
                throw new DaoException("下架商品失败,id=" + id);
            }

            // 下架成功后，调用远程服务，删除Elasticsearch中的数据

            searchFeignInterface.deleteFromES(
                    item4SearchMapper.selectItem4SearchById(id)
            );

            // 淘汰缓存中的数据
            try {
                redisDao.del(itemKeyPrefix + item.getId());
                redisDao.del(itemDescKeyPrefix + item.getId());
                redisDao.del(itemParamItemKeyPrefix + item.getId());
            }catch (Exception e){
                e.printStackTrace();
            }

            return BaizhanResult.ok();
        }catch (DaoException e){
            throw e;
        }catch (Exception e){
            throw new DaoException("下架商品错误，" + e.getMessage(), e);
        }
    }

    private BaizhanResult updateStatus(Long id, int status){
        try {
            TbItem item = itemMapper.selectById(id);
            // 设置要更新的数据
            item.setStatus(status); // 修改状态
            item.setUpdated(new Date()); // 更新时间
            int rows = itemMapper.updateById(item);
            if (rows != 1) {
                throw new DaoException("修改商品失败, id = " + id + ", status = " + status);
            }
            return BaizhanResult.ok();
        }catch (DaoException e){
            throw e;
        }catch (Exception e){
            throw new DaoException("修改商品错误，" + e.getMessage(), e);
        }
    }

    /**
     * 商品上架，修改状态status=1
     * @param id
     * @return
     */
    @Override
    @Transactional(rollbackFor = {DaoException.class})
    @LcnTransaction
    public BaizhanResult onshelfItemById(Long id) {
        try {
            TbItem item = itemMapper.selectById(id);
            // 设置要更新的数据
            item.setStatus(1); // 上架状态
            item.setUpdated(new Date()); // 更新时间
            int rows = itemMapper.updateById(item);
            if (rows != 1) {
                throw new DaoException("上架商品失败,id=" + id);
            }

            // 上架商品成功后，新增数据到Elasticsearch
            searchFeignInterface.insert2ES(
                    item4SearchMapper.selectItem4SearchById(id)
            );

            try {
                // 保存商品相关数据到redis
                redisDao.set(itemKeyPrefix + id, item);
                redisDao.set(itemDescKeyPrefix + id, itemDescMapper.selectById(id));
                QueryWrapper<TbItemParamItem> queryWrapper =
                        new QueryWrapper<>();
                queryWrapper.eq("item_id", id);
                redisDao.set(itemParamItemKeyPrefix + id,
                        itemParamItemMapper.selectOne(queryWrapper));
            }catch (Exception e){
                e.printStackTrace();
                redisDao.del(itemKeyPrefix + item.getId());
                redisDao.del(itemDescKeyPrefix + item.getId());
                redisDao.del(itemParamItemKeyPrefix + item.getId());
            }

            return BaizhanResult.ok();
        }catch (DaoException e){
            throw e;
        }catch (Exception e){
            throw new DaoException("上架商品错误，" + e.getMessage(), e);
        }
    }

    /**
     * 根据主键删除商品。标记删除。修改状态status=3
     * @param id
     * @return
     */
    @Override
    @Transactional(rollbackFor = {DaoException.class})
    @LcnTransaction
    public BaizhanResult deleteItemById(Long id) {
        try {
            TbItem item = itemMapper.selectById(id);
            // 设置要更新的数据
            item.setStatus(3); // 删除状态
            item.setUpdated(new Date()); // 更新时间
            int rows = itemMapper.updateById(item);
            if (rows != 1) {
                throw new DaoException("删除商品失败,id=" + id);
            }

            // 下架商品成功后，删除Elasticsearch中的数据
            searchFeignInterface.deleteFromES(
                    item4SearchMapper.selectItem4SearchById(id)
            );

            // 淘汰缓存中的数据
            try {
                redisDao.del(itemKeyPrefix + item.getId());
                redisDao.del(itemDescKeyPrefix + item.getId());
                redisDao.del(itemParamItemKeyPrefix + item.getId());
            }catch (Exception e){
                e.printStackTrace();
            }

            return BaizhanResult.ok();
        }catch (DaoException e){
            throw e;
        }catch (Exception e){
            throw new DaoException("删除商品错误，" + e.getMessage(), e);
        }
    }

    /**
     * 新增商品，本地事务。保证三张表格数据同时新增成功，同时新增失败。
     * @param item
     * @param itemDesc
     * @param itemParamItem
     * @return
     */
    @Override
    @Transactional(rollbackFor = {DaoException.class})
    @LcnTransaction
    public BaizhanResult insertTbItem(TbItem item, TbItemDesc itemDesc, TbItemParamItem itemParamItem) {
        try {
            // 完善所有要新增的数据的内容。
            Long itemId = IDUtils.genItemId();
            Date now = new Date();
            item.setId(itemId);
            item.setStatus(1); // 数据库中表格数据的状态字段。 1-正常；2-下架；3-删除
            item.setCreated(now);
            item.setUpdated(now);

            itemDesc.setItemId(itemId);
            itemDesc.setCreated(now);
            itemDesc.setUpdated(now);

            itemParamItem.setId(IDUtils.genItemId());
            itemParamItem.setItemId(itemId);
            itemParamItem.setCreated(now);
            itemParamItem.setUpdated(now);

            // 新增数据到数据库、
            // 新增商品
            int rows = itemMapper.insert(item);
            if (rows != 1) {
                throw new DaoException("新增商品 - 新增商品到数据库错误");
            }

            rows = itemDescMapper.insert(itemDesc);
            if (rows != 1) {
                throw new DaoException("新增商品 - 新增商品详情到数据库错误");
            }

            rows = itemParamItemMapper.insert(itemParamItem);
            if (rows != 1) {
                throw new DaoException("新增商品 - 新增商品规格到数据库错误");
            }

            // 新增商品成功后，同步Elasticsearch数据
            searchFeignInterface.insert2ES(
                    item4SearchMapper.selectItem4SearchById(item.getId())
            );

            try {
                // 保存新的商品相关数据到Redis
                redisDao.set(itemKeyPrefix + item.getId(), item);
                redisDao.set(itemDescKeyPrefix + item.getId(), itemDesc);
                redisDao.set(itemParamItemKeyPrefix + item.getId(), itemParamItem);
            }catch (Exception e){
                e.printStackTrace();
                // 如果发生了异常，则代表缓存同步失败。删除缓存，保证无脏数据
                redisDao.del(itemKeyPrefix + item.getId());
                redisDao.del(itemDescKeyPrefix + item.getId());
                redisDao.del(itemParamItemKeyPrefix + item.getId());
                // 如，也可以发送一个消息到RabbitMQ，让消费者去访问Redis，淘汰数据或同步数据。
            }

            return BaizhanResult.ok();
        }catch (DaoException e){
            // 手工抛出的异常
            throw e;
        }catch (Exception e){
            throw new DaoException("新增商品时发生错误：" + e.getMessage(), e);
        }
    }

    /**
     * 分页查询，表格 tb_item
     * @param page
     * @param rows
     * @return
     */
    @Override
    public BaizhanResult selectItemsByPage(int page, int rows) {
        try {

            // IPage<查询结果泛型> selectPage(IPage<查询结果泛型> 分页条件, QueryWrapper 查询条件)
            // IPage接口中，定义了若干方法，可以获取分页查询的总计数据行数，页数，当前页数据集合等。
            IPage<TbItem> iPage = new Page<>(page, rows);
            IPage<TbItem> resultPage = itemMapper.selectPage(iPage, null);
            List<TbItem> list = resultPage.getRecords(); // 当前页数据集合
            // resultPage.getSize(); // 每页多少行数据
            long total = resultPage.getTotal(); // 总计多少行数据
            // resultPage.getPages(); // 总计多少页
            // resultPage.getCurrent(); // 当前第几页

            // 使用Map传递最终查询结果
            Map<String, Object> result = new HashMap<>();
            result.put("result", list);
            result.put("total", total);

            return BaizhanResult.ok(result);
        }catch (Exception e){
            throw new DaoException("分页查询商品时，发生错误:"+ e.getMessage(), e);
        }
    }
}
