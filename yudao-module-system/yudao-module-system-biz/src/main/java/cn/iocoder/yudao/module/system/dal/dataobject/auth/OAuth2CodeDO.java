package cn.iocoder.yudao.module.system.dal.dataobject.auth;

import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * OAuth2 授权码 DO
 *
 * @author 芋道源码
 */
@TableName("system_oauth2_code")
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class OAuth2CodeDO extends BaseDO {

    /**
     * 编号，数据库递增
     */
    private Long id;
    /**
     * 授权码
     */
    private String code;
    /**
     * 用户编号
     */
    private Long userId;
    /**
     * 用户类型
     *
     * 枚举 {@link UserTypeEnum}
     */
    private Integer userType;
    /**
     * 应用编号
     *
     * 关联 {@link OAuth2ApplicationDO#getId()}
     */
    private Long applicationId;
    /**
     * 刷新令牌
     *
     * 关联 {@link OAuth2RefreshTokenDO#getRefreshToken()}
     */
    private String refreshToken;
    /**
     * 过期时间
     */
    private Date expiresTime;
    /**
     * 创建 IP
     */
    private String createIp;

}
