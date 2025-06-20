package cn.iocoder.yudao.module.wms.service.inbound;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.wms.controller.admin.approval.history.vo.WmsApprovalReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.inbound.vo.WmsInboundPageReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.inbound.vo.WmsInboundRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.inbound.vo.WmsInboundSaveReqVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.inbound.WmsInboundDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.inbound.item.WmsInboundItemLogicDO;
import cn.iocoder.yudao.module.wms.enums.inbound.WmsInboundAuditStatus;
import jakarta.validation.Valid;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 入库单 Service 接口
 *
 * @author 李方捷
 */
public interface WmsInboundService {

    /**
     * 创建入库单
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    WmsInboundDO createInbound(WmsInboundSaveReqVO createReqVO);

    /**
     * 更新入库单
     *
     * @param updateReqVO 更新信息
     */
    WmsInboundDO updateInbound(WmsInboundSaveReqVO updateReqVO);

    WmsInboundDO updateInboundAuditStatus(Long id, Integer status);

    /**
     * 删除入库单
     *
     * @param id 编号
     */
    void deleteInbound(Long id);

    /**
     * 获得入库单
     *
     * @param id 编号
     * @return 入库单
     */
    WmsInboundDO getInbound(Long id);

    /**
     * 校验存在性
     */
    WmsInboundDO validateInboundExists(Long id);

    /**
     * 获得入库单分页
     *
     * @param pageReqVO 分页查询
     * @return 入库单分页
     */
    PageResult<WmsInboundDO> getInboundPage(WmsInboundPageReqVO pageReqVO);

    /**
     * 按 warehouseId 查询 WmsInboundDO
     */
    List<WmsInboundDO> selectByWarehouseId(Long warehouseId, int limit);

    /**
     * 审批入库单
     *
     * @param event 审批事件
     * @param approvalReqVO 审批信息
     */
    void approve(WmsInboundAuditStatus.Event event, WmsApprovalReqVO approvalReqVO);

    /**
     * 获得入库单详情
     *
     * @param id 编号
     * @return 入库单
     */
    WmsInboundRespVO getInboundWithItemList(Long id);

    /**
     * 完成入库单
     *
     * @param inboundRespVO 入库单
     */
    void finishInbound(WmsInboundRespVO inboundRespVO);

    /**
     * 批量查询
     *
     * @param ids 入库单id集合
     * @return 入库单集合
     */
    List<WmsInboundDO> selectByIds(List<Long> ids);

    /**
     * 获得入库单列表
     *
     * @param pageReqVO 查询条件
     * @return 入库单列表
     */
    List<WmsInboundDO> getSimpleList(@Valid WmsInboundPageReqVO pageReqVO);

    /**
     * 装配仓库信息
     *
     * @param list 入库单集合
     */
    void assembleWarehouse(List<WmsInboundRespVO> list);

    /**
     * 装配公司信息
     *
     * @param list 入库单集合
     */
    void assembleCompany(List<WmsInboundRespVO> list);

    /**
     * 装配审批历史信息
     *
     * @param list 入库单集合
     */
    void assembleApprovalHistory(List<WmsInboundRespVO> list);

    /**
     * 按入库顺序获得第一个入库批次
     * @param warehouseId 仓库编号
     * @param productId 产品编号
     * @param olderFirst 是否按入库时间升序
     */
    WmsInboundItemLogicDO getInboundItemLogic(Long warehouseId, Long productId, boolean olderFirst);

    /**
     * 按入库顺序获得第一个入库批次
     * @param warehouseId 仓库编号
     * @param productId 产品编号
     * @param olderFirst 是否按入库时间升序
     */
    List<WmsInboundItemLogicDO> selectInboundItemLogicList(Long warehouseId, Long productId, boolean olderFirst);

    /**
     * 按入库顺序获得第一个入库批次
     * @param warehouseId 仓库编号
     * @param productIds 产品编号
     * @param olderFirst 是否按入库时间升序
     */
    Map<Long, WmsInboundItemLogicDO> getInboundItemLogicMap(Long warehouseId, List<Long> productIds, boolean olderFirst);

    /**
     * 按入库顺序获得入库批次列表
     *
     * @param warehouseId 仓库编号
     * @param productId   产品编号
     * @param olderFirst  是否按入库时间升序
     */
    List<WmsInboundItemLogicDO> getInboundItemLogicList(Long warehouseId, Long productId, Long deptId, boolean olderFirst);

    /**
     * 创建盘点入库单
     */
    WmsInboundDO createForStockCheck(WmsInboundSaveReqVO inboundSaveReqVO);

    /**
     * 更新上架状态
     */
    void updateShelvingStatus(Set<Long> set);


    List<WmsInboundDO> getInboundList(Integer upstreamType, Long upstreamId);
    /**
     * 创建调拨入库单
     */
    WmsInboundDO createForTransfer(WmsInboundSaveReqVO inboundSaveReqVO);

    void forceAbandon(WmsApprovalReqVO approvalReqVO);

    /**
     * 根据仓库ID，商品ID和库位号查询入库单
     */
    WmsInboundDO getByDetails(Long warehouseId, Long productId, Long companyId, Long deptId);

//    /**
//     * 根据产品ID，库位ID和仓库ID查询
//     */
//    List<WmsInboundItemFlowDetailVO> selectByProductIdAndBinIdAndWarehouseId(Long warehouseId, Long fromBinId, Long productId, int limit);
}
