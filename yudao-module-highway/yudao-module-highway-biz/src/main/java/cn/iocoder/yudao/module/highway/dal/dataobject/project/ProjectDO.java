package cn.iocoder.yudao.module.highway.dal.dataobject.project;

import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 项目管理 DO
 *
 * @author 研值担当
 */
@TableName("highway_project")
@KeySequence("highway_project_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectDO extends BaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;
    /**
     * 项目编号
     */
    private String code;
    /**
     * 项目名称
     */
    private String pname;
    /**
     * 项目描述
     */
    private String description;

}