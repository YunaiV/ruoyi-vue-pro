package cn.iocoder.yudao.module.mes.service.wm.miscissue;

import cn.iocoder.yudao.module.mes.controller.admin.wm.miscissue.vo.line.MesWmMiscIssueLineSaveReqVO;

/**
 * MES 杂项出库明细 Service 接口
 */
public interface MesWmMiscIssueDetailService {

    /**
     * 创建杂项出库明细
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createMiscIssueDetail(MesWmMiscIssueLineSaveReqVO createReqVO);

    /**
     * 更新杂项出库明细
     *
     * @param updateReqVO 更新信息
     */
    void updateMiscIssueDetail(MesWmMiscIssueLineSaveReqVO updateReqVO);

    /**
     * 删除杂项出库明细（根据出库单ID）
     *
     * @param issueId 出库单ID
     */
    void deleteMiscIssueDetailByIssueId(Long issueId);

    /**
     * 删除杂项出库明细（根据行ID）
     *
     * @param lineId 行ID
     */
    void deleteMiscIssueDetailByLineId(Long lineId);

    /**
     * 校验杂项出库明细是否存在
     *
     * @param id 编号
     */
    void validateMiscIssueDetailExists(Long id);

}
