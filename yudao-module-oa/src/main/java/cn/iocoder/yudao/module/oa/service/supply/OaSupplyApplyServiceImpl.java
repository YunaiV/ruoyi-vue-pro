package cn.iocoder.yudao.module.oa.service.supply;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.common.util.object.ObjectUtils;
import cn.iocoder.yudao.module.bpm.api.task.BpmProcessInstanceApi;
import cn.iocoder.yudao.module.bpm.api.task.dto.BpmProcessInstanceCreateReqDTO;
import cn.iocoder.yudao.module.bpm.enums.task.BpmProcessInstanceStatusEnum;
import cn.iocoder.yudao.module.oa.controller.admin.supply.vo.apply.*;
import cn.iocoder.yudao.module.oa.controller.admin.supply.vo.issue.*;
import cn.iocoder.yudao.module.oa.dal.dataobject.supply.*;
import cn.iocoder.yudao.module.oa.dal.mysql.supply.*;
import cn.iocoder.yudao.module.oa.dal.redis.no.OaNoRedisDAO;
import cn.iocoder.yudao.module.oa.enums.BpmModelConstants;
import cn.iocoder.yudao.module.oa.enums.supply.*;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.*;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.*;
import static cn.iocoder.yudao.module.oa.enums.ErrorCodeConstants.*;

/**
 * 用品领用申请 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class OaSupplyApplyServiceImpl implements OaSupplyApplyService {

    @Resource
    private OaSupplyApplyMapper supplyApplyMapper;
    @Resource
    private OaSupplyApplyItemMapper supplyApplyItemMapper;
    @Resource
    private OaSupplyItemService supplyItemService;
    @Resource
    private AdminUserApi adminUserApi;
    @Resource
    private BpmProcessInstanceApi processInstanceApi;
    @Resource
    private OaNoRedisDAO noRedisDAO;

    @Override
    public Long getUnreturnedSupplyItemCount(Long itemId) {
        return supplyApplyItemMapper.selectCountByItemIdAndUnreturned(itemId, OaSupplyManageTypeEnum.BORROWABLE.getType());
    }

    @Override
    public Long getPendingSupplyItemCount(Long itemId) {
        return supplyApplyItemMapper.selectCountByItemIdAndStatus(itemId, OaSupplyApplyItemStatusEnum.PENDING.getStatus());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createSupplyApply(OaSupplyApplySaveReqVO createReqVO, Long userId) {
        // 1.1 校验领用物品有效性，必填信息由保存 VO 校验
        List<OaSupplyApplyItemDO> applyItems = BeanUtils.toBean(createReqVO.getItems(), OaSupplyApplyItemDO.class);
        Map<Long, OaSupplyItemDO> items = validateSupplyApplyItems(applyItems);
        // 1.2 查询申请人部门
        AdminUserRespDTO user = adminUserApi.validateUser(userId);
        // 1.3 生成并校验申请单号
        String no = noRedisDAO.generate(OaNoRedisDAO.SUPPLY_APPLY_NO_PREFIX);
        if (supplyApplyMapper.selectByNo(no) != null) {
            throw exception(SUPPLY_APPLY_NO_DUPLICATE);
        }

        // 2.1 保存草稿，创建人由公共审计字段填写
        OaSupplyApplyDO apply = BeanUtils.toBean(createReqVO, OaSupplyApplyDO.class).setId(null)
                .setNo(no).setDeptId(user.getDeptId()).setStatus(BpmProcessInstanceStatusEnum.NOT_START.getStatus());
        supplyApplyMapper.insert(apply);
        // 2.2 保存领用明细
        saveSupplyApplyItems(apply.getId(), applyItems, items);
        return apply.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSupplyApply(OaSupplyApplySaveReqVO updateReqVO, Long userId) {
        // 1.1 校验本人可编辑申请
        OaSupplyApplyDO apply = validateSupplyApplyExists(updateReqVO.getId());
        validateSupplyApplyOwner(apply, userId);
        validateSupplyApplyEditable(apply);
        // 1.2 校验新的领用明细
        List<OaSupplyApplyItemDO> applyItems = BeanUtils.toBean(updateReqVO.getItems(), OaSupplyApplyItemDO.class);
        Map<Long, OaSupplyItemDO> items = validateSupplyApplyItems(applyItems);

        // 2.1 更新申请内容
        supplyApplyMapper.updateById(BeanUtils.toBean(updateReqVO, OaSupplyApplyDO.class));
        // 2.2 重新保存草稿明细
        supplyApplyItemMapper.deleteByApplyId(apply.getId());
        saveSupplyApplyItems(apply.getId(), applyItems, items);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteSupplyApply(Long id, Long userId) {
        // 1.1 校验申请归属
        OaSupplyApplyDO apply = validateSupplyApplyExists(id);
        validateSupplyApplyOwner(apply, userId);
        // 1.2 校验申请可编辑
        validateSupplyApplyEditable(apply);

        // 2. 删除申请及明细
        supplyApplyItemMapper.deleteByApplyId(id);
        supplyApplyMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String submitSupplyApply(Long id, Long userId) {
        // 1.1 校验申请归属及状态
        OaSupplyApplyDO apply = validateSupplyApplyExists(id);
        validateSupplyApplyOwner(apply, userId);
        validateSupplyApplyEditable(apply);
        // 1.2 校验领用物品仍然有效，必填信息已在保存时校验，提交不扣减库存
        List<OaSupplyApplyItemDO> items = supplyApplyItemMapper.selectListByApplyId(id);
        validateSupplyApplyItems(items);

        // 2. 更新审批中状态
        int count = supplyApplyMapper.updateStatusAndClearProcessInstanceId(id, apply.getStatus(), BpmProcessInstanceStatusEnum.RUNNING.getStatus());
        if (count == 0) {
            throw exception(SUPPLY_APPLY_STATUS_INVALID);
        }

        // 3.1 发起审批
        String processInstanceId = processInstanceApi.createProcessInstance(userId,
                new BpmProcessInstanceCreateReqDTO().setProcessDefinitionKey(BpmModelConstants.SUPPLY_APPLY)
                        .setBusinessKey(id.toString()).setVariables(new HashMap<>()));
        // 3.2 绑定流程编号
        supplyApplyMapper.updateById(new OaSupplyApplyDO().setId(id).setProcessInstanceId(processInstanceId));
        return processInstanceId;
    }

    @Override
    public void cancelSupplyApply(Long id, Long userId) {
        // 1.1 校验申请归属
        OaSupplyApplyDO apply = validateSupplyApplyExists(id);
        validateSupplyApplyOwner(apply, userId);
        // 1.2 校验审批中状态
        if (ObjUtil.notEqual(apply.getStatus(), BpmProcessInstanceStatusEnum.RUNNING.getStatus())) {
            throw exception(SUPPLY_APPLY_STATUS_INVALID);
        }

        // 2. 取消流程，由监听器回写结果
        processInstanceApi.cancelProcessInstanceByStartUser(userId, apply.getProcessInstanceId(), "申请人取消用品申请");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSupplyApplyStatus(Long id, String processInstanceId, Integer status) {
        // 1. 校验审批结果属于当前申请
        OaSupplyApplyDO apply = validateSupplyApplyExists(id);
        // 忽略不属于当前流程的审批结果
        if (apply.getProcessInstanceId() != null && ObjUtil.notEqual(apply.getProcessInstanceId(), processInstanceId)) {
            return;
        }

        // 2.1 保存审批状态
        supplyApplyMapper.updateById(new OaSupplyApplyDO().setId(id).setStatus(status));
        // 2.2 审批通过后允许发放，重复回调不重置已发放明细
        if (ObjUtil.equal(status, BpmProcessInstanceStatusEnum.APPROVE.getStatus()) && ObjUtil.notEqual(apply.getStatus(), status)) {
            supplyApplyItemMapper.updateStatusByApplyId(id, OaSupplyApplyItemStatusEnum.PENDING.getStatus());
        }
    }

    @Override
    public OaSupplyApplyDO getSupplyApply(Long id) {
        return supplyApplyMapper.selectById(id);
    }

    @Override
    public PageResult<OaSupplyApplyDO> getSupplyApplyPage(OaSupplyApplyPageReqVO reqVO, Long userId) {
        return supplyApplyMapper.selectPage(reqVO, userId);
    }

    @Override
    public List<OaSupplyApplyDO> getSupplyApplyList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return supplyApplyMapper.selectByIds(ids);
    }

    @Override
    public List<OaSupplyApplyItemDO> getSupplyApplyItemList(Long applyId) {
        return supplyApplyItemMapper.selectListByApplyId(applyId);
    }


    /**
     * 校验用品申请存在
     *
     * @param id 编号
     * @return 用品申请
     */
    private OaSupplyApplyDO validateSupplyApplyExists(Long id) {
        OaSupplyApplyDO apply = supplyApplyMapper.selectById(id);
        if (apply == null) {
            throw exception(SUPPLY_APPLY_NOT_EXISTS);
        }
        return apply;
    }

    /**
     * 校验申请归属
     *
     * @param apply 申请
     * @param userId 操作人编号
     */
    private void validateSupplyApplyOwner(OaSupplyApplyDO apply, Long userId) {
        if (ObjUtil.notEqual(apply.getCreator(), userId.toString())) {
            throw exception(SUPPLY_APPLY_ACCESS_DENIED);
        }
    }

    /**
     * 校验申请可编辑
     *
     * @param apply 申请
     */
    private void validateSupplyApplyEditable(OaSupplyApplyDO apply) {
        // 草稿、驳回和取消的申请允许编辑
        if (ObjectUtils.notEqualsAny(apply.getStatus(), BpmProcessInstanceStatusEnum.NOT_START.getStatus(),
                BpmProcessInstanceStatusEnum.REJECT.getStatus(), BpmProcessInstanceStatusEnum.CANCEL.getStatus())) {
            throw exception(SUPPLY_APPLY_STATUS_INVALID);
        }
    }

    /**
     * 校验领用物品有效且不重复
     *
     * @param items 领用明细
     * @return 物品映射
     */
    private Map<Long, OaSupplyItemDO> validateSupplyApplyItems(List<OaSupplyApplyItemDO> items) {
        if (CollUtil.isEmpty(items)) {
            return Collections.emptyMap();
        }
        Set<Long> ids = convertSet(items, OaSupplyApplyItemDO::getItemId);
        Map<Long, OaSupplyItemDO> itemMap = supplyItemService.getSupplyItemMap(ids);
        if (ids.size() != items.size() || itemMap.size() != ids.size()) {
            throw exception(SUPPLY_APPLY_ITEM_INVALID);
        }
        for (OaSupplyApplyItemDO item : items) {
            // TODO DONE @AI：用品启用状态使用 CommonStatusEnum，避免魔法值
            if (ObjUtil.notEqual(itemMap.get(item.getItemId()).getStatus(), CommonStatusEnum.ENABLE.getStatus())) {
                throw exception(SUPPLY_APPLY_ITEM_INVALID);
            }
        }
        return itemMap;
    }

    /**
     * 保存申请明细
     *
     * @param applyId 申请编号
     * @param applyItems 领用明细
     * @param items 物品映射
     */
    private void saveSupplyApplyItems(Long applyId, List<OaSupplyApplyItemDO> applyItems,
                                      Map<Long, OaSupplyItemDO> items) {
        if (CollUtil.isEmpty(applyItems)) {
            return;
        }
        for (OaSupplyApplyItemDO applyItem : applyItems) {
            OaSupplyItemDO item = items.get(applyItem.getItemId());
            applyItem.setApplyId(applyId).setItemName(item.getName()).setModel(item.getModel())
                    .setUnit(item.getUnit()).setManageType(item.getManageType())
                    .setIssuedQuantity(0).setReturnedQuantity(0).setStatus(OaSupplyApplyItemStatusEnum.APPLYING.getStatus());
            supplyApplyItemMapper.insert(applyItem);
        }
    }

}
