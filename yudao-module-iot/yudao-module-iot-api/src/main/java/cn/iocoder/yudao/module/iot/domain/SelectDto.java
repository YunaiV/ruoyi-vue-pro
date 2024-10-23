package cn.iocoder.yudao.module.iot.domain;

import lombok.Data;

import java.util.Set;

@Data
public class SelectDto {

    //    @NotBlank(message = "invalid operation: dataBaseName can not be empty")
    private String dataBaseName;

//    @NotBlank(message = "invalid operation: tableName can not be empty")
    private String tableName;

    //    @NotBlank(message = "invalid operation: fieldName can not be empty")
    private String fieldName;

    //    @NotNull(message = "invalid operation: startTime can not be null")
    private Long startTime;

    //    @NotNull(message = "invalid operation: endTime can not be null")
    private Long endTime;

    private String type;

    private Set<Integer> orgIds;

    private String deviceId;
}
