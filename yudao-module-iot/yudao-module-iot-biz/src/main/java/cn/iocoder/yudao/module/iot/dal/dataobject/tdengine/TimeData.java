package cn.iocoder.yudao.module.iot.dal.dataobject.tdengine;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 统计的时间数据
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TimeData {

    /**
     * 时间
     */
    private long time;

    /**
     * 数据值
     */
    private Object data;

}
