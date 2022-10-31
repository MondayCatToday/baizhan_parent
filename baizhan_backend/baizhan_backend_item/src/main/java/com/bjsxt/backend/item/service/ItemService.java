package com.bjsxt.backend.item.service;

import com.bjsxt.commons.pojo.BaizhanResult;
import com.bjsxt.pojo.TbItem;
import com.bjsxt.pojo.TbItemDesc;
import com.bjsxt.pojo.TbItemParamItem;

public interface ItemService {
    BaizhanResult selectItemsByPage(int page, int rows);

    BaizhanResult insertTbItem(TbItem item, TbItemDesc itemDesc, TbItemParamItem itemParamItem);

    BaizhanResult deleteItemById(Long id);

    BaizhanResult onshelfItemById(Long id);

    BaizhanResult offshelfItemById(Long id);

    BaizhanResult preUpdateItem(Long id);

    BaizhanResult updateTbItem(TbItem item, TbItemDesc itemDesc, TbItemParamItem itemParamItem);

    BaizhanResult selectAll4SearchInit();
}
