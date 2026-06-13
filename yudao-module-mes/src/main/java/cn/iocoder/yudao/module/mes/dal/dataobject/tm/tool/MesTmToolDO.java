package cn.iocoder.yudao.module.mes.dal.dataobject.tm.tool;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.mes.enums.tm.MesTmToolStatusEnum;
import cn.iocoder.yudao.module.mes.enums.tm.MesTmMaintenTypeEnum;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;
import cn.iocoder.yudao.module.mes.enums.DictTypeConstants;

/**
 * MES 工具台账 DO
 *
 * @author 芋道源码
 */
@TableName("mes_tm_tool")
@KeySequence("mes_tm_tool_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MesTmToolDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 工具编码
     */
    private String code;
    /**
     * 工具名称
     */
    private String name;
    /**
     * 品牌
     */
    private String brand;
    /**
     * 型号规格
     */
    private String specification;
    /**
     * 工具类型编号
     *
     * 关联 {@link MesTmToolTypeDO#getId()}
     */
    private Long toolTypeId;
    /**
     * 数量
     */
    private Integer quantity;
    /**
     * 可用数量
     */
    private Integer availableQuantity;
    /**
     * 保养维护类型
     *
     * 字典 {@link DictTypeConstants#MES_TM_MAINTEN_TYPE}
     * 枚举 {@link MesTmMaintenTypeEnum}
     */
    private Integer maintenType;
    /**
     * 下次保养周期（次数）
     */
    private Integer nextMaintenPeriod;
    /**
     * 下次保养日期
     */
    private LocalDateTime nextMaintenDate;
    /**
     * 状态
     *
     * 字典 {@link DictTypeConstants#MES_TM_TOOL_STATUS}
     * 枚举 {@link MesTmToolStatusEnum}
     */
    private Integer status;
    /**
     * 备注
     */
    private String remark;

}
