package cn.iocoder.yudao.adminserver.modules.system.dal.dataobject.notice;

import cn.iocoder.yudao.adminserver.modules.system.enums.notice.SysNoticeTypeEnum;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 通知公告表
 *
 * @author ruoyi
 */
@TableName("sys_notice")
@Data
@EqualsAndHashCode(callSuper = true)
public class SysNoticeDO extends TenantBaseDO {

    /**
     * 公告ID
     */
    private Long id;
    /**
     * 公告标题
     */
    private String title;
    /**
     * 公告类型
     *
     * 枚举 {@link SysNoticeTypeEnum}
     */
    @TableField("notice_type")
    private Integer type;
    /**
     * 公告内容
     */
    private String content;
    /**
     * 公告状态
     *
     * 枚举 {@link CommonStatusEnum}
     */
    private Integer status;

}
