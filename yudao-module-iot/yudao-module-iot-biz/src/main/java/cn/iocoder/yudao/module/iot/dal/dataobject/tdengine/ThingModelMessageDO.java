package cn.iocoder.yudao.module.iot.dal.dataobject.tdengine;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// TODO @芋艿：纠结下字段
@Deprecated
/**
 * TD 物模型消息日志的数据库
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ThingModelMessageDO {



    /**
     * 消息 ID
     */
    private String id;

    /**
     * 系统扩展参数
     * 
     * 例如：设备状态、系统时间、固件版本等系统级信息
     */
    private Object system;

    /**
     * 请求方法
     *
     * 例如：thing.event.property.post
     */
    private String method;

    /**
     * 请求参数
     */
    private Object params;

    /**
     * 属性上报时间戳
     */
    private Long time;

    /**
     * 设备信息
     */
    private String productKey;


    /**
     * 设备 key
     */
    private String deviceKey;

}
