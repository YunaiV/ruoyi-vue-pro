package cn.iocoder.yudao.module.tms.service.first.mile;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.tms.api.first.mile.dto.TmsFistMileItemUpdateDTO;
import cn.iocoder.yudao.module.tms.api.first.mile.dto.TmsFistMileUpdateDTO;
import cn.iocoder.yudao.module.tms.controller.admin.fee.vo.TmsFeeRespVO;
import cn.iocoder.yudao.module.tms.controller.admin.first.mile.vo.req.TmsFirstMileAuditReqVO;
import cn.iocoder.yudao.module.tms.controller.admin.first.mile.vo.req.TmsFirstMilePageReqVO;
import cn.iocoder.yudao.module.tms.controller.admin.first.mile.vo.req.TmsFirstMileSaveReqVO;
import cn.iocoder.yudao.module.tms.controller.admin.first.mile.vo.resp.TmsFirstMileRespVO;
import cn.iocoder.yudao.module.tms.dal.dataobject.first.mile.TmsFirstMileDO;
import cn.iocoder.yudao.module.tms.dal.dataobject.first.mile.item.TmsFirstMileItemDO;
import cn.iocoder.yudao.module.tms.dal.dataobject.first.mile.request.TmsFirstMileRequestDO;
import cn.iocoder.yudao.module.tms.service.bo.TmsFirstMileBO;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 头程单 Service 接口
 *
 * @author wdy
 */
public interface TmsFirstMileService {

    /**
     * 创建头程单
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createFirstMile(TmsFirstMileSaveReqVO createReqVO);

    /**
     * 更新头程单
     *
     * @param updateReqVO 更新信息
     */
    void updateFirstMile(TmsFirstMileSaveReqVO updateReqVO);

    /**
     * 删除头程单
     *
     * @param id 编号
     */
    void deleteFirstMile(Long id);

    /**
     * 校验是否存在
     *
     * @param id id
     * @return tmsFirstMileDO
     */
    TmsFirstMileDO validateFirstMileExists(Long id);

    /**
     * 校验明细行存在
     *
     * @param id 明细行ID
     * @return 明细行
     */
    TmsFirstMileItemDO validateFirstMileItemExists(Long id);

    /**
     * 批量校验明细行存在
     *
     * @param ids 明细行ID列表
     * @return 明细行列表
     */
    List<TmsFirstMileItemDO> validateFirstMileItemExists(List<Long> ids);

    /**
     * 获得头程单
     *
     * @param id 编号
     * @return 头程单
     */
    TmsFirstMileDO getFirstMile(Long id);

    /**
     * 获得头程单BO
     */
    TmsFirstMileBO getFirstMileBO(Long id);

    /**
     * 获得头程单分页BO
     *
     * @param pageReqVO 分页查询
     * @return 头程单分页
     */
    PageResult<TmsFirstMileBO> getFirstMileBOPage(TmsFirstMilePageReqVO pageReqVO);

    /**
     * 获取最新可用的单据编号
     *
     * @return 单据编号
     */
    String getLatestCode();

    /**
     * 提交审核
     *
     * @param ids 头程单IDs
     */
    void submitAudit(@Size(min = 1, message = "至少提交审核一个头程单") List<Long> ids);

    /**
     * 审核|反审核
     *
     * @param req vo
     */
    void review(TmsFirstMileAuditReqVO req);
    // ==================== 子表（头程单明细） ====================

    /**
     * 获得头程单明细列表
     *
     * @param firstMileId 头程主表ID
     * @return 头程单明细列表
     */
    List<TmsFirstMileItemDO> getFirstMileItemListByFirstMileId(Long firstMileId);

    /**
     * 通过申请项ID获得 头程明细列表
     */
    List<TmsFirstMileItemDO> getFirstMileItemListByRequestItemId(Long requestItemId);

    // ==================== 子表（出运订单费用明细） ====================
    //装配库存公司
    void assembleTmsFirstMileStockRespVO(TmsFirstMileRespVO tmsFirstMileRespVO);

    /**
     * 根据源ID获取费用列表
     *
     * @param sourceId 源ID
     * @return 费用列表
     */
    List<TmsFeeRespVO> getFeeListBySourceId(Long sourceId);

    /**
     * 更新头程单状态
     *
     * @param tmsFistMileUpdateDTO dto
     */
    void updateFirstMileStatus(TmsFistMileUpdateDTO tmsFistMileUpdateDTO);

    /**
     * 更新头程单明细出库信息
     *
     * @param dto 出库信息
     */
    void updateFirstMileItemOutbound(TmsFistMileItemUpdateDTO dto);

    /**
     * 批量查询申请单MAP
     *
     * @param requestItemIds 申请项IDS
     * @return 申请单(主表)的MAP
     */
    Map<Long, TmsFirstMileRequestDO> getRequestMap(Set<Long> requestItemIds);
}