package cn.iocoder.yudao.module.weapp.dal.dataobject.appslist;

import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 小程序清单 DO
 *
 * @author 芋道源码
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
     * 主键ID;主键iD
     */
    @TableId
    private Integer id;
    /**
     * 小程序名称;小程序名称
     */
    private String weappName;
    /**
     * 小程序OPENID;小程序OPENID
     */
    private String weappOpenid;
    /**
     * 分类ID;所属分类ID
     */
    private String classId;
    /**
     * 小程序简介;小程序说明
     */
    private String description;
    /**
     * 小程序图标;小程序图标
     */
    private String logoImg;
    /**
     * 状态;状态
     */
    private String status;
    /**
     * 更新人;更新人
     */
    private String updatedBy;
    /**
     * 更新时间;数据更新时间
     */
    private LocalDateTime updatedTime;

}