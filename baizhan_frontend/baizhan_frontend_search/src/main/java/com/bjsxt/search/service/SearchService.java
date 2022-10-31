package com.bjsxt.search.service;

import com.bjsxt.commons.pojo.BaizhanResult;

import java.util.List;
import java.util.Map;

public interface SearchService {
    String init();

    BaizhanResult search(String q, int page, int rows);

    BaizhanResult insert2ES(Map<String, Object> newData);

    BaizhanResult update2ES(List<Map<String, Object>> datas);

    BaizhanResult deleteFromES(Map<String, Object> oldData);
}
