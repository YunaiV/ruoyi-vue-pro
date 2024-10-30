package cn.iocoder.yudao.module.system.controller.admin.dept.vo.dept;

import cn.iocoder.yudao.module.system.dal.dataobject.dept.DeptDO;
import lombok.Data;

import java.util.List;

/**
 * @className: DeptTreeRespVO
 * @author: Wqh
 * @date: 2024/10/30 15:21
 * @Version: 1.0
 * @description:
 */
@Data
public class DeptTreeRespVO {
    /**
     * 部门编号
     */
    private Long id;
    /**
     * 部门名称
     */
    private String name;
    /**
     * 子部门
     */
    private List<DeptTreeRespVO> children;
}
