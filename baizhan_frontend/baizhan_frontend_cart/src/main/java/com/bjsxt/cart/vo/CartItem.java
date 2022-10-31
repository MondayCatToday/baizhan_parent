package com.bjsxt.cart.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 具体的购物车中的每个商品
 */
@Data
public class CartItem implements Serializable {
    /**
     * 商品主键
     */
    private Long id;

    /**
     * 商品标题
     */
    private String title;

    /**
     * 商品卖点
     */
    private String sellPoint;

    /**
     * 商品价格，单位为：分
     */
    private Long price;

    /**
     * 购买数量
     */
    private Integer num;

    /**
     * 商品图片
     */
    private String image;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    private Date updated;
}
