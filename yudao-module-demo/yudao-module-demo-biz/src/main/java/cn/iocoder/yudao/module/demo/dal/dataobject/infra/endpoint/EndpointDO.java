package cn.iocoder.yudao.module.demo.dal.dataobject.infra.endpoint;

import lombok.*;
import java.util.*;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 区块链节点 DO
 *
 * @author ruanzh.eth
 */
@TableName("demo_infra_endpoint")
@KeySequence("demo_infra_endpoint_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EndpointDO extends BaseDO {

    /**
     * 节点编号
     */
    @TableId
    private Long id;
    /**
     * 网络编号
     */
    private Long netId;
    /**
     * 名称
     */
    private String name;
    /**
     * 地址
     */
    private String url;
    /**
     * 是否被墙
     */
    private Boolean blocked;
    /**
     * 信息
     */
    private String info;

}
