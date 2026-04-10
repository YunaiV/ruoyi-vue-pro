package cn.iocoder.yudao.module.iot.dal.dataobject.rule;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.rule.config.IotAbstractDataSinkConfig;
import cn.iocoder.yudao.module.iot.enums.rule.IotDataSinkTypeEnum;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * IoT 数据流转目的 DO
 *
 * @author 芋道源码
 */
@TableName(value = "iot_data_sink", autoResultMap = true)
@KeySequence("iot_data_bridge_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IotDataSinkDO extends BaseDO {

    /**
     * 数据流转目的编号
     */
    @TableId
    private Long id;
    /**
     * 数据流转目的名称
     */
    private String name;
    /**
     * 数据流转目的描述
     */
    private String description;
    /**
     * 数据流转目的状态
     *
     *  枚举 {@link CommonStatusEnum}
     */
    private Integer status;

    /**
     * 数据流转目的类型
     *
     * 枚举 {@link IotDataSinkTypeEnum}
     */
    private Integer type;
    /**
     * 数据流转目的配置
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private IotAbstractDataSinkConfig config;

}
