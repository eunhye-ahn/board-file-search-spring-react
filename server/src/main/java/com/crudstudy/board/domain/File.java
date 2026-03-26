package com.crudstudy.board.domain;

import com.crudstudy.board.domain.base.Base;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Table: file
 * Columns:
 * id bigint AI PK
 * post_id bigint
 * original_name varchar(255)
 * stored_name varchar(255)
 * file_path varchar(500)
 * file_size bigint
 * created_at datetime
 */

@Entity
@Table(name = "file")
@Getter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class File extends Base {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @Column(nullable = false, name="original_name")
    private String originalName;

    //실제 저장이름 : 중복 방지를 위해 UUID+originalname으로 저장
    @Column(nullable = false, name="stored_name")
    private String storedName;

    //파일 다운로드 / 바로보기 접근 시 접근할 경로
    @Column(nullable = false, name="file_path", length = 500)
    private String filePath;

    //파일 크기 표시용
    @Column(nullable = false, name="file_size")
    private Long fileSize;

    //바로보기 시 브라우저가 파일을 어떻게 렌더링할지 결정
    @Column(nullable = false, name="content_type")
    private String contentType;
}
