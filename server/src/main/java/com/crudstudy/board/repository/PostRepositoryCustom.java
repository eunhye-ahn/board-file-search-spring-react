package com.crudstudy.board.repository;

import com.crudstudy.board.domain.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

public interface PostRepositoryCustom {
    Page<Post> search(String keyword, String type,
                      LocalDateTime startDate, LocalDateTime endDate,
                      Pageable pageable);
}
