package com.lookfit.cart.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QCart is a Querydsl query type for Cart
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QCart extends EntityPathBase<Cart> {

    private static final long serialVersionUID = 609068421L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QCart cart = new QCart("cart");

    public final NumberPath<Integer> amount = createNumber("amount", Integer.class);

    public final StringPath imageUrl = createString("imageUrl");

    public final com.lookfit.member.domain.QMember member;

    public final StringPath memberId = createString("memberId");

    public final com.lookfit.product.domain.QProduct product;

    public final StringPath productId = createString("productId");

    public final StringPath productName = createString("productName");

    public final NumberPath<java.math.BigDecimal> productPrice = createNumber("productPrice", java.math.BigDecimal.class);

    public QCart(String variable) {
        this(Cart.class, forVariable(variable), INITS);
    }

    public QCart(Path<? extends Cart> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QCart(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QCart(PathMetadata metadata, PathInits inits) {
        this(Cart.class, metadata, inits);
    }

    public QCart(Class<? extends Cart> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.member = inits.isInitialized("member") ? new com.lookfit.member.domain.QMember(forProperty("member")) : null;
        this.product = inits.isInitialized("product") ? new com.lookfit.product.domain.QProduct(forProperty("product")) : null;
    }

}

