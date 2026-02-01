package com.lookfit.product.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QProduct is a Querydsl query type for Product
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QProduct extends EntityPathBase<Product> {

    private static final long serialVersionUID = -671783187L;

    public static final QProduct product = new QProduct("product");

    public final StringPath description = createString("description");

    public final StringPath imageUrl = createString("imageUrl");

    public final StringPath pcategory = createString("pcategory");

    public final StringPath pcompany = createString("pcompany");

    public final StringPath pID = createString("pID");

    public final StringPath pname = createString("pname");

    public final NumberPath<java.math.BigDecimal> pprice = createNumber("pprice", java.math.BigDecimal.class);

    public final NumberPath<Integer> pstock = createNumber("pstock", Integer.class);

    public QProduct(String variable) {
        super(Product.class, forVariable(variable));
    }

    public QProduct(Path<? extends Product> path) {
        super(path.getType(), path.getMetadata());
    }

    public QProduct(PathMetadata metadata) {
        super(Product.class, metadata);
    }

}

