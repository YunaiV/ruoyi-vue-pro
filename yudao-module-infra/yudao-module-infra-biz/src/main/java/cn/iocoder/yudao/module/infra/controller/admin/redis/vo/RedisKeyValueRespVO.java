package cn.iocoder.yudao.module.infra.controller.admin.redis.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

@ApiModel("管理后台 - 单个 Redis Key Value Response VO")
@Data
@AllArgsConstructor
public class RedisKeyValueRespVO {

    @ApiModelProperty(value = "c5f6990767804a928f4bb96ca249febf", required = true, example = "String")
    private String key;

    @ApiModelProperty(required = true, example = "String")
    private String value;

}
