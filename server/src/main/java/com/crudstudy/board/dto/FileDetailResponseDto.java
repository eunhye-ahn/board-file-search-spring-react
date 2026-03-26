package com.crudstudy.board.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FileDetailResponseDto {
    private String fileName;
    private String filePath;
    private Long fileSize;
    private String fileType;
}
