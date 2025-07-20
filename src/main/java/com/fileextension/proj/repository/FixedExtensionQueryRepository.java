package com.fileextension.proj.repository;

import com.fileextension.proj.entity.FixedExtension;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import static com.fileextension.proj.entity.QFixedExtension.fixedExtension;

@Repository
@RequiredArgsConstructor
public class FixedExtensionQueryRepository {

    private final JPAQueryFactory queryFactory;

    public List<FixedExtension> findAllOrderByDisplayName() {
        return queryFactory
            .selectFrom(fixedExtension)
            .orderBy(fixedExtension.displayName.asc())
            .fetch();
    }


    public Optional<Boolean> findIsBlockedByExtensionName(String extensionName) {
        return Optional.ofNullable(
            queryFactory
                .select(fixedExtension.isBlocked)
                .from(fixedExtension)
                .where(fixedExtension.extensionName.eq(extensionName))
                .fetchOne()
        );
    }

    public boolean existsByExtensionName(String extensionName) {
        return queryFactory
            .selectOne()
            .from(fixedExtension)
            .where(fixedExtension.extensionName.eq(extensionName))
            .fetchFirst() != null;
    }
} 