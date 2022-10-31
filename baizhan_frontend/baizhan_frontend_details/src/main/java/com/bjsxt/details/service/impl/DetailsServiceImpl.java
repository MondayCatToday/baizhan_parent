package com.bjsxt.details.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bjsxt.commons.pojo.BaizhanResult;
import com.bjsxt.details.service.DetailsService;
import com.bjsxt.mapper.TbItemDescMapper;
import com.bjsxt.mapper.TbItemMapper;
import com.bjsxt.mapper.TbItemParamItemMapper;
import com.bjsxt.pojo.TbItem;
import com.bjsxt.pojo.TbItemDesc;
import com.bjsxt.pojo.TbItemParamItem;
import com.bjsxt.redis.dao.RedisDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * 前台后端 - 商品详情子系统 - 服务实现
 */
@Service
@Slf4j
public class DetailsServiceImpl implements DetailsService {
    @Autowired
    private TbItemMapper itemMapper;
    @Autowired
    private TbItemDescMapper itemDescMapper;
    @Autowired
    private TbItemParamItemMapper itemParamItemMapper;
    @Value("${baizhan.frontend.details.itemKeyPrefix}")
    private String itemKeyPrefix;
    @Value("${baizhan.frontend.details.itemDescKeyPrefix}")
    private String itemDescKeyPrefix;
    @Value("${baizhan.frontend.details.itemParamItemKeyPrefix}")
    private String itemParamItemKeyPrefix;
    @Autowired
    private RedisDao redisDao;
    @Value("${baizhan.frontend.details.lock.itemKeyPrefix}")
    private String itemLockKeyPrefix;
    @Value("${baizhan.frontend.details.lock.itemDescKeyPrefix}")
    private String itemDescLockKeyPrefix;
    @Value("${baizhan.frontend.details.lock.itemParamItemKeyPrefix}")
    private String itemParamItemLockKeyPrefix;

    private final ThreadLocal<Integer> times = new ThreadLocal<>();
    private final Random random = new Random();

    /**
     * 根据商品主键，查询规格
     * 缓存处理
     * @param itemId
     * @return
     */
    @Override
    public BaizhanResult selectItemParamItemByItemId(Long itemId) {
        String key = itemParamItemKeyPrefix + itemId;

        try {
            // 查询redis缓存
            TbItemParamItem cache = redisDao.get(key);
            if (cache != null) {
                // 缓存中查询到结果，直接返回。
                return BaizhanResult.ok(cache);
            }
        }catch (Exception e){
            e.printStackTrace();
            log.error("查询商品规格数据 - redis缓存查询错误：" + e.getMessage());
        }

        QueryWrapper<TbItemParamItem> queryWrapper =
                new QueryWrapper<>();
        queryWrapper.eq("item_id", itemId);
        TbItemParamItem itemParamItem =
                itemParamItemMapper.selectOne(queryWrapper);

        try {
            // 保存数据库查询结果到redis
            redisDao.set(key, itemParamItem);
        }catch (Exception e){
            e.printStackTrace();
            log.error("商品详情查询逻辑 - 保存商品规格数据到redis发生错误：" + e.getMessage());
        }

        return BaizhanResult.ok(itemParamItem);
    }

    /**
     * 根据商品主键，查询商品详情
     *
     * 前端问题： 修改文件
     * \itbaizhan-shop-frontend\src\pages\Details\Evaluate\EvaluateRight\EvalRightJieShao\index.vue
     * 注释36行css高度限制
     *
     * @param itemId
     * @return
     */
    @Override
    public BaizhanResult selectItemDescByItemId(Long itemId) {
        String key = itemDescKeyPrefix + itemId;

        try{
            // 查询缓存
            TbItemDesc cache = redisDao.get(key);
            if(cache != null){
                // 有缓存
                return BaizhanResult.ok(cache);
            }
        }catch (Exception e){
            e.printStackTrace();
            log.error("商品详情查询逻辑 - 查询redis中的商品图文介绍发生错误 ： " + e.getMessage());
        }

        TbItemDesc itemDesc = itemDescMapper.selectById(itemId);

        try{
            // 保存数据库查询结果到redis
            redisDao.set(key, itemDesc);
        }catch (Exception e){
            e.printStackTrace();
            log.error("商品详情查询逻辑 - 保存商品图文介绍到redis发生错误 ： " + e.getMessage());
        }

        return BaizhanResult.ok(itemDesc);
    }

    /**
     * 根据主键查询商品
     * 分析问题1：
     *  缓存穿透： 当Redis中没有商品的缓存数据时，客户端高并发请求，查看这个商品数据。
     *    所有的客户端请求，穿过缓存redis，进入数据库MySQL，查询商品信息。并同时把查询结果保存到Redis中。
     *    这个过程，数据库MySQL压力极高，性能降低。Redis写压力极高，反复覆盖同一条数据，性能降低，
     *    且没有意义。最严重的后果，微服务服务器宕机或数据库宕机。
     *  解决方案： 让并发的查询一条数据的请求，只有一个访问数据库，其他请求都阻塞等待。
     *    等待访问数据库的请求返回，并保存查询结果到缓存。其他请求在缓存中查询。
     *  实现方式： 加锁。Synchronized或Lock能在分布式系统中生效么？Synchronized或Lock只能保证
     *    一个JVM进程内，高并发同步处理。
     *    使用分布式锁，实现。
     *  分布式锁： 就是一个标记锁，在一个分布式系统所有节点都能访问到的地方，记录一个锁标记。
     *    记录锁标记的线程，代表获取到锁，执行结束后，需要释放锁标记（删除锁标记）。
     *    只要找一个可以让分布式系统所有节点都访问的数据存储就可以了。
     *
     * 分析问题2：
     *   缓存黑洞：如果缓存数据没有有效期，随着时间推移，缓存数据越来越多，内存不足，应该如何处理？
     *     商业项目中，不是所有的数据都永久保存的。大多数的数据，都是暂时缓存的。
     *     如何确定数据的缓存时间？经过分析、采样、多次使用得到。
     *     如：商品数据缓存多久？时长分析，商品一般热度周期，如14天。设定商品缓存时间是14天。
     *         在14天内，经过PV和UV的数据分析（大数据系统介入），A商品热度高，B商品热度低。
     *         当14天到期时，再增加缓存，A缓存期是14*2天；B缓存期是14*0.5天
     *
     * 分析问题3：
     *   缓存雪崩：限制大多数数据都有有效期，但是，有效期是固定值。如果发生，高并发客户端访问，
     *   查询不同的数据。缓存到Redis中。因为时间几乎一致，那么大量数据回同时到期。
     *   当缓存过时后，还有高并发请求，查询不同的数据，都进入数据库查询。只要请求量级足够大。
     *   数据足够多，都可能导致数据库宕机。
     *   大量数据同时过期，造成高并发请求同时访问数据库，最严重导致数据库宕机的情况。
     *   解决方案： 设置随机的有效时长。 用固定时长+随机时长。
     *
     * 分析问题4：
     *   缓存击穿：如果有客户端高并发请求服务器，查询不存在的数据，代码逻辑，先访问缓存，数据一定不存在。
     *   再访问数据库，数据一定不存在。只要请求的并发足够高，查询不存在的数据采样足够多，就可能
     *   造成数据库压力过高，最终导致数据库宕机。
     *   解决方案：
     *     即使数据库查询数据无返回结果，也要缓存到Redis中。只不过无结果的数据，缓存有效期极短。
     *
     *
     * @param id
     * @return
     */
    @Override
    public BaizhanResult selectItemById(Long id) {
        String key = itemKeyPrefix + id;
        try{
            // 查询缓存
            TbItem cache = redisDao.get(key);
            if(cache != null){
                // 有缓存
                // 删除自旋计数
                times.remove();
                return BaizhanResult.ok(cache);
            }
        }catch (Exception e){
            e.printStackTrace();
            log.error("商品详情查询逻辑 - 查询商品基本信息缓存发生错误：" + e.getMessage());
        }

        // 如果没有缓存如何处理？使用分布式锁，让一个线程访问数据库，查询数据。
        // 其他的线程阻塞等待，访问缓存查询数据。
        // 尝试获取分布式锁。就是使用setnx的方式，在redis中保存一个数据。value不关注。
        // 为了避免，可能发生的数据库查询问题，导致锁标记无法删除。设置锁的短暂有效期
        boolean isLocked = redisDao.setnx(itemLockKeyPrefix + id, "lock",
                2L, TimeUnit.SECONDS);
        System.out.println(isLocked);
        // 判断是否拿到分布式锁。
        if(!isLocked){
            // 没有获取锁标记。阻塞等待，一段时间后，再次查询redis
            // 如果有缓存，则访问缓存，返回结果。如果没有缓存，尝试获取分布式锁
            // 自旋
            try { // 阻塞100毫秒
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            // 获取自旋次数
            Integer t = times.get();
            if(t == null){
                t = 0;
                times.set(t);
            }
            if(t >= 10){
                // 已经自旋10次。返回一个降级数据。
                // 返回降级数据前，一定要删除ThreadLocal中保存的计数
                times.remove();
                return BaizhanResult.error("服务器忙，请稍后重试");
            }
            // 自旋不足10次。计数+1
            t = t + 1;
            times.set(t);
            return selectItemById(id);
        }

        TbItem item = itemMapper.selectById(id);

        try{
            if(item != null) {
                // 保存数据库查询结果到Redis，设定缓存的有效期。14天
                redisDao.set(key, item,
                        14L * 24 * 60 * 60 + random.nextInt(7200), TimeUnit.SECONDS);
            }else{
                // 查询无结果。短期缓存，避免可能发生的缓存击穿。
                item = new TbItem();
                item.setId(-1L); // 保存一个绝对不可能出现的商品数据主键。让前端应用配合判断。
                redisDao.set(key, item, 30L, TimeUnit.SECONDS);
            }
        }catch (Exception e){
            e.printStackTrace();
            log.error("商品详情查询逻辑 - 保存商品基本信息到redis发生错误 ： " + e.getMessage());
        }finally {
            // 保证自旋计数一定被删除
            times.remove();
            // 删除分布式锁
            redisDao.del(itemLockKeyPrefix + id);
        }

        return BaizhanResult.ok(item);
    }
}
