package com.crudstudy.board.controller;

import com.crudstudy.board.dto.CommentResponseDto;
import com.crudstudy.board.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;

    @PostMapping("/api/posts/{postId}/comments")
    public ResponseEntity<?> addComment(@PathVariable Long postId,
                                        @RequestBody String content) {
        commentService.saveComment(postId, content);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .build();
    }
    //댓글삭제
    @DeleteMapping("/api/comments/{commentId}")
    public ResponseEntity<?> deleteComment(@PathVariable Long commentId){
        commentService.deleteComment( commentId);
        return ResponseEntity
                .status(HttpStatus.OK)
                .build();
    }
    //댓글수정
    @PutMapping("/api/comments/{commentId}")
    public ResponseEntity<?> updateComment(@PathVariable Long commentId,
                                           @RequestBody String content) {
        commentService.updateComment(commentId, content);
        return ResponseEntity
                .status(HttpStatus.OK)
                .build();
    }
    //해당 글 댓글조회(페이징)
    //
    @GetMapping("/api/posts/{postId}/comments")
    public ResponseEntity<?> getCommentsByPost(@RequestParam(defaultValue = "0") int page,
            @PathVariable Long postId) {
        List<CommentResponseDto> result = commentService.getComments(page, postId);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(result);
    }
}
