package cn.iocoder.yudao.module.ai1.dal.dataobject.model;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * AI1 供应商 DO
 *
 * 供应商即模型提供方（OpenAI 兼容端点），维护接口地址与密钥
 *
 * @author 芋道源码
 */
@TableName("ai1_provider")
@KeySequence("ai1_provider_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Ai1ProviderDO extends TenantBaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 名称
     */
    private String name;
    /**
     * 接口地址
     */
    private String baseUrl;
    /**
     * API 密钥
     */
    private String apiKey;
    /**
     * 请求附属 Header
     *
     * JSON 数组，格式为 [{"key":"...","value":"..."}]；value 可包含 {session} 占位符，请求时替换为会话编号
     */
    private String headers;
    /**
     * 状态
     *
     * 枚举 {@link CommonStatusEnum}
     */
    private Integer status;
    /**
     * 备注
     */
    private String remark;

}
