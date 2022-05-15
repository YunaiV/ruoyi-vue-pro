package cn.iocoder.yudao.module.system.dal.dataobject.oauth2;

import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;
import java.util.List;

/**
 * OAuth2 授权码 DO
 *
 * @author 芋道源码
 */
@TableName(value = "system_oauth2_code", autoResultMap = true)
@KeySequence("system_oauth2_code_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
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
     * 客户端编号
     *
     * 关联 {@link OAuth2ClientDO#getClientId()}
     */
    private String clientId;
    /**
     * 授权范围
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> scopes;
    /**
     * 重定向地址
     */
    private String redirectUri;
    /**
     * 状态
     */
    private String state;
    /**
     * 过期时间
     */
    private Date expiresTime;

}
