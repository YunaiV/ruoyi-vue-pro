package cn.iocoder.yudao.module.tms.service.first.mile.request;

import cn.iocoder.yudao.module.tms.dal.dataobject.first.mile.request.item.TmsFirstMileRequestItemDO;

import java.util.List;

/**
 * 头程申请表明细 Service 接口
 *
 * @author wdy
 */
public interface TmsFirstMileRequestItemService {


    /**
     * 创建头程申请表明细
     *
     * @param requestId 头程申请单id
     * @param list      集合
     */
    void createFirstMileRequestItemList(Long requestId, List<TmsFirstMileRequestItemDO> list);

    /**
     * 更新头程申请单子表
     *
     * @param requestId 头程申请单id
     * @param list 头程申请单子表
     */
    void updateFirstMileRequestItemList(Long requestId, List<TmsFirstMileRequestItemDO> list);
    /**
     * 删除头程申请表明细
     *
     * @param id 编号
     */
    void deleteFirstMileRequestItem(Long id);

    /**
     * 根据主单id，删除关联子表
     */
    void deleteFirstMileRequestItemByRequestId(Long requestId);

    /**
     * 获得头程申请表明细
     *
     * @param id 编号
     * @return 头程申请表明细
     */
    TmsFirstMileRequestItemDO getFirstMileRequestItem(Long id);

    /**
     * 通过关联id获得头程申请单
     *
     * @param id 关联申请项id
     * @return 头程申请单
     */
    List<TmsFirstMileRequestItemDO> getFirstMileRequestItemListByRequestId(Long id);

    //valid
    TmsFirstMileRequestItemDO validateFirstMileRequestItemExists(Long id);

    /**
     * 更新头程申请表明细状态
     *
     * @param id          头程申请表明细id
     * @param openStatus  开关状态
     * @param orderStatus 采购状态
     * @param closeQty 最终的已订购数量
     */
    void updateFirstMileRequestItemStatus(Long id, Integer openStatus, Integer orderStatus, Integer closeQty);
}