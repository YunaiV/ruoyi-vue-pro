package cn.iocoder.yudao.module.iot.dal.dataobject.thinkmodelfunction;

import lombok.*;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * IoT 产品物模型 DO
 *
 * @author 芋道源码
 */
@TableName("iot_think_model_function")
@KeySequence("iot_think_model_function_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IotThinkModelFunctionDO extends BaseDO {

    /**
     * 产品ID
     */
    @TableId
    private Long id;
    /**
     * 产品标识
     */
    private String productKey;
    /**
     * 属性列表
     */
    private String properties;
    /**
     * 服务列表
     */
    private String services;
    /**
     * 事件列表
     */
    private String events;

}