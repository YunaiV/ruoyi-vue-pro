package cn.iocoder.yudao.module.mes.service.wm.returnissue;

import cn.iocoder.yudao.module.mes.controller.admin.wm.returnissue.vo.detail.MesWmReturnIssueDetailSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.returnissue.MesWmReturnIssueDetailDO;
import jakarta.validation.Valid;

import java.util.List;

/**
 * MES 生产退料明细 Service 接口
 */
public interface MesWmReturnIssueDetailService {

    /**
     * 创建生产退料明细
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createReturnIssueDetail(@Valid MesWmReturnIssueDetailSaveReqVO createReqVO);

    /**
     * 更新生产退料明细
     *
     * @param updateReqVO 更新信息
     */
    void updateReturnIssueDetail(@Valid MesWmReturnIssueDetailSaveReqVO updateReqVO);

    /**
     * 删除生产退料明细
     *
     * @param id 编号
     */
    void deleteReturnIssueDetail(Long id);

    /**
     * 获得生产退料明细
     *
     * @param id 编号
     * @return 生产退料明细
     */
    MesWmReturnIssueDetailDO getReturnIssueDetail(Long id);

    /**
     * 根据行 ID 获取明细列表
     *
     * @param lineId 行 ID
     * @return 明细列表
     */
    List<MesWmReturnIssueDetailDO> getReturnIssueDetailListByLineId(Long lineId);

    /**
     * 根据退料单 ID 获取明细列表
     *
     * @param issueId 退料单 ID
     * @return 明细列表
     */
    List<MesWmReturnIssueDetailDO> getReturnIssueDetailListByIssueId(Long issueId);

    /**
     * 根据退料单 ID 删除明细
     *
     * @param issueId 退料单 ID
     */
    void deleteReturnIssueDetailByIssueId(Long issueId);

    /**
     * 根据行 ID 删除明细
     *
     * @param lineId 行 ID
     */
    void deleteReturnIssueDetailByLineId(Long lineId);

}
