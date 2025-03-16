package cn.iocoder.yudao.module.iot.dal.dataobject.ota;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.*;

import java.util.List;

/**
 * IoT OTA 升级任务 DO
 *
 * @author 芋道源码
 */
@TableName(value = "iot_ota_upgrade_task", autoResultMap = true)
@KeySequence("iot_ota_upgrade_task_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IotOtaUpgradeTaskDO extends BaseDO {

    /**
     * 任务编号
     */
    @TableField
    private Long id;
    /**
     * 任务名称
     */
    private String name;
    /**
     * 任务描述
     */
    private String description;

    /**
     * 固件编号
     * <p>
     * 关联 {@link IotOtaFirmwareDO#getId()}
     */
    private Long firmwareId;

    /**
     * 任务状态
     * <p>
     * 关联 {@link cn.iocoder.yudao.module.iot.enums.ota.IotOtaUpgradeTaskStatusEnum}
     */
    private Integer status;

    /**
     * 升级范围
     * <p>
     * 关联 {@link cn.iocoder.yudao.module.iot.enums.ota.IotOtaUpgradeTaskScopeEnum}
     */
    private Integer scope;
    /**
     * 设备数量
     */
    private Long deviceCount;
    /**
     * 选中的设备编号数组
     * <p>
     * 关联 {@link IotDeviceDO#getId()}
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Long> deviceIds;

}
