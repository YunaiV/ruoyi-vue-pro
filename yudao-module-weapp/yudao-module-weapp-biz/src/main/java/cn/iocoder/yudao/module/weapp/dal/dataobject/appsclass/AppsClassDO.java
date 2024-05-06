package cn.iocoder.yudao.module.weapp.dal.dataobject.appsclass;

import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 小程序分类 DO
 *
 * @author 芋道源码
 */
@TableName("weapp_apps_class")
@KeySequence("weapp_apps_class_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppsClassDO extends BaseDO {

    /**
     * 主键ID;主键ID
     */
    @TableId
    private Integer id;
    /**
     * 分类名;分类名称
     */
    private String className;
    /**
     * 状态;是否启用
     */
    private String status;

}