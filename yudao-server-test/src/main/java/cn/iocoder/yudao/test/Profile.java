package cn.iocoder.yudao.test;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author: LeeFJ
 * @date: 2025/4/8 9:49
 * @description:
 */
@AllArgsConstructor
@Getter
public enum Profile {

    LOCAL("http://127.0.0.1:55002", "wangdongyu", "qwe123,./"),
    LeeFJ("http://192.168.10.131:9369","wangdongyu", "qwe123,./");



    private String url;
    private String name;
    private String password;

}
