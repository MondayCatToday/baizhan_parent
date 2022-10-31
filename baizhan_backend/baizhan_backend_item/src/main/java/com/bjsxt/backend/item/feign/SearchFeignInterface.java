package com.bjsxt.backend.item.feign;

import com.bjsxt.commons.pojo.BaizhanResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

@FeignClient("baizhan-frontend-search")
public interface SearchFeignInterface {
    @PostMapping("/search/insert2ES")
    public BaizhanResult insert2ES(@RequestBody Map<String, Object> newData);

    @PostMapping("/search/update2ES")
    public BaizhanResult update2ES(@RequestBody List<Map<String, Object>> datas);

    @PostMapping("/search/deleteFromES")
    public BaizhanResult deleteFromES(@RequestBody Map<String, Object> oldData);
}
