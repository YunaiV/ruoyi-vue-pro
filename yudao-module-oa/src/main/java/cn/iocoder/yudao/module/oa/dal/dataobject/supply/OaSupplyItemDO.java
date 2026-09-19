package cn.iocoder.yudao.module.oa.dal.dataobject.supply;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.oa.enums.supply.*;
import cn.iocoder.yudao.module.oa.enums.DictTypeConstants;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.module.system.api.dept.dto.DeptRespDTO;

import java.math.BigDecimal;

/**
 * OA 办公用品 DO
 *
 * @author 芋道源码
 */
@TableName(value = "oa_supply_item", autoResultMap = true)
@KeySequence("oa_supply_item_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class OaSupplyItemDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 所属部门编号
     *
     * 关联 {@link DeptRespDTO#getId()}
     */
    private Long deptId;
    /**
     * 物品名称
     */
    private String name;
    /**
     * 物品编码
     */
    private String no;
    /**
     * 类别
     *
     * 枚举 {@link OaSupplyCategoryEnum}
     * 字典 {@link DictTypeConstants#SUPPLY_CATEGORY}
     */
    private Integer category;
    /**
     * 管理类型
     *
     * 枚举 {@link OaSupplyManageTypeEnum}
     */
    private Integer manageType;
    /**
     * 规格型号
     */
    private String model;
    /**
     * 计量单位
     */
    private String unit;
    /**
     * 参考单价
     */
    private BigDecimal referencePrice;
    /**
     * 库存数量
     */
    private Integer stockQuantity;
    /**
     * 最低库存预警
     */
    private Integer minStockQuantity;
    /**
     * 物品图片 URL
     */
    private String picUrl;
    /**
     * 状态
     *
     * 枚举 {@link CommonStatusEnum}
     */
    private Integer status;
    /**
     * 显示顺序
     */
    private Integer sort;
    /**
     * 备注
     */
    private String remark;

}
