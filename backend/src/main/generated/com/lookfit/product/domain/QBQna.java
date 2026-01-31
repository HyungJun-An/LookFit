package com.lookfit.product.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QBQna is a Querydsl query type for BQna
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QBQna extends EntityPathBase<BQna> {

    private static final long serialVersionUID = 1818665188L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QBQna bQna = new QBQna("bQna");

    public final StringPath bCategory = createString("bCategory");

    public final StringPath bContent = createString("bContent");

    public final DateTimePath<java.time.LocalDateTime> bDate = createDateTime("bDate", java.time.LocalDateTime.class);

    public final NumberPath<Integer> bNo = createNumber("bNo", Integer.class);

    public final NumberPath<Integer> bPwd = createNumber("bPwd", Integer.class);

    public final NumberPath<Integer> bReadcount = createNumber("bReadcount", Integer.class);

    public final StringPath bTitle = createString("bTitle");

    public final StringPath bWriter = createString("bWriter");

    public final DateTimePath<java.time.LocalDateTime> deletedAt = createDateTime("deletedAt", java.time.LocalDateTime.class);

    public final StringPath lockFlag = createString("lockFlag");

    public final StringPath pID = createString("pID");

    public final QProduct product;

    public QBQna(String variable) {
        this(BQna.class, forVariable(variable), INITS);
    }

    public QBQna(Path<? extends BQna> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QBQna(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QBQna(PathMetadata metadata, PathInits inits) {
        this(BQna.class, metadata, inits);
    }

    public QBQna(Class<? extends BQna> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.product = inits.isInitialized("product") ? new QProduct(forProperty("product")) : null;
    }

}

