package com.bjsxt.search.controller;

import com.bjsxt.commons.pojo.BaizhanResult;
import com.bjsxt.search.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 前台后端系统 - 搜索子系统 - 控制器
 */
@RestController
public class SearchController {
    @Autowired
    private SearchService searchService;

    /**
     * 双写一致，搜索系统需要提供的服务有：
     *  1. 新增商品，后台新增商品数据 和 上架商品，后台上架商品，同步新增到Elasticsearch中。
     *     需要的数据，新增到Elasticsearch的商品相关数据。
     *     回滚时，删除新增的数据。
     *  2. 更新商品，后台更新商品数据，同步更新到Elasticsearch中。
     *     需要新的商品相关数据，更新覆盖Elasticsearch中原有的旧数据。
     *     回滚时，把旧的数据覆盖新的数据。
     *  3. 删除商品和下架，后台删除|下架商品，从Elasticsearch中删除对应数据。
     *     删除Elasticsearch现有的商品相关数据（主键）
     *     回滚时，把删除的数据恢复到Elasticsearch中。
     */

    /**
     * 新增商品相关数据到Elasticsearch
     * @return
     */
    @PostMapping("/search/insert2ES")
    public BaizhanResult insert2ES(@RequestBody Map<String, Object> newData){
        return searchService.insert2ES(newData);
    }

    /**
     * 更新商品相关数据到Elasticsearch
     * @param datas 0下标位置是新数据。1下标位置是旧数据。
     * @return
     */
    @PostMapping("/search/update2ES")
    public BaizhanResult update2ES(@RequestBody List<Map<String, Object>> datas){
        return searchService.update2ES(datas);
    }

    /**
     * 从Elasticsearch中删除商品相关数据
     * @return
     */
    @PostMapping("/search/deleteFromES")
    public BaizhanResult deleteFromES(@RequestBody Map<String, Object> oldData){
        return searchService.deleteFromES(oldData);
    }

    /**
     * 搜索逻辑
     * 根据搜索条件q，在搜索引擎Elasticsearch中搜索商品信息。
     * 一定不能有魔鬼搜索。
     * 必须在前端和后端进行两次校验。必须保证搜索条件q，非空，不是空字符串。
     * 假设，出现了魔鬼搜索，如何处理？ 返回空数据。 相当于降级|托底。
     * @param q
     * @param page
     * @param rows
     * @return
     */
    @PostMapping("/search/list")
    public BaizhanResult search(String q, int page, int rows){
        return searchService.search(q, page, rows);
    }

    /**
     * 初始化Elasticsearch中的商品数据
     * @return
     */
    @PostMapping("/init")
    public String init(){
        return searchService.init();
    }
}
