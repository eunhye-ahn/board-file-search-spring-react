package com.crudstudy.board.domain.base;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@MappedSuperclass
@Getter
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseTime extends Base {
    @Column(name = "updated_at")
    @LastModifiedDate
    protected LocalDateTime updatedAt;
//    private LocalDateTime updatedAt;
}
