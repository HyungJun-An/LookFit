package com.lookfit.search.domain;

import com.lookfit.product.domain.Product;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Setting;

import java.math.BigDecimal;

@Document(indexName = "products")
@Setting(settingPath = "elasticsearch/product-settings.json")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDocument {

    @Id
    private String pID;

    @Field(type = FieldType.Text, analyzer = "nori_analyzer")
    private String pname;

    @Field(type = FieldType.Double)
    private BigDecimal pprice;

    @Field(type = FieldType.Keyword)
    private String pcategory;

    @Field(type = FieldType.Text, analyzer = "nori_analyzer")
    private String description;

    @Field(type = FieldType.Keyword)
    private String pcompany;

    @Field(type = FieldType.Integer)
    private Integer pstock;

    @Field(type = FieldType.Keyword)
    private String imageUrl;

    /**
     * Product 엔티티를 ProductDocument로 변환
     */
    public static ProductDocument from(Product product) {
        if (product == null) {
            return null;
        }

        return ProductDocument.builder()
                .pID(product.getPID())
                .pname(product.getPname())
                .pprice(product.getPprice())
                .pcategory(product.getPcategory())
                .description(product.getDescription())
                .pcompany(product.getPcompany())
                .pstock(product.getPstock())
                .imageUrl(product.getImageUrl())
                .build();
    }
}
