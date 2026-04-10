package cn.iocoder.yudao.module.mes.service.wm.miscissue;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.mes.controller.admin.wm.miscissue.vo.line.MesWmMiscIssueLinePageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.wm.miscissue.vo.line.MesWmMiscIssueLineSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.miscissue.MesWmMiscIssueLineDO;
import jakarta.validation.Valid;

import java.util.List;

/**
 * MES 杂项出库单行 Service 接口
 */
public interface MesWmMiscIssueLineService {

    /**
     * 创建杂项出库单行
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createMiscIssueLine(@Valid MesWmMiscIssueLineSaveReqVO createReqVO);

    /**
     * 修改杂项出库单行
     *
     * @param updateReqVO 修改信息
     */
    void updateMiscIssueLine(@Valid MesWmMiscIssueLineSaveReqVO updateReqVO);

    /**
     * 删除杂项出库单行（级联删除明细）
     *
     * @param id 编号
     */
    void deleteMiscIssueLine(Long id);

    /**
     * 获得杂项出库单行
     *
     * @param id 编号
     * @return 杂项出库单行
     */
    MesWmMiscIssueLineDO getMiscIssueLine(Long id);

    /**
     * 获得杂项出库单行分页
     *
     * @param pageReqVO 分页参数
     * @return 杂项出库单行分页
     */
    PageResult<MesWmMiscIssueLineDO> getMiscIssueLinePage(MesWmMiscIssueLinePageReqVO pageReqVO);

    /**
     * 按出库单编号获得行列表
     *
     * @param issueId 出库单编号
     * @return 行列表
     */
    List<MesWmMiscIssueLineDO> getMiscIssueLineListByIssueId(Long issueId);

    /**
     * 按出库单编号批量删除行
     *
     * @param issueId 出库单编号
     */
    void deleteMiscIssueLineByIssueId(Long issueId);

    /**
     * 校验杂项出库单行是否存在
     *
     * @param id 编号
     */
    void validateMiscIssueLineExists(Long id);

}
