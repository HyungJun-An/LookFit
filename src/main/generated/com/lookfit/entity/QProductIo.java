package com.lookfit.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QProductIo is a Querydsl query type for ProductIo
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QProductIo extends EntityPathBase<ProductIo> {

    private static final long serialVersionUID = -1864375501L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QProductIo productIo = new QProductIo("productIo");

    public final NumberPath<Integer> amount = createNumber("amount", Integer.class);

    public final QBuy buy;

    public final NumberPath<Integer> iono = createNumber("iono", Integer.class);

    public final QMember member;

    public final StringPath memberid = createString("memberid");

    public final NumberPath<Integer> orderno = createNumber("orderno", Integer.class);

    public final DateTimePath<java.time.LocalDateTime> pDate = createDateTime("pDate", java.time.LocalDateTime.class);

    public final StringPath pID = createString("pID");

    public final QProduct product;

    public final StringPath status = createString("status");

    public QProductIo(String variable) {
        this(ProductIo.class, forVariable(variable), INITS);
    }

    public QProductIo(Path<? extends ProductIo> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QProductIo(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QProductIo(PathMetadata metadata, PathInits inits) {
        this(ProductIo.class, metadata, inits);
    }

    public QProductIo(Class<? extends ProductIo> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.buy = inits.isInitialized("buy") ? new QBuy(forProperty("buy"), inits.get("buy")) : null;
        this.member = inits.isInitialized("member") ? new QMember(forProperty("member")) : null;
        this.product = inits.isInitialized("product") ? new QProduct(forProperty("product")) : null;
    }

}

