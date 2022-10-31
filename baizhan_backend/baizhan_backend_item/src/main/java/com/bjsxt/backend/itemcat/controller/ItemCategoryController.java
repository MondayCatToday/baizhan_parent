package com.bjsxt.backend.itemcat.controller;

import com.bjsxt.backend.itemcat.service.ItemCategoryService;
import com.bjsxt.commons.exception.DaoException;
import com.bjsxt.commons.pojo.BaizhanResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 后台后端系统 - 商品管理系统 - 商品分类控制器
 */
@RestController
@Slf4j
public class ItemCategoryController {
    @Autowired
    private ItemCategoryService itemCategoryService;

    /**
     * 根据父商品类型主键，查询子商品类型集合。
     * 无分页逻辑。
     * @param id 父商品类型主键
     * @return
     */
    @PostMapping("/backend/itemCategory/selectItemCategoryByParentId")
    public BaizhanResult selectItemCategoriesByParentId(
            @RequestParam(defaultValue = "0") Long id){
        try {
            log.info("根据父商品类型主键 ：" + id + "，查询子商品类型集合");
            return itemCategoryService.selectItemCategoriesByParentId(id);
        }catch (DaoException e){
            log.error(e.getMessage());
            return BaizhanResult.error("服务器忙，请稍后重试");
        }
    }
}
