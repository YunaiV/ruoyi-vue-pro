package cn.iocoder.yudao.module.iot.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 统计的时间数据
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageCountVo {

    /**
     * 时间
     */
    private String time;

    /**
     * 数据值
     */
    private Object data;

}
