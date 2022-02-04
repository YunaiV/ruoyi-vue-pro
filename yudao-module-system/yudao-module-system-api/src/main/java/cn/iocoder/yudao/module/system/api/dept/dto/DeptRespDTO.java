package cn.iocoder.yudao.module.system.api.dept.dto;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import lombok.Data;

/**
 * 部门 Response DTO
 *
 * @author 芋道源码
 */
@Data
public class DeptRespDTO {

    /**
     * 部门编号
     */
    private Long id;
    /**
     * 部门名称
     */
    private String name;
    /**
     * 父部门编号
     */
    private Long parentId;
    /**
     * 负责人的用户编号
     */
    private Long leaderUserId;
    /**
     * 部门状态
     *
     * 枚举 {@link CommonStatusEnum}
     */
    private Integer status;

}
