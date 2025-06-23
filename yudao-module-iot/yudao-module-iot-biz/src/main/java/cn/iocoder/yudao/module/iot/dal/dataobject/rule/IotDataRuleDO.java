package cn.iocoder.yudao.module.iot.dal.dataobject.rule;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * IoT 数据流转 DO
 *
 * 组合 {@link IotDataRuleSourceDO} => {@link IotDataRuleSinkDO}
 *
 * @author 芋道源码
 */
@TableName(value = "iot_data_flow", autoResultMap = true)
@KeySequence("iot_data_flow_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IotDataRuleDO {

    /**
     * 数据流转编号
     */
    private Long id;
    /**
     * 数据流转名称
     */
    private String name;
    /**
     * 数据流转描述
     */
    private String description;
    /**
     * 数据流转状态
     *
     * 枚举 {@link CommonStatusEnum}
     */
    private Integer status;

    /**
     * 数据源编号
     *
     * 关联 {@link IotDataRuleSourceDO#getId()}
     */
    private List<Long> sourceIds;
    /**
     * 数据目的编号
     *
     * 关联 {@link IotDataRuleSinkDO#getId()}
     */
    private List<Long> sinkIds;

    // TODO @芋艿：未来考虑使用 groovy；支持数据处理；

}
