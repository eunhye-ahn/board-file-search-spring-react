package com.crudstudy.board.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class PostDetailResponseDto {
    private String title;
    private String content;
    private List<FileDetailResponseDto> files;
    private LocalDateTime createdAt;
}
