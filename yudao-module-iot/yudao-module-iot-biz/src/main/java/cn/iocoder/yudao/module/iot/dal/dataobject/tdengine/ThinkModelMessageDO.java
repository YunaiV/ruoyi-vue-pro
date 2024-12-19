package cn.iocoder.yudao.module.iot.dal.dataobject.tdengine;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.xmlbeans.impl.xb.xsdschema.Public;


/**
 * TD 物模型消息日志的数据库
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ThinkModelMessageDO {
    /**
     * 数据库名称
     */
    private String dataBaseName;

    // TODO @haohao：superTableName 和 tableName 是不是合并。因为每个 mapper 操作的时候，有且只会使用到其中一个。
    /**
     * 超级表名称
     */
    private String superTableName;

    /**
     * 表名称
     */
    private String tableName;

    /**
     * 消息ID
     */
    private String id;

    /**
     * 扩展功能的参数
     */
    private Object sys;

    /**
     * 请求方法 例如：thing.event.property.post
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
     * 设备 key
     */
    private String deviceKey;


}
