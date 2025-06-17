package cn.iocoder.yudao.module.system.controller.admin.user.vo.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 部门的数据权限 Response VO
 *
 * @author 芋道源码
 */
@Data
public class UserDataPermissionRespVO {

    /**
     * 是否可查看全部数据
     */
    @Schema(description = "是否可查看全部数据", example = "false")
    private Boolean all;
    /**
     * 是否可查看自己的数据
     */
    @Schema(description = "是否可查看自己的数据", example = "false")
    private Boolean self;
    /**
     * 可查看的部门编号数组
     */
    @Schema(description = "可查看的部门编号数组", example = "")
    private List<DeptVO> deptList;

    public UserDataPermissionRespVO() {
        this.all = false;
        this.self = false;
        this.deptList = new ArrayList<>();
    }

    @Data
    public static class DeptVO {
        @Schema(description = "部门ID", example = "")
        private Long id;
        @Schema(description = "部门名称", example = "")
        private String name;
    }
}
