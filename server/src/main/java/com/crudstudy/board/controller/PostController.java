package com.crudstudy.board.controller;

import com.crudstudy.board.domain.Post;
import com.crudstudy.board.dto.*;
import com.crudstudy.board.service.FileService;
import com.crudstudy.board.service.PostService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RequiredArgsConstructor
@RestController
public class PostController {
    private final PostService postService;

    /**
     * [WHAT] @RequestPart : multipart/form-data(혼합데이터) 요청에서
     *          각 파트를 개별로 받는 어노테이션
     * @RequestBody와 달리 파일(바이너리) + JSON 데이터를 함께 받을 수 있음
     *
     * @param request (application/json)
     * @param files (multipart/form-data)
     *              -> required=false : NPE 방지 로직 필수 구현
     *
     *  [WHAT] consumes = MediaType.MULTIPART_FORM_DATA_VALUE
     *              :  multipart/form-data(혼합데이터) 형식의 요청만 허용
     *          -> 잘못된 요청 형식을 명확한 에러(415)로 반환
     */
    //글작성
    @PostMapping(value="/api/posts", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createPost(
            HttpSession session,
            @RequestPart PostRequestDto request,
            @RequestPart(required = false) List<MultipartFile> files
            ) {
        SecurityContext context = (SecurityContext) session.getAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY
        );
        String email = context.getAuthentication().getName();
        PostCreateResponseDto result = postService.save(email, request,files);
        return ResponseEntity.
                status(HttpStatus.CREATED)
                .body(result);
    }

    //글삭제
    @DeleteMapping("/api/posts/{postId}")
    public ResponseEntity<?> deletePost(@PathVariable Long postId) {
        postService.delete(postId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .build();
    }

    /**
     * 업데이트 로직 put(전체데이터 전송) vs patch(바뀐 것만 전송) 중 선택
     * put : 필드적을때
     * patch : 필드많을때
     */
    @PutMapping(value = "/api/posts/{postId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updatePost(@PathVariable Long postId,
                                        @RequestPart PostUpdateRequestDto request,
                                        @RequestPart(required = false) List<MultipartFile> files){
        System.out.println("title:" + request.getTitle());
        System.out.println("content:" + request.getContent());

        postService.updatePost(postId,request,files);

        return ResponseEntity
                .status(HttpStatus.OK)
                .build();
    }

    //글 상세조회
    @GetMapping("/api/posts/{postId}")
    public ResponseEntity<?> getPost(@PathVariable Long postId) {
        //글 상세조회서비스
        PostDetailResponseDto post = postService.getPostDetail(postId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(post);
    }

    //글 전체조회(페이징)
    @GetMapping("/api/posts")
    public ResponseEntity<?> getAllPosts(
            @RequestParam(defaultValue = "1") int page //클라에서 보내는
    ) {
        PostPageResponseDto result = postService.getPostList(page);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(result);
    }
}
