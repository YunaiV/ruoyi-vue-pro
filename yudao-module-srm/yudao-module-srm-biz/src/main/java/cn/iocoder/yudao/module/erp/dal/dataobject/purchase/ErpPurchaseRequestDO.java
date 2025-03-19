package cn.iocoder.yudao.module.erp.dal.dataobject.purchase;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * ERP采购申请单 DO
 *
 * @author 索迈管理员
 */
@TableName("erp_purchase_request")
@KeySequence("erp_purchase_request_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErpPurchaseRequestDO extends BaseDO {

    /**
     * id
     */
    @TableId
    private Long id;
    /**
     * 单据编号
     */
    private String no;
    /**
     * 申请人
     */
    private String applicant;
    /**
     * 申请部门
     */
    private String applicationDept;
    /**
     * 单据日期
     */
    private LocalDateTime requestTime;
    /**
     * 审核状态(0:待审核，1:审核通过，2:审核未通过)
     */
    private Integer status;
    /**
     * 关闭状态（0已关闭，1已开启）
     */
    private Integer offStatus;
    /**
     * 订购状态（0部分订购，1全部订购）
     */
    private Integer orderStatus;
    /**
     * 审核者
     */
    private String auditor;
    /**
     * 审核时间
     */
    private LocalDateTime auditTime;

}