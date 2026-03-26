package com.crudstudy.board.domain.base;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@EntityListeners(AuditingEntityListener.class)
@MappedSuperclass
public abstract class Base {
    @Column(nullable = false, name="created_at")
    @CreatedDate
    protected LocalDateTime createdAt;
//    private LocalDateTime createdAt;
}
