package cn.iocoder.yudao.module.oa.service.supply;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.oa.controller.admin.supply.vo.apply.*;
import cn.iocoder.yudao.module.oa.controller.admin.supply.vo.issue.*;
import cn.iocoder.yudao.module.oa.dal.dataobject.supply.*;

import javax.validation.Valid;
import java.util.*;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;

/**
 * 用品领用申请 Service 接口
 *
 * @author 芋道源码
 */
public interface OaSupplyApplyService {

    /**
     * 获得物品借出未归还的明细数量
     *
     * @param itemId 物品编号
     * @return 借出未归还的明细数量
     */
    Long getUnreturnedSupplyItemCount(Long itemId);

    /**
     * 获得物品待发放的明细数量
     *
     * @param itemId 物品编号
     * @return 待发放的明细数量
     */
    Long getPendingSupplyItemCount(Long itemId);

    /**
     * 创建用品领用申请
     *
     * @param createReqVO 创建信息
     * @param userId 操作人编号
     * @return 用品领用申请编号
     */
    Long createSupplyApply(@Valid OaSupplyApplySaveReqVO createReqVO, Long userId);

    /**
     * 更新用品领用申请
     *
     * @param updateReqVO 更新信息
     * @param userId 操作人编号
     */
    void updateSupplyApply(@Valid OaSupplyApplySaveReqVO updateReqVO, Long userId);

    /**
     * 删除用品领用申请
     *
     * @param id 编号
     * @param userId 操作人编号
     */
    void deleteSupplyApply(Long id, Long userId);

    /**
     * 提交用品领用申请
     *
     * @param id 编号
     * @param userId 操作人编号
     * @return 流程实例编号
     */
    String submitSupplyApply(Long id, Long userId);

    /**
     * 取消用品领用申请
     *
     * @param id 编号
     * @param userId 操作人编号
     */
    void cancelSupplyApply(Long id, Long userId);

    /**
     * 回写用品申请审批结果
     *
     * @param id 编号
     * @param processInstanceId 流程实例编号
     * @param status 审批状态
     */
    void updateSupplyApplyStatus(Long id, String processInstanceId, Integer status);

    /**
     * 获得用品领用申请
     *
     * @param id 编号
     * @return 用品领用申请
     */
    OaSupplyApplyDO getSupplyApply(Long id);

    /**
     * 获得本人用品领用申请分页
     *
     * @param reqVO 请求参数
     * @param userId 操作人编号
     * @return 本人用品领用申请分页
     */
    PageResult<OaSupplyApplyDO> getSupplyApplyPage(OaSupplyApplyPageReqVO reqVO, Long userId);

    /**
     * 获得用品申请列表
     *
     * @param ids 编号集合
     * @return 用品申请列表
     */
    List<OaSupplyApplyDO> getSupplyApplyList(Collection<Long> ids);

    /**
     * 获得用品申请映射
     *
     * @param ids 编号集合
     * @return 用品申请映射
     */
    default Map<Long, OaSupplyApplyDO> getSupplyApplyMap(Collection<Long> ids) {
        List<OaSupplyApplyDO> applies = getSupplyApplyList(ids);
        return convertMap(applies, OaSupplyApplyDO::getId);
    }

    /**
     * 获得用品申请明细列表
     *
     * @param applyId 申请编号
     * @return 用品申请明细列表
     */
    List<OaSupplyApplyItemDO> getSupplyApplyItemList(Long applyId);

}
