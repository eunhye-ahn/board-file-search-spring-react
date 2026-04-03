package com.crudstudy.board.repository;

import com.crudstudy.board.domain.Post;
import com.crudstudy.board.domain.QPost;
import com.querydsl.core.BooleanBuilder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

public class PostRepositoryImpl implements PostRepositoryCustom{

    @Override
    public Page<Post> search(String keyword, String type, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        QPost post = QPost.post;
        BooleanBuilder builder = new BooleanBuilder();

        //키워드검색
        if(StringUtils.hasText(keyword)){
            switch (type) {
                case "title" -> builder.and(post.title.containsIgnoreCase(keyword));
                case "content" -> builder.and(post.content.containsIgnoreCase(keyword));
                case "titleContent" ->
                        builder.and(post.title.containsIgnoreCase(keyword)
                        .or(post.content.containsIgnoreCase(keyword))
                        );
//                case "author" ->
//                        builder.and(post.author.containsIgnoreCase(keyword));
            }
        }
        return null;
    }
}
