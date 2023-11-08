package cn.iocoder.yudao.module.infra.dal.dataobject.demo02;

import lombok.*;
import java.util.*;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 学生 DO
 *
 * @author 芋道源码
 */
@TableName("infra_demo_student")
@KeySequence("infra_demo_student_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InfraDemoStudentDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;

}