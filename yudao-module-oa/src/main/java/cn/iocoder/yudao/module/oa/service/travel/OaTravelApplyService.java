package cn.iocoder.yudao.module.oa.service.travel;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import jakarta.validation.Valid;
import cn.iocoder.yudao.module.oa.controller.admin.travel.vo.apply.*;
import cn.iocoder.yudao.module.oa.dal.dataobject.travel.OaTravelApplyDO;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;

/**
 * 出差申请 Service 接口
 *
 * @author 芋道源码
 */
public interface OaTravelApplyService {

    /**
     * 创建出差申请草稿
     *
     * @param reqVO 单据内容
     * @param userId 当前用户编号
     * @return 单据编号
     */
    Long createTravelApply(@Valid OaTravelApplySaveReqVO reqVO, Long userId);

    /**
     * 更新出差申请草稿
     *
     * @param reqVO 单据内容
     * @param userId 当前用户编号
     */
    void updateTravelApply(@Valid OaTravelApplySaveReqVO reqVO, Long userId);

    /**
     * 提交已保存的出差申请
     *
     * @param submitReqVO 单据编号及发起人选择的审批人
     * @param userId 当前用户编号
     */
    void submitTravelApply(OaTravelApplySubmitReqVO submitReqVO, Long userId);

    /**
     * 撤回本人的审批中出差申请
     *
     * @param id 单据编号
     * @param userId 当前用户编号
     */
    void cancelTravelApply(Long id, Long userId);

    /**
     * 删除本人允许删除状态的出差申请
     *
     * @param id 单据编号
     * @param userId 当前用户编号
     */
    void deleteTravelApply(Long id, Long userId);

    /**
     * 获得出差申请详情
     *
     * @param id 单据编号
     * @return 单据
     */
    OaTravelApplyDO getTravelApply(Long id);

    /**
     * 获得本人出差申请分页
     *
     * @param userId 当前用户编号
     * @param reqVO 分页条件
     * @return 本人单据分页
     */
    PageResult<OaTravelApplyDO> getTravelApplyPage(Long userId, OaTravelApplyPageReqVO reqVO);

    /**
     * 获得用户审批通过的出差天数合计
     *
     * @param userIds 用户编号集合
     * @param startTime 申请开始时间范围，包含边界；跨月申请整笔计入开始月份
     * @return 用户编号与出差天数的映射，无申请的用户不返回
     */
    Map<Long, Integer> getApprovedTravelDaysMap(Collection<Long> userIds, LocalDateTime[] startTime);

    /**
     * 更新出差申请审批状态
     *
     * @param id 单据编号
     * @param status 审批状态
     */
    void updateTravelApplyStatus(Long id, Integer status);

    /**
     * 获得本人审批通过的出差申请
     *
     * @param userId 当前用户编号
     * @return 可关联的出差申请列表
     */
    List<OaTravelApplyDO> getApprovedTravelApplyList(Long userId);

    /**
     * 校验所选出差申请属于本人且已通过
     *
     * @param id 申请编号
     * @param userId 当前用户编号
     * @return 出差申请
     */
    OaTravelApplyDO validateApprovedTravelApply(Long id, Long userId);

    /**
     * 批量查询出差申请用于展示单号
     *
     * @param ids 申请编号集合
     * @return 出差申请列表
     */
    List<OaTravelApplyDO> getTravelApplyList(Collection<Long> ids);

    /**
     * 获得出差申请 Map
     *
     * @param ids 申请编号集合
     * @return 申请编号与申请的映射
     */
    default Map<Long, OaTravelApplyDO> getTravelApplyMap(Collection<Long> ids) {
        return convertMap(getTravelApplyList(ids), OaTravelApplyDO::getId);
    }

    /**
     * 关联报销审批通过后标记出差已报销
     *
     * @param id 申请编号
     */
    void updateTravelApplyReimburseStatus(Long id);

}
