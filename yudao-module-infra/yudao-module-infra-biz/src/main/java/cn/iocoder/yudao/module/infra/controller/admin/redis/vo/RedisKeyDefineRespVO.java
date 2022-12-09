package cn.iocoder.yudao.module.infra.controller.admin.redis.vo;

import cn.iocoder.yudao.framework.redis.core.RedisKeyDefine;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.Duration;

@Schema(title = "管理后台 - Redis Key 信息 Response VO")
@Data
@Builder
@AllArgsConstructor
public class RedisKeyDefineRespVO {

    @Schema(title = "Key 模板", required = true, example = "login_user:%s")
    private String keyTemplate;

    @Schema(title = "Key 类型的枚举", required = true, example = "String")
    private RedisKeyDefine.KeyTypeEnum keyType;

    @Schema(title = "Value 类型", required = true, example = "java.lang.String")
    private Class<?> valueType;

    @Schema(title = "超时类型", required = true, example = "1")
    private RedisKeyDefine.TimeoutTypeEnum timeoutType;

    @Schema(title = "过期时间，单位：毫秒", required = true, example = "1024")
    private Duration timeout;

    @Schema(title = "备注", required = true, example = "啦啦啦啦~")
    private String memo;

}
