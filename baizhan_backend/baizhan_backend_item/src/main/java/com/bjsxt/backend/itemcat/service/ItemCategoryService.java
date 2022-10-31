package com.bjsxt.backend.itemcat.service;

import com.bjsxt.commons.pojo.BaizhanResult;

public interface ItemCategoryService {
    BaizhanResult selectItemCategoriesByParentId(Long id);
}
