package cn.iocoder.yudao.module.system.dal.dataobject.auth;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * OAuth2 客户端 DO
 *
 * 如下字段，考虑到使用相对不是很高频，主要是一些开关，暂时不支持：
 * authorized_grant_types、authorities、additional_information、autoapprove、resource_ids、scope
 *
 * @author 芋道源码
 */
@TableName("system_oauth2_application")
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class OAuth2ClientDO extends BaseDO {

    /**
     * 客户端编号
     *
     * 由于 SQL Server 在存储 String 主键有点问题，所以暂时使用 Long 类型
     */
    @TableId
    private Long id;
    /**
     * 客户端密钥
     */
    private String secret;
    /**
     * 应用名
     */
    private String name;
    /**
     * 应用图标
     */
    private String logo;
    /**
     * 应用描述
     */
    private String description;
    /**
     * 状态
     *
     * 枚举 {@link CommonStatusEnum}
     */
    private Integer status;
    /**
     * 访问令牌的有效期
     */
    private Integer accessTokenValiditySeconds;
    /**
     * 刷新令牌的有效期
     */
    private Integer refreshTokenValiditySeconds;
    /**
     * 可重定向的 URI 地址
     */
    private List<String> redirectUris;

}
