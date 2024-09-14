package cn.iocoder.yudao.module.iot.dal.dataobject.thinkmodelfunction;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.product.IotProductDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * IoT 产品物模型功能 DO
 *
 * 每个 {@link IotProductDO} 和 {@link IotThinkModelFunctionDO} 是“一对多”的关系，它的每个属性、事件、服务都对应一条记录
 *
 * @author 芋道源码
 */
@TableName("iot_think_model_function")
@KeySequence("iot_think_model_function_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IotThinkModelFunctionDO extends BaseDO {

    /**
     * 物模型功能编号
     */
    @TableId
    private Long id;
    // TODO @haohao：是不是有一个 identifier，需要要有哈
    // TODO @haohao：name、description 属性，还有个类型
    /**
     * 产品标识
     *
     * 关联 {@link IotProductDO#getId()}
     */
    private Long productId;
    /**
     * 产品标识
     *
     * 关联 {@link IotProductDO#getProductKey()}
     */
    private String productKey;

    // TODO @haohao：是不是可以搞成 ThingModelProperty、ThingModelEvent、ThingModelService 进行存储
    /**
     * 属性列表
     */
    private String properties;

    /**
     * 服务列表
     */
    private String services;

    /**
     * 事件列表
     */
    private String events;

}