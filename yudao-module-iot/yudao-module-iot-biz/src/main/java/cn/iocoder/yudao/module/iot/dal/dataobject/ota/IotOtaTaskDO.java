package cn.iocoder.yudao.module.iot.dal.dataobject.ota;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.iot.enums.ota.IotOtaTaskDeviceScopeEnum;
import cn.iocoder.yudao.module.iot.enums.ota.IotOtaTaskStatusEnum;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * IoT OTA 升级任务 DO
 *
 * @author 芋道源码
 */
@TableName(value = "iot_ota_task", autoResultMap = true)
@KeySequence("iot_ota_task_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IotOtaTaskDO extends BaseDO {

    /**
     * 任务编号
     */
    @TableId
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
     * 关联 {@link IotOtaTaskStatusEnum}
     */
    private Integer status;

    /**
     * 设备升级范围
     * <p>
     * 关联 {@link IotOtaTaskDeviceScopeEnum}
     */
    private Integer deviceScope;
    /**
     * 设备总数数量
     */
    private Integer deviceTotalCount;
    /**
     * 设备成功数量
     */
    private Integer deviceSuccessCount;

}
