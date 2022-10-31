package com.bjsxt.backend.contentcategory.service;

import com.bjsxt.commons.pojo.BaizhanResult;
import com.bjsxt.pojo.TbContentCategory;

public interface ContentCategoryService {

    BaizhanResult selectContentCategoriesByParentId(Long id);

    BaizhanResult insertContentCategory(TbContentCategory contentCategory);

    BaizhanResult deleteContentCategoryById(Long id);

    BaizhanResult updateContentCategory(TbContentCategory contentCategory);
}
