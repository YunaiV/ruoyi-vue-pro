package cn.iocoder.yudao.module.iot.dal.dataobject.thinkmodelfunction;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.iot.controller.admin.thinkmodelfunction.thingModel.ThingModelEvent;
import cn.iocoder.yudao.module.iot.controller.admin.thinkmodelfunction.thingModel.ThingModelProperty;
import cn.iocoder.yudao.module.iot.dal.dataobject.product.IotProductDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * IoT 产品物模型功能 DO
 *
 * 每个 {@link IotProductDO} 和 {@link IotThinkModelFunctionDO2} 是“一对多”的关系，它的每个属性、事件、服务都对应一条记录
 *
 * @author 芋道源码
 */
@TableName("iot_think_model_function")
@KeySequence("iot_think_model_function_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IotThinkModelFunctionDO2 extends BaseDO {

    /**
     * 物模型功能编号
     */
    @TableId
    private Long id;

    /**
     * 功能标识
     */
    private String identifier;
    /**
     * 功能名称
     */
    private String name;
    /**
     * 功能描述
     */
    private String description;

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

    /**
     * 功能类型
     *
     * 1 - 属性
     * 2 - 服务
     * 3 - 事件
     */
    // TODO @haohao：枚举
    private Integer type;

    /**
     * 属性
     */
    private ThingModelProperty property;
    /**
     * 事件
     */
    private ThingModelEvent event;
    /**
     * 服务
     */
    private String service;

}