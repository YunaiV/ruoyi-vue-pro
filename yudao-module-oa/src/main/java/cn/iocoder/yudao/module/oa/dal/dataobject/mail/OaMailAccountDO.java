package cn.iocoder.yudao.module.oa.dal.dataobject.mail;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * OA 企业邮箱账号 DO
 *
 * 使用 {@link #getCreator()} 作为账号归属人，关联 {@link AdminUserRespDTO#getId()}。
 * 仅保存账号配置，不保存远端邮件、附件及同步状态。
 *
 * @author 芋道源码
 */
@TableName("oa_mail_account")
@KeySequence("oa_mail_account_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class OaMailAccountDO extends BaseDO {

    /**
     * 账号编号
     */
    @TableId
    private Long id;
    /**
     * 服务配置编号
     *
     * 关联 {@link OaMailProviderDO#getId()}
     */
    private Long providerId;
    /**
     * 邮箱地址
     */
    private String mail;
    /**
     * 登录用户名
     */
    private String username;
    /**
     * 密码或授权码
     */
    private String password;
    /**
     * 是否默认发件账号
     */
    private Boolean defaultStatus;
    /**
     * 状态
     *
     * 枚举 {@link CommonStatusEnum}
     */
    private Integer status;

}
