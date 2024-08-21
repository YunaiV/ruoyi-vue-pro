package com.somle.dingtalk.model;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Id;

import com.alibaba.fastjson2.PropertyNamingStrategy;
import com.alibaba.fastjson2.annotation.JSONType;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class DingTalkDepartment {
    private Boolean autoAddUser;
    private Boolean createDeptGroup;
    @Id
    private Long deptId;
    // private String ext;
    private String name;
    private Long parentId;
    private String sourceIdentifier;

    //custom
    private DingTalkDepartment parent;
    private Integer level;
    private List<DingTalkDepartment> child = new ArrayList();

    // public List<DingTalkDepartment> getPath() {
    //     return Objects.requireNonNullElse(path, new ArrayList<>());
    // }

    // public EsbDepartment toEsbDepartmentWithParent(EsbDepartment parent) {
    //     EsbDepartment esbDepartment = EsbDepartment.builder()
    //         .id(String.valueOf(deptId))
    //         .nameZh(name)
    //         .parent(parent)
    //         .level(level)
    //         .build();
    //     esbDepartment.setChilds(child.stream().map(n->n.toEsbDepartmentWithParent(esbDepartment)).toList());
    //     return esbDepartment;
    // }

    // public EsbDepartment toEsbDepartment() {
    //     return toEsbDepartmentWithParent(null);
    // }
}
