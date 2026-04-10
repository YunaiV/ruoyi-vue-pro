package cn.iocoder.yudao.module.mes.service.wm.miscissue;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.mes.controller.admin.wm.miscissue.vo.MesWmMiscIssuePageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.wm.miscissue.vo.MesWmMiscIssueSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.miscissue.MesWmMiscIssueDO;
import jakarta.validation.Valid;

/**
 * MES 杂项出库单 Service 接口
 */
public interface MesWmMiscIssueService {

    /**
     * 创建杂项出库单
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createMiscIssue(@Valid MesWmMiscIssueSaveReqVO createReqVO);

    /**
     * 修改杂项出库单
     *
     * @param updateReqVO 修改信息
     */
    void updateMiscIssue(@Valid MesWmMiscIssueSaveReqVO updateReqVO);

    /**
     * 删除杂项出库单（级联删除行+明细）
     *
     * @param id 编号
     */
    void deleteMiscIssue(Long id);

    /**
     * 获得杂项出库单
     *
     * @param id 编号
     * @return 杂项出库单
     */
    MesWmMiscIssueDO getMiscIssue(Long id);

    /**
     * 获得杂项出库单分页
     *
     * @param pageReqVO 分页参数
     * @return 杂项出库单分页
     */
    PageResult<MesWmMiscIssueDO> getMiscIssuePage(MesWmMiscIssuePageReqVO pageReqVO);

    /**
     * 提交杂项出库单（草稿 → 待执行出库）
     *
     * @param id 编号
     */
    void submitMiscIssue(Long id);

    /**
     * 执行出库（待执行出库 → 已完成），扣减库存
     *
     * @param id 编号
     */
    void finishMiscIssue(Long id);

    /**
     * 取消杂项出库单（任意非已完成/已取消状态 → 已取消）
     *
     * @param id 编号
     */
    void cancelMiscIssue(Long id);

    /**
     * 校验杂项出库单存在且处于可编辑状态（草稿）
     *
     * @param id 编号
     * @return 杂项出库单
     */
    MesWmMiscIssueDO validateMiscIssueEditable(Long id);

}
