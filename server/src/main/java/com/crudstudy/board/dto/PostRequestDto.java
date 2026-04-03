package com.crudstudy.board.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@AllArgsConstructor
public class PostRequestDto {
    private String title;
    private String content;
    private Long userId;
}
