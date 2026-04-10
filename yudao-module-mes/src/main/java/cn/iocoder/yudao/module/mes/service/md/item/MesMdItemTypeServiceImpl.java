package cn.iocoder.yudao.module.mes.service.md.item;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.mes.controller.admin.md.item.vo.type.MesMdItemTypeListReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.md.item.vo.type.MesMdItemTypeSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.item.MesMdItemTypeDO;
import cn.iocoder.yudao.module.mes.dal.mysql.md.item.MesMdItemTypeMapper;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.*;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.mes.enums.ErrorCodeConstants.*;

/**
 * MES 物料产品分类 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class MesMdItemTypeServiceImpl implements MesMdItemTypeService {

    @Resource
    private MesMdItemTypeMapper itemTypeMapper;

    @Resource
    @Lazy // 延迟加载，避免循环依赖
    private MesMdItemService itemService;

    @Override
    public Long createItemType(MesMdItemTypeSaveReqVO createReqVO) {
        // 校验数据
        validateItemTypeSaveData(createReqVO);

        // 插入
        MesMdItemTypeDO itemType = BeanUtils.toBean(createReqVO, MesMdItemTypeDO.class);
        itemTypeMapper.insert(itemType);
        return itemType.getId();
    }

    @Override
    public void updateItemType(MesMdItemTypeSaveReqVO updateReqVO) {
        // 校验存在
        validateItemTypeExists(updateReqVO.getId());
        // 校验数据
        validateItemTypeSaveData(updateReqVO);

        // 更新
        MesMdItemTypeDO updateObj = BeanUtils.toBean(updateReqVO, MesMdItemTypeDO.class);
        itemTypeMapper.updateById(updateObj);
    }

    private void validateItemTypeSaveData(MesMdItemTypeSaveReqVO reqVO) {
        // 校验父分类编号的有效性
        validateParentItemType(reqVO.getId(), reqVO.getParentId());
        // 校验分类名称的唯一性
        validateItemTypeNameUnique(reqVO.getId(), reqVO.getParentId(), reqVO.getName());
        // 校验分类编码的唯一性
        validateItemTypeCodeUnique(reqVO.getId(), reqVO.getParentId(), reqVO.getCode());
    }

    @Override
    public void deleteItemType(Long id) {
        // 1.1 校验存在
        validateItemTypeExists(id);
        // 1.2 校验是否有子分类
        if (itemTypeMapper.selectCountByParentId(id) > 0) {
            throw exception(MD_ITEM_TYPE_EXITS_CHILDREN);
        }
        // 1.3 校验是否有物料
        if (itemService.getItemCountByItemTypeId(id) > 0) {
            throw exception(MD_ITEM_TYPE_EXITS_ITEM);
        }

        // 2. 删除
        itemTypeMapper.deleteById(id);
    }

    private void validateItemTypeExists(Long id) {
        if (itemTypeMapper.selectById(id) == null) {
            throw exception(MD_ITEM_TYPE_NOT_EXISTS);
        }
    }

    private void validateParentItemType(Long id, Long parentId) {
        if (parentId == null || MesMdItemTypeDO.PARENT_ID_ROOT.equals(parentId)) {
            return;
        }
        // 1. 不能设置自己为父分类
        if (Objects.equals(id, parentId)) {
            throw exception(MD_ITEM_TYPE_PARENT_ERROR);
        }
        // 2. 父分类不存在
        MesMdItemTypeDO parentItemType = itemTypeMapper.selectById(parentId);
        if (parentItemType == null) {
            throw exception(MD_ITEM_TYPE_PARENT_NOT_EXITS);
        }
        // 3. 递归校验父分类，如果父分类是自己的子分类，则报错，避免形成环路
        if (id == null) { // id 为空，说明新增，不需要考虑环路
            return;
        }
        for (int i = 0; i < Short.MAX_VALUE; i++) {
            // 3.1 校验环路
            parentId = parentItemType.getParentId();
            if (Objects.equals(id, parentId)) {
                throw exception(MD_ITEM_TYPE_PARENT_IS_CHILD);
            }
            // 3.2 继续递归下一级父分类
            if (parentId == null || MesMdItemTypeDO.PARENT_ID_ROOT.equals(parentId)) {
                break;
            }
            parentItemType = itemTypeMapper.selectById(parentId);
            if (parentItemType == null) {
                break;
            }
        }
    }

    private void validateItemTypeNameUnique(Long id, Long parentId, String name) {
        MesMdItemTypeDO itemType = itemTypeMapper.selectByParentIdAndName(parentId, name);
        if (itemType == null) {
            return;
        }
        if (ObjUtil.notEqual(itemType.getId(), id)) {
            throw exception(MD_ITEM_TYPE_NAME_DUPLICATE);
        }
    }

    private void validateItemTypeCodeUnique(Long id, Long parentId, String code) {
        MesMdItemTypeDO itemType = itemTypeMapper.selectByParentIdAndCode(parentId, code);
        if (itemType == null) {
            return;
        }
        if (ObjUtil.notEqual(itemType.getId(), id)) {
            throw exception(MD_ITEM_TYPE_CODE_DUPLICATE);
        }
    }

    @Override
    public MesMdItemTypeDO getItemType(Long id) {
        return itemTypeMapper.selectById(id);
    }

    @Override
    public List<MesMdItemTypeDO> getItemTypeList(MesMdItemTypeListReqVO listReqVO) {
        return itemTypeMapper.selectList(listReqVO);
    }

    @Override
    public List<MesMdItemTypeDO> getItemTypeList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return itemTypeMapper.selectByIds(ids);
    }

    @Override
    public List<MesMdItemTypeDO> getItemTypeChildrenList(Long parentId) {
        List<MesMdItemTypeDO> allList = itemTypeMapper.selectList(); // 分类总量小，全量查
        // 递归收集所有子分类
        List<MesMdItemTypeDO> result = new ArrayList<>();
        collectItemTypeChildren(result, parentId, allList);
        return result;
    }

    /**
     * 递归收集所有子分类
     */
    private void collectItemTypeChildren(List<MesMdItemTypeDO> result, Long parentId, List<MesMdItemTypeDO> allList) {
        for (MesMdItemTypeDO itemType : allList) {
            if (Objects.equals(itemType.getParentId(), parentId)) {
                result.add(itemType);
                collectItemTypeChildren(result, itemType.getId(), allList);
            }
        }
    }

}
