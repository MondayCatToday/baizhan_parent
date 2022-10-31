package com.bjsxt.portal.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bjsxt.commons.exception.DaoException;
import com.bjsxt.commons.pojo.BaizhanResult;
import com.bjsxt.mapper.TbContentMapper;
import com.bjsxt.pojo.TbContent;
import com.bjsxt.portal.service.PortalService;
import com.bjsxt.redis.dao.RedisDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * 前台后端系统 - 门户系统 - 服务实现
 */
@Service
@Slf4j
public class PortalServiceImpl implements PortalService {
    /**
     * 大广告的内容分类主键。
     * 使用配置文件传递数据。
     */
    @Value("${baizhan.mysql.bigad.id}")
    private Long bigAdCategoryId;
    /**
     * 门户显示的轮播广告个数
     */
    @Value("${baizhan.portal.bigad.nums}")
    private int bigAdNums;
    @Value("${baizhan.portal.bigad.cache.keyPrefix}")
    private String bigAdCacheKeyPrefix;

    @Autowired
    private TbContentMapper contentMapper;

    @Autowired
    private RedisDao redisDao;

    /**
     * 查询大广告。
     * 根据内容分类主键，查询内容数据的集合。
     * 大广告内容分类主键=89
     *
     * 使用缓存redis，提高查询效率。
     * 流程：
     *  1. 访问redis，查询数据。如果有数据，则加工处理并返回，或直接返回。
     *  2. 如果缓存中没有数据，则访问数据库，查询数据。
     *  3. 把数据库的查询结果，缓存到redis数据库中，缓存的数据，可以是加工后的，也可以是未加工的。
     *  4. 加工数据库查询结果并返回，或直接返回数据库查询结果。
     * 注意：
     *  redis缓存数据库的所有读写操作（增删改查），必须不影响正常流程。
     *  如：redis服务器宕机，不能影响访问数据库查询。
     *  如：redis查询结果是null，不能应为空指针异常，影响后续代码流程。
     *
     * @return
     */
    @Override
    public BaizhanResult showBigAd() {
        try {
            try {
                // 查询redis缓存数据库
                String key = bigAdCacheKeyPrefix + bigAdCategoryId;
                List<TbContent> cacheList = redisDao.get(key);
                // 判断redis中的缓存数据是否存在
                if (cacheList != null && cacheList.size() > 0) {
                    // 缓存在redis中的大广告，是有效的
                    // 加工缓存的大广告，随机排序，只保留6个广告
                    cacheList = this.execBigAd(cacheList);
                    // 返回加工后的大广告数据，后续的数据库访问操作不需要执行。
                    return BaizhanResult.ok(cacheList);
                }
            }catch (Exception e){
                // e.printStackTrace();
                // 访问redis缓存数据库的时候，发生了错误。
                // 错误不能影响后续代码正常执行。catch捕获异常，异常消失。后续代码正常执行。
                log.error(e.getMessage());
            }

            // 内容分类主键 查询条件
            QueryWrapper<TbContent> queryWrapper =
                    new QueryWrapper<>();
            queryWrapper.eq("category_id", bigAdCategoryId);
            // 查询大广告内容数据集合
            List<TbContent> contents =
                    contentMapper.selectList(queryWrapper);

            try {
                // 把数据库的查询结果保存到redis缓存数据库中。让后续的查询访问redis，提高效率
                String key = bigAdCacheKeyPrefix + bigAdCategoryId;
                redisDao.set(key, contents);
            }catch (Exception e){
                // redis缓存数据库访问错误，不影响后续代码流程。
                log.error(e.getMessage());
            }

            // 管理广告的个数
            contents = this.execBigAd(contents);

            return BaizhanResult.ok(contents);
        }catch (Exception e){
            throw new DaoException("查询大广告数据错误："
                    + e.getMessage(), e);
        }
    }

    /**
     * 处理轮播广告。
     * 1. 每次返回的广告个数固定。
     * 2. 广告的顺序，随机排序。
     * @param resource
     * @return
     */
    private List<TbContent> execBigAd(List<TbContent> resource){
        // 让参数集合中的元素随机排序
        Collections.shuffle(resource);

        // 判断查询的广告个数和要显示的广告个数大小
        if(resource.size() > bigAdNums){
            // 查询的广告个数太多，只保留bigAdNums个。
            for(int i = resource.size() - 1; i >= bigAdNums; i--){
                resource.remove(i);
            }
        }
        return resource;
    }
}
