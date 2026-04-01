package com.crudstudy.board.domain;

import com.crudstudy.board.domain.base.BaseTime;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Table: comment
 * Columns:
 * id bigint AI PK
 * post_id bigint
 * content text
 * is_deleted tinyint(1)
 * deleted_at datetime
 * created_at datetime
 * updated_at datetim
 */

/**
 * String -> varchar(255) 기본매핑
 * @Column(length = 500) -> varchar(500) 매핑
 * @Column(columnDefinition = "TEXT") -> text ~65,535bytes
 */

@Entity
@Table(name = "comment")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Comment extends BaseTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name="post_id",nullable = false)
    private Post post;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    /**
     * Boolean (대문자) - null 허용
     * boolean (소문자) - null 불가, 기본값 false
     * @Builder 쓰면 기본값이 무시되어서 명시 필요
     */
    @Column(nullable = false, name = "is_deleted")
    @Builder.Default
    private boolean isDeleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public void deleteComment(){
        isDeleted = true;
        deletedAt = LocalDateTime.now();
    }

    public void updateComment(String content){
        this.content = content;
    }
}
