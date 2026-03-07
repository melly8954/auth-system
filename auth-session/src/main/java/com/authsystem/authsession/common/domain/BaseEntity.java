package com.authsystem.authsession.common.domain;

import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@MappedSuperclass   // 상속한 Entity에 컬럼 포함
@EntityListeners(AuditingEntityListener.class)  // auditing 동작
public abstract class BaseEntity {

    @CreatedDate    // 생성 시 자동 저장
    private LocalDateTime createdAt;

    @LastModifiedDate   // 수정 시 자동 갱신
    private LocalDateTime updatedAt;
}
