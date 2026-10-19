package cn.iocoder.yudao.module.oa.dal.dataobject.mail;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.oa.enums.mail.OaMailFolderTypeEnum;
import com.baomidou.mybatisplus.annotation.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 企业邮箱文件夹 DO
 *
 * @author 芋道源码
 */
@TableName("oa_mail_folder")
@KeySequence("oa_mail_folder_seq")
@Data
@EqualsAndHashCode(callSuper = true)
public class OaMailFolderDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 邮箱账号编号
     *
     * 关联 {@link OaMailAccountDO#getId()}
     */
    private Long accountId;
    /**
     * 远端文件夹完整名称
     */
    private String name;
    /**
     * 文件夹类型
     *
     * 枚举 {@link OaMailFolderTypeEnum}
     */
    private String type;
    /**
     * 远端文件夹 UID 有效期
     */
    private Long uidValidity;
    /**
     * 最近完整同步时间
     */
    private LocalDateTime syncTime;
    /**
     * 远端文件夹是否可用
     */
    private Boolean available;

}
