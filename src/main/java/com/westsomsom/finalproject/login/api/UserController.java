package com.westsomsom.finalproject.login.api;

import com.westsomsom.finalproject.login.MsgEntity;
import com.westsomsom.finalproject.login.dao.MemberRepository;
import com.westsomsom.finalproject.login.domain.Member;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class UserController {
    private final MemberRepository memberRepository; // 유저정보 수정에 필요해서 추가

    @GetMapping("/mypage")
    public String getMyPage(HttpSession session, Model model) {
        // 세션에서 로그인한 사용자 정보 가져오기
        Member member = (Member) session.getAttribute("member");
        if (member == null) {
            model.addAttribute("error", "No logged-in user found");
            return "error"; // 에러 페이지로 이동
        }

        // 사용자 정보를 모델에 추가
        model.addAttribute("member", member);
        return "mypage"; // 마이페이지 뷰로 이동
    }

    //마이페이지 유저정보 수정 (카카오에서 가져온 정보를 제외한 성별, 연령대, 거주지에 대한 내용에 대해서만 수정가능)
    @PutMapping("/mypage/update")
    public ResponseEntity<MsgEntity> updateUserInfo(
            @RequestParam("gender") Integer gender,
            @RequestParam("ageGroup") String ageGroup,
            @RequestParam("residence") String residence,
            HttpSession session) {

        // Get 로그인된 유저의 세션
        Member member = (Member) session.getAttribute("member");
        if (member == null) {
            return ResponseEntity.badRequest().body(new MsgEntity("No logged-in user found", null));
        }

        // 유저정보 업데이트
        member.setGender(gender);
        member.setAgeGroup(ageGroup);
        member.setResidence(residence);

        // 변경된 내용 데이터베이스에 저장
        memberRepository.save(member);

        // 세션 객체 업데이트
        session.setAttribute("member", member);

        return ResponseEntity.ok(new MsgEntity("User information updated successfully", member));
    }
}
