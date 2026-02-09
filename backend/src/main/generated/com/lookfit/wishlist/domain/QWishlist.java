package com.lookfit.wishlist.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QWishlist is a Querydsl query type for Wishlist
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QWishlist extends EntityPathBase<Wishlist> {

    private static final long serialVersionUID = -556009009L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QWishlist wishlist = new QWishlist("wishlist");

    public final DateTimePath<java.time.LocalDateTime> addedAt = createDateTime("addedAt", java.time.LocalDateTime.class);

    public final com.lookfit.member.domain.QMember member;

    public final StringPath memberId = createString("memberId");

    public final com.lookfit.product.domain.QProduct product;

    public final StringPath productId = createString("productId");

    public QWishlist(String variable) {
        this(Wishlist.class, forVariable(variable), INITS);
    }

    public QWishlist(Path<? extends Wishlist> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QWishlist(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QWishlist(PathMetadata metadata, PathInits inits) {
        this(Wishlist.class, metadata, inits);
    }

    public QWishlist(Class<? extends Wishlist> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.member = inits.isInitialized("member") ? new com.lookfit.member.domain.QMember(forProperty("member")) : null;
        this.product = inits.isInitialized("product") ? new com.lookfit.product.domain.QProduct(forProperty("product")) : null;
    }

}

