package com.lookfit.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QFileResource is a Querydsl query type for FileResource
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QFileResource extends EntityPathBase<FileResource> {

    private static final long serialVersionUID = -1696582612L;

    public static final QFileResource fileResource = new QFileResource("fileResource");

    public final StringPath contentType = createString("contentType");

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final StringPath domainId = createString("domainId");

    public final StringPath domainType = createString("domainType");

    public final StringPath fileId = createString("fileId");

    public final NumberPath<Long> fileSize = createNumber("fileSize", Long.class);

    public final StringPath originalName = createString("originalName");

    public final StringPath s3Url = createString("s3Url");

    public QFileResource(String variable) {
        super(FileResource.class, forVariable(variable));
    }

    public QFileResource(Path<? extends FileResource> path) {
        super(path.getType(), path.getMetadata());
    }

    public QFileResource(PathMetadata metadata) {
        super(FileResource.class, metadata);
    }

}

