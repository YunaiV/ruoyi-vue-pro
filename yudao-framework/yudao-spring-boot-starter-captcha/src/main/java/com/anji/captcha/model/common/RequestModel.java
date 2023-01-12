/*
 *Copyright © 2018 anji-plus
 *安吉加加信息技术有限公司
 *http://www.anji-plus.com
 *All rights reserved.
 */
package com.anji.captcha.model.common;


import cn.hutool.core.util.StrUtil;
import lombok.Data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

@Data
public class RequestModel implements Serializable {

    private static final long serialVersionUID = -5800786065305114784L;

    /**
     * 当前请求接口路径 /business/accessUser/login
     */
    private String servletPath;

    /**
     * {"reqData":{"password":"*****","userName":"admin"},"sign":"a304a7f296f565b6d2009797f68180f0","time":"1542456453355","token":""}
     */
    private String requestString;

    /**
     * {"password":"****","userName":"admin"}
     */
    private HashMap reqData;

    private String token;

    private Long userId;

    private String userName;

    private List<Long> projectList;

    //拥有哪些分组
    private List<Long> groupIdList;

    private String target;

    private String sign;

    private String time;

    private String sourceIP;

    /**
     * 校验自身参数合法性
     *
     * @return
     */
    public boolean isVaildateRequest() {
        if (StrUtil.isBlank(sign) || StrUtil.isBlank(time)) {
            return false;
        }
        return true;
    }

    public String getServletPath() {
        return servletPath;
    }

    public void setServletPath(String servletPath) {
        this.servletPath = servletPath;
    }

}
