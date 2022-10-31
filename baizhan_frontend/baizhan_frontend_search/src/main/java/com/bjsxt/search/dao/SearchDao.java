package com.bjsxt.search.dao;

import com.bjsxt.search.pojo.Item4Search;

import java.util.List;
import java.util.Map;

public interface SearchDao {
    // 创建索引，并设置映射
    void init();
    // 批量新增到Elasticsearch
    void batchSave(List<Item4Search> list);
    // 删除索引
    void deleteIndex();
    // 搜索方法
    Map<String, Object> search(String q, int page, int rows);

    void save(Item4Search item4Search);

    void delete(String id);
}
