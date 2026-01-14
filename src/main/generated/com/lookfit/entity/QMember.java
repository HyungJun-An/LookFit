package com.lookfit.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QMember is a Querydsl query type for Member
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QMember extends EntityPathBase<Member> {

    private static final long serialVersionUID = -1768659300L;

    public static final QMember member = new QMember("member1");

    public final NumberPath<Integer> age = createNumber("age", Integer.class);

    public final DateTimePath<java.time.LocalDateTime> deletedate = createDateTime("deletedate", java.time.LocalDateTime.class);

    public final StringPath delflag = createString("delflag");

    public final StringPath email = createString("email");

    public final DateTimePath<java.time.LocalDateTime> enrolldate = createDateTime("enrolldate", java.time.LocalDateTime.class);

    public final StringPath favorite = createString("favorite");

    public final StringPath gender = createString("gender");

    public final StringPath grade = createString("grade");

    public final StringPath memberid = createString("memberid");

    public final StringPath membername = createString("membername");

    public final StringPath password = createString("password");

    public final StringPath phone = createString("phone");

    public final StringPath regflag = createString("regflag");

    public QMember(String variable) {
        super(Member.class, forVariable(variable));
    }

    public QMember(Path<? extends Member> path) {
        super(path.getType(), path.getMetadata());
    }

    public QMember(PathMetadata metadata) {
        super(Member.class, metadata);
    }

}

