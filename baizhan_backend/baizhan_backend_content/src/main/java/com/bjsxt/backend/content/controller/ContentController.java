package com.bjsxt.backend.content.controller;

import com.bjsxt.backend.content.service.ContentService;
import com.bjsxt.commons.pojo.BaizhanResult;
import com.bjsxt.pojo.TbContent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 后台后端系统 - CMS系统 - 内容控制器
 */
@RestController
@Slf4j
public class ContentController {
    @Autowired
    private ContentService contentService;

    /**
     * 根据主键，删除内容
     * @param id
     * @return
     */
    @PostMapping("/backend/content/deleteContentByIds")
    public BaizhanResult deleteContentById(Long id){
        try {
            log.info("删除内容，主键是：" + id);
            return contentService.deleteContentById(id);
        }catch (Exception e){
            log.error(e.getMessage());
            return BaizhanResult.error("服务器忙，请稍后重试");
        }
    }

    /**
     * 新增内容
     * @param content
     * @return
     */
    @PostMapping("/backend/content/insertTbContent")
    public BaizhanResult insertTbContent(TbContent content){
        try {
            log.info("新增内容， 标题是：" + content.getTitle());
            return contentService.insertTbContent(content);
        }catch (Exception e){
            log.error(e.getMessage());
            return BaizhanResult.error("服务器忙，请稍后重试");
        }
    }

    /**
     * 根据内容分类主键，查询内容集合。
     * 无分页要求， 查询表格 tb_content
     * @param categoryId 内容分类主键。
     * @return
     */
    @PostMapping("/backend/content/selectTbContentAllByCategoryId")
    public BaizhanResult selectTbContentsByCategoryId(Long categoryId){
        try {
            log.info("根据内容分类主键 "+categoryId+"，查询内容集合。");
            return contentService.selectTbContentsByCategoryId(categoryId);
        }catch ( Exception e ){
            log.error(e.getMessage());
            return BaizhanResult.error("服务器忙，请稍后重试");
        }
    }
}
