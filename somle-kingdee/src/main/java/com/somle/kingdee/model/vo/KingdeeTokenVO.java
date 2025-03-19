package com.somle.kingdee.model.vo;


import lombok.Data;

/**
 * KingdeeTokenVO - 用于传输金蝶令牌相关数据的 VO 类
 * 该类用于表示金蝶令牌的数据结构，返回给调用端。
 */
@Data
public class KingdeeTokenVO {

    // 唯一标识符
    private String id;

    // 应用的 Key
//    private String appKey;

    // 应用的 Secret
//    private String appSecret;

    // 旧的应用 Secret（如果有的话）
//    private String oldAppSecret;

    // 令牌状态，通常是启用（1）或禁用（0）
    private int status;

    // 访问令牌，用于 API 调用时的认证
    private String accessToken;

    // 应用令牌
    private String appToken;

    // 令牌过期时间（通常是 Unix 时间戳格式）
    private String expires;

    // 用户编号
//    private String userNumber;

    // 服务编号
    private String serviceId;

    // 客户端编号
    private String clientId;

    // 外部编号
    private String externalNumber;

    // 外部实例编号
    private String outerInstanceId;

    // 协议公司名称
    private String agreementCompanyName;

    // 协议授权产品类型
    private String agreementAuthProductType;

    // 协议管理员姓名
    private String agreementAdministratorName;

    // 协议管理员账号
    private String agreementAdministratorAccountNumber;

    // 租户编号
//    private String tenantId;

    // 账户编号
//    private String accountId;

    // 用户唯一标识符
    private String uid;

    // tid，一般用于标识金蝶的服务实例
    private int tid;

    // 创建时间，格式化为时间字符串
    private String createTime;

    // 开通时间，格式化为时间字符串
    private String openTime;

    // 密钥更新时间
    private String secretUpdateTime;

    // 是否为品牌，标识是否为品牌供应商
    private String isBrand;

    // 品牌供应商编号
    private String brandSupplierId;

    // 所属集团名称
    private String groupName;

    // 账户名称
    private String accountName;

    // 域名，通常用于认证或鉴权
    private String domain;

    // 实例编号
//    private String instanceId;

    // 实例过期时间
    private String instanceExpiresTime;

    // 是否保留数据，通常是“是”或“否”
    private String isRetainData;

    // 试用订单编号
    private String trialOrderId;

    // 自定义字段，应用签名（可能用于数据验证等）
    private String appSignature;
}
