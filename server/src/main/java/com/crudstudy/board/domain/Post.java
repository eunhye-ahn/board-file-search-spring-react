package com.crudstudy.board.domain;

import com.crudstudy.board.domain.base.BaseTime;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

/**
 * creationTimestamp vs createdDate (스프링 친화적 but 설정필요) => @EntityListeners
 *
 * post 랑 file간의 양방향연결?? 또는 post->file로 단방향 연결
 * why : post를 업로드할때 file을 같이 업로드해야해서 한 클래스에서 서비스 로직을 묶어야 트랜잭션 가능
 * 근데 post에서 file 연결이 없어서 File을 별도로 계속 불러와줘야함
 */

@Entity
@Table(name = "post")
@Getter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class Post extends BaseTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="user_id")
    private User user;

    @Column(name="view_count")
    private int viewCount = 0;

    @Column(name="is_deleted")
    private boolean isDeleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public void delete(){
        isDeleted = true;
        deletedAt = LocalDateTime.now();
    }
    public void update(String title, String content){
        this.title = title;
        this.content = content;
    }
    public void increaseViewCount(){
        viewCount++;
    }
}
