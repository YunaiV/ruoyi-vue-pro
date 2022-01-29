package cn.iocoder.yudao.module.system.controller.auth.vo.session;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Date;

@ApiModel(value = "用户在线 Session Response VO", description = "相比用户基本信息来说，会多部门、用户账号等信息")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SysUserSessionPageItemRespVO extends PageParam {

    @ApiModelProperty(value = "Session 编号", required = true, example = "fe50b9f6-d177-44b1-8da9-72ea34f63db7")
    private String id;

    @ApiModelProperty(value = "用户 IP", required = true, example = "127.0.0.1")
    private String userIp;

    @ApiModelProperty(value = "浏览器 UserAgent", required = true, example = "Mozilla/5.0")
    private String userAgent;

    @ApiModelProperty(value = "登录时间", required = true)
    private Date createTime;

    @ApiModelProperty(value = "用户账号", required = true, example = "yudao")
    private String username;

    @ApiModelProperty(value = "部门名称", example = "研发部")
    private String deptName;

}
