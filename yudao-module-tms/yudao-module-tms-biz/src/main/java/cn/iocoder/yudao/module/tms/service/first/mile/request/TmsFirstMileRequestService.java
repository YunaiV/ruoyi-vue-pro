package cn.iocoder.yudao.module.tms.service.first.mile.request;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.tms.controller.admin.first.mile.request.vo.TmsFirstMileRequestAuditReqVO;
import cn.iocoder.yudao.module.tms.controller.admin.first.mile.request.vo.TmsFirstMileRequestPageReqVO;
import cn.iocoder.yudao.module.tms.controller.admin.first.mile.request.vo.TmsFirstMileRequestSaveReqVO;
import cn.iocoder.yudao.module.tms.controller.admin.first.mile.vo.req.TmsFirstMileSaveReqVO;
import cn.iocoder.yudao.module.tms.dal.dataobject.first.mile.request.TmsFirstMileRequestDO;
import cn.iocoder.yudao.module.tms.dal.dataobject.first.mile.request.item.TmsFirstMileRequestItemDO;
import cn.iocoder.yudao.module.tms.service.bo.TmsFirstMileRequestBO;
import jakarta.validation.Valid;

import java.util.List;
import java.util.Map;

/**
 * 头程单申请 Service 接口
 *
 * @author wdy
 */
public interface TmsFirstMileRequestService {

    /**
     * 创建头程单申请
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createFirstMileRequest(@Valid TmsFirstMileRequestSaveReqVO createReqVO);

    /**
     * 更新头程单申请
     *
     * @param updateReqVO 更新信息
     */
    void updateFirstMileRequest(@Valid TmsFirstMileRequestSaveReqVO updateReqVO);

    /**
     * 删除头程单申请
     *
     * @param id 编号
     */
    void deleteFirstMileRequest(Long id);

    /**
     * 获得头程单申请
     *
     * @param id 编号
     * @return 头程单申请
     */
    TmsFirstMileRequestDO getFirstMileRequest(Long id);


    /**
     * 获得头程申请单BO分页
     *
     * @param pageReqVO 分页查询
     * @return 头程申请单BO分页
     */
    PageResult<TmsFirstMileRequestBO> getFirstMileRequestBOPage(TmsFirstMileRequestPageReqVO pageReqVO);

    /**
     * 获得头程申请单BO
     *
     * @param id 主表编号
     * @return 头程申请单BO
     */
    TmsFirstMileRequestBO getFirstMileRequestBO(Long id);

    /**
     * 货单头程申请单主表DO
     */
    TmsFirstMileRequestDO validateFirstMileRequestExists(Long id);

    /**
     * 修改主单状态,3个状态 开关 订购 审核,审核意见
     */
    TmsFirstMileRequestDO updateFirstMileRequestStatus(Long id, Integer offStatus, Integer orderStatus, Integer auditStatus, String auditMsg);

    // ==================== 子表（头程申请表明细） ====================

    /**
     * 通过itemID拿到DO
     */
    TmsFirstMileRequestItemDO getFirstMileRequestItem(Long id);

    /**
     * 修改子单状态，开关、订购
     */
    TmsFirstMileRequestItemDO updateFirstMileRequestItemStatus(Long id, Integer openStatus, Integer orderStatus);

    /**
     * 获得头程申请表明细列表
     *
     * @param requestId 所属申请单ID
     * @return 头程申请表明细列表LIST
     */
    List<TmsFirstMileRequestItemDO> getFirstMileRequestItemListByRequestId(Long requestId);

    /**
     * 提交审核
     *
     * @param ids 头程申请单ID列表
     */
    void submitAudit(List<Long> ids);

    /**
     * 审核|反审核
     *
     * @param req vo
     */
    void review(TmsFirstMileRequestAuditReqVO req);

    /**
     * 启用/关闭申请单子项，自动更新主表状态
     */
    void switchTmsFirstMileOpenStatus(List<Long> itemIds, Boolean enable);

    /**
     * 获取最新的单号Code
     *
     * @return 单号Code
     */
    String getLatestCode();

    /**
     * 合并头程申请单
     *
     * @param createReqVO vo
     * @return 合并后的头程单编号
     */
    Long mergeFirstMileRequest(TmsFirstMileSaveReqVO createReqVO);

    /**
     * 获取头程申请表明细列表MAP
     *
     * @param requestIds 头程申请项IDs
     * @return map
     */
    Map<Long, TmsFirstMileRequestItemDO> getFirstMileRequestItemListMap(List<Long> requestIds);
}