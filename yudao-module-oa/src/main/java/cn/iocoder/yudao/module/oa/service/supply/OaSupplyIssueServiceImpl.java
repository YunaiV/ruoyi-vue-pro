package cn.iocoder.yudao.module.oa.service.supply;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.bpm.enums.task.BpmProcessInstanceStatusEnum;
import cn.iocoder.yudao.module.oa.controller.admin.supply.vo.issue.OaSupplyIssueReqVO;
import cn.iocoder.yudao.module.oa.controller.admin.supply.vo.issue.OaSupplyIssuePageReqVO;
import cn.iocoder.yudao.module.oa.controller.admin.supply.vo.issue.OaSupplyReturnReqVO;
import cn.iocoder.yudao.module.oa.dal.dataobject.supply.OaSupplyApplyDO;
import cn.iocoder.yudao.module.oa.dal.dataobject.supply.OaSupplyApplyItemDO;
import cn.iocoder.yudao.module.oa.dal.mysql.supply.OaSupplyApplyItemMapper;
import cn.iocoder.yudao.module.oa.enums.supply.OaSupplyApplyItemStatusEnum;
import cn.iocoder.yudao.module.oa.enums.supply.OaSupplyManageTypeEnum;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.module.oa.enums.ErrorCodeConstants.*;

/**
 * 用品领用发放 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class OaSupplyIssueServiceImpl implements OaSupplyIssueService {

    @Resource
    private OaSupplyApplyItemMapper supplyApplyItemMapper;

    @Resource
    private OaSupplyApplyService supplyApplyService;
    @Resource
    private OaSupplyItemService supplyItemService;

    @Resource
    private AdminUserApi adminUserApi;

    @Override
    public PageResult<OaSupplyApplyItemDO> getSupplyIssuePage(OaSupplyIssuePageReqVO reqVO) {
        // 1. 查询申请人筛选范围
        List<String> creators = null;
        if (StrUtil.isNotBlank(reqVO.getCreatorName())) {
            List<AdminUserRespDTO> users = adminUserApi.getUserListByNickname(reqVO.getCreatorName());
            if (CollUtil.isEmpty(users)) {
                return PageResult.empty();
            }
            creators = convertList(users, user -> user.getId().toString());
        }

        // 2. 查询领用明细分页
        return supplyApplyItemMapper.selectPage(reqVO, creators);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void issueSupply(OaSupplyIssueReqVO reqVO, Long userId) {
        // 1.1 校验明细待发放
        OaSupplyApplyItemDO item = validateSupplyApplyItemExists(reqVO.getId());
        if (ObjUtil.notEqual(item.getStatus(), OaSupplyApplyItemStatusEnum.PENDING.getStatus())) {
            throw exception(SUPPLY_APPLY_STATUS_INVALID);
        }
        // 1.2 校验申请已审批通过
        OaSupplyApplyDO apply = supplyApplyService.getSupplyApply(item.getApplyId());
        if (apply == null) {
            throw exception(SUPPLY_APPLY_NOT_EXISTS);
        }
        if (ObjUtil.notEqual(apply.getStatus(), BpmProcessInstanceStatusEnum.APPROVE.getStatus())) {
            throw exception(SUPPLY_APPLY_STATUS_INVALID);
        }

        // 2.1 一次发放后，借用品转为待归还，其他用品转为已领用
        Integer status = ObjUtil.equal(item.getManageType(), OaSupplyManageTypeEnum.BORROWABLE.getType())
                ? OaSupplyApplyItemStatusEnum.RETURN_PENDING.getStatus() : OaSupplyApplyItemStatusEnum.RECEIVED.getStatus();
        int count = supplyApplyItemMapper.updateByIdAndStatus(new OaSupplyApplyItemDO().setId(item.getId())
                .setIssuedQuantity(reqVO.getIssuedQuantity()).setIssueRemark(reqVO.getIssueRemark())
                .setIssueUserId(userId).setIssueTime(LocalDateTime.now()).setStatus(status), item.getStatus());
        if (count == 0) {
            throw exception(SUPPLY_APPLY_STATUS_INVALID);
        }
        // 2.2 扣减实际发放数量，库存不足时一并回滚
        supplyItemService.updateSupplyItemStockQuantity(item.getItemId(), -reqVO.getIssuedQuantity());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void returnSupply(OaSupplyReturnReqVO reqVO) {
        // 1.1 校验明细待归还
        OaSupplyApplyItemDO item = validateSupplyApplyItemExists(reqVO.getId());
        if (ObjUtil.notEqual(item.getStatus(), OaSupplyApplyItemStatusEnum.RETURN_PENDING.getStatus())) {
            throw exception(SUPPLY_APPLY_STATUS_INVALID);
        }
        // 1.2 本次归还不能超过剩余数量
        int quantity = item.getReturnedQuantity() + reqVO.getQuantity();
        if (quantity > item.getIssuedQuantity()) {
            throw exception(SUPPLY_APPLY_QTY_INVALID);
        }

        // 2.1 累计归还数量，全部归还后结束
        int count = supplyApplyItemMapper.updateByIdAndReturnedQuantity(new OaSupplyApplyItemDO().setId(item.getId())
                .setReturnedQuantity(quantity).setReturnRemark(reqVO.getReturnRemark())
                .setStatus(quantity == item.getIssuedQuantity() ? OaSupplyApplyItemStatusEnum.RETURNED.getStatus()
                        : OaSupplyApplyItemStatusEnum.RETURN_PENDING.getStatus()), item.getReturnedQuantity());
        if (count == 0) {
            throw exception(SUPPLY_APPLY_STATUS_INVALID);
        }
        // 2.2 将本次归还数量补回库存
        supplyItemService.updateSupplyItemStockQuantity(item.getItemId(), reqVO.getQuantity());
    }

    /**
     * 校验领用明细存在
     *
     * @param id 明细编号
     * @return 领用明细
     */
    private OaSupplyApplyItemDO validateSupplyApplyItemExists(Long id) {
        OaSupplyApplyItemDO item = supplyApplyItemMapper.selectById(id);
        if (item == null) {
            throw exception(SUPPLY_ISSUE_NOT_EXISTS);
        }
        return item;
    }

}
