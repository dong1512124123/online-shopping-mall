package com.shop.mall.controller;

import com.shop.mall.service.MemberService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/member")
public class AdminMemberController {

    private final MemberService memberService;

    public AdminMemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("members", memberService.findAllMembers());
        return "admin/member/list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("member", memberService.findById(id));
        return "admin/member/detail";
    }

    @PostMapping("/{id}/toggle")
    public String toggleEnabled(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        memberService.toggleEnabled(id);
        redirectAttributes.addFlashAttribute("message", "회원 상태가 변경되었습니다.");
        return "redirect:/admin/member";
    }
}
