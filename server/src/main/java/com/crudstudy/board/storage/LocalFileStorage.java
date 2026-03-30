package com.crudstudy.board.storage;


import com.crudstudy.board.domain.File;
import com.crudstudy.board.dto.FileUploadResult;
import com.crudstudy.board.dto.FileViewResponseDto;
import com.crudstudy.board.exception.CustomException;
import com.crudstudy.board.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * 서버시작 > 스프링이 자동으로 @Component 붙은 애들을 다 객체로 만들어서
 *              컨테이너에 보관
 */
@Profile("local")
@Component
public class LocalFileStorage implements FileStorage {

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Override
    public FileUploadResult save(MultipartFile file) {
        //[WHAT] 문자열 경로를 Path 객체로 변환
        //[WHY] 문자열로 경로를 다루면 OS마다 구분자가 달라서 오류 발생
        Path dirPath = Paths.get(uploadDir);
        //[WHAT] Files - 파일/디텍토리 조작 유틸클래스
        //패스위치에 /uploadDir 폴더가 없는 경우 생성 > 배포했을때 자동 생성되도록
        if (!Files.exists(dirPath)) {
            try{
                Files.createDirectories(dirPath);
            }catch(IOException e){
                throw new CustomException(ErrorCode.FILE_UPLOAD_FAILED);
            }
        }

        //같은 파일명이 들어와도 덮어쓰기 방지
        String storedName = UUID.randomUUID()+"_"+file.getOriginalFilename();
        Path filePath = dirPath.resolve(storedName);


        //디스크에 파일저장(
        // Files.copy가 내부적으로 스트림을 열고 닫아줌
        //  -> 그래서 따로 try-with-resource를 안써도됨
        // )
        try {
            Files.copy(file.getInputStream(), filePath);
        }catch(IOException e){
            throw new CustomException(ErrorCode.FILE_UPLOAD_FAILED);
        }

        String filePathStr = filePath.toString();
        String resourceType = file.getContentType();

        return new FileUploadResult(storedName, filePathStr, resourceType);
    }

    @Override
    public void delete(String filename, String resourceType) {
        Path filePath = Paths.get(uploadDir,filename);
        try{
            Files.deleteIfExists(filePath);
        }catch(IOException e){ //파일 시스템 권한이 없을 때 발생
            throw new CustomException(ErrorCode.FILE_DELETE_FAILED);
        }
    }

    public Resource getResource(String filePath){
        return new FileSystemResource(filePath);
    }
}
