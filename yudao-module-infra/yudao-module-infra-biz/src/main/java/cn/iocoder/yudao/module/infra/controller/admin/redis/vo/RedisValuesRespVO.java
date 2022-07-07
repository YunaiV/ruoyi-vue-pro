package cn.iocoder.yudao.module.infra.controller.admin.redis.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

@ApiModel("管理后台 - Redis Key Value onse VO")
@Data
@Builder
public class RedisValuesRespVO {

    @ApiModelProperty(value = "oauth2_access_token:%s", required = true, example = "String")
    private String keyTemplate;

    @ApiModelProperty(value = "c5f6990767804a928f4bb96ca249febf", required = true, example = "String")
    private String key;

    @ApiModelProperty(required = true, example = "String")
    private String value;

    public RedisValuesRespVO(String keyTemplate, String key, String value){
        this.keyTemplate = StringUtils.replace(keyTemplate, ":", "");
        this.key = StringUtils.replace(key, keyTemplate, "");
        this.value = value;
    }


}
