package cn.iocoder.yudao.module.wms.service.md.item;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.wms.controller.admin.md.item.vo.category.WmsItemCategoryListReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.md.item.vo.category.WmsItemCategorySaveReqVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.md.item.WmsItemCategoryDO;
import cn.iocoder.yudao.module.wms.dal.mysql.md.item.WmsItemCategoryMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.*;

/**
 * WMS 商品分类 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class WmsItemCategoryServiceImpl implements WmsItemCategoryService {

    @Resource
    private WmsItemCategoryMapper categoryMapper;

    @Resource
    private WmsItemService itemService;

    @Override
    public Long createItemCategory(WmsItemCategorySaveReqVO createReqVO) {
        // 校验数据
        validateCategorySaveData(createReqVO);

        // 插入
        WmsItemCategoryDO category = BeanUtils.toBean(createReqVO, WmsItemCategoryDO.class);
        categoryMapper.insert(category);
        return category.getId();
    }

    @Override
    public void updateItemCategory(WmsItemCategorySaveReqVO updateReqVO) {
        // 校验存在
        validateItemCategoryExists(updateReqVO.getId());
        // 校验数据
        validateCategorySaveData(updateReqVO);

        // 更新
        WmsItemCategoryDO updateObj = BeanUtils.toBean(updateReqVO, WmsItemCategoryDO.class);
        categoryMapper.updateById(updateObj);
    }

    private void validateCategorySaveData(WmsItemCategorySaveReqVO reqVO) {
        validateCategoryCodeUnique(reqVO.getId(), reqVO.getCode());
        validateCategoryNameUnique(reqVO.getId(), reqVO.getParentId(), reqVO.getName());
        validateParentCategory(reqVO.getId(), reqVO.getParentId());
    }

    private void validateCategoryCodeUnique(Long id, String code) {
        WmsItemCategoryDO category = categoryMapper.selectByCode(code);
        if (category == null) {
            return;
        }
        // 如果 id 为空，说明新增；和数据库已有 code 重复
        if (id == null) {
            throw exception(ITEM_CATEGORY_CODE_DUPLICATE);
        }
        if (ObjectUtil.notEqual(category.getId(), id)) {
            throw exception(ITEM_CATEGORY_CODE_DUPLICATE);
        }
    }

    private void validateCategoryNameUnique(Long id, Long parentId, String name) {
        WmsItemCategoryDO category = categoryMapper.selectByParentIdAndName(parentId, name);
        if (category == null) {
            return;
        }
        // 如果 id 为空，说明不用比较是否为相同 id 的商品分类
        if (id == null) {
            throw exception(ITEM_CATEGORY_NAME_DUPLICATE);
        }
        if (ObjectUtil.notEqual(category.getId(), id)) {
            throw exception(ITEM_CATEGORY_NAME_DUPLICATE);
        }
    }

    private void validateParentCategory(Long id, Long parentId) {
        if (parentId == null || WmsItemCategoryDO.PARENT_ID_ROOT.equals(parentId)) {
            return;
        }
        // 1. 不能设置自己为父分类
        if (ObjectUtil.equal(parentId, id)) {
            throw exception(ITEM_CATEGORY_PARENT_ERROR);
        }
        // 2. 父分类不存在
        WmsItemCategoryDO parentCategory = categoryMapper.selectById(parentId);
        if (parentCategory == null) {
            throw exception(ITEM_CATEGORY_PARENT_NOT_EXISTS);
        }
        // 3. 递归校验父分类，如果父分类是自己的子分类，则报错，避免形成环路
        if (id == null) { // id 为空，说明新增，不需要考虑环路
            return;
        }
        for (int i = 0; i < Short.MAX_VALUE; i++) {
            // 3.1 校验环路
            parentId = parentCategory.getParentId();
            if (ObjectUtil.equal(id, parentId)) {
                throw exception(ITEM_CATEGORY_PARENT_IS_CHILD);
            }
            // 3.2 继续递归上级父分类
            if (parentId == null || WmsItemCategoryDO.PARENT_ID_ROOT.equals(parentId)) {
                break;
            }
            parentCategory = categoryMapper.selectById(parentId);
            if (parentCategory == null) {
                break;
            }
        }
    }

    @Override
    public void deleteItemCategory(Long id) {
        // 校验存在
        validateItemCategoryExists(id);
        // 有子分类不能删
        if (categoryMapper.selectCountByParentId(id) > 0) {
            throw exception(ITEM_CATEGORY_HAS_CHILDREN);
        }
        // 校验分类下不存在商品
        if (itemService.getItemCountByCategoryId(id) > 0) {
            throw exception(ITEM_CATEGORY_HAS_ITEM);
        }

        // 删除
        categoryMapper.deleteById(id);
    }

    @Override
    public WmsItemCategoryDO validateItemCategoryExists(Long id) {
        WmsItemCategoryDO category = categoryMapper.selectById(id);
        if (category == null) {
            throw exception(ITEM_CATEGORY_NOT_EXISTS);
        }
        return category;
    }

    @Override
    public WmsItemCategoryDO getItemCategory(Long id) {
        return categoryMapper.selectById(id);
    }

    @Override
    public List<WmsItemCategoryDO> getItemCategoryList(WmsItemCategoryListReqVO listReqVO) {
        return categoryMapper.selectList(listReqVO);
    }

    @Override
    public List<WmsItemCategoryDO> getItemCategoryList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return categoryMapper.selectByIds(ids);
    }

    @Override
    public Set<Long> getSelfAndChildItemCategoryIdList(Long id) {
        if (id == null) {
            return null;
        }
        Set<Long> ids = new HashSet<>();
        ids.add(id);
        Collection<Long> parentIds = Collections.singleton(id);
        for (int i = 0; i < Short.MAX_VALUE; i++) {
            List<WmsItemCategoryDO> children = categoryMapper.selectListByParentIds(parentIds);
            if (CollUtil.isEmpty(children)) {
                break;
            }
            ids.addAll(convertSet(children, WmsItemCategoryDO::getId));
            parentIds = convertSet(children, WmsItemCategoryDO::getId);
        }
        return ids;
    }

}
