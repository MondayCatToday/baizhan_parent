package com.bjsxt.backend.item.controller;

import com.bjsxt.backend.item.service.ItemService;
import com.bjsxt.commons.exception.DaoException;
import com.bjsxt.commons.pojo.BaizhanResult;
import com.bjsxt.pojo.TbItem;
import com.bjsxt.pojo.TbItemDesc;
import com.bjsxt.pojo.TbItemParamItem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 后台后端系统 - 商品管理控制器
 */
@RestController
@Slf4j
public class ItemController {
    @Autowired
    private ItemService itemService;

    /**
     * 为搜索系统，初始化数据提供的查询所有方法
     * 查询所有状态为1的商品，所有的商品分类，所有的商品详情。
     * @return
     */
    @PostMapping("/backend/item/selectAll4SearchInit")
    public BaizhanResult selectAll4SearchInit(){
        try {
            log.info("为搜索系统，初始化数据提供的查询");
            return itemService.selectAll4SearchInit();
        }catch (Exception e){
            log.error(e.getMessage());
            return BaizhanResult.error(e.getMessage());
        }
    }

    /**
     * 更新商品
     * mybatis-plus更新数据的时候，回判断，要更新的数据的属性是否是null或'0'.
     * 如果是，则忽略此字段的更新。如果不是，则更新字段新数据。
     * @param item 商品基本数据
     * @param itemDesc 商品详情
     * @param itemParamItem 商品规格
     * @return
     */
    @PostMapping("/backend/item/updateTbItem")
    public BaizhanResult updateTbItem(TbItem item, TbItemDesc itemDesc,
                                      TbItemParamItem itemParamItem){
        try {
            log.info("更新商品，id = " + item.getId());
            return itemService.updateTbItem(item, itemDesc, itemParamItem);
        }catch (Exception e){
            log.error(e.getMessage());
            return BaizhanResult.error("服务器忙，请稍后重试");
        }
    }

    /**
     * 预更新处理，根据商品主键，查询商品相关数据。
     * @param id
     * @return
     */
    @PostMapping("/backend/item/preUpdateItem")
    public BaizhanResult preUpdateItem(Long id){
        try {
            log.info("预更新商品，id = " + id);
            return itemService.preUpdateItem(id);
        }catch (Exception e){
            log.error(e.getMessage());
            return BaizhanResult.error("服务器忙，请稍后重试");
        }
    }

    /**
     * 商品下架
     * @param id 要下架的商品主键
     * @return
     */
    @PostMapping("/backend/item/offshelfItemById")
    public BaizhanResult offshelfItemById(Long id){
        try{
            log.info("商品下架， id  = " + id);
            return itemService.offshelfItemById(id);
        }catch (Exception e){
            log.error(e.getMessage());
            return BaizhanResult.error("服务器忙，请稍后重试");
        }
    }

    /**
     * 商品上架
     * @param id 要上架的商品主键
     * @return
     */
    @PostMapping("/backend/item/onshelfItemById")
    public BaizhanResult onshelfItemById(Long id){
        try{
            log.info("商品上架，id = " + id);
            return itemService.onshelfItemById(id);
        }catch (Exception e){
            log.error(e.getMessage());
            return BaizhanResult.error("服务器忙，请稍后重试");
        }
    }

    /**
     * 根据主键删除商品
     * @param id
     * @return
     */
    @PostMapping("/backend/item/deleteItemById")
    public BaizhanResult deleteItemById(Long id){
        try {
            log.info("删除商品：" + id);
            return itemService.deleteItemById(id);
        }catch (Exception e){
            log.error(e.getMessage());
            return BaizhanResult.error("服务器忙，请稍后重试");
        }
    }

    /**
     * 新增商品
     * @param item 商品基本信息 tb_item
     * @param itemDesc 商品详情信息，图文介绍 tb_item_desc
     * @param itemParamItem 商品规格数据， tb_item_param_item
     * @return
     */
    @PostMapping("/backend/item/insertTbItem")
    public BaizhanResult insertTbItem(TbItem item, TbItemDesc itemDesc,
                                      TbItemParamItem itemParamItem){
        try {
            log.info("新增商品到数据库");
            return itemService.insertTbItem(item, itemDesc, itemParamItem);
        }catch (Exception e){
            log.error(e.getMessage());
            return BaizhanResult.error("服务器忙，请稍后重试");
        }
    }

    /**
     * 分页查询商品
     * @return
     */
    @GetMapping("/backend/item/selectTbItemAllByPage")
    public BaizhanResult selectItemsByPage(@RequestParam(defaultValue = "1") int page,
                                           @RequestParam(defaultValue = "10") int rows){
        try {
            log.info("分页查询商品，第" + page + "页，每页" + rows + "行");
            return itemService.selectItemsByPage(page, rows);
        }catch (Exception e){
            log.error(e.getMessage());
            return BaizhanResult.error("服务器忙，请稍后重试");
        }
    }
}
