package com.crudstudy.board.dto;

import lombok.Getter;
import org.springframework.core.io.Resource;

@Getter
public class FileDownloadResponseDto {
    private String fileName;
    private Resource resource; //로컬용
    private String url;         //cloudinary용

    public FileDownloadResponseDto(String fileName, Resource resource) {
        this.fileName = fileName;
        this.resource = resource;
    }
    public FileDownloadResponseDto(String fileName, String url) {
        this.fileName = fileName;
        this.url = url;
    }
}
