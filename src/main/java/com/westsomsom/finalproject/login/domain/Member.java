package com.westsomsom.finalproject.login.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor //매개변수가 없는 기본 생성자를 생성 -> 엔티티 클래스에 기본 생성자가 있어야 하기 때문에 사용
public class Member {
    @Id
    @GeneratedValue
    @Column(name = "member_id")

    private Long member_id;
    private String username;
    private String email;

    private Integer gender; // 0: 남자, 1: 여자
    private String ageGroup; // 연령대
    private String residence; // 거주지
}
