package cn.iocoder.yudao.module.crm.dal.dataobject.business;

import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 商机状态 DO
 *
 * @author ljlleo
 */
@TableName("crm_business_status")
@KeySequence("crm_business_status_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CrmBusinessStatusDO {

    /**
     * 主键
     */
    @TableId
    private Long id;
    /**
     * 状态类型编号
     *
     * 关联 {@link CrmBusinessStatusTypeDO#getId()}
     */
    private Long typeId;
    /**
     * 状态名
     */
    private String name;
    /**
     * 赢单率
     *
     * TODO 这里是不是改成 Integer 存储，百分比 * 100 ；
     */
    private String percent;
    /**
     * 排序
     */
    private Integer sort;

}
