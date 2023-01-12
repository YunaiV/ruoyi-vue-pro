/*
 *Copyright © 2018 anji-plus
 *安吉加加信息技术有限公司
 *http://www.anji-plus.com
 *All rights reserved.
 */
package com.anji.captcha.model.common;

import com.anji.captcha.util.StringUtils;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

public class RequestModel implements Serializable {

    private static final long serialVersionUID = -5800786065305114784L;

    /**当前请求接口路径 /business/accessUser/login */
    private String servletPath;

    /** {"reqData":{"password":"*****","userName":"admin"},"sign":"a304a7f296f565b6d2009797f68180f0","time":"1542456453355","token":""} */
    private String requestString;

    /** {"password":"****","userName":"admin"} */
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
     * @return
     */
    public boolean isVaildateRequest() {
        if (StringUtils.isBlank(sign) || StringUtils.isBlank(time)) {
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

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public List<Long> getProjectList() {
        return projectList;
    }

    public void setProjectList(List<Long> projectList) {
        this.projectList = projectList;
    }

    public List<Long> getGroupIdList() {
        return groupIdList;
    }

    public void setGroupIdList(List<Long> groupIdList) {
        this.groupIdList = groupIdList;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getSourceIP() {
        return sourceIP;
    }

    public void setSourceIP(String sourceIP) {
        this.sourceIP = sourceIP;
    }

    public String getRequestString() {
        return requestString;
    }

    public void setRequestString(String requestString) {
        this.requestString = requestString;
    }

    public HashMap getReqData() {
        return reqData;
    }

    public void setReqData(HashMap reqData) {
        this.reqData = reqData;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }
}
