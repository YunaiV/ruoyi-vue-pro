package cn.iocoder.yudao.module.iot.controller.admin.device.vo.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class IotTimeDataRespVO {

    /**
     * 时间
     */
    private long time;

    /**
     * 数据值
     */
    private Object data;

}
