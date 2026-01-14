package com.lookfit.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QBReview is a Querydsl query type for BReview
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QBReview extends EntityPathBase<BReview> {

    private static final long serialVersionUID = -717212008L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QBReview bReview = new QBReview("bReview");

    public final StringPath bContent = createString("bContent");

    public final DateTimePath<java.time.LocalDateTime> bDate = createDateTime("bDate", java.time.LocalDateTime.class);

    public final NumberPath<Integer> bNo = createNumber("bNo", Integer.class);

    public final StringPath bOriginalFilename = createString("bOriginalFilename");

    public final StringPath bRenamedFilename = createString("bRenamedFilename");

    public final StringPath bWriter = createString("bWriter");

    public final DateTimePath<java.time.LocalDateTime> deletedAt = createDateTime("deletedAt", java.time.LocalDateTime.class);

    public final StringPath pID = createString("pID");

    public final QProduct product;

    public final NumberPath<Integer> rating = createNumber("rating", Integer.class);

    public QBReview(String variable) {
        this(BReview.class, forVariable(variable), INITS);
    }

    public QBReview(Path<? extends BReview> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QBReview(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QBReview(PathMetadata metadata, PathInits inits) {
        this(BReview.class, metadata, inits);
    }

    public QBReview(Class<? extends BReview> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.product = inits.isInitialized("product") ? new QProduct(forProperty("product")) : null;
    }

}

