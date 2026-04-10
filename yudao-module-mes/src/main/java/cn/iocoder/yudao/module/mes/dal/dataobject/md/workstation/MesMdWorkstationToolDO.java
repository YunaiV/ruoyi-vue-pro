package cn.iocoder.yudao.module.mes.dal.dataobject.md.workstation;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * MES 工装夹具资源 DO
 *
 * @author 芋道源码
 */
@TableName("mes_md_workstation_tool")
@KeySequence("mes_md_workstation_tool_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MesMdWorkstationToolDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 工作站编号
     *
     * 关联 {@link MesMdWorkstationDO#getId()}
     */
    private Long workstationId;
    /**
     * 工具类型编号
     *
     * TODO @芋艿：等 tm 模块，关联工具类型表
     */
    private Long toolTypeId;
    /**
     * 数量
     */
    private Integer quantity;
    /**
     * 备注
     */
    private String remark;

}
