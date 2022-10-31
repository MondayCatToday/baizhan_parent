package com.bjsxt.search.service.impl;

import com.bjsxt.commons.pojo.BaizhanResult;
import com.bjsxt.search.dao.SearchDao;
import com.bjsxt.search.feign.BackendItemFeignInterface;
import com.bjsxt.search.pojo.Item4Search;
import com.bjsxt.search.service.SearchService;
import com.codingapi.txlcn.tc.annotation.TccTransaction;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 前台后端系统 - 搜索子系统 - 服务实现
 */
@Service
public class SearchServiceImpl implements SearchService {
    @Autowired
    private BackendItemFeignInterface backendItemFeignInterface;
    @Autowired
    private SearchDao searchDao;

    /**
     * 删除
     * @param oldData
     * @return
     */
    @Override
    @Transactional
    @TccTransaction
    public BaizhanResult deleteFromES(Map<String, Object> oldData) {
        searchDao.delete(oldData.get("id").toString());
        return BaizhanResult.ok();
    }

    // 删除确认
    public BaizhanResult confirmDeleteFromES(Map<String, Object> oldData){
        return BaizhanResult.ok();
    }
    // 删除取消
    public BaizhanResult cancelDeleteFromES(Map<String, Object> oldData){
        Item4Search item4Search = map2Item(oldData);
        searchDao.save(item4Search);
        return BaizhanResult.error("删除Elasticsearch中数据发生错误");
    }

    /**
     * 更新
     * @param datas
     * @return
     */
    @Override
    @Transactional
    @TccTransaction
    public BaizhanResult update2ES(List<Map<String, Object>> datas) {
        // 要更新的新的数据
        Item4Search item4Search = map2Item(datas.get(0));
        searchDao.save(item4Search);
        return BaizhanResult.ok();
    }

    // 更新确认
    public BaizhanResult confirmUpdate2ES(List<Map<String, Object>> datas){
        return BaizhanResult.ok();
    }
    // 更新取消
    public BaizhanResult cancelUpdate2ES(List<Map<String, Object>> datas){
        Item4Search item4Search = map2Item(datas.get(1));
        searchDao.save(item4Search);
        return BaizhanResult.error("更新数据到Elasticsearch失败");
    }

    /**
     * 新增商品相关数据到Elasticsearch
     */
    @Override
    @Transactional
    @TccTransaction
    public BaizhanResult insert2ES(Map<String, Object> newData) {
        Item4Search item4Search = map2Item(newData);
        searchDao.save(item4Search);
        return BaizhanResult.ok();
    }

    // 新增商品确认逻辑
    public BaizhanResult confirmInsert2ES(Map<String, Object> newData){
        return BaizhanResult.ok();
    }
    // 新增商品取消逻辑
    public BaizhanResult cancelInsert2ES(Map<String, Object> newData){
        // 删除新增的数据
        searchDao.delete(newData.get("id").toString());
        return BaizhanResult.error("新增数据到Elasticsearch错误");
    }

    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private Item4Search map2Item(Map<String, Object> source){
        Item4Search item4Search = new Item4Search();
        // 把Map中的每个键值对，赋值给item4Search对象。
        item4Search.setId(source.get("id").toString());
        item4Search.setTitle(source.get("title").toString());
        item4Search.setSellPoint(source.get("sellPoint").toString());
        item4Search.setCategoryName(source.get("categoryName").toString());
        item4Search.setImage(source.get("image").toString());
        item4Search.setItemDesc(source.get("itemDesc").toString());
        item4Search.setPrice(Long.parseLong(source.get("price").toString()));
        System.out.println(source.get("updated").getClass().getName());
        try {
            item4Search.setUpdated(sdf.parse(source.get("updated").toString()));
        } catch (ParseException e) {
            item4Search.setUpdated(null);
            e.printStackTrace();
        }
        return item4Search;
    }

    /**
     * 搜索逻辑
     * @param q
     * @param page
     * @param rows
     * @return
     */
    @Override
    public BaizhanResult search(String q, int page, int rows) {
        // 判断搜索条件，不能是null或者空白字符串。避免魔鬼搜索和错误结果
        if(StringUtils.isBlank(q)){
            // 错误搜索条件，返回托底数据
            return BaizhanResult.error("搜索的数据不存在，请重新搜索");
        }
        Map<String, Object> searchResult = searchDao.search(q, page, rows);
        return BaizhanResult.ok(searchResult);
    }

    /**
     * 1. 访问数据库，查询商品，商品分类，商品详情数据。
     *    使用远程服务调用实现。调用baizhan_backend_item系统中提供的功能
     *    远程服务调用次数影响系统性能，尽量减少远程访问的次数，提升性能。
     * 2. 把查询结果，组装处理，转换成Item4Search类型
     * 3. 初始化索引
     *    3.1 创建索引，设置映射
     *    3.2 数据批量新增
     * @return
     */
    @Override
    public String init() {
        // 远程服务调用，查询商品相关数据
        BaizhanResult result =
                backendItemFeignInterface.selectAll4SearchInit();
        if(result.getStatus() != 200){
            // 查询错误
            return "error";
        }
        // 查询成功，获取查询结果集合。
        List<Map<String, Object>> datas = (List<Map<String, Object>>) result.getData();
        // 日期转换工具
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        // 转换数据，把Map中的数据，赋值给Item4Search类型的对象。
        List<Item4Search> saveDocs = new ArrayList<>(datas.size());
        for(Map<String, Object> row : datas){
            // 每行数据，对应一个Item4Search类型对象。
            Item4Search item4Search = map2Item(row);

            saveDocs.add(item4Search);
        }

        try {
            // 初始化
            // 创建索引，设置映射
            searchDao.init();

            // 批量新增数据
            searchDao.batchSave(saveDocs);
        }catch (Exception e){
            // 发生异常的时候，前置的初始化逻辑和批量新增逻辑，最好取消
            // 其影响的Elasticsearch结果。
            // 删除新创建的索引。
            searchDao.deleteIndex();
            return "error";
        }

        return "success";
    }
}
