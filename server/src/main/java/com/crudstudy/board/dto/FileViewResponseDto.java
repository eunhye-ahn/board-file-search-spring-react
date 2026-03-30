package com.crudstudy.board.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.core.io.Resource;

@Getter
public class FileViewResponseDto {
    private Resource resource; //로컬용
    private String url;         //cloudinary용
    private String fileName;
    private String resourceType;

    // 로컬 생성자
    public FileViewResponseDto(Resource resource, String fileName, String resourceType) {
        this.resource = resource;
        this.fileName = fileName;
        this.resourceType = resourceType;
    }

    // Cloudinary 생성자
    public FileViewResponseDto(String url, String fileName, String resourceType) {
        this.url = url;
        this.fileName = fileName;
        this.resourceType = resourceType;
    }
}
