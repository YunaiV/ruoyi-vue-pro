package cn.iocoder.yudao.module.crm.dal.dataobject.businessstatustype;

import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 商机状态类型 DO
 *
 * @author ljlleo
 */
@TableName("crm_business_status_type")
@KeySequence("crm_business_status_type_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CrmBusinessStatusTypeDO {

    /**
     * 主键
     */
    @TableId
    private Long id;
    /**
     * 状态类型名
     */
    private String name;
    /**
     * 使用的部门编号
     */
    private String deptIds;
    /**
     * 开启状态
     */
    private Boolean status;

}
