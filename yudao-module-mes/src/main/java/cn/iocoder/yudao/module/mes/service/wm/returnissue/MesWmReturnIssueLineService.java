package cn.iocoder.yudao.module.mes.service.wm.returnissue;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.mes.controller.admin.wm.returnissue.vo.line.MesWmReturnIssueLinePageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.wm.returnissue.vo.line.MesWmReturnIssueLineSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.returnissue.MesWmReturnIssueLineDO;
import jakarta.validation.Valid;

import java.math.BigDecimal;
import java.util.List;

/**
 * MES 生产退料单行 Service 接口
 */
public interface MesWmReturnIssueLineService {

    /**
     * 创建生产退料单行
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createReturnIssueLine(@Valid MesWmReturnIssueLineSaveReqVO createReqVO);

    /**
     * 更新生产退料单行
     *
     * @param updateReqVO 更新信息
     */
    void updateReturnIssueLine(@Valid MesWmReturnIssueLineSaveReqVO updateReqVO);

    /**
     * 删除生产退料单行
     *
     * @param id 编号
     */
    void deleteReturnIssueLine(Long id);

    /**
     * 获得生产退料单行
     *
     * @param id 编号
     * @return 生产退料单行
     */
    MesWmReturnIssueLineDO getReturnIssueLine(Long id);

    /**
     * 获得生产退料单行分页
     *
     * @param pageReqVO 分页查询
     * @return 生产退料单行分页
     */
    PageResult<MesWmReturnIssueLineDO> getReturnIssueLinePage(MesWmReturnIssueLinePageReqVO pageReqVO);

    /**
     * 根据退料单 ID 获取行列表
     *
     * @param issueId 退料单 ID
     * @return 行列表
     */
    List<MesWmReturnIssueLineDO> getReturnIssueLineListByIssueId(Long issueId);

    /**
     * 根据退料单 ID 删除行
     *
     * @param issueId 退料单 ID
     */
    void deleteReturnIssueLineByIssueId(Long issueId);

    /**
     * 校验生产退料单行是否存在
     *
     * @param id 编号
     * @return 生产退料单行
     */
    MesWmReturnIssueLineDO validateReturnIssueLineExists(Long id);

    /**
     * 刷新退料单下所有行的质量状态（退料类型变更时调用）
     *
     * @param issueId 退料单 ID
     * @param issueType 退料类型
     */
    void updateReturnIssueQualityStatusByIssueId(Long issueId, Integer issueType);

    /**
     * RQC 检验完成后，根据合格品/不合格品数量更新生产退料单行
     *
     * <p>拆分逻辑：
     * <ul>
     *     <li>全部合格：直接更新行的 qualityStatus 为合格</li>
     *     <li>全部不合格：直接更新行的 qualityStatus 为不合格</li>
     *     <li>部分合格部分不合格：拆分原行（原行改为合格+数量=合格数，新增一行不合格+数量=不合格数）</li>
     * </ul>
     * 同时检查退料单下所有行是否都已检验完毕，若全检完则将退料单状态设为待上架。
     *
     * @param sourceLineId        来源行 ID
     * @param sourceDocId         来源退料单 ID
     * @param checkResult         检验结果
     * @param qualifiedQuantity   合格品数量
     * @param unqualifiedQuantity 不合格品数量
     */
    void updateReturnIssueLineWhenRqcFinish(Long sourceLineId, Long sourceDocId, Integer checkResult,
                                            BigDecimal qualifiedQuantity, BigDecimal unqualifiedQuantity);

}
