package com.shop.mall.service;

import com.shop.mall.entity.Admin;
import com.shop.mall.entity.Member;
import com.shop.mall.repository.AdminRepository;
import com.shop.mall.repository.MemberRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final MemberRepository memberRepository;
    private final AdminRepository adminRepository;

    public CustomUserDetailsService(MemberRepository memberRepository, AdminRepository adminRepository) {
        this.memberRepository = memberRepository;
        this.adminRepository = adminRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 관리자 먼저 확인
        Optional<Admin> admin = adminRepository.findByUsername(username);
        if (admin.isPresent()) {
            Admin a = admin.get();
            return new User(a.getUsername(), a.getPassword(),
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN")));
        }

        // 일반 회원 확인
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username));

        if (!member.getEnabled()) {
            throw new UsernameNotFoundException("비활성화된 계정입니다.");
        }

        return new User(member.getUsername(), member.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
    }
}
