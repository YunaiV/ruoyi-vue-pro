package cn.iocoder.yudao.module.infra.shardingdemo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.baomidou.dynamic.datasource.annotation.DS;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 分库分表示例订单 Mapper
 *
 * 在 Mapper 上添加 @DS 注解，保证逻辑表 demo_sharding_order 的 SQL 都使用 sharding 数据源
 *
 * @author 芋道源码
 */
@Mapper
@DS(DemoShardingOrderMapper.DATA_SOURCE)
public interface DemoShardingOrderMapper extends BaseMapperX<DemoShardingOrderDO> {

    /**
     * 分库分表的数据源名，对应 spring.datasource.dynamic.datasource.sharding 配置项
     */
    String DATA_SOURCE = "sharding";

    default List<DemoShardingOrderDO> selectListByUserId(Long userId) {
        return selectList(new LambdaQueryWrapperX<DemoShardingOrderDO>()
                .eq(DemoShardingOrderDO::getUserId, userId)
                .orderByDesc(DemoShardingOrderDO::getId));
    }

    default PageResult<DemoShardingOrderDO> selectPage(PageParam pageParam, Long userId) {
        return selectPage(pageParam, new LambdaQueryWrapperX<DemoShardingOrderDO>()
                .eqIfPresent(DemoShardingOrderDO::getUserId, userId) // 有 userId 时只查询一个分片，否则查询全部分片后归并
                .orderByDesc(DemoShardingOrderDO::getId));
    }

}
