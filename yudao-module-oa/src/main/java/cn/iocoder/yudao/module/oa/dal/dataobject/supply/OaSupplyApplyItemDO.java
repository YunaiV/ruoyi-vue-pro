package cn.iocoder.yudao.module.oa.dal.dataobject.supply;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.oa.enums.supply.*;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * OA 用品申请明细 DO
 *
 * @author 芋道源码
 */
@TableName(value = "oa_supply_apply_item", autoResultMap = true)
@KeySequence("oa_supply_apply_item_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class OaSupplyApplyItemDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 领用申请编号
     *
     * 关联 {@link OaSupplyApplyDO#getId()}
     */
    private Long applyId;
    /**
     * 办公用品编号
     *
     * 关联 {@link OaSupplyItemDO#getId()}
     */
    private Long itemId;
    /**
     * 物品名称快照
     *
     * 冗余 {@link OaSupplyItemDO#getName()}
     */
    private String itemName;
    /**
     * 规格型号快照
     *
     * 冗余 {@link OaSupplyItemDO#getModel()}
     */
    private String model;
    /**
     * 计量单位快照
     *
     * 冗余 {@link OaSupplyItemDO#getUnit()}
     */
    private String unit;
    /**
     * 管理类型快照
     *
     * 枚举 {@link OaSupplyManageTypeEnum}
     * 冗余 {@link OaSupplyItemDO#getManageType()}
     */
    private Integer manageType;
    /**
     * 申请数量
     */
    private Integer applyQuantity;
    /**
     * 实发数量
     */
    private Integer issuedQuantity;
    /**
     * 累计归还数量
     */
    private Integer returnedQuantity;
    /**
     * 明细状态
     *
     * 枚举 {@link OaSupplyApplyItemStatusEnum}
     */
    private Integer status;
    /**
     * 发放人用户编号
     *
     * 关联 {@link AdminUserRespDTO#getId()}
     */
    private Long issueUserId;
    /**
     * 发放时间
     */
    private LocalDateTime issueTime;
    /**
     * 发放备注
     */
    private String issueRemark;
    /**
     * 归还备注
     */
    private String returnRemark;

}
