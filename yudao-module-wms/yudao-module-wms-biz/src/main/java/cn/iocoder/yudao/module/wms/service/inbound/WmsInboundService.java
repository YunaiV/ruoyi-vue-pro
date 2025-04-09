package cn.iocoder.yudao.module.wms.service.inbound;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.wms.controller.admin.approval.history.vo.WmsApprovalReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.inbound.vo.WmsInboundPageReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.inbound.vo.WmsInboundRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.inbound.vo.WmsInboundSaveReqVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.inbound.WmsInboundDO;
import cn.iocoder.yudao.module.wms.enums.inbound.WmsInboundAuditStatus;
import jakarta.validation.Valid;
import java.util.List;

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
    WmsInboundDO createInbound(@Valid WmsInboundSaveReqVO createReqVO);

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

    void approve(WmsInboundAuditStatus.Event event, WmsApprovalReqVO approvalReqVO);

    WmsInboundRespVO getInboundWithItemList(Long id);

    void finishInbound(WmsInboundRespVO inboundRespVO);

    List<WmsInboundDO> selectByIds(List<Long> ids);

    List<WmsInboundDO> getSimpleList(@Valid WmsInboundPageReqVO pageReqVO);

    void assembleWarehouse(List<WmsInboundRespVO> list);

    void assembleDept(List<WmsInboundRespVO> list);

    void assembleCompany(List<WmsInboundRespVO> list);

    void assembleApprovalHistory(List<WmsInboundRespVO> list);
}
