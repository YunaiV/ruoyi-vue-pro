package cn.iocoder.yudao.module.ai1.dal.dataobject.mcp;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import cn.iocoder.yudao.module.ai1.enums.mcp.Ai1McpTransportEnum;
import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * AI1 MCP 服务 DO
 *
 * @author 芋道源码
 */
@TableName("ai1_mcp")
@KeySequence("ai1_mcp_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Ai1McpDO extends TenantBaseDO {

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
     * 传输方式
     *
     * 枚举 {@link Ai1McpTransportEnum}；以该列为准，保存时同步写回 {@link #config} 的 transport 字段
     */
    private String transport;
    // TODO @AI：不使用 @TableField(updateStrategy = FieldStrategy.ALWAYS)；通过 updateForSave 主动去 set 下；
    /**
     * 服务地址，远程必填、本地为空
     *
     * 切换为本地时需要清空，故更新时总是写入
     */
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String url;
    // TODO @AI：需要 map 么？
    /**
     * 请求头，JSON 对象
     */
    private String headers;
    /**
     * 完整 MCP 配置，JSON 对象
     *
     * 远程：{transport, url, headers}；本地：{transport, command, args, env, cwd}
     */
    private String config;
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
