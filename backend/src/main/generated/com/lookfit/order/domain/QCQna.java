package com.lookfit.order.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QCQna is a Querydsl query type for CQna
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QCQna extends EntityPathBase<CQna> {

    private static final long serialVersionUID = 1473207010L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QCQna cQna = new QCQna("cQna");

    public final com.lookfit.product.domain.QBQna bQna;

    public final DateTimePath<java.time.LocalDateTime> cCommentDate = createDateTime("cCommentDate", java.time.LocalDateTime.class);

    public final StringPath cContent = createString("cContent");

    public final NumberPath<Integer> cNo = createNumber("cNo", Integer.class);

    public final NumberPath<Integer> cRef = createNumber("cRef", Integer.class);

    public final StringPath cWriter = createString("cWriter");

    public final DateTimePath<java.time.LocalDateTime> deletedAt = createDateTime("deletedAt", java.time.LocalDateTime.class);

    public QCQna(String variable) {
        this(CQna.class, forVariable(variable), INITS);
    }

    public QCQna(Path<? extends CQna> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QCQna(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QCQna(PathMetadata metadata, PathInits inits) {
        this(CQna.class, metadata, inits);
    }

    public QCQna(Class<? extends CQna> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.bQna = inits.isInitialized("bQna") ? new com.lookfit.product.domain.QBQna(forProperty("bQna"), inits.get("bQna")) : null;
    }

}

