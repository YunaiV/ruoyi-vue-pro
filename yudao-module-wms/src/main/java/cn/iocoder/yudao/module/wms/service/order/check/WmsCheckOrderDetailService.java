package cn.iocoder.yudao.module.wms.service.order.check;

import cn.iocoder.yudao.module.wms.controller.admin.order.check.vo.order.WmsCheckOrderSaveReqVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.order.check.WmsCheckOrderDetailDO;

import java.util.Collection;
import java.util.List;

/**
 * WMS 盘库单明细 Service 接口
 *
 * @author 芋道源码
 */
public interface WmsCheckOrderDetailService {

    /**
     * 创建盘库单明细列表
     *
     * @param orderId 盘库单编号
     * @param reqVO 盘库单保存信息
     */
    void createCheckOrderDetailList(Long orderId, WmsCheckOrderSaveReqVO reqVO);

    /**
     * 更新盘库单明细列表
     *
     * @param orderId 盘库单编号
     * @param reqVO 盘库单保存信息
     */
    void updateCheckOrderDetailList(Long orderId, WmsCheckOrderSaveReqVO reqVO);

    /**
     * 按盘库单编号删除明细列表
     *
     * @param orderId 盘库单编号
     */
    void deleteCheckOrderDetailListByOrderId(Long orderId);

    /**
     * 按盘库单编号获得明细列表
     *
     * @param orderId 盘库单编号
     * @return 明细列表
     */
    List<WmsCheckOrderDetailDO> getCheckOrderDetailList(Long orderId);

    /**
     * 按盘库单编号集合获得明细列表
     *
     * @param orderIds 盘库单编号集合
     * @return 明细列表
     */
    List<WmsCheckOrderDetailDO> getCheckOrderDetailList(Collection<Long> orderIds);

    /**
     * 校验盘库单明细列表存在
     *
     * @param orderId 盘库单编号
     * @return 明细列表
     */
    List<WmsCheckOrderDetailDO> validateCheckOrderDetailListExists(Long orderId);

    /**
     * 获得指定 SKU 的盘库单明细数量
     *
     * @param skuId SKU 编号
     * @return 盘库单明细数量
     */
    long getCheckOrderDetailCountBySkuId(Long skuId);

}
