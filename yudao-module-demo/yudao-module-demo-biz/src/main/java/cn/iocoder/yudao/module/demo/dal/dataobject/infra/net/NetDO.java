package cn.iocoder.yudao.module.demo.dal.dataobject.infra.net;

import lombok.*;
import java.util.*;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 区块链网络 DO
 *
 * @author ruanzh.eth
 */
@TableName("demo_infra_net")
@KeySequence("demo_infra_net_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NetDO extends BaseDO {

    /**
     * 网络编号
     */
    @TableId
    private Long id;
    /**
     * 名称
     */
    private String name;
    /**
     * 区块浏览器
     */
    private String explorer;
    /**
     * 原生代币
     */
    private String symbol;
    /**
     * 节点
     */
    private String endpoint;
    /**
     * 网络类型
     */
    private String type;
    /**
     * 信息
     */
    private String info;

}
