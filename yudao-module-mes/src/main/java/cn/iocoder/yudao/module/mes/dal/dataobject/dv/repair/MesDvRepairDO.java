package cn.iocoder.yudao.module.mes.dal.dataobject.dv.repair;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.dv.machinery.MesDvMachineryDO;
import cn.iocoder.yudao.module.mes.enums.dv.MesDvRepairResultEnum;
import cn.iocoder.yudao.module.mes.enums.dv.MesDvRepairStatusEnum;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * MES 维修工单 DO
 *
 * @author 芋道源码
 */
@TableName("mes_dv_repair")
@KeySequence("mes_dv_repair_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MesDvRepairDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 维修工单编码
     */
    private String code;
    /**
     * 维修工单名称
     */
    private String name;
    /**
     * 设备编号
     *
     * 关联 {@link MesDvMachineryDO#getId()}
     */
    private Long machineryId;
    /**
     * 报修日期
     */
    private LocalDateTime requireDate;
    /**
     * 维修完成日期
     */
    private LocalDateTime finishDate;
    /**
     * 验收日期
     */
    private LocalDateTime confirmDate;
    /**
     * 维修结果
     *
     * 枚举 {@link MesDvRepairResultEnum}
     */
    private Integer result;
    /**
     * 维修人用户编号
     *
     * 关联 AdminUserDO#getId()
     */
    private Long acceptedUserId;
    /**
     * 验收人用户编号
     *
     * 关联 AdminUserDO#getId()
     */
    private Long confirmUserId;
    // DONE @芋艿：sourceDocType、sourceDocId、sourceDocCode 为预留字段，对齐：暂未使用
    /**
     * 来源单据类型
     */
    private Integer sourceDocType;
    /**
     * 来源单据编号
     */
    private Long sourceDocId;
    /**
     * 来源单据编码
     */
    private String sourceDocCode;
    /**
     * 状态
     *
     * 枚举 {@link MesDvRepairStatusEnum}
     */
    private Integer status;
    /**
     * 备注
     */
    private String remark;

}
