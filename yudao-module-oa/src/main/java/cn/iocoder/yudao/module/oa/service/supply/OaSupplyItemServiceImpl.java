package cn.iocoder.yudao.module.oa.service.supply;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.oa.controller.admin.supply.vo.item.*;
import cn.iocoder.yudao.module.oa.dal.dataobject.supply.*;
import cn.iocoder.yudao.module.oa.dal.mysql.supply.*;
import cn.iocoder.yudao.module.system.api.dept.DeptApi;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.context.annotation.Lazy;
import org.springframework.validation.annotation.Validated;

import java.util.*;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.oa.enums.ErrorCodeConstants.*;

/**
 * 办公用品 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class OaSupplyItemServiceImpl implements OaSupplyItemService {

    @Resource
    private OaSupplyItemMapper supplyItemMapper;

    @Resource
    @Lazy // 延迟加载，避免循环依赖
    private OaSupplyApplyService supplyApplyService;

    @Resource
    private DeptApi deptApi;

    @Override
    public Long createSupplyItem(OaSupplyItemSaveReqVO createReqVO) {
        // 1.1 校验物品编码
        validateSupplyItemNoUnique(null, createReqVO.getNo());
        // 1.2 校验所属部门
        deptApi.validateDeptList(Collections.singletonList(createReqVO.getDeptId()));

        // 2. 保存物品
        OaSupplyItemDO item = BeanUtils.toBean(createReqVO, OaSupplyItemDO.class);
        supplyItemMapper.insert(item);
        return item.getId();
    }

    @Override
    public void updateSupplyItem(OaSupplyItemSaveReqVO updateReqVO) {
        // 1.1 校验物品存在
        validateSupplyItemExists(updateReqVO.getId());
        // 1.2 校验物品编码
        validateSupplyItemNoUnique(updateReqVO.getId(), updateReqVO.getNo());
        // 1.3 校验所属部门
        deptApi.validateDeptList(Collections.singletonList(updateReqVO.getDeptId()));

        // 2. 更新物品
        supplyItemMapper.updateById(BeanUtils.toBean(updateReqVO, OaSupplyItemDO.class));
    }

    @Override
    public void deleteSupplyItem(Long id) {
        // 1. 校验物品存在
        validateSupplyItemExists(id);
        // 1.2 借出的物品归还完成后才允许删除
        if (supplyApplyService.getUnreturnedSupplyItemCount(id) > 0) {
            throw exception(SUPPLY_ITEM_NOT_RETURNED);
        }
        // 1.3 待发放的物品完成发放处理后才允许删除
        if (supplyApplyService.getPendingSupplyItemCount(id) > 0) {
            throw exception(SUPPLY_ITEM_PENDING_ISSUE);
        }

        // 2. 删除物品
        supplyItemMapper.deleteById(id);
    }

    @Override
    public OaSupplyItemDO getSupplyItem(Long id) {
        return supplyItemMapper.selectById(id);
    }

    @Override
    public PageResult<OaSupplyItemDO> getSupplyItemPage(OaSupplyItemPageReqVO pageReqVO) {
        return supplyItemMapper.selectPage(pageReqVO);
    }

    @Override
    public List<OaSupplyItemDO> getSupplyItemList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return supplyItemMapper.selectByIds(ids);
    }

    @Override
    public void stockInSupplyItem(OaSupplyItemStockInReqVO reqVO) {
        // 1. 校验物品存在
        validateSupplyItemExists(reqVO.getId());

        // 2. 增加库存
        updateSupplyItemStockQuantity(reqVO.getId(), reqVO.getQuantity());
    }

    @Override
    public void updateSupplyItemStockQuantity(Long id, Integer quantity) {
        // 1. 校验物品存在
        validateSupplyItemExists(id);

        // 2. 原子增减库存，扣减时不允许负库存
        if (supplyItemMapper.updateStockQuantity(id, quantity) == 0) {
            throw exception(SUPPLY_STOCK_INSUFFICIENT);
        }
    }

    /**
     * 校验办公用品存在
     *
     * @param id 编号
     * @return 办公用品
     */
    @SuppressWarnings("UnusedReturnValue")
    private OaSupplyItemDO validateSupplyItemExists(Long id) {
        OaSupplyItemDO item = supplyItemMapper.selectById(id);
        if (item == null) {
            throw exception(SUPPLY_ITEM_NOT_EXISTS);
        }
        return item;
    }

    /**
     * 校验物品编码唯一
     *
     * @param id 物品编号
     * @param no 物品编码
     */
    private void validateSupplyItemNoUnique(Long id, String no) {
        if (StrUtil.isBlank(no)) {
            return;
        }
        OaSupplyItemDO item = supplyItemMapper.selectByNo(no);
        if (item != null && ObjUtil.notEqual(item.getId(), id)) {
            throw exception(SUPPLY_ITEM_NO_DUPLICATE);
        }
    }

}
