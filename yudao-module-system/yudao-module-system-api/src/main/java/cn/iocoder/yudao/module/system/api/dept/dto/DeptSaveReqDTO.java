package cn.iocoder.yudao.module.system.api.dept.dto;

import lombok.Data;

/**
 * @className: DeptReqDTO
 * @author: Wqh
 * @date: 2024/11/4 14:41
 * @Version: 1.0
 * @description:
 */
@Data
public class DeptSaveReqDTO {
    /*
    * 部门编号
    **/
    private Long id;

    /*
    * 部门名称
    **/
    private String name;

    /*
     * 父部门id
     **/
    private Long parentId;

    /*
     * 显示顺序
     **/
    private Integer sort;

    /*
     * 负责人的用户编号
     **/
    private Long leaderUserId;

    /*
     * 联系电话
     **/
    private String phone;

    /*
     * 邮箱
     **/
    private String email;

    /*
     * 状态
     **/
    private Integer status;
}
