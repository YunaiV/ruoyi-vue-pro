package cn.iocoder.yudao.module.weapp.dal.dataobject.appslist;

import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 小程序清单 DO
 *
 * @author jingjianqian
 */
@TableName("weapp_apps_list")
@KeySequence("weapp_apps_list_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppsListDO extends BaseDO {

    /**
     * 小程序名称
     */
    private String weappName;
    /**
     * 小程序OPENID
     */
    private String weappOpenid;
    /**
     * 分类ID
     */
    private String classId;
    /**
     * 小程序图标
     */
    private String logoImg;
    /**
     * 发布状态
     *
     * 枚举 {@link TODO weapp_publish_status 对应的类}
     */
    private Integer status;
    /**
     * 主键ID
     */
    @TableId
    private Long id;
    /**
     * 小程序简介
     */
    private String description;

}