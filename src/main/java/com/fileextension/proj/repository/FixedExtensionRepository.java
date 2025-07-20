package com.fileextension.proj.repository;

import com.fileextension.proj.entity.FixedExtension;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FixedExtensionRepository extends JpaRepository<FixedExtension, Long> {

    // 기본 CRUD 작업만 JPA Repository에서 처리
    // 복잡한 쿼리는 QueryDSL로 이동 (FixedExtensionQueryRepository에서 처리)
} 