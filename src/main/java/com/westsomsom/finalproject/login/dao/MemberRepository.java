package com.westsomsom.finalproject.login.dao;

import com.westsomsom.finalproject.login.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    Member save(Member member);
}
