package com.bjsxt.details.service;

import com.bjsxt.commons.pojo.BaizhanResult;

public interface DetailsService {
    BaizhanResult selectItemById(Long id);

    BaizhanResult selectItemDescByItemId(Long itemId);

    BaizhanResult selectItemParamItemByItemId(Long itemId);
}
