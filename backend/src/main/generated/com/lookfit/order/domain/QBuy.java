package com.lookfit.order.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QBuy is a Querydsl query type for Buy
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QBuy extends EntityPathBase<Buy> {

    private static final long serialVersionUID = -645213691L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QBuy buy = new QBuy("buy");

    public final com.lookfit.member.domain.QMember member;

    public final StringPath memberid = createString("memberid");

    public final DateTimePath<java.time.LocalDateTime> orderdate = createDateTime("orderdate", java.time.LocalDateTime.class);

    public final NumberPath<Integer> orderno = createNumber("orderno", Integer.class);

    public final StringPath resAddress = createString("resAddress");

    public final StringPath resName = createString("resName");

    public final StringPath resPhone = createString("resPhone");

    public final StringPath resRequirement = createString("resRequirement");

    public final NumberPath<java.math.BigDecimal> totalprice = createNumber("totalprice", java.math.BigDecimal.class);

    public QBuy(String variable) {
        this(Buy.class, forVariable(variable), INITS);
    }

    public QBuy(Path<? extends Buy> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QBuy(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QBuy(PathMetadata metadata, PathInits inits) {
        this(Buy.class, metadata, inits);
    }

    public QBuy(Class<? extends Buy> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.member = inits.isInitialized("member") ? new com.lookfit.member.domain.QMember(forProperty("member")) : null;
    }

}

