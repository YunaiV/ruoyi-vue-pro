package cn.iocoder.yudao.module.deepay.service.inventory;

import cn.iocoder.yudao.module.deepay.dal.dataobject.DeepayInventoryDO;

import java.util.List;

/**
 * Deepay 库存业务服务接口。
 *
 * <p>封装库存的初始化、锁定、确认及状态维护逻辑。</p>
 */
public interface InventoryService {

    /**
     * 初始化库存。
     *
     * <p>在链码生成后调用，设置默认库存数量为 10。</p>
     *
     * @param chainCode 商品链码
     */
    void initInventory(String chainCode);

    /**
     * 下单锁定库存。
     *
     * <p>stock &gt; 0 时：stock -= 1，lockedStock += 1；
     * 同时更新库存状态，若 stock == 0 则自动触发生产。</p>
     *
     * @param chainCode 商品链码
     * @return true 表示锁定成功，false 表示库存不足
     */
    boolean lockStock(String chainCode);

    /**
     * 支付成功释放锁定库存。
     *
     * <p>lockedStock -= 1（商品已售出，锁定库存归零）。</p>
     *
     * @param chainCode 商品链码
     */
    void confirmPayment(String chainCode);

    /**
     * 手动补充库存。
     *
     * <p>后台管理员手动增加库存时调用。</p>
     *
     * @param chainCode 商品链码
     * @param quantity  增加的数量（必须 &gt; 0）
     */
    void addStock(String chainCode, int quantity);

    /**
     * 查询所有库存记录。
     *
     * @return 所有库存列表
     */
    List<DeepayInventoryDO> listAll();

    /**
     * 根据链码查询库存记录。
     *
     * @param chainCode 商品链码
     * @return 库存记录
     */
    DeepayInventoryDO getByChainCode(String chainCode);

}
