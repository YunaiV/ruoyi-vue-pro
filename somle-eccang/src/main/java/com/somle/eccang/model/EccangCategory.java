package com.somle.eccang.model;


import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class EccangCategory {
    private String actionType;
    private Integer pcId;
    private String pcName;
    private String pcNameEn;
    private Integer pcPid;
    private Integer pcLevel;
    private String pcReferenceCode;
    private String pcHsCode;
    private String pcUpdateTime;
    private String userOrganizationId;
    private String pcSortId;
    private String warehouseId;
    private String pcShortname;
}
