package com.bjsxt.backend.itemparam.controller;

import com.bjsxt.backend.itemparam.service.ItemParamService;
import com.bjsxt.commons.exception.DaoException;
import com.bjsxt.commons.pojo.BaizhanResult;
import com.bjsxt.pojo.TbItemParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 后台后端系统 - 商品管理系统 - 规格参数控制器
 */
@RestController
@Slf4j
public class ItemParamController {
    @Autowired
    private ItemParamService itemParamService;

    /**
     * 根据商品类型主键，查询规格参数
     * @return
     */
    @GetMapping("/backend/itemParam/selectItemParamByItemCatId/{itemCatId}")
    public BaizhanResult selectItemParamByItemCatId(@PathVariable("itemCatId") Long itemCatId){
        try {
            log.info("根据商品类型主键-"+itemCatId+"，查询规格参数");
            return itemParamService.selectItemParamByItemCatId(itemCatId);
        }catch (Exception e){
            log.error(e.getMessage());
            return BaizhanResult.error("服务器忙，请稍后重试");
        }
    }

    /**
     * 删除规格参数
     */
    @GetMapping("/backend/itemParam/deleteItemParamById")
    public BaizhanResult deleteItemParamById(Long id){
        try {
            log.info("删除规格参数:"+id);
            return itemParamService.deleteItemParamById(id);
        }catch (Exception e){
            log.error(e.getMessage());
            return BaizhanResult.error("服务器忙,请稍后重试");
        }
    }

    /**
     * 新增规格参数到数据库
     * @param itemParam 要新增的数据
     * @return
     */
    @PostMapping("/backend/itemParam/insertItemParam")
    public BaizhanResult insertItemParam(TbItemParam itemParam){
        try {
            log.info("新增规格参数到数据库:" + itemParam.toString());
            return itemParamService.insertItemParam(itemParam);
        }catch (Exception e){
            log.error(e.getMessage());
            return BaizhanResult.error("服务器忙,请稍后重试");
        }
    }

    /**
     * 根据商品类型主键，查询规格参数
     * 对应商品类型有规格参数数据，返回错误结果， status = 400
     * 反之，返回正确结果，status = 200
     * @param itemCatId 商品类型主键。
     * @return
     */
    @GetMapping("/backend/itemParam/selectHaveParam")
    public BaizhanResult selectHaveByItemCatId(Long itemCatId){
        try {
            log.info("根据商品类型主键，查询规格参数");
            return itemParamService.selectHaveByItemCatId(itemCatId);
        }catch (Exception e){
            log.error(e.getMessage());
            return BaizhanResult.error("商品分类存在规格参数，请选择其他商品分类");
        }
    }

    /**
     * 查询所有的规格参数。
     * 访问数据库表格tb_item_param。
     * @return BaizhanResult - 自定义的返回结果类型。
     *  包含属性： status - 状态编码； msg - 返回字符串消息； data - 返回数据对象。
     */
    @GetMapping("/backend/itemParam/selectItemParamAll")
    public BaizhanResult selectAllItemParams(){
        try {
            log.info("查询规格参数");
            return itemParamService.selectAllItemParams();
        }catch (DaoException e){
            log.error("查询规格参数错误：" + e.getMessage());
            return BaizhanResult.error("服务器忙，请稍后重试");
        }
    }
}
