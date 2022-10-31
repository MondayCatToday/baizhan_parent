package com.bjsxt.backend.itemparam.service;

import com.bjsxt.commons.pojo.BaizhanResult;
import com.bjsxt.pojo.TbItemParam;

public interface ItemParamService {
    BaizhanResult selectAllItemParams();

    BaizhanResult selectHaveByItemCatId(Long itemCatId);

    BaizhanResult insertItemParam(TbItemParam itemParam);

    BaizhanResult deleteItemParamById(Long id);

    BaizhanResult selectItemParamByItemCatId(Long itemCatId);
}
