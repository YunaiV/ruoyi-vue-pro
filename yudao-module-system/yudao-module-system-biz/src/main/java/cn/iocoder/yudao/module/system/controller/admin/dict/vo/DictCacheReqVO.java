package cn.iocoder.yudao.module.system.controller.admin.dict.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 字典缓存操作 Request VO")
@Data
public class DictCacheReqVO {

    @Schema(description = "字典类型", example = "sys_user_sex")
    private String dictType;

    @Schema(description = "字典类型数组", example = "sys_user_sex,sys_notice_type")
    private String[] dictTypes;

    @Schema(description = "SQL 语句")
    private String sql;
}
