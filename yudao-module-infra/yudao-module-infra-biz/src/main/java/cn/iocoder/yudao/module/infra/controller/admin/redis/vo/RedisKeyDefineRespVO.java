package cn.iocoder.yudao.module.infra.controller.admin.redis.vo;

import cn.iocoder.yudao.framework.redis.core.RedisKeyDefine;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.Duration;

@ApiModel("管理后台 - Redis Key 信息 Response VO")
@Data
@Builder
@AllArgsConstructor
public class RedisKeyDefineRespVO {

    @ApiModelProperty(value = "Key 模板", required = true, example = "login_user:%s")
    private String keyTemplate;

    @ApiModelProperty(value = "Key 类型的枚举", required = true, example = "String")
    private RedisKeyDefine.KeyTypeEnum keyType;

    @ApiModelProperty(value = "Value 类型", required = true, example = "java.lang.String")
    private Class<?> valueType;

    @ApiModelProperty(value = "超时类型", required = true, example = "1")
    private RedisKeyDefine.TimeoutTypeEnum timeoutType;

    @ApiModelProperty(value = "过期时间，单位：毫秒", required = true, example = "1024")
    private Duration timeout;

    @ApiModelProperty(value = "备注", required = true, example = "啦啦啦啦~")
    private String memo;

}
