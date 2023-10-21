package cn.iocoder.yudao.module.crm.dal.dataobject.business;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商机 DO
 *
 * @author ljlleo
 */
@TableName("crm_business")
@KeySequence("crm_business_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CrmBusinessDO extends BaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;
    /**
     * 商机名称
     */
    private String name;
    /**
     * 商机状态类型编号
     */
    private Long statusTypeId;
    /**
     * 商机状态编号
     */
    private Long statusId;
    /**
     * 下次联系时间
     */
    private LocalDateTime contactNextTime;
    /**
     * 客户编号
     */
    private Long customerId;
    /**
     * 预计成交日期
     */
    private LocalDateTime dealTime;
    /**
     * 商机金额
     */
    private BigDecimal price;
    /**
     * 整单折扣
     */
    private BigDecimal discountPercent;
    /**
     * 产品总金额
     */
    private BigDecimal productPrice;
    /**
     * 备注
     */
    private String remark;
    /**
     * 负责人的用户编号
     */
    private Long ownerUserId;
    /**
     * 只读权限的用户编号数组
     */
    private String roUserIds;
    /**
     * 读写权限的用户编号数组
     */
    private String rwUserIds;
    /**
     * 1赢单2输单3无效
     */
    private Integer endStatus;
    /**
     * 结束时的备注
     */
    private String endRemark;
    /**
     * 最后跟进时间
     */
    private LocalDateTime contactLastTime;
    /**
     * 跟进状态
     */
    private Integer followUpStatus;

}
