package com.shop.mall.controller;

import com.shop.mall.entity.Member;
import com.shop.mall.service.MemberService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/mypage")
public class MyPageController {

    private final MemberService memberService;

    public MyPageController(MemberService memberService) {
        this.memberService = memberService;
    }

    // 마이페이지 메인
    @GetMapping
    public String myPage(Authentication authentication, Model model) {
        Member member = memberService.findByUsername(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("회원 정보를 찾을 수 없습니다."));
        model.addAttribute("member", member);
        return "mypage/index";
    }

    // 프로필 수정
    @PostMapping("/update")
    public String updateProfile(@RequestParam String nickname,
                                @RequestParam String phone,
                                Authentication authentication,
                                RedirectAttributes redirectAttributes) {
        try {
            if (nickname == null || nickname.trim().isEmpty() || nickname.length() > 50) {
                throw new IllegalArgumentException("닉네임은 1~50자 이내로 입력해주세요.");
            }
            if (phone != null && phone.length() > 20) {
                throw new IllegalArgumentException("전화번호 형식이 올바르지 않습니다.");
            }
            memberService.updateProfile(authentication.getName(), nickname.trim(), phone);
            redirectAttributes.addFlashAttribute("success", "프로필이 수정되었습니다.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/mypage";
    }

    // 비밀번호 변경
    @PostMapping("/password")
    public String changePassword(@RequestParam String currentPassword,
                                 @RequestParam String newPassword,
                                 Authentication authentication,
                                 RedirectAttributes redirectAttributes) {
        try {
            memberService.changePassword(authentication.getName(), currentPassword, newPassword);
            redirectAttributes.addFlashAttribute("success", "비밀번호가 변경되었습니다.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/mypage";
    }
}
