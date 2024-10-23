package cn.iocoder.yudao.module.iot.domain;

import lombok.Data;


@Data
public class TagsSelectDao {

    //    @NotBlank(message = "invalid operation: dataBaseName can not be empty")
    private String dataBaseName;

    //    @NotBlank(message = "invalid operation: stableName can not be empty")
    private String stableName;

    //    @NotBlank(message = "invalid operation: tagsName can not be empty")
    private String tagsName;

    //    @NotNull(message = "invalid operation: startTime can not be null")
    private Long startTime;

    //    @NotNull(message = "invalid operation: endTime can not be null")
    private Long endTime;


}
