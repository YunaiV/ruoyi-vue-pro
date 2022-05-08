package cn.iocoder.yudao.module.system.dal.dataobject.auth;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * OAuth2 客户端 DO
 *
 * 为什么不使用 Client 作为表名？
 * 1. clientId 字段被占用，导致表的 id 无法有合适的缩写
 * 2. 大多数 Github、Gitee 等平台，都会习惯称为第三方接入应用
 *
 * 如下字段，考虑到使用相对不是很高频，主要是一些开关，暂时不支持：
 * authorized_grant_types、authorities、access_token_validity、refresh_token_validity、additional_information、autoapprove、resource_ids、scope
 *
 * @author 芋道源码
 */
@TableName("system_oauth2_application")
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class OAuth2ApplicationDO extends BaseDO {

    /**
     * 编号，数据库递增
     */
    private Long id;
    /**
     * 客户端编号
     */
    private String clientId;
    /**
     * 客户端密钥
     */
    private String clientSecret;
    /**
     * 可重定向的 URI 地址
     */
    private List<String> redirectUris;
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

}
