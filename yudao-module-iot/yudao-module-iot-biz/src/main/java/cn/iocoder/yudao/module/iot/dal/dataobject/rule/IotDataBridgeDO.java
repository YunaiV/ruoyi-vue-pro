package cn.iocoder.yudao.module.iot.dal.dataobject.rule;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.iot.controller.admin.rule.vo.databridge.config.IotDataBridgeAbstractConfig;
import cn.iocoder.yudao.module.iot.enums.rule.IotDataBridgeDirectionEnum;
import cn.iocoder.yudao.module.iot.enums.rule.IotDataBridgeTypeEnum;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.*;

/**
 * IoT 数据桥梁 DO
 *
 * @author 芋道源码
 */
@TableName(value = "iot_data_bridge", autoResultMap = true)
@KeySequence("iot_data_bridge_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IotDataBridgeDO extends BaseDO {

    /**
     * 桥梁编号
     */
    @TableId
    private Long id;
    /**
     * 桥梁名称
     */
    private String name;
    /**
     * 桥梁描述
     */
    private String description;
    /**
     * 桥梁状态
     *
     * 枚举 {@link CommonStatusEnum}
     */
    private Integer status;
    /**
     * 桥梁方向
     *
     * 枚举 {@link IotDataBridgeDirectionEnum}
     */
    private Integer direction;

    /**
     * 桥梁类型
     *
     * 枚举 {@link IotDataBridgeTypeEnum}
     */
    private Integer type;

    /**
     * 桥梁配置
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private IotDataBridgeAbstractConfig config;

}
