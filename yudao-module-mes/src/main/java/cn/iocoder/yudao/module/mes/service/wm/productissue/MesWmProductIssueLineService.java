package cn.iocoder.yudao.module.mes.service.wm.productissue;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.mes.controller.admin.wm.productissue.vo.line.MesWmProductIssueLinePageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.wm.productissue.vo.line.MesWmProductIssueLineSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.productissue.MesWmProductIssueLineDO;
import jakarta.validation.Valid;

import java.util.List;

/**
 * MES 领料出库单行 Service 接口
 */
public interface MesWmProductIssueLineService {

    /**
     * 创建领料出库单行
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createProductIssueLine(@Valid MesWmProductIssueLineSaveReqVO createReqVO);

    /**
     * 更新领料出库单行
     *
     * @param updateReqVO 更新信息
     */
    void updateProductIssueLine(@Valid MesWmProductIssueLineSaveReqVO updateReqVO);

    /**
     * 删除领料出库单行
     *
     * @param id 编号
     */
    void deleteProductIssueLine(Long id);

    /**
     * 获得领料出库单行
     *
     * @param id 编号
     * @return 领料出库单行
     */
    MesWmProductIssueLineDO getProductIssueLine(Long id);

    /**
     * 获得领料出库单行分页
     *
     * @param pageReqVO 分页查询
     * @return 领料出库单行分页
     */
    PageResult<MesWmProductIssueLineDO> getProductIssueLinePage(MesWmProductIssueLinePageReqVO pageReqVO);

    /**
     * 根据领料单ID获取行列表
     *
     * @param issueId 领料单ID
     * @return 行列表
     */
    List<MesWmProductIssueLineDO> getProductIssueLineListByIssueId(Long issueId);

    /**
     * 根据领料单ID删除行
     *
     * @param issueId 领料单ID
     */
    void deleteProductIssueLineByIssueId(Long issueId);

    /**
     * 校验领料出库单行是否存在
     *
     * @param id 编号
     * @return 领料出库单行
     */
    MesWmProductIssueLineDO validateProductIssueLineExists(Long id);

}
