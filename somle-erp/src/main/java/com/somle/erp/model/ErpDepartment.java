package com.somle.erp.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Transient;

import com.alibaba.fastjson2.PropertyNamingStrategy;
import com.alibaba.fastjson2.annotation.JSONType;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JSONType(naming = PropertyNamingStrategy.SnakeCase)
public class ErpDepartment {
    @Id
    private Long id;
    private String nameZh;
    private Integer level;
    private Long parentId;

    @Transient
    private ErpDepartment parent;

    @Transient
    private List<ErpDepartment> children;

    public ErpDepartment getParent(Integer parentLevel) {
        if (parentLevel >= level) {
            throw new RuntimeException("parent level " + parentLevel + "should be lower than current level " + level);
        }
        return parent.level <= parentLevel ? parent : parent.getParent(parentLevel);
    }
}
