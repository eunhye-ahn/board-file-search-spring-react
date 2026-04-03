package com.crudstudy.board.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class PostListReponseDto {
    private Long postId;
    private String title;
    private String userName;
    private LocalDateTime createdAt;
    private int viewCount;
    private List<FileByPostsListResponseDto> files;
}
