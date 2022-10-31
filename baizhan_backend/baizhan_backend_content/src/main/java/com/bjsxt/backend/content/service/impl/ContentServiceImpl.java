package com.bjsxt.backend.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bjsxt.backend.content.service.ContentService;
import com.bjsxt.commons.exception.DaoException;
import com.bjsxt.commons.pojo.BaizhanResult;
import com.bjsxt.mapper.TbContentMapper;
import com.bjsxt.pojo.TbContent;
import com.bjsxt.redis.dao.RedisDao;
import com.bjsxt.utils.IDUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * 后台后端系统 - CMS - 内容服务实现类
 */
@Service
public class ContentServiceImpl implements ContentService {
    @Autowired
    private TbContentMapper contentMapper;

    /**
     * 根据主键，删除内容。
     * @param id
     * @return
     */
    @Override
    @Transactional(rollbackFor = {DaoException.class})
    public BaizhanResult deleteContentById(Long id) {
        try {
            TbContent current = contentMapper.selectById(id);

            int rows = contentMapper.deleteById(id);
            if (rows != 1) {
                throw new DaoException("删除内容错误，主键是：" + id);
            }

            try {
                // 内容删除成功，开始同步redis缓存数据。
                this.syncRedisCache(current.getCategoryId());
            }catch (Exception e){
                e.printStackTrace();
            }

            return BaizhanResult.ok();
        }catch (DaoException e){
            throw e;
        }catch (Exception e){
            throw new DaoException("删除内容时发生错误：" + e.getMessage(), e);
        }
    }

    /**
     * 新增内容
     *
     * 内容数据，可能存在在redis缓存数据库中。
     * 那么新增的内容数据，是否需要同步的保存到redis缓存中？
     * 如果删除内容数据，是否需要同步删除redis的缓存？
     *
     * 双写一致：
     *  双写 - 代码要对数据库DB和数据库DB以外的其他数据存储，做同步的增删改操作。
     *  参考思想 - cache aside pattern 边路缓存
     *
     * cache aside pattern : 对双写一致性的管理思想。是建议，不是必要。
     *  当写数据库数据的时候（增删改），先处理数据库的数据，再淘汰（删除）缓存中对应的数据。
     *  原因：数据库的写操作，再性能上，比缓存的访问低。因为数据库的处理逻辑，包括事务。且数据库
     *  是文件管理，就是IO操作。如果先管理缓存数据，后管理数据库数据，可能造成缓存中有脏数据。如：
     *  缓存新增数据成功，数据库新增失败，事务回滚。缓存的数据就是脏数据。
     *  先管理数据库的数据，可在保证事务成功的前提下，再增删改数据到缓存，即使缓存管理失败，还可以
     *  通过查询逻辑，维护缓存的数据。
     *  淘汰缓存优于同步缓存：淘汰是删除，只考虑删除成功即可。缓存数据由查询逻辑管理。同步缓存，如果
     *  失败，一样需要查询逻辑管理缓存，可能是一个重复的逻辑。
     *
     * 开发中的双写一致处理方案：
     *  先增删改数据库中的数据，成功后，增删改缓存中的数据。让缓存数据和数据库数据一致。
     *  如果，增删改缓存数据失败，还可以让查询逻辑，再次管理缓存。
     *
     * @param content
     * @return
     */
    @Override
    @Transactional(rollbackFor = {DaoException.class})
    public BaizhanResult insertTbContent(TbContent content) {
        try {
            // 完善要新增的内容数据。 数据完整性处理。
            content.setId(IDUtils.genItemId());
            Date now = new Date();
            content.setCreated(now);
            content.setUpdated(now);

            // 新增内容到数据库
            int rows = contentMapper.insert(content);
            if (rows != 1) {
                throw new DaoException("新增内容错误， 标题是：" + content.getTitle());
            }

            try {
                // 数据库新增内容成功，开始同步缓存redis
                this.syncRedisCache(content.getCategoryId());
            }catch (Exception e){
                // 缓存redis数据同步发生错误。捕获异常，保证后续逻辑正常执行。
                e.printStackTrace();
            }

            return BaizhanResult.ok();
        }catch (DaoException e){
            throw e;
        }catch (Exception e){
            throw new DaoException("新增内容时发生错误：" + e.getMessage(), e);
        }
    }

    @Value("${baizhan.portal.bigad.cache.keyPrefix}")
    private String keyPrefix;
    @Autowired
    private RedisDao redisDao;

    /**
     * 同步redis中的内容缓存。
     *  查询新增或删除的内容所属分类对应的所有内容。
     *  把查询结果，保存到redis中。
     * @param categoryId 内容分类主键。
     */
    private void syncRedisCache(Long categoryId){
        // 查询内容分类对应的所有内容集合。
        QueryWrapper<TbContent> queryWrapper =
                new QueryWrapper<>();
        queryWrapper.eq("category_id", categoryId);
        List<TbContent> result = contentMapper.selectList(queryWrapper);
        // 把查询结果保存到redis缓存中
        String key = keyPrefix + categoryId;
        redisDao.set(key, result);
    }

    /**
     * 根据内容分类主键，查询内容数据集合
     * @param categoryId
     * @return
     */
    @Override
    public BaizhanResult selectTbContentsByCategoryId(Long categoryId) {
        try {
            QueryWrapper<TbContent> queryWrapper =
                    new QueryWrapper<>();
            // 设置查询条件。
            queryWrapper.eq("category_id", categoryId);
            List<TbContent> list = contentMapper.selectList(queryWrapper);

            return BaizhanResult.ok(list);
        }catch (Exception e){
            throw new DaoException("根据内容分类主键：" + categoryId + " 查询内容集合错误", e);
        }
    }
}
