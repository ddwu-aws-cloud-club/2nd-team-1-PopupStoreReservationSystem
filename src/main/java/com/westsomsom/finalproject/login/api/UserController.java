package com.westsomsom.finalproject.login.api;

import com.westsomsom.finalproject.login.response.MsgEntity;
import com.westsomsom.finalproject.user.dao.UserInfoRepository;
import com.westsomsom.finalproject.user.domain.UserInfo;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class UserController {
    private final UserInfoRepository userInfoRepository;

    @GetMapping("/api/user/{userId}")
    public ResponseEntity<MsgEntity> getUserInfo(@PathVariable("userId") String userId) {
        UserInfo userInfo = userInfoRepository.findByUserId(userId);

        if (userInfo == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new MsgEntity("User not found", null));
        }

        return ResponseEntity.ok(new MsgEntity("User information fetched successfully", userInfo));
    }

    @PutMapping("/api/user/{userId}")
    public ResponseEntity<MsgEntity> updateUserInfo(
            @PathVariable("userId") String userId,
            @RequestParam("phone") String phone,
            @RequestParam("gender") String gender,
            @RequestParam("age") int age,
            @RequestParam("cstAddrNo") String cstAddrNo) {

        UserInfo userInfo = userInfoRepository.findByUserId(userId);
        if (userInfo == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new MsgEntity("User not found", null));
        }

        // 추가 정보를 받아서 UserInfo 객체에 저장
        userInfo.setPhone(phone);
        userInfo.setGender(gender);
        userInfo.setAge(age);
        userInfo.setCstAddrNo(cstAddrNo);

        userInfoRepository.save(userInfo);

        return ResponseEntity.ok(new MsgEntity("User information updated successfully", userInfo));
    }
}
