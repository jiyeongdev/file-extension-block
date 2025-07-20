package com.fileextension.proj.repository;

import com.fileextension.proj.entity.CustomExtension;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.fileextension.proj.entity.QCustomExtension.customExtension;

@Repository
@RequiredArgsConstructor
public class CustomExtensionQueryRepository {

    private final JPAQueryFactory queryFactory;

    public List<CustomExtension> findAllOrderByCreatedAtDesc() {
        return queryFactory
            .selectFrom(customExtension)
            .orderBy(customExtension.createdAt.desc())
            .fetch();
    }

    public long countCustomExtensions() {
        return queryFactory
            .selectFrom(customExtension)
            .fetchCount();
    }

    public boolean existsByExtensionName(String extensionName) {
        return queryFactory
            .selectOne()
            .from(customExtension)
            .where(customExtension.extensionName.eq(extensionName))
            .fetchFirst() != null;
    }
} 