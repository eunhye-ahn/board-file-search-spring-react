package com.crudstudy.board.service;

import com.crudstudy.board.domain.Comment;
import com.crudstudy.board.domain.Post;
import com.crudstudy.board.domain.User;
import com.crudstudy.board.dto.CommentRequestDto;
import com.crudstudy.board.dto.CommentResponseDto;
import com.crudstudy.board.exception.CustomException;
import com.crudstudy.board.exception.ErrorCode;
import com.crudstudy.board.repository.CommentRepository;
import com.crudstudy.board.repository.PostRepository;
import com.crudstudy.board.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    /**
     *
     * Post와 참조 관계를 끊고 postId만 낳으면 안되냐?
     * 어차피 Post랑 동시에 저장되지않잖아
     * : 그럼 삭제된 포스트인데 댓글이 존재하는 고아데이터가 존재할 수 있음
     */
    @Transactional
    public void saveComment(Long postId, String content, Long userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(()-> new CustomException(ErrorCode.POST_NOT_FOUND));
        //User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        //유저엔티티에 유저디테일 직접구현하면 db조회없이 바로꺼내기 가능
        //-> 도메인객체에서 시큐리티 로직이 섞이기때문에 분리함
        User user = userRepository.findById(userId)
                .orElseThrow(()-> new CustomException(ErrorCode.USER_NOT_FOUND));
        Comment comment = Comment.builder()
                .post(post)
                .content(content)
                .user(user)
                .build();
        commentRepository.save(comment);
    }

    //소프트딜리트
    @Transactional
    public void deleteComment(Long commentId) {
        //삭제 전에 존재확인
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(()->new CustomException(ErrorCode.COMMENT_NOT_FOUND));
        //더티체킹
        comment.deleteComment();
    }
    @Transactional
    public void updateComment(Long commentId, String content) {
        //업데이트전에 존재확인
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(()->new CustomException(ErrorCode.COMMENT_NOT_FOUND));
        //업데이트
        comment.updateComment(content);
    }

    public Page<CommentResponseDto> getComments(int page, Long postId) {
        Pageable pageable = PageRequest.of(page, 5, Sort.by("createdAt").descending());
        Post post = postRepository.findById(postId)
                .orElseThrow(()->new CustomException(ErrorCode.POST_NOT_FOUND));
        return commentRepository.findByPostAndIsDeletedFalse(post, pageable)
                .map(comment -> new CommentResponseDto(
                        comment.getId(),
                        comment.getContent(),
                        /**
                         * n+1문제 주의
                         * findByPostAndIsDeletedFalse() 실행
                         * → SELECT * FROM comment WHERE post_id = 1    (1번)
                         *     ↓
                         * 댓글 3개 반환 [comment1, comment2, comment3]
                         *     ↓
                         * map()으로 순회하면서 getUser().getName() 호출
                         * → SELECT * FROM user WHERE id = 1   (comment1의 유저)
                         * → SELECT * FROM user WHERE id = 2   (comment2의 유저)
                         * → SELECT * FROM user WHERE id = 3   (comment3의 유저)
                         */
                        comment.getUser().getName(),
                        comment.getCreatedAt()));
    }
    /**
     * stream>map
     * map
     * 차이점
     */
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

