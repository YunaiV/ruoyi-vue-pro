package cn.iocoder.yudao.module.system.controller.admin.user.vo.user;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@ApiModel("管理后台 - 用户导入 Response VO")
@Data
@Builder
public class UserImportRespVO {

    @ApiModelProperty(value = "创建成功的用户名数组", required = true)
    private List<String> createUsernames;

    @ApiModelProperty(value = "更新成功的用户名数组", required = true)
    private List<String> updateUsernames;

    @ApiModelProperty(value = "导入失败的用户集合", required = true, notes = "key 为用户名，value 为失败原因")
    private Map<String, String> failureUsernames;

}
