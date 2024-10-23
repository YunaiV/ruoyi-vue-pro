package cn.iocoder.yudao.module.iot.domain;

import lombok.Data;

/**
 * @ClassDescription: 查询可视化所需入参对象
 * @ClassName: SelectDto
 * @Author: andyz
 * @Date: 2022-07-29 14:12:26
 * @Version 1.0
 */
@Data
public class DeviceDataVo {


    private String deviceId;

    private Long lastTime;
}
