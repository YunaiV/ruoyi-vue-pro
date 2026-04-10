package cn.iocoder.yudao.module.mes.service.wm.productissue;

import cn.iocoder.yudao.module.mes.controller.admin.wm.productissue.vo.detail.MesWmProductIssueDetailSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.productissue.MesWmProductIssueDetailDO;
import jakarta.validation.Valid;

import java.util.List;

/**
 * MES 领料出库明细 Service 接口
 */
public interface MesWmProductIssueDetailService {

    /**
     * 创建领料出库明细
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createProductIssueDetail(@Valid MesWmProductIssueDetailSaveReqVO createReqVO);

    /**
     * 更新领料出库明细
     *
     * @param updateReqVO 更新信息
     */
    void updateProductIssueDetail(@Valid MesWmProductIssueDetailSaveReqVO updateReqVO);

    /**
     * 删除领料出库明细
     *
     * @param id 编号
     */
    void deleteProductIssueDetail(Long id);

    /**
     * 获得领料出库明细
     *
     * @param id 编号
     * @return 领料出库明细
     */
    MesWmProductIssueDetailDO getProductIssueDetail(Long id);

    /**
     * 根据行ID获取明细列表
     *
     * @param lineId 行ID
     * @return 明细列表
     */
    List<MesWmProductIssueDetailDO> getProductIssueDetailListByLineId(Long lineId);

    /**
     * 根据领料单ID获取明细列表
     *
     * @param issueId 领料单ID
     * @return 明细列表
     */
    List<MesWmProductIssueDetailDO> getProductIssueDetailListByIssueId(Long issueId);

    /**
     * 根据领料单ID删除明细
     *
     * @param issueId 领料单ID
     */
    void deleteProductIssueDetailByIssueId(Long issueId);

    /**
     * 根据行ID删除明细
     *
     * @param lineId 行ID
     */
    void deleteProductIssueDetailByLineId(Long lineId);

}
