package cn.iocoder.yudao.module.mes.service.wm.outsourceissue;

import cn.iocoder.yudao.module.mes.controller.admin.wm.outsourceissue.vo.detail.MesWmOutsourceIssueDetailSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.outsourceissue.MesWmOutsourceIssueDetailDO;
import jakarta.validation.Valid;

import java.util.List;

/**
 * MES 外协发料单明细 Service 接口
 *
 * @author 芋道源码
 */
public interface MesWmOutsourceIssueDetailService {

    /**
     * 创建外协发料单明细
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createOutsourceIssueDetail(@Valid MesWmOutsourceIssueDetailSaveReqVO createReqVO);

    /**
     * 修改外协发料单明细
     *
     * @param updateReqVO 修改信息
     */
    void updateOutsourceIssueDetail(@Valid MesWmOutsourceIssueDetailSaveReqVO updateReqVO);

    /**
     * 删除外协发料单明细
     *
     * @param id 编号
     */
    void deleteOutsourceIssueDetail(Long id);

    /**
     * 获得外协发料单明细
     *
     * @param id 编号
     * @return 外协发料单明细
     */
    MesWmOutsourceIssueDetailDO getOutsourceIssueDetail(Long id);

    /**
     * 获得外协发料单明细列表
     *
     * @param issueId 发料单ID
     * @return 外协发料单明细列表
     */
    List<MesWmOutsourceIssueDetailDO> getOutsourceIssueDetailListByIssueId(Long issueId);

    /**
     * 获得外协发料单明细列表
     *
     * @param lineId 行ID
     * @return 外协发料单明细列表
     */
    List<MesWmOutsourceIssueDetailDO> getOutsourceIssueDetailListByLineId(Long lineId);

    /**
     * 删除外协发料单明细
     *
     * @param issueId 发料单ID
     */
    void deleteOutsourceIssueDetailByIssueId(Long issueId);

    /**
     * 删除外协发料单明细
     *
     * @param lineId 行ID
     */
    void deleteOutsourceIssueDetailByLineId(Long lineId);

}
