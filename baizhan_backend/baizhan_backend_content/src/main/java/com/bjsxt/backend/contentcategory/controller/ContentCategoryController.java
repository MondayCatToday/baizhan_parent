package com.bjsxt.backend.contentcategory.controller;

import com.bjsxt.backend.contentcategory.service.ContentCategoryService;
import com.bjsxt.commons.pojo.BaizhanResult;
import com.bjsxt.pojo.TbContentCategory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 后台后端系统 - CMS系统 - 内容分类控制器
 */
@RestController
@Slf4j
public class ContentCategoryController {
    @Autowired
    private ContentCategoryService contentCategoryService;

    /**
     * 修改内容分类
     * @param contentCategory
     * @return
     */
    @PostMapping("/backend/contentCategory/updateContentCategory")
    public BaizhanResult updateContentCategory(TbContentCategory contentCategory){
        try {
            log.info("修改内容分类,主键是：" + contentCategory.getId());
            return contentCategoryService.updateContentCategory(contentCategory);
        }catch (Exception e){
            log.error(e.getMessage());
            return BaizhanResult.error("服务器忙，请稍后重试");
        }
    }

    /**
     * 根据主键，删除内容分类
     * @param id
     * @return
     */
    @PostMapping("/backend/contentCategory/deleteContentCategoryById")
    public BaizhanResult deleteContentCategoryById(Long id){
        try {
            log.info("根据主键 : " +id+" ，删除内容分类");
            return contentCategoryService.deleteContentCategoryById(id);
        }catch (Exception e){
            log.error(e.getMessage());
            return BaizhanResult.error("服务器忙，请稍后重试");
        }
    }

    /**
     * 新增内容分类
     * @param contentCategory
     * @return
     */
    @PostMapping("/backend/contentCategory/insertContentCategory")
    public BaizhanResult insertContentCategory(TbContentCategory contentCategory){
        try {
            log.info("新增内容分类，内容分类名是：" + contentCategory.getName());
            return contentCategoryService.insertContentCategory(contentCategory);
        }catch (Exception e){
            log.error(e.getMessage());
            return BaizhanResult.error("服务器忙，请稍后重试");
        }
    }

    /**
     * 根据父内容分类主键，查询子内容分类集合。
     * @param id
     * @return
     */
    @PostMapping("/backend/contentCategory/selectContentCategoryByParentId")
    public BaizhanResult selectContentCategoriesByParentId(
            @RequestParam(defaultValue = "0") Long id){
        try {
            log.info("根据父内容分类主键，查询子内容分类集合");
            return contentCategoryService.selectContentCategoriesByParentId(id);
        }catch (Exception e){
            log.error(e.getMessage());
            return BaizhanResult.error("服务器忙，请稍后重试");
        }
    }
}
