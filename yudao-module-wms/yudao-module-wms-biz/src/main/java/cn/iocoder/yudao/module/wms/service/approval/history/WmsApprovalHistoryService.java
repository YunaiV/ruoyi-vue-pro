package cn.iocoder.yudao.module.wms.service.approval.history;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.wms.controller.admin.approval.history.vo.WmsApprovalHistoryPageReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.approval.history.vo.WmsApprovalHistoryRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.approval.history.vo.WmsApprovalHistorySaveReqVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.approval.history.WmsApprovalHistoryDO;
import cn.iocoder.yudao.module.wms.enums.common.WmsBillType;
import jakarta.validation.Valid;

import java.util.List;
import java.util.Map;

/**
 * 审批历史 Service 接口
 *
 * @author 李方捷
 */
public interface WmsApprovalHistoryService {

    /**
     * 创建审批历史
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    WmsApprovalHistoryDO createApprovalHistory(@Valid WmsApprovalHistorySaveReqVO createReqVO);

    /**
     * 更新审批历史
     *
     * @param updateReqVO 更新信息
     */
    WmsApprovalHistoryDO updateApprovalHistory(@Valid WmsApprovalHistorySaveReqVO updateReqVO);

    /**
     * 删除审批历史
     *
     * @param id 编号
     */
    void deleteApprovalHistory(Long id);

    /**
     * 获得审批历史
     *
     * @param id 编号
     * @return 审批历史
     */
    WmsApprovalHistoryDO getApprovalHistory(Long id);

    /**
     * 获得审批历史分页
     *
     * @param pageReqVO 分页查询
     * @return 审批历史分页
     */
    PageResult<WmsApprovalHistoryDO> getApprovalHistoryPage(WmsApprovalHistoryPageReqVO pageReqVO);

    Map<Long, List<WmsApprovalHistoryRespVO>> selectGroupedApprovalHistory(WmsBillType wmsBillType, List<Long> billIds);
}
