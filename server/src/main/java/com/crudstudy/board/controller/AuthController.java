package com.crudstudy.board.controller;

import com.crudstudy.board.dto.LoginRequestDto;
import com.crudstudy.board.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;


/**
 *
 세션
 → 시큐리티가 세션 생성, 쿠키 발급, 검증 다 해줌

 JWT
 → 토큰 생성 로직 직접 구현 - JwtProvider
 → 토큰 검증 필터 직접 구현 - JwtFilter
 → 쿠키/헤더 설정 직접  - JwtFilter
 → Refresh Token 관리 직접  - JwtFilter
 → 만료 처리 직접 - JwtProvider(토큰파싱+예외처리)

 /**
 * [WHAT] 인증+인가를 해주는 틀
 *          -> 그걸 무엇으로 처리하는가? 세션 or JWT
 * [흐름] HTTP요청 > FilterChain(필터들이 순서대로 처리)
 *       > AuthenticationManager(인증처리) > UserDetailsService(사용자정보조회)
 *       > SecurityContext(인증결과저장) > Controller 도달
 *
 * [비교]                     세션         vs      JWT 적용 차이
 * 필터                       기본필터             JwtFilter 직접추가
 * 인증정보저장                  서버세션           SecurityContext에만
 * UserDetailsService       세션 생성시 1회       매요청마다 호출
 */

@RestController
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    //login
    @PostMapping("/api/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDto request) {
        //인증처리 서비스
        authService.login(request);
        
        return ResponseEntity
                .status(HttpStatus.OK)
                .build();
    }
    //logout
    @PostMapping("/api/logout")
    public ResponseEntity<?> logout(@AuthenticationPrincipal UserDetails user, HttpServletResponse response) {
        //쿠키삭제+서버에세션삭제
        authService.logout(user, response);
        //리턴 ok
    }
}
