package com.bjsxt.portal.controller;

import com.bjsxt.commons.pojo.BaizhanResult;
import com.bjsxt.portal.service.PortalService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 前台后端系统 - 门户系统 - 控制器
 */
@RestController
@Slf4j
public class PortalController {
    @Autowired
    private PortalService portalService;

    /**
     * 查询轮播广告数据。
     * @return
     */
    @GetMapping("/portal/showBigAd")
    public BaizhanResult showBigAd(){
        try {
            log.info("门户系统-查询大广告");
            return portalService.showBigAd();
        }catch (Exception e){
            log.error(e.getMessage());
            return BaizhanResult.error("服务器忙，请稍后重试");
        }
    }
}
