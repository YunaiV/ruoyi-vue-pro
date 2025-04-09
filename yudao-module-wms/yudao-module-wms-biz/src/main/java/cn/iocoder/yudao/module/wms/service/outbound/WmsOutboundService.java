package cn.iocoder.yudao.module.wms.service.outbound;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.wms.controller.admin.approval.history.vo.WmsApprovalReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.outbound.vo.WmsOutboundPageReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.outbound.vo.WmsOutboundRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.outbound.vo.WmsOutboundSaveReqVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.outbound.WmsOutboundDO;
import cn.iocoder.yudao.module.wms.enums.outbound.WmsOutboundAuditStatus;
import jakarta.validation.Valid;
import java.util.List;

/**
 * 出库单 Service 接口
 *
 * @author 李方捷
 */
public interface WmsOutboundService {

    /**
     * 创建出库单
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    WmsOutboundDO createOutbound(@Valid WmsOutboundSaveReqVO createReqVO);

    /**
     * 更新出库单
     *
     * @param updateReqVO 更新信息
     */
    WmsOutboundDO updateOutbound(@Valid WmsOutboundSaveReqVO updateReqVO);

    /**
     * 删除出库单
     *
     * @param id 编号
     */
    void deleteOutbound(Long id);

    /**
     * 获得出库单
     *
     * @param id 编号
     * @return 出库单
     */
    WmsOutboundDO getOutbound(Long id);

    /**
     * 获得出库单分页
     *
     * @param pageReqVO 分页查询
     * @return 出库单分页
     */
    PageResult<WmsOutboundDO> getOutboundPage(WmsOutboundPageReqVO pageReqVO);

    void approve(WmsOutboundAuditStatus.Event event, WmsApprovalReqVO approvalReqVO);

    WmsOutboundDO updateOutboundAuditStatus(Long id, Integer status);

    WmsOutboundRespVO getOutboundWithItemList(Long id);

    void finishOutbound(WmsOutboundRespVO outboundRespVO);

    WmsOutboundDO validateOutboundExists(Long id);

    List<WmsOutboundDO> selectByIds(List<Long> list);

    List<WmsOutboundDO> getSimpleList(@Valid WmsOutboundPageReqVO pageReqVO);

    void assembleWarehouse(List<WmsOutboundRespVO> list);

    void assembleDept(List<WmsOutboundRespVO> list);

    void assembleCompany(List<WmsOutboundRespVO> list);

    void assembleApprovalHistory(List<WmsOutboundRespVO> list);
}
