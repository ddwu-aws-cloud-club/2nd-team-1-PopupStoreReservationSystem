package com.westsomsom.finalproject.login.api;

import com.westsomsom.finalproject.login.MsgEntity;
import com.westsomsom.finalproject.login.application.KakaoService;
import com.westsomsom.finalproject.login.dao.MemberRepository;
import com.westsomsom.finalproject.login.domain.Member;
import com.westsomsom.finalproject.login.dto.KakaoDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("kakao")
public class KakaoController {
    private final KakaoService kakaoService;

    @GetMapping("/callback")
    public ResponseEntity<MsgEntity> callback(HttpServletRequest request) throws Exception{
        KakaoDTO kakaoInfo = kakaoService.getKakaoInfo(request.getParameter("code"));

        return ResponseEntity.ok()
                .body(new MsgEntity("Success", kakaoInfo));
        //HTTP응답을 생성, Ok응답을 반환하며, 본문(body)로 MsgEntity 객체를 담고있음
    }

    //여기부터 새로운 코드 post를 위한 코드
    private final MemberRepository memberRepository;

    @PostMapping("/update-info")
    public ResponseEntity<MsgEntity> updateUserInfo(
            @RequestParam("gender") Integer gender,
            @RequestParam("ageGroup") String ageGroup,
            @RequestParam("residence") String residence,
            HttpSession session) {

        // 세션에서 로그인한 사용자 정보 가져오기
        Member member = (Member) session.getAttribute("member");
        if (member == null) {
            return ResponseEntity.badRequest().body(new MsgEntity("No logged-in user found", null));
        }

        // DB에서 실제 사용자 엔티티 조회
        Member sessionMember = memberRepository.findById(member.getMember_id())
                .orElseThrow(() -> new IllegalStateException("User not found in database"));

        // 사용자 정보 업데이트
        member.setGender(gender);
        member.setAgeGroup(ageGroup);
        member.setResidence(residence);

        //변경사항 저장
        memberRepository.save(member);

        // 세션 정보도 업데이트
        session.setAttribute("member", sessionMember);

        return ResponseEntity.ok(new MsgEntity("User info updated successfully", member));
    }
}
