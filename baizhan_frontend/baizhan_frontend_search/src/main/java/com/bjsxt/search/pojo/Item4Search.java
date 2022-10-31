package com.bjsxt.search.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.io.Serializable;
import java.util.Date;

/**
 * 为搜索逻辑提供的商品实体类型。
 * 对应Elasticsearch中的索引。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "baizhan_item", replicas = 0)
public class Item4Search implements Serializable {
    @Id
    @Field(value = "id", type = FieldType.Keyword)
    private String id;
    @Field(value = "title", type = FieldType.Text, analyzer = "ik_max_word")
    private String title;
    @Field(value = "sellPoint", type = FieldType.Text, analyzer = "ik_max_word")
    private String sellPoint;
    @Field(value = "price", type = FieldType.Long, index = false)
    private Long price;
    @Field(value = "image", type = FieldType.Keyword, index = false)
    private String image;
    @Field(value = "categoryName", type = FieldType.Text, analyzer = "ik_smart")
    private String categoryName;
    @Field(value = "itemDesc", type = FieldType.Text, analyzer = "ik_smart")
    private String itemDesc;
    @Field(value = "updated", type = FieldType.Date, index = false, format = DateFormat.basic_date_time)
    private Date updated;
}
