package cn.iocoder.yudao.module.mes.dal.dataobject.tm.tool;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.mes.enums.tm.MesTmMaintenTypeEnum;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;
import cn.iocoder.yudao.module.mes.enums.DictTypeConstants;

/**
 * MES 工具类型 DO
 *
 * @author 芋道源码
 */
@TableName("mes_tm_tool_type")
@KeySequence("mes_tm_tool_type_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MesTmToolTypeDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 类型编码
     */
    private String code;
    /**
     * 类型名称
     */
    private String name;
    /**
     * 是否编码管理
     */
    private Boolean codeFlag;
    /**
     * 保养维护类型
     *
     * 字典 {@link DictTypeConstants#MES_TM_MAINTEN_TYPE}
     * 枚举 {@link MesTmMaintenTypeEnum}
     */
    private Integer maintenType;
    /**
     * 保养周期
     */
    private Integer maintenPeriod;
    /**
     * 备注
     */
    private String remark;

}
