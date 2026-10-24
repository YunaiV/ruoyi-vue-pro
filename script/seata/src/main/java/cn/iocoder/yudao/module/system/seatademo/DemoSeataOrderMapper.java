package cn.iocoder.yudao.module.system.seatademo;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import com.baomidou.dynamic.datasource.annotation.DS;
import org.apache.ibatis.annotations.Mapper;

/**
 * 分布式事务示例订单 Mapper
 *
 * 在 Mapper 上添加 @DS 注解，保证 demo_seata_order 表的 SQL 都使用 seata_order 数据源
 *
 * @author 芋道源码
 */
@Mapper
@DS(DemoSeataOrderMapper.DATA_SOURCE)
public interface DemoSeataOrderMapper extends BaseMapperX<DemoSeataOrderDO> {

    /**
     * 订单库的数据源名，对应 spring.datasource.dynamic.datasource.seata_order 配置项
     */
    String DATA_SOURCE = "seata_order";

}
