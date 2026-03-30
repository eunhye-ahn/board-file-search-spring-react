package com.crudstudy.board.service;

import com.crudstudy.board.domain.File;
import com.crudstudy.board.dto.*;
import com.crudstudy.board.domain.Post;
import com.crudstudy.board.exception.CustomException;
import com.crudstudy.board.exception.ErrorCode;
import com.crudstudy.board.repository.FileRepository;
import com.crudstudy.board.storage.CloudinaryFileStorage;
import com.crudstudy.board.storage.FileStorage;
import com.crudstudy.board.storage.LocalFileStorage;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * 파일 업로드/삭제 비즈니스 로직 처리 (저장하기 전)
 *
 * [WHAT] MultipartFile : 클라이언트가 파일을 업로드할 때 Spring이 파일을 담아주는 객체
 *
 * getOriginalFilename     원본파일명
 * getSize                  파일크기
 * getContentType           파일타입
 * getBytes                 실제 바이너리 데이터
 * getInputStream           파일 읽기용 스트림 -> 실제 파일을 저장소에 복사할 때 사용
 *                             Files.copy(multipartFile.getInputStream(), targetPath)
 * isEmpty                  파일이 비었는지
 *
 * 위의 정보를 얻기위해 MultipartFile 객체로 변환해서 저장하는 것
 *
 * [흐름] 클라이언트 파일 선택
 *           > HTTP 요청에 바이너리로 담김 (multipart/form-data)
 *           > Spring이 MultipartFile로 변환
 *           > FileStorage에서 getInputStream()으로 저장소에 복사
 *           > FileService에서 파일 정보 DB 저장
 */

@Service
@RequiredArgsConstructor
public class FileService {
    private final FileStorage fileStorage;
    private final FileRepository fileRepository;

    //여러 파일 처리
    public void uploadFiles(Post post, List<MultipartFile> files) {
        if (files != null && !files.isEmpty()) {
            for (MultipartFile file : files) {
                uploadFile(post, file);
            }
        }
    }
    public void uploadFile(Post post, MultipartFile file) {
        System.out.println("contentType : " + file.getContentType());
        FileUploadResult result = fileStorage.save(file);

        File fileEntity = File.builder()
                .post(post)
                .originalName(file.getOriginalFilename())
                .storedName(result.getStoredName()) //public_id
                .filePath(result.getFilePath())     //secure_url
                .fileSize(file.getSize())
                .resourceType(result.getResourceType())
                .build();
        fileRepository.save(fileEntity);
    }

    /**
     * [refactoring]
     *  param : post->postId 로 변경
     *  save는 post를 참조하고있는 file을 빌드해야되기때문에
     *  post 객체 필요 (id만 저장 X -> jpa 연관관계가 끊어짐)
     *  delete나 조회는 이미 저장된 post를 file리포에서 찾으면 되기때문에
     *  post 객체 불필요 (id로 접근가능)
     */
    //POST 삭제 시 -> 연관 파일 하드딜리트
    public void deleteAllFile(Long postId){
        List<File> files = fileRepository.findByPostId(postId);
        if(files != null && !files.isEmpty()){
            for(File file : files){
                fileStorage.delete(file.getStoredName(),file.getResourceType());
            }
        }
        fileRepository.deleteByPostId(postId);
    }

    public void deleteSelectedFile(List<Long> fileIds){
        if(fileIds != null && !fileIds.isEmpty()){
            for(Long fileId : fileIds){
                File selectedFile = fileRepository.findById(fileId)
                        .orElseThrow(() -> new CustomException(ErrorCode.FILE_NOT_FOUND));
                fileStorage.delete(selectedFile.getStoredName(), selectedFile.getResourceType());
            }
            fileRepository.deleteAllByIdIn(fileIds);
        }
    }

    //포스트상세조회 - 파일표시
    public List<FileDetailResponseDto> getFilesByPost(Long postId){
        //파일찾기
        return fileRepository.findByPostId(postId)
                .stream()
                .map(file -> new FileDetailResponseDto(
                        file.getOriginalName(),
                        file.getFilePath(),
                        file.getFileSize(),
                        file.getResourceType()
                        ))
                .toList();
    }

    //포스트 목록조회용
    public List<FileByPostsListResponseDto> getFileDownload(Long postId){
        return fileRepository.findByPostId(postId)
                .stream()
                .map(file -> new FileByPostsListResponseDto(
                        file.getId()
                ))
                .toList();
    }

    /**
     * [WHAT] Resource : 바이트로 읽을 준비가 된 객체
     * Resource {
     *     boolean exists();          // 파일 존재하는지
     *     String getFilename();      // 파일 이름
     *     long contentLength();      // 파일 크기
     *     InputStream getInputStream(); // ← 실제 바이트 읽는 통로
     * }
     *
     * [흐름] loadFile() Resource반환 > 컨트롤러가 바디에 담아서 응답
     *          >스프링이 리소스를 보고 getInputStream()을 호출해서 바이트로 읽음
     *          >실제로 바이트로 읽는건 스프링이 알아서 처리
     */
    //파일 다운로드 - 파일 바이트 전송
    public FileDownloadResponseDto loadFile(Long fileId){
        //fileId로 db에서 파일 정보 조회, 없으면 예외 발생
        File file = fileRepository.findById(fileId)
                .orElseThrow(() -> new CustomException(ErrorCode.FILE_NOT_FOUND));

        if(fileStorage instanceof LocalFileStorage localFileStorage){
            Resource resource = localFileStorage.getResource(file.getFilePath());
            return new FileDownloadResponseDto(file.getOriginalName(),resource);
        }
        if(fileStorage instanceof CloudinaryFileStorage cloudinaryFileStorage){
            String url = cloudinaryFileStorage.getUrl(file.getFilePath());
            return new FileDownloadResponseDto(file.getOriginalName(), url);
        }
        throw new CustomException(ErrorCode.FILE_NOT_FOUND);
    }

    /**
     * 파일 다운로드 할때 :
     * file의 오리지널 네임
     * file의 path > 읽을수있도록 Path로 변환해서 전달 - Resource 객체 구현
     * Resource : 파일을 바이트타입으로 읽어서 클라에게 전달
     */


    //파일 바로보기(리소스객체, 오리지널네임, 컨텐트타입)
    public FileViewResponseDto getViewFile(Long fileId){
        File file = fileRepository.findById(fileId)
                .orElseThrow(() -> new CustomException(ErrorCode.FILE_NOT_FOUND));

//        Resource resource = ((LocalFileStorage)fileStorage).getResource(file.getFilePath());
        if(fileStorage instanceof LocalFileStorage localFileStorage){
            Resource resource = localFileStorage.getResource(file.getFilePath());
            return new FileViewResponseDto(resource, file.getOriginalName(), file.getResourceType());
        }
        if(fileStorage instanceof CloudinaryFileStorage cloudinaryFileStorage){
            String url = cloudinaryFileStorage.getUrl(file.getFilePath());
            return new FileViewResponseDto(url, file.getOriginalName(), file.getResourceType());
        }
        throw new CustomException(ErrorCode.FILE_NOT_FOUND);
    }
}
