package com.bjsxt.backend.contentcategory.vo;

import com.bjsxt.pojo.TbContentCategory;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class TbContentCategoryVO extends TbContentCategory {
    public Boolean getLeaf(){
        return !getIsParent();
    }
    public void setLeaf(Boolean leaf){}
}
