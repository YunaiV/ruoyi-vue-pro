package com.somle.erp.model;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Transient;



@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
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
