package cn.iocoder.yudao.module.infra.dal.dataobject.demo.demo03;

import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 学生课程 DO
 *
 * @author 芋道源码
 */
@TableName("infra_demo03_course")
@KeySequence("infra_demo03_course_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Demo03CourseDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 学生编号
     */
    private Long studentId;
    /**
     * 名字
     */
    private String name;
    /**
     * 分数
     */
    private Integer score;

}