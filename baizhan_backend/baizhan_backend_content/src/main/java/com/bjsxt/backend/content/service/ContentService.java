package com.bjsxt.backend.content.service;

import com.bjsxt.commons.pojo.BaizhanResult;
import com.bjsxt.pojo.TbContent;

public interface ContentService {
    BaizhanResult selectTbContentsByCategoryId(Long categoryId);

    BaizhanResult insertTbContent(TbContent content);

    BaizhanResult deleteContentById(Long id);
}
