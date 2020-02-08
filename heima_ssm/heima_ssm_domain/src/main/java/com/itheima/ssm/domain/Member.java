package com.itheima.ssm.domain;

import lombok.Data;

@Data
public class Member {
    private String id;
    private String name;
    private String nickname;
    private String phoneNum;
    private String email;
//省略getter/setter
}
