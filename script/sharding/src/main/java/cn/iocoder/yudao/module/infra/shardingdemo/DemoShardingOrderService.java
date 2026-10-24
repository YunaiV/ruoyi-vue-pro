package cn.iocoder.yudao.module.infra.shardingdemo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.baomidou.dynamic.datasource.annotation.DS;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.List;

/**
 * 分库分表示例订单 Service
 *
 * 1. 带 userId（分片键）的方法，只路由到一个物理库的一张物理表
 * 2. 不带 userId 的方法，会查询全部 4 个物理节点，再由 ShardingSphere 归并结果
 *
 * @author 芋道源码
 */
@Service
@DS(DemoShardingOrderMapper.DATA_SOURCE) // 先切换到 sharding 数据源，再开启 @Transactional 事务
@RequiredArgsConstructor // 使用构造方法注入，避免 JDK 8 与 JDK 17+ 分支的 @Resource 包名不同
public class DemoShardingOrderService {

    private final DemoShardingOrderMapper demoShardingOrderMapper;

    @Transactional(rollbackFor = Exception.class) // 同一个 userId 只在一个物理库，所以是单库的本地事务
    public Long createOrder(Long userId, String name) {
        Assert.notNull(userId, "用户编号（分片键）不能为空");
        DemoShardingOrderDO order = DemoShardingOrderDO.builder().userId(userId).name(name).build();
        demoShardingOrderMapper.insert(order);
        return order.getId();
    }

    @Transactional(rollbackFor = Exception.class)
    public int updateOrderName(Long userId, Long id, String name) {
        Assert.notNull(userId, "用户编号（分片键）不能为空");
        // 更新条件带上分片键；更新内容不包含 userId，分片键创建后不允许修改
        DemoShardingOrderDO updateObj = DemoShardingOrderDO.builder().name(name).build();
        return demoShardingOrderMapper.update(updateObj, new LambdaQueryWrapperX<DemoShardingOrderDO>()
                .eq(DemoShardingOrderDO::getUserId, userId)
                .eq(DemoShardingOrderDO::getId, id));
    }

    @Transactional(rollbackFor = Exception.class)
    public int deleteOrder(Long userId, Long id) {
        Assert.notNull(userId, "用户编号（分片键）不能为空");
        return demoShardingOrderMapper.delete(new LambdaQueryWrapperX<DemoShardingOrderDO>()
                .eq(DemoShardingOrderDO::getUserId, userId)
                .eq(DemoShardingOrderDO::getId, id));
    }

    public DemoShardingOrderDO getOrder(Long userId, Long id) {
        Assert.notNull(userId, "用户编号（分片键）不能为空");
        return demoShardingOrderMapper.selectOne(DemoShardingOrderDO::getUserId, userId,
                DemoShardingOrderDO::getId, id);
    }

    public List<DemoShardingOrderDO> getOrderListByUserId(Long userId) {
        Assert.notNull(userId, "用户编号（分片键）不能为空");
        return demoShardingOrderMapper.selectListByUserId(userId);
    }

    /**
     * 获得订单分页
     *
     * @param pageParam 分页参数
     * @param userId    用户编号，可为空；为空时查询全部分片，页码越大，归并的数据越多
     * @return 订单分页
     */
    public PageResult<DemoShardingOrderDO> getOrderPage(PageParam pageParam, Long userId) {
        return demoShardingOrderMapper.selectPage(pageParam, userId);
    }

}
