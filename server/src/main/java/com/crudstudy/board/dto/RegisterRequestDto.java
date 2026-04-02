package com.crudstudy.board.dto;

import com.crudstudy.board.validator.ValidPassword;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class RegisterRequestDto {
    @NotBlank(message = "이름을 입력하세요")
    private String name;

    @NotBlank(message = "이메일을 입력하세요")
    @Email
    private String email;

    @NotBlank(message = "비밀번호를 입력하세요")
    @ValidPassword
    private String password;
}
