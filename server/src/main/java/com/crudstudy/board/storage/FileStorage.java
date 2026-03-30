package com.crudstudy.board.storage;

import com.crudstudy.board.dto.FileUploadResult;
import org.springframework.web.multipart.MultipartFile;

/**
 * [WHAT] 파일 저장소 추상화 인터페이스
 *
 * [WHY] 저장 방식(로컬/S3 등)이 바뀌어도 서비스 코드를 수정하지 않기 위해
 *      저장소를 인터페이스로 분리함
 *
 * [흐름] controller -> postservice -> fileservice -> filestorage -> localstorage
 *                                                -> s3storage
 *
 * 인터페이스 메서드는 기본이 public abstract
 */
public interface FileStorage {
    FileUploadResult save(MultipartFile file);
    void delete(String filename, String resourceType);
}
