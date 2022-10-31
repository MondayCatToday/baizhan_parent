package com.bjsxt.cart.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * 购物车类型。描述一个用户的购物车。
 */
@Data
public class Cart implements Serializable {
    /**
     * 建议。如果把Java的Map保存到Redis数据库中。
     * Map的key的类型，建议使用自定义类型或者字符串。
     * 因为，部分版本的Redis服务器或Redis客户端，会把
     * redis中保存的数学类型数据，当作字符串处理。
     * 用于保存具体购物车数据的集合。
     */
    private Map<String, CartItem> carts = new HashMap<>();

    /**
     * 从购物车中删除预购商品
     * @param itemId
     */
    public void deleteFromCart(Long itemId){
        carts.remove(itemId.toString());
    }

    /**
     * 修改商品的购买数量
     * @param itemId
     * @param num
     */
    public void updateNum(Long itemId, int num){
        CartItem item = carts.get(itemId.toString());
        item.setNum(num);
    }

    /**
     * 增加商品到购物车
     * @param cartItem
     */
    public void add2Cart(CartItem cartItem){
        // 校验要新增到购物车的商品在购物车中是否存在
        CartItem currentItem = carts.get(cartItem.getId().toString());
        if(currentItem == null) {
            // 商品在购物车中不存在,直接新增
            carts.put(cartItem.getId().toString(), cartItem);
        }else{
            // 商品在购物车中存在,累加购买数量.
            currentItem.setNum(
                    currentItem.getNum() + cartItem.getNum()
            );
        }
    }

    /**
     * 返回购物车中所有的商品列表。
     * 注意：建议不要定义方法名称为 getXxx | setXxx。
     * 当前方法不是属性访问方法，
     * @return
     */
    public Collection<CartItem> showCart(){
        return carts.values();
    }
}
