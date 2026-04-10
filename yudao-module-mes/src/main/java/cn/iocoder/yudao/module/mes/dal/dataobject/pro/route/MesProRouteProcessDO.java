package cn.iocoder.yudao.module.mes.dal.dataobject.pro.route;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.pro.process.MesProProcessDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * MES 工艺路线工序 DO
 *
 * @author 芋道源码
 */
@TableName("mes_pro_route_process")
@KeySequence("mes_pro_route_process_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MesProRouteProcessDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
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
     * 序号
     */
    private Integer sort;
    /**
     * 下一道工序编号
     *
     * 关联 {@link MesProProcessDO#getId()}
     */
    private Long nextProcessId;
    /**
     * 与下一道工序关系
     *
     * 字典 {@link cn.iocoder.yudao.module.mes.enums.DictTypeConstants#MES_PRO_LINK_TYPE}
     * 枚举 {@link cn.iocoder.yudao.module.mes.enums.pro.MesProLinkTypeEnum}
     */
    private Integer linkType;
    /**
     * 准备时间（分钟）
     */
    private Integer prepareTime;
    /**
     * 等待时间（分钟）
     */
    private Integer waitTime;
    /**
     * 甘特图显示颜色
     */
    private String colorCode;
    /**
     * 是否关键工序
     */
    private Boolean keyFlag;
    /**
     * 是否质检工序
     */
    private Boolean checkFlag;
    /**
     * 备注
     */
    private String remark;

}
