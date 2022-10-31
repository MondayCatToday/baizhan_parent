package com.bjsxt.search.dao.impl;

import com.bjsxt.search.dao.SearchDao;
import com.bjsxt.search.pojo.Item4Search;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 前台后端系统 - 搜索子系统 - Elasticsearch数据访问对象
 */
@Repository
public class SearchDaoImpl implements SearchDao {
    @Autowired
    private ElasticsearchRestTemplate restTemplate;

    /**
     * 新增商品到Elasticsearch
     * @param item4Search
     */
    @Override
    public void save(Item4Search item4Search) {
        restTemplate.save(item4Search);
    }

    @Override
    public void delete(String id) {
        restTemplate.delete(id, Item4Search.class);
    }

    /**
     * 搜索逻辑
     * @param q 条件
     * @param page
     * @param rows
     * @return
     *  { total : 总计数据行数, list : [{要展示的一个商品对象 Item4Search}] }
     */
    @Override
    public Map<String, Object> search(String q, int page, int rows) {
        // 多条件搜索，任意条件满足，即返回
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        // 标题
        queryBuilder.should(QueryBuilders.matchQuery("title", q));
        // 卖点
        queryBuilder.should(QueryBuilders.matchQuery("sellPoint", q));
        // 详情
        queryBuilder.should(QueryBuilders.matchQuery("itemDesc", q));
        // 商品分类名称
        queryBuilder.should(QueryBuilders.matchQuery("categoryName", q));

        // 高亮字段 - 标题
        HighlightBuilder.Field titleField = new HighlightBuilder.Field("title");
        titleField.preTags("<span style='color:red'>");
        titleField.postTags("</span>");

        // 高亮字段 - 卖点
        HighlightBuilder.Field sellPointField = new HighlightBuilder.Field("sellPoint");
        sellPointField.preTags("<span style='color:red'>");
        sellPointField.postTags("</span>");

        Query query = new NativeSearchQueryBuilder()
                .withQuery(queryBuilder) // 搜索条件
                .withPageable(
                        PageRequest.of((page - 1), rows,
                                Sort.by(
                                        Sort.Order.desc("updated"),
                                        Sort.Order.asc("id")
                                ))
                ) // 分页条件和排序条件
                .withHighlightFields(titleField, sellPointField) // 高亮字段
                .build();
        SearchHits<Item4Search> hits = restTemplate.search(query, Item4Search.class);
        // 处理搜索结果，得到需要的数据集合
        List<Item4Search> dataList = new ArrayList<>(rows);
        for(SearchHit<Item4Search> hit : hits){
            // 获取搜索的一个数据对象，无高亮数据
            Item4Search item = hit.getContent();
            // 处理高亮
            List<String> titleHl = hit.getHighlightField("title");
            if(titleHl != null && titleHl.size() > 0){
                // 有高亮标题，设置
                item.setTitle(titleHl.get(0));
            }

            List<String> sellPointHl = hit.getHighlightField("sellPoint");
            if(sellPointHl != null && sellPointHl.size() > 0){
                // 有高亮卖点，设置
                item.setSellPoint(sellPointHl.get(0));
            }
            dataList.add(item);
        }

        // 封装返回结果
        Map<String, Object> map = new HashMap<>();
        map.put("total", hits.getTotalHits()); // 符合搜索条件的总计数据行数
        map.put("list", dataList);

        return map;
    }

    @Override
    public void init() {
        // 创建索引
        IndexOperations indexOps =
                restTemplate.indexOps(Item4Search.class);
        indexOps.create();
        // 设置映射
        indexOps.putMapping(indexOps.createMapping());
    }

    /**
     * 批量新增
     * @param list
     */
    @Override
    public void batchSave(List<Item4Search> list) {
        // 批量新增
        restTemplate.save(list);
    }

    @Override
    public void deleteIndex() {
        // 删除索引
        restTemplate.indexOps(Item4Search.class)
                .delete();
    }
}
