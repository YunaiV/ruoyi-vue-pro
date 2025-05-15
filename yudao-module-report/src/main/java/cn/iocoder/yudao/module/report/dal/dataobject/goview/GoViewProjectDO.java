package cn.iocoder.yudao.module.report.dal.dataobject.goview;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * GoView 项目表
 *
 * 每个大屏图标，对应一个项目
 *
 * @author 芋道源码
 */
@TableName(value = "report_go_view_project", autoResultMap = true) // 由于 SQL Server 的 system_user 是关键字，所以使用 system_users
@KeySequence("report_go_view_project_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoViewProjectDO extends BaseDO {

    /**
     * 编号，数据库自增
     */
    @TableId
    private Long id;
    /**
     * 项目名称
     */
    private String name;
    /**
     * 预览图片 URL
     */
    private String picUrl;
    /**
     * 报表内容
     *
     * JSON 配置，使用字符串存储
     */
    private String content;
    /**
     * 发布状态
     *
     * 0 - 已发布
     * 1 - 未发布
     *
     * 枚举 {@link cn.iocoder.yudao.framework.common.enums.CommonStatusEnum}
     */
    private Integer status;
    /**
     * 项目备注
     */
    private String remark;
}
