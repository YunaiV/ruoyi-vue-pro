package cn.iocoder.yudao.module.mes.dal.dataobject.pro.task;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.client.MesMdClientDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.item.MesMdItemDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.workstation.MesMdWorkstationDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.pro.process.MesProProcessDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.pro.route.MesProRouteDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.pro.workorder.MesProWorkOrderDO;
import cn.iocoder.yudao.module.mes.enums.pro.MesProTaskStatusEnum;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import cn.iocoder.yudao.module.mes.enums.DictTypeConstants;

/**
 * MES 生产任务 DO
 *
 * @author 芋道源码
 */
@TableName("mes_pro_task")
@KeySequence("mes_pro_task_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MesProTaskDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 任务编码
     */
    private String code;
    /**
     * 任务名称
     */
    private String name;
    /**
     * 生产工单编号
     *
     * 关联 {@link MesProWorkOrderDO#getId()}
     */
    private Long workOrderId;
    /**
     * 工作站编号
     *
     * 关联 {@link MesMdWorkstationDO#getId()}
     */
    private Long workstationId;
    /**
     * 工艺路线编号
     *
     * 关联 {@link MesProRouteDO#getId()}
     */
    private Long routeId;
    /**
     * 工序编号
     *
     * 关联 {@link MesProProcessDO#getId()}
     */
    private Long processId;
    /**
     * 产品物料编号
     *
     * 关联 {@link MesMdItemDO#getId()}
     */
    private Long itemId;

    /**
     * 排产数量
     */
    private BigDecimal quantity;
    /**
     * 已生产数量
     */
    private BigDecimal producedQuantity;
    /**
     * 合格品数量
     */
    private BigDecimal qualifyQuantity;
    /**
     * 不良品数量
     */
    private BigDecimal unqualifyQuantity;
    /**
     * 调整数量
     */
    private BigDecimal changedQuantity;

    /**
     * 客户编号
     *
     * 关联 {@link MesMdClientDO#getId()}
     */
    private Long clientId;
    /**
     * 开始生产时间
     */
    private LocalDateTime startTime;
    /**
     * 生产时长（工作日，1=8小时）
     */
    private Integer duration;
    /**
     * 结束生产时间
     */
    private LocalDateTime endTime;
    /**
     * 甘特图显示颜色
     */
    private String colorCode;
    /**
     * 完成日期
     */
    private LocalDateTime finishDate;
    /**
     * 取消日期
     */
    private LocalDateTime cancelDate;

    /**
     * 任务状态
     *
     * 字典 {@link DictTypeConstants#MES_PRO_TASK_STATUS}
     * 枚举 {@link MesProTaskStatusEnum}
     */
    private Integer status;
    /**
     * 备注
     */
    private String remark;

}
