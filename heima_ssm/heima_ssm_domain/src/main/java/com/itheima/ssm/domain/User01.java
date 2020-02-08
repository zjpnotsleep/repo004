package com.itheima.ssm.domain;

import java.util.List;

public class User01 extends UserInfo{
    List<String> userIds;

    public List<String> getUserIds() {
        return userIds;
    }

    public void setUserIds(List<String> userIds) {
        this.userIds = userIds;
    }

    @Override
    public String toString() {
        return "User01{" +
                "userIds=" + userIds +
                "} " + super.toString();
    }
}
