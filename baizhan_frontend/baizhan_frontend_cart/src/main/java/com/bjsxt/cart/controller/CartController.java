package com.bjsxt.cart.controller;

import com.bjsxt.cart.service.CartService;
import com.bjsxt.commons.pojo.BaizhanResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;

/**
 * 前台后端系统 - 购物车子系统 - 控制器
 */
@RestController
public class CartController {
    @Autowired
    private CartService cartService;

    /**
     * 从购物车中删除商品
     * @param itemId
     * @return
     */
    @PostMapping("/cart/deleteItemFromCart")
    public BaizhanResult deleteItemFromCart(Long itemId, HttpSession session){
        return cartService.deleteItemFromCart(itemId, session);
    }

    /**
     * 修改购物车中商品购买数量
     * @param itemId
     * @param num
     * @param session
     * @return
     */
    @PostMapping("/cart/updateItemNum")
    public BaizhanResult updateItemNum(Long itemId, int num, HttpSession session){
        return cartService.updateItemNum(itemId, num, session);
    }

    /**
     * 新增商品到购物车
     * @param itemId
     * @param num
     * @param session
     * @return
     */
    @GetMapping("/cart/addItem")
    public BaizhanResult addItem2Cart(Long itemId, int num, HttpSession session){
        return cartService.addItem2Cart(itemId, num, session);
    }

    /**
     * 查看购物车列表数据。
     * @param session 获取Spring Session提供的分布式共享会话对象。
     * @return
     */
    @PostMapping("/cart/showCart")
    public BaizhanResult showCart(HttpSession session){
        return cartService.showCart(session);
    }
}
