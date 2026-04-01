package com.crudstudy.board.controller;

import com.crudstudy.board.dto.FileDownloadResponseDto;
import com.crudstudy.board.dto.FileViewResponseDto;
import com.crudstudy.board.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class FileController {
    private final FileService fileService;

    //파일을 다운로드하려면 파일정보를 가져와야하니까
    @GetMapping("/api/files/download/{fileId}")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long fileId) {
        FileDownloadResponseDto result = fileService.loadFile(fileId);
        if(result.getResource() != null) {
            return ResponseEntity
                    .status(HttpStatus.OK)
                    /**
                     * [WHAT] HTTP 헤더 > Content-Disposition 헤더
                     *      : 브라우저에게 이 응답을 어떻게 처리할지 알려주는 헤더
                     *
                     *  Content-Disposition: inline      →  브라우저에서 바로보기
                     * Content-Disposition: attachment  →  열지말고 바로 저장
                     */
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + result.getFileName() + "\"")
                    //바이너리 파일 명시
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(result.getResource());
        }
        else {
            //클라우디너리 리다이렉트
            return ResponseEntity
                    .status(HttpStatus.FOUND)
                    .header(HttpHeaders.LOCATION, result.getUrl() + "?fl_attachment=true")
                    .build();
        }
    }

    @GetMapping("/api/files/view/{fileId}")
    public ResponseEntity<?> viewFile(@PathVariable Long fileId) {

        FileViewResponseDto response = fileService.getViewFile(fileId);

        if(response.getResource() != null) {
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "inline; filename=\""+response.getFileName()+"\"")
                    .contentType(MediaType.parseMediaType(response.getResourceType()))
                    .body(response.getResource());
        }
        else{
            System.out.print(response.getUrl());
            return ResponseEntity
                    .status(HttpStatus.FOUND)
                    .header(HttpHeaders.LOCATION, response.getUrl())
                    .build();
        }
    }
}
