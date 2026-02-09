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
    private String productId;

    @Field(type = FieldType.Text, analyzer = "nori_analyzer")
    private String productName;

    @Field(type = FieldType.Double)
    private BigDecimal productPrice;

    @Field(type = FieldType.Keyword)
    private String productCategory;

    @Field(type = FieldType.Text, analyzer = "nori_analyzer")
    private String description;

    @Field(type = FieldType.Keyword)
    private String productCompany;

    @Field(type = FieldType.Integer)
    private Integer productStock;

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
                .productId(product.getProductId())
                .productName(product.getProductName())
                .productPrice(product.getProductPrice())
                .productCategory(product.getProductCategory())
                .description(product.getDescription())
                .productCompany(product.getProductCompany())
                .productStock(product.getProductStock())
                .imageUrl(product.getImageUrl())
                .build();
    }
}
