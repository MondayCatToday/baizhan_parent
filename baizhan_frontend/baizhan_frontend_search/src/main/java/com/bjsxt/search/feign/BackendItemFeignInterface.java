package com.bjsxt.search.feign;

import com.bjsxt.commons.pojo.BaizhanResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient("baizhan-backend-item")
public interface BackendItemFeignInterface {
    @PostMapping("/backend/item/selectAll4SearchInit")
    public BaizhanResult selectAll4SearchInit();
}
