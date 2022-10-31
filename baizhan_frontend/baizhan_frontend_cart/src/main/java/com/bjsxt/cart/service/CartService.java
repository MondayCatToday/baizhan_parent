package com.bjsxt.cart.service;

import com.bjsxt.commons.pojo.BaizhanResult;

import javax.servlet.http.HttpSession;

public interface CartService {
    BaizhanResult showCart(HttpSession session);

    BaizhanResult addItem2Cart(Long itemId, int num, HttpSession session);

    BaizhanResult updateItemNum(Long itemId, int num, HttpSession session);

    BaizhanResult deleteItemFromCart(Long itemId, HttpSession session);
}
