package cn.iocoder.yudao.module.fms.service.config;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsAuxiliaryCombinationDO;
import cn.iocoder.yudao.module.fms.dal.dataobject.voucher.FmsVoucherEntryDO;
import cn.iocoder.yudao.module.fms.dal.mysql.config.FmsAuxiliaryCombinationMapper;
import javax.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.anyMatch;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;

/**
 * FMS 辅助核算组合 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class FmsAuxiliaryCombinationServiceImpl implements FmsAuxiliaryCombinationService {

    @Resource
    private FmsAuxiliaryCombinationMapper auxiliaryCombinationMapper;

    @Override
    public void setAuxiliaryCombinationIds(Long accountSetId, List<FmsVoucherEntryDO> entries) {
        // 1. 一次加载账套已有的辅助核算组合
        List<FmsAuxiliaryCombinationDO> combinations = auxiliaryCombinationMapper.selectListByAccountSetId(accountSetId);

        // 2. 逐条复用或创建组合并回填分录
        for (FmsVoucherEntryDO entry : entries) {
            // 2.1 无辅助核算项目时，清空分录的组合编号
            if (CollUtil.isEmpty(entry.getAuxiliaries())) {
                entry.setAssistCombinationId(null);
                continue;
            }
            // 2.2 转换辅助核算组合明细
            List<FmsAuxiliaryCombinationDO.AuxiliaryItem> items = convertList(entry.getAuxiliaries(),
                    auxiliary -> FmsAuxiliaryCombinationDO.AuxiliaryItem.builder()
                            .type(auxiliary.getType()).typeId(auxiliary.getTypeId())
                            .itemId(auxiliary.getItemId()).name(auxiliary.getName()).build());
            // 2.3 复用或创建辅助核算组合
            FmsAuxiliaryCombinationDO combination = CollUtil.findOne(combinations, item ->
                    ObjUtil.equal(item.getSubjectId(), entry.getSubjectId()) && isSameCombination(item.getItems(), items));
            if (combination == null) {
                combination = new FmsAuxiliaryCombinationDO().setAccountSetId(accountSetId)
                        .setSubjectId(entry.getSubjectId()).setItems(items);
                auxiliaryCombinationMapper.insert(combination);
                combinations.add(combination);
            }
            // 2.4 回填分录的辅助核算组合编号
            entry.setAssistCombinationId(combination.getId());
        }
    }

    @Override
    public FmsAuxiliaryCombinationDO saveAuxiliaryCombination(Long accountSetId, Long subjectId,
                                                              List<FmsAuxiliaryCombinationDO.AuxiliaryItem> items) {
        // 1. 查询已有组合
        FmsAuxiliaryCombinationDO combination = CollUtil.findOne(
                auxiliaryCombinationMapper.selectListByAccountSetId(accountSetId),
                item -> ObjUtil.equal(item.getSubjectId(), subjectId)
                        && isSameCombination(item.getItems(), items));
        if (combination != null) {
            return combination;
        }

        // 2. 创建组合
        combination = new FmsAuxiliaryCombinationDO().setAccountSetId(accountSetId)
                .setSubjectId(subjectId).setItems(items);
        auxiliaryCombinationMapper.insert(combination);
        return combination;
    }

    @Override
    public Long getAuxiliaryCombinationCountBySubjectIds(Long accountSetId, Collection<Long> subjectIds) {
        return auxiliaryCombinationMapper.selectCountByAccountSetIdAndSubjectIds(accountSetId, subjectIds);
    }

    @Override
    public void updateAuxiliaryCombinationSubject(Long accountSetId, Long subjectId, Long targetSubjectId) {
        auxiliaryCombinationMapper.updateSubject(accountSetId, subjectId,
                new FmsAuxiliaryCombinationDO().setSubjectId(targetSubjectId));
    }

    @Override
    public void deleteAuxiliaryCombinationByAuxiliaryItemIds(
            Long accountSetId, Collection<Long> auxiliaryItemIds) {
        // 1. 校验辅助核算项目编号数组非空
        if (CollUtil.isEmpty(auxiliaryItemIds)) {
            return;
        }

        // 2. 查询包含指定辅助核算项目的组合
        Set<Long> auxiliaryItemIdSet = new HashSet<>(auxiliaryItemIds);
        List<FmsAuxiliaryCombinationDO> combinations = auxiliaryCombinationMapper
                .selectListByAccountSetId(accountSetId);
        List<Long> combinationIds = convertList(combinations, FmsAuxiliaryCombinationDO::getId,
                combination -> CollUtil.isNotEmpty(combination.getItems())
                        && anyMatch(combination.getItems(),
                        item -> auxiliaryItemIdSet.contains(item.getItemId())));

        // 3. 删除辅助核算组合
        if (CollUtil.isNotEmpty(combinationIds)) {
            auxiliaryCombinationMapper.deleteByIds(combinationIds);
        }
    }

    /**
     * 判断两个辅助核算组合是否相同
     *
     * @param left 左侧辅助核算项目数组
     * @param right 右侧辅助核算项目数组
     * @return 是否相同
     */
    private boolean isSameCombination(List<FmsAuxiliaryCombinationDO.AuxiliaryItem> left,
                                      List<FmsAuxiliaryCombinationDO.AuxiliaryItem> right) {
        // 项目数量不同，不是同一组合
        if (CollUtil.size(left) != CollUtil.size(right)) {
            return false;
        }
        // 按辅助核算类别和项目的固定顺序逐项比较
        for (int index = 0; index < CollUtil.size(left); index++) {
            FmsAuxiliaryCombinationDO.AuxiliaryItem leftItem = left.get(index);
            FmsAuxiliaryCombinationDO.AuxiliaryItem rightItem = right.get(index);
            if (ObjUtil.notEqual(leftItem.getTypeId(), rightItem.getTypeId())
                    || ObjUtil.notEqual(leftItem.getItemId(), rightItem.getItemId())) {
                return false;
            }
        }
        return true;
    }

}
