package cn.iocoder.yudao.module.mes.service.wm.outsourceissue;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.mes.controller.admin.wm.outsourceissue.vo.MesWmOutsourceIssuePageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.wm.outsourceissue.vo.MesWmOutsourceIssueSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.outsourceissue.MesWmOutsourceIssueDO;
import jakarta.validation.Valid;

/**
 * MES 外协发料单 Service 接口
 *
 * @author 芋道源码
 */
public interface MesWmOutsourceIssueService {

    /**
     * 创建外协发料单
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createOutsourceIssue(@Valid MesWmOutsourceIssueSaveReqVO createReqVO);

    /**
     * 修改外协发料单
     *
     * @param updateReqVO 修改信息
     */
    void updateOutsourceIssue(@Valid MesWmOutsourceIssueSaveReqVO updateReqVO);

    /**
     * 删除外协发料单（级联删除行和明细）
     *
     * @param id 编号
     */
    void deleteOutsourceIssue(Long id);

    /**
     * 获得外协发料单
     *
     * @param id 编号
     * @return 外协发料单
     */
    MesWmOutsourceIssueDO getOutsourceIssue(Long id);

    /**
     * 获得外协发料单分页
     *
     * @param pageReqVO 分页参数
     * @return 外协发料单分页
     */
    PageResult<MesWmOutsourceIssueDO> getOutsourceIssuePage(MesWmOutsourceIssuePageReqVO pageReqVO);

    /**
     * 提交到待拣货（草稿 → 待拣货）
     *
     * @param id 编号
     */
    void submitOutsourceIssue(Long id);

    /**
     * 执行拣货（待拣货 → 待执行出库）
     *
     * @param id 编号
     */
    void stockOutsourceIssue(Long id);

    /**
     * 完成外协发料出库（待执行出库 → 已完成，扣减库存）
     *
     * @param id 编号
     */
    void finishOutsourceIssue(Long id);

    /**
     * 取消外协发料单
     *
     * @param id 编号
     */
    void cancelOutsourceIssue(Long id);

    /**
     * 校验外协发料单数量（行数量 = 明细数量之和）
     *
     * @param id 编号
     * @return true 表示数量匹配，false 表示不匹配
     */
    Boolean checkOutsourceIssueQuantity(Long id);

    /**
     * 根据供应商 ID 统计外协发料单数量
     *
     * @param vendorId 供应商 ID
     * @return 数量
     */
    Long getOutsourceIssueCountByVendorId(Long vendorId);

}
