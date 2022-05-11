package cn.iocoder.yudao.module.system.dal.dataobject.auth;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.system.enums.auth.OAuth2GrantTypeEnum;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * OAuth2 客户端 DO
 *
 * @author 芋道源码
 */
@TableName(value = "system_oauth2_client", autoResultMap = true)
@Data
@EqualsAndHashCode(callSuper = true)
public class OAuth2ClientDO extends BaseDO {

    /**
     * 编号，数据库自增
     *
     * 由于 SQL Server 在存储 String 主键有点问题，所以暂时使用 Long 类型
     */
    @TableId
    private Long id;
    /**
     * 客户端编号
     */
    private String clientId;
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
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> redirectUris;
    /**
     * 是否自动授权
     */
    private Boolean autoApprove;
    /**
     * 授权类型（模式）
     *
     * 枚举 {@link OAuth2GrantTypeEnum}
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> authorizedGrantTypes;
    /**
     * 授权范围
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> scopes;
    /**
     * 权限
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> authorities;
    /**
     * 资源
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> resourceIds;
    /**
     * 附加信息，JSON 格式
     */
    private String additionalInformation;

}
