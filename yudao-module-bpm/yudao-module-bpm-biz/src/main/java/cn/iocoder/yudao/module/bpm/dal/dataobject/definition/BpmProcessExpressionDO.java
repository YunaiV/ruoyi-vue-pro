package cn.iocoder.yudao.module.bpm.dal.dataobject.definition;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * BPM 流程表达式 DO
 *
 * @author 芋道源码
 */
@TableName("bpm_process_expression")
@KeySequence("bpm_process_expression_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BpmProcessExpressionDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 表达式名字
     */
    private String name;
    /**
     * 表达式状态
     *
     * 枚举 {@link TODO common_status 对应的类}
     */
    private Integer status;
    /**
     * 表达式
     */
    private String expression;

}