/*
 *Copyright © 2018 anji-plus
 *安吉加加信息技术有限公司
 *http://www.anji-plus.com
 *All rights reserved.
 */
package com.anji.captcha.model.vo;

import lombok.Data;

import java.awt.*;
import java.io.Serializable;
import java.util.List;

@Data
public class CaptchaVO implements Serializable {

    /**
     * 验证码id(后台申请)
     */
    private String captchaId;

    private String projectCode;

    /**
     * 验证码类型:(clickWord,blockPuzzle)
     */
    private String captchaType;

    private String captchaOriginalPath;

    private String captchaFontType;

    private Integer captchaFontSize;

    private String secretKey;

    /**
     * 原生图片base64
     */
    private String originalImageBase64;

    /**
     * 滑块点选坐标
     */
    private PointVO point;

    /**
     * 滑块图片base64
     */
    private String jigsawImageBase64;

    /**
     * 点选文字
     */
    private List<String> wordList;

    /**
     * 点选坐标
     */
    private List<Point> pointList;


    /**
     * 点坐标(base64加密传输)
     */
    private String pointJson;


    /**
     * UUID(每次请求的验证码唯一标识)
     */
    private String token;

    /**
     * 校验结果
     */
    private Boolean result = false;

    /**
     * 后台二次校验参数
     */
    private String captchaVerification;

    /***
     * 客户端UI组件id,组件初始化时设置一次，UUID
     */
    private String clientUid;
    /***
     * 客户端的请求时间，预留字段
     */
    private Long ts;

    /***
     * 客户端ip+userAgent
     */
    private String browserInfo;

    public void resetClientFlag() {
        this.browserInfo = null;
        this.clientUid = null;
    }

}
