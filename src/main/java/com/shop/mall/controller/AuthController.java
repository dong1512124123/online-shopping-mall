package com.shop.mall.controller;

import com.shop.mall.service.MemberService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/auth")
public class AuthController {

    private final MemberService memberService;

    public AuthController(MemberService memberService) {
        this.memberService = memberService;
    }

    @GetMapping("/login")
    public String loginPage(@RequestParam(value = "error", required = false) String error,
                            @RequestParam(value = "logout", required = false) String logout,
                            Model model) {
        if (error != null) {
            model.addAttribute("errorMessage", "아이디 또는 비밀번호가 일치하지 않습니다.");
        }
        if (logout != null) {
            model.addAttribute("logoutMessage", "로그아웃 되었습니다.");
        }
        return "auth/login";
    }

    @GetMapping("/register")
    public String registerPage() {
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@RequestParam String username,
                           @RequestParam String password,
                           @RequestParam String passwordConfirm,
                           @RequestParam String nickname,
                           @RequestParam String email,
                           @RequestParam(required = false) String phone,
                           RedirectAttributes redirectAttributes) {
        try {
            // 입력값 검증
            if (username == null || username.trim().length() < 4 || username.trim().length() > 50) {
                throw new IllegalArgumentException("아이디는 4~50자여야 합니다.");
            }
            if (!username.matches("^[a-zA-Z0-9_]+$")) {
                throw new IllegalArgumentException("아이디는 영문, 숫자, 밑줄(_)만 사용 가능합니다.");
            }
            if (!password.equals(passwordConfirm)) {
                throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
            }
            if (password.length() < 8 || password.length() > 100) {
                throw new IllegalArgumentException("비밀번호는 8~100자여야 합니다.");
            }
            if (nickname == null || nickname.trim().isEmpty() || nickname.trim().length() > 50) {
                throw new IllegalArgumentException("닉네임은 1~50자여야 합니다.");
            }
            if (email == null || !email.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
                throw new IllegalArgumentException("올바른 이메일 형식이 아닙니다.");
            }
            if (phone != null && !phone.isBlank() && !phone.matches("^01[0-9]-?\\d{3,4}-?\\d{4}$")) {
                throw new IllegalArgumentException("올바른 전화번호 형식이 아닙니다.");
            }

            memberService.register(username.trim(), password, nickname.trim(), email.trim(), phone);
            redirectAttributes.addFlashAttribute("successMessage", "회원가입이 완료되었습니다. 로그인해주세요.");
            return "redirect:/auth/login";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/auth/register";
        }
    }
}
