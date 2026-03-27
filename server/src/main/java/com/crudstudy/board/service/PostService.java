package com.crudstudy.board.service;

import com.crudstudy.board.domain.Post;
import com.crudstudy.board.dto.*;
import com.crudstudy.board.exception.CustomException;
import com.crudstudy.board.exception.ErrorCode;
import com.crudstudy.board.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RequiredArgsConstructor
@Service
public class PostService {
    private final PostRepository postRepository;
    private final FileService fileService;

    /**
     * [WHY] PostService 안에서 FileService를 호출처리하는 이유
     *      post 안에 file이 첨부되는 형식이기 때문에
     *      post가 저장이 안되면 file도 저장안되는 트랜잭션으로 묶여야하므로 한클래스에서 호출해야한다
     *
     * Q : TransientPropertyValueException: post가 비영속 상태에서 file 저장 호출로 에러
     * [주의] file이 post를 참조하고 있기때문에 post데이터가 저장된 후 file이 저장되어야한다.
     */
    //글작성
    @Transactional
    public void save(PostRequestDto request, List<MultipartFile> files) {
        //글 저장
        Post post = Post.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .build();
        postRepository.save(post);
        fileService.uploadFiles(post,files);
    }

    /**
     * 파일 : 하드딜리트
     * 글 : 소프트딜리트
     *
     * 소프트딜리트는 도메인 클래스의 메서드로 제어
     * => Dirty Checking (변경 감지)
     *      : JPA가 트랜잭션 안에서 엔티티 변경을
     *          자동으로 감지해서 UPDATE 쿼리를 날려주는 기능
     */
    //글삭제
    @Transactional
    public void delete(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(()-> new CustomException(ErrorCode.POST_NOT_FOUND));

        //같은 postId 쓰는 파일 하드딜리트
        fileService.deleteAllFile(post.getId());

        //소프트 딜리트 > 더티체킹으로 db 자동 update
        post.delete();
    }

    //글수정
    @Transactional
    public void updatePost(Long postId, PostUpdateRequestDto request, List<MultipartFile> files) {
        Post post = postRepository.findById(postId)
                .orElseThrow(()-> new CustomException(ErrorCode.POST_NOT_FOUND));

        //글수정
        post.update(request.getTitle(), request.getContent());

        //뉴파일 추가 - 더티체킹
        fileService.uploadFiles(post,files);

        //삭제파일아이디로 파일 삭제 - 더티체킹
        fileService.deleteSelectedFile(request.getDeleteFileIds());
    }

    //글 상세조회
    @Transactional
    public PostDetailResponseDto getPostDetail(Long postId) {
        //포스트찾기
        Post post = postRepository.findById(postId)
                .orElseThrow(()-> new CustomException(ErrorCode.POST_NOT_FOUND));
        post.increaseViewCount();
        //파일서비스 호출
        List<FileDetailResponseDto> files = fileService.getFilesByPost(post.getId());
        //dto에 담기
        System.out.println("updatedAt:"+post.getUpdatedAt());
        System.out.println("createdAt:"+post.getCreatedAt()); //jpa가 못읽어옴
        return new PostDetailResponseDto(post.getTitle(), post.getContent(),
                files, post.getCreatedAt());
    }

    /**
     * [WHAT] 게시글 목록 페이징 조회
     * offset 방식 : page(몇번째 페이지) * size(한 페이지당 개수)로 시작 위치 계산
     *
     * [WHY] 전체 데이터를 한번에 조회하면 성능 저하
     *      페이징으로 필요한 만큼만 조회
     *
     * [흐름]
     * 1. 클라이언트가 page, size 파라미터로 요청
     *      GET /api/posts?page=0&size=10
     *          ↓
     * 2. PageRequest.of(page, size, sort) 로 Pageable 객체 생성
     *      - page : 0부터 시작
     *      - size : 한페이지당 기준
     *      - sort : 정렬기준
     *          ↓
     * 3. JPA가 Pageable 받아서 자동으로 offset 쿼리 생성
     *      SELECT * FROM post ORDER BY created_at DESC LIMIT 10 OFFSET 0
     *          ↓
     * 4. Page<Post> → map()으로 PostListResponseDto 변환
     *          ↓
     * 5. Page 객체 반환 (content, totalPages, totalElements 등 포함)
     */
    //글 목록조회
    public Page<PostListReponseDto> getPostList(int page) {
        Pageable pageable = PageRequest.of(page, 3, Sort.by("createdAt").descending());

        return postRepository.findAll(pageable) //pageable 조건으로 post목록조회
                .map(post -> new PostListReponseDto(
                        post.getTitle(),
                        post.getCreatedAt(),
                        post.getViewCount(),
                        fileService.getFileDownload(post.getId())
                ));
    }
}