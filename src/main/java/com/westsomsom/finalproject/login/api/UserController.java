package com.westsomsom.finalproject.login.api;

import com.westsomsom.finalproject.login.response.MsgEntity;
import com.westsomsom.finalproject.user.dao.UserInfoRepository;
import com.westsomsom.finalproject.user.domain.UserInfo;
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
    private final UserInfoRepository userInfoRepository;

    @GetMapping("/mypage")
    public String getMyPage(HttpSession session, Model model) {
        UserInfo userInfo = (UserInfo) session.getAttribute("userInfo");
        if (userInfo == null) {
            model.addAttribute("error", "No logged-in user found");
            return "error";
        }

        model.addAttribute("userInfo", userInfo);
        return "mypage";
    }

    @PutMapping("/mypage/update")
    public ResponseEntity<MsgEntity> updateUserInfo(
            @RequestParam("phone") String phone,
            @RequestParam("gender") String gender,
            @RequestParam("age") int age,
            @RequestParam("cstAddrNo") String cstAddrNo,
            HttpSession session) {

        UserInfo userInfo = (UserInfo) session.getAttribute("userInfo");
        if (userInfo == null) {
            return ResponseEntity.badRequest().body(new MsgEntity("No logged-in user found", null));
        }
        userInfo.setPhone(phone);
        userInfo.setGender(gender);
        userInfo.setAge(age);
        userInfo.setCstAddrNo(cstAddrNo);

        userInfoRepository.save(userInfo);

        session.setAttribute("userInfo", userInfo);

        return ResponseEntity.ok(new MsgEntity("User information updated successfully", userInfo));
    }
}
