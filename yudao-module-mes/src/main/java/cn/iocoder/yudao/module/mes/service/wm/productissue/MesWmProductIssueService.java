package cn.iocoder.yudao.module.mes.service.wm.productissue;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.mes.controller.admin.wm.productissue.vo.MesWmProductIssuePageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.wm.productissue.vo.MesWmProductIssueSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.productissue.MesWmProductIssueDO;
import jakarta.validation.Valid;

/**
 * MES 领料出库单 Service 接口
 */
public interface MesWmProductIssueService {

    /**
     * 创建领料出库单
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createProductIssue(@Valid MesWmProductIssueSaveReqVO createReqVO);

    /**
     * 修改领料出库单
     *
     * @param updateReqVO 修改信息
     */
    void updateProductIssue(@Valid MesWmProductIssueSaveReqVO updateReqVO);

    /**
     * 删除领料出库单
     *
     * @param id 编号
     */
    void deleteProductIssue(Long id);

    /**
     * 获得领料出库单
     *
     * @param id 编号
     * @return 领料出库单
     */
    MesWmProductIssueDO getProductIssue(Long id);

    /**
     * 获得领料出库单分页
     *
     * @param pageReqVO 分页参数
     * @return 领料出库单分页
     */
    PageResult<MesWmProductIssueDO> getProductIssuePage(MesWmProductIssuePageReqVO pageReqVO);

    /**
     * 校验领料出库单是否存在
     *
     * @param id 编号
     * @return 领料出库单
     */
    MesWmProductIssueDO validateProductIssueExists(Long id);

    /**
     * 校验领料出库单存在且为准备中状态
     *
     * @param id 编号
     * @return 领料出库单
     */
    MesWmProductIssueDO validateProductIssueExistsAndPrepare(Long id);

    /**
     * 提交领料出库单（草稿 → 待拣货）
     *
     * @param id 编号
     */
    void submitProductIssue(Long id);

    /**
     * 执行拣货（待拣货 → 待执行领出）
     *
     * @param id 编号
     */
    void stockProductIssue(Long id);

    /**
     * 执行领出/出库（待执行领出 → 已完成），更新库存台账
     *
     * @param id 编号
     */
    void finishProductIssue(Long id);

    /**
     * 取消领料出库单（任意非已完成/已取消状态 → 已取消）
     *
     * @param id 编号
     */
    void cancelProductIssue(Long id);

    /**
     * 校验领料出库单的数量：每行明细数量之和是否等于行领料数量
     *
     * @param id 编号
     * @return 是否全部一致
     */
    Boolean checkProductIssueQuantity(Long id);

}
