package cn.iocoder.yudao.module.crm.dal.dataobject.business;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.framework.mybatis.core.type.LongListTypeHandler;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.util.List;

/**
 * 商机状态类型 DO
 *
 * @author ljlleo
 */
@TableName(value = "crm_business_status_type", autoResultMap = true)
@KeySequence("crm_business_status_type_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CrmBusinessStatusTypeDO extends BaseDO {

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
    @TableField(typeHandler = LongListTypeHandler.class)
    private List<Long> deptIds;
    /**
     * 开启状态
     *
     * TODO 改成 Integer，关联 CommonStatus
     */
    private Boolean status;

}
