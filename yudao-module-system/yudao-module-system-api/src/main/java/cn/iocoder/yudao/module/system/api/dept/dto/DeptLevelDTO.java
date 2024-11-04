package cn.iocoder.yudao.module.system.api.dept.dto;

import lombok.Data;

/**
 * @className: DeptLevelDTO
 * @author: Wqh
 * @date: 2024/11/1 14:56
 * @Version: 1.0
 * @description:
 */
@Data
public class DeptLevelDTO implements Comparable<DeptLevelDTO>{
    // 部门编号
    private Long deptId;
    // 部门名称
    private String deptName;
    // 部门等级
    private Integer level;

    public DeptLevelDTO(Long deptId, String deptName,Integer level) {
        this.deptId = deptId;
        this.deptName = deptName;
        this.level = level;
    }


    @Override
    public int compareTo(DeptLevelDTO o) {
        return Integer.compare(o.level,this.level);
    }
}
