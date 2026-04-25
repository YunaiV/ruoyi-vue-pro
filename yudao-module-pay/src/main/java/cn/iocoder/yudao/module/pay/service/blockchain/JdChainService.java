package cn.iocoder.yudao.module.pay.service.blockchain;

import java.util.Map;

/**
 * JD Chain 存证服务接口
 * 实现类由具体的 JD Chain SDK 集成提供；不集成时该 Bean 不注册，
 * BlockchainAsyncService 自动降级为本地哈希方案。
 *
 * @author deepay
 */
public interface JdChainService {

    /**
     * 将数据写入 JD Chain 数据账户
     *
     * @param dataAccountAddress 数据账户地址（建议用 "ORDER_" + orderId 派生）
     * @param key                链上 Key
     * @param value              待存证的数据（Map 会被序列化为 JSON）
     * @return 链上交易哈希
     */
    String storeData(String dataAccountAddress, String key, Map<String, Object> value);

    /**
     * 检查 JD Chain 节点连通性
     *
     * @return true 表示可用
     */
    boolean isHealthy();

}
