package com.bjsxt.cart.service.impl;

import com.bjsxt.cart.service.CartService;
import com.bjsxt.cart.vo.Cart;
import com.bjsxt.cart.vo.CartItem;
import com.bjsxt.commons.pojo.BaizhanResult;
import com.bjsxt.pojo.TbItem;
import com.bjsxt.pojo.TbUser;
import com.bjsxt.redis.dao.RedisDao;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;
import java.util.Collection;

/**
 * 前台后端系统 - 购物车子系统 - 服务实现
 */
@Service
public class CartServiceImpl implements CartService {
    @Value("${baizhan.cart.keyPrefix}")
    private String keyPrefix;
    @Autowired
    private RedisDao redisDao;
    @Value("${baizhan.frontend.details.itemKeyPrefix}")
    private String itemKeyPrefix;

    /**
     * 删除商品
     *
     * @param itemId
     * @param session
     * @return
     */
    @Override
    public BaizhanResult deleteItemFromCart(Long itemId, HttpSession session) {
        // 查询购物车
        Cart cart = redisDao.get(keyPrefix +
                ((TbUser) session.getAttribute("user")).getId());

        // 删除购物车中的商品
        cart.deleteFromCart(itemId);

        // 把修改后的购物车保存到Redis中
        redisDao.set(keyPrefix + ((TbUser) session.getAttribute("user")).getId(),
                cart);

        return BaizhanResult.ok();
    }

    /**
     * 修改购物车中商品购买数量
     *
     * @param itemId
     * @param num
     * @param session
     * @return
     */
    @Override
    public BaizhanResult updateItemNum(Long itemId, int num, HttpSession session) {
        // 访问Redis，查询用户的购物车。
        Cart cart =
                redisDao.get(keyPrefix + ((TbUser) session.getAttribute("user")).getId());
        // 修改商品的购买数量。
        cart.updateNum(itemId, num);
        // 把修改后的购物车数据保存到Redis中。
        redisDao.set(keyPrefix + ((TbUser) session.getAttribute("user")).getId(),
                cart);

        return BaizhanResult.ok();
    }

    /**
     * 增加商品到购物车
     * 客户端传递的只有商品的主键和购买的数量。
     * 如何查询商品的完整数据？
     * 从MySQL查询，数据最准确，效率相对较低。
     * 从Redis查询，数据可能有延迟，效率相对较高。 选用方案。
     * 因为，当前逻辑的前置条件是，客户查看商品详情，redis中一定有商品缓存数据。
     *
     * @param itemId 商品主键
     * @param num    商品数量。
     * @return
     */
    @Override
    public BaizhanResult addItem2Cart(Long itemId, int num, HttpSession session) {
        // 从redis中查询商品数据
        TbItem item = redisDao.get(itemKeyPrefix + itemId);
        // 商品有没有可能不存在？
        if (item == null) {
            // 商品不存在，后台系统可能下架或删除商品。
            return BaizhanResult.error("商品已下架");
        }
        // 商品存在，则保存到购物车中。
        CartItem cartItem = new CartItem();
        // 把查询的商品数据，赋值到购物车商品对象中。
        BeanUtils.copyProperties(item, cartItem);
        // 设置商品的购买数量。
        cartItem.setNum(num);

        // 查询购物车对象
        Cart cart =
                redisDao.get(keyPrefix + ((TbUser) session.getAttribute("user")).getId());
        // 判断购物车是否存在
        if (cart == null) {
            cart = new Cart();
            redisDao.set(keyPrefix + ((TbUser) session.getAttribute("user")).getId(),
                    cart);
        }

        // 把购物车商品对象，保存到购物车中。
        cart.add2Cart(cartItem);

        // 把新的购物车保存到Redis中。刷新数据
        redisDao.set(keyPrefix + ((TbUser) session.getAttribute("user")).getId(),
                cart);
        return BaizhanResult.ok();
    }

    /**
     * 查看购物车列表
     * 购物车数据是基于自定义的RedisDao做读写访问。
     * 购物车在Redis中的key，使用前缀+登录用户主键定义。
     * <p>
     * 思考：在查看购物车列表数据时，如何界定购物车中的商品的状态。
     * 如：购物车中的商品已经下架或删除，应该如何处理？
     * 如：购物车中的商品库存为0，或小于预购数量，应该如何处理？
     *
     * @param session
     * @return
     */
    @Override
    public BaizhanResult showCart(HttpSession session) {
        // 登录用户数据保存在分布式共享会话对象中。
        TbUser user = (TbUser) session.getAttribute("user");
        // 拼接购物车在redis中的key
        String key = keyPrefix + user.getId();
        // 使用RedisDao，查询Redis中的购物车数据
        Cart cart = redisDao.get(key);

        if (cart == null) {
            // 未使用过购物车，用户无购物车。
            // 初始化一个空的购物车，并保存到Redis中。
            cart = new Cart();
            redisDao.set(key, cart);
        }
        // 购物车对象，一定非空。
        // 获取购物车中保存的所有商品数据。
        Collection<CartItem> items = cart.showCart();

        return BaizhanResult.ok(items);
    }
}
