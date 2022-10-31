package com.bjsxt.backend.itemcat.vo;

import com.bjsxt.pojo.TbItemCat;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 当前系统中需要使用的商品类型值对象。value object
 * 只在当前系统中使用。且用于传递数据使用。
 * 不于数据库有任何关联。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class TbItemCatVO extends TbItemCat {
    // 编写推导属性。
    public Boolean getLeaf(){
        return !getIsParent();
    }
    public void setLeaf(Boolean leaf){
        // 推导属性，不需要考虑赋值。
        // jackson要求，类型中除class以外的所有属性，必须成对
        // 提供getter/setter
    }
}
