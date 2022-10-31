package com.bjsxt.details.controller;

import com.bjsxt.commons.pojo.BaizhanResult;
import com.bjsxt.details.service.DetailsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 前台后端 - 商品详情子系统 - 控制器
 */
@RestController
@Slf4j
public class DetailsController {
    @Autowired
    private DetailsService detailsService;

    /**
     * 根据商品主键，查询规格
     * @param itemId
     * @return
     */
    @PostMapping("/item/selectTbItemParamItemByItemId")
    public BaizhanResult selectItemParamItemByItemId(Long itemId){
        return detailsService.selectItemParamItemByItemId(itemId);
    }

    /**
     * 根据商品主键，查询商品详情。
     * @param itemId
     * @return
     */
    @PostMapping("/item/selectItemDescByItemId")
    public BaizhanResult selectItemDescByItemId(Long itemId){
        return detailsService.selectItemDescByItemId(itemId);
    }

    /**
     * 查询商品基本信息。
     * @param id
     * @return
     */
    @PostMapping("/item/selectItemInfo")
    public BaizhanResult selectItemById(Long id){
        return detailsService.selectItemById(id);
    }
}
