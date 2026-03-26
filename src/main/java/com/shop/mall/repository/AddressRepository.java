package com.shop.mall.repository;

import com.shop.mall.entity.Address;
import com.shop.mall.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface AddressRepository extends JpaRepository<Address, Long> {
    List<Address> findByMemberOrderByIsDefaultDesc(Member member);
    Optional<Address> findByMemberAndIsDefaultTrue(Member member);
}
