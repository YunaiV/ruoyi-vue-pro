/*
 *Copyright © 2018 anji-plus
 *安吉加加信息技术有限公司
 *http://www.anji-plus.com
 *All rights reserved.
 */
package com.anji.captcha.model.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.text.MessageFormat;

/**
 * 返回应答码
 *
 * @author
 */
@AllArgsConstructor
@Getter
public enum RepCodeEnum {

    /**
     * 0001 - 0099 网关应答码
     */
    SUCCESS("0000", "成功"),
    ERROR("0001", "操作失败"),
    EXCEPTION("9999", "服务器内部异常"),

    BLANK_ERROR("0011", "{0}不能为空"),
    NULL_ERROR("0011", "{0}不能为空"),
    NOT_NULL_ERROR("0012", "{0}必须为空"),
    NOT_EXIST_ERROR("0013", "{0}数据库中不存在"),
    EXIST_ERROR("0014", "{0}数据库中已存在"),
    PARAM_TYPE_ERROR("0015", "{0}类型错误"),
    PARAM_FORMAT_ERROR("0016", "{0}格式错误"),

    API_CAPTCHA_INVALID("6110", "验证码已失效，请重新获取"),
    API_CAPTCHA_COORDINATE_ERROR("6111", "验证失败"),
    API_CAPTCHA_ERROR("6112", "获取验证码失败,请联系管理员"),
    API_CAPTCHA_BASEMAP_NULL("6113", "底图未初始化成功，请检查路径"),

    API_REQ_LIMIT_GET_ERROR("6201", "get接口请求次数超限，请稍后再试!"),
    API_REQ_INVALID("6206", "无效请求，请重新获取验证码"),
    API_REQ_LOCK_GET_ERROR("6202", "接口验证失败数过多，请稍后再试"),
    API_REQ_LIMIT_CHECK_ERROR("6204", "check接口请求次数超限，请稍后再试!"),
    API_REQ_LIMIT_VERIFY_ERROR("6205", "verify请求次数超限!");
    private final String code;
    private final String desc;


    /**
     * 将入参fieldNames与this.desc组合成错误信息
     * {fieldName}不能为空
     *
     * @param fieldNames
     * @return
     */
    public ResponseModel parseError(Object... fieldNames) {
        ResponseModel errorMessage = new ResponseModel();
        String newDesc = MessageFormat.format(this.desc, fieldNames);

        errorMessage.setRepCode(this.code);
        errorMessage.setRepMsg(newDesc);
        return errorMessage;
    }

}
