package cn.iocoder.yudao.module.mes.service.wm.returnsales;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.mes.controller.admin.wm.returnsales.vo.line.MesWmReturnSalesLinePageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.wm.returnsales.vo.line.MesWmReturnSalesLineSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.returnsales.MesWmReturnSalesLineDO;
import jakarta.validation.Valid;

import java.math.BigDecimal;
import java.util.List;

/**
 * MES 销售退货单行 Service 接口
 *
 * @author 芋道源码
 */
public interface MesWmReturnSalesLineService {

    /**
     * 创建销售退货单行
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createReturnSalesLine(@Valid MesWmReturnSalesLineSaveReqVO createReqVO);

    /**
     * 更新销售退货单行
     *
     * @param updateReqVO 更新信息
     */
    void updateReturnSalesLine(@Valid MesWmReturnSalesLineSaveReqVO updateReqVO);

    /**
     * 删除销售退货单行
     *
     * @param id 编号
     */
    void deleteReturnSalesLine(Long id);

    /**
     * 获得销售退货单行
     *
     * @param id 编号
     * @return 销售退货单行
     */
    MesWmReturnSalesLineDO getReturnSalesLine(Long id);

    /**
     * 获得销售退货单行分页
     *
     * @param pageReqVO 分页查询
     * @return 销售退货单行分页
     */
    PageResult<MesWmReturnSalesLineDO> getReturnSalesLinePage(MesWmReturnSalesLinePageReqVO pageReqVO);

    /**
     * 根据退货单ID获取行列表
     *
     * @param returnId 退货单ID
     * @return 行列表
     */
    List<MesWmReturnSalesLineDO> getReturnSalesLineListByReturnId(Long returnId);

    /**
     * 根据退货单ID删除行
     *
     * @param returnId 退货单ID
     */
    void deleteReturnSalesLineByReturnId(Long returnId);

    /**
     * 校验销售退货单行是否存在
     *
     * @param id 编号
     * @return 销售退货单行
     */
    MesWmReturnSalesLineDO validateReturnSalesLineExists(Long id);

    /**
     * 批量更新退货单行的质量状态
     *
     * @param returnId 退货单ID
     * @param qualityStatus 质量状态
     */
    void updateQualityStatusByReturnId(Long returnId, Integer qualityStatus);

    /**
     * RQC 检验完成后，根据合格品/不合格品数量更新销售退货单行
     *
     * <p>拆分逻辑：
     * <ul>
     *     <li>全部合格：直接更新行的 qualityStatus 为合格</li>
     *     <li>全部不合格：直接更新行的 qualityStatus 为不合格</li>
     *     <li>部分合格部分不合格：拆分原行（原行改为合格+数量=合格数，新增一行不合格+数量=不合格数）</li>
     * </ul>
     * 同时检查退货单下所有行是否都已检验完毕，若全检完则将退货单状态设为待上架。
     *
     * @param sourceLineId        来源行 ID
     * @param sourceDocId         来源退货单 ID
     * @param checkResult         检验结果
     * @param qualifiedQuantity   合格品数量
     * @param unqualifiedQuantity 不合格品数量
     */
    void updateReturnSalesLineWhenRqcFinish(Long sourceLineId, Long sourceDocId, Integer checkResult,
                                            BigDecimal qualifiedQuantity, BigDecimal unqualifiedQuantity);

}
