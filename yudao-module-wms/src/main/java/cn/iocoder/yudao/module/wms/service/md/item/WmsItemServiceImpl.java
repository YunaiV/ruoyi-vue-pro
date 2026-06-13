package cn.iocoder.yudao.module.wms.service.md.item;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.wms.controller.admin.md.item.vo.item.WmsItemListReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.md.item.vo.item.WmsItemPageReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.md.item.vo.item.WmsItemSaveReqVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.md.item.WmsItemDO;
import cn.iocoder.yudao.module.wms.dal.mysql.md.item.WmsItemMapper;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.*;

/**
 * WMS 商品 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class WmsItemServiceImpl implements WmsItemService {

    @Resource
    private WmsItemMapper itemMapper;
    @Resource
    @Lazy // 延迟加载，避免循环依赖
    private WmsItemCategoryService categoryService;
    @Resource
    @Lazy // 延迟加载，避免循环依赖
    private WmsItemBrandService brandService;
    @Resource
    private WmsItemSkuService itemSkuService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createItem(WmsItemSaveReqVO createReqVO) {
        // 校验数据
        validateItemSaveData(createReqVO);

        // 插入商品
        WmsItemDO item = BeanUtils.toBean(createReqVO, WmsItemDO.class);
        itemMapper.insert(item);
        // 插入 SKU
        itemSkuService.createItemSkuList(item.getId(), createReqVO.getSkus());
        return item.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateItem(WmsItemSaveReqVO updateReqVO) {
        // 校验存在
        validateItemExists(updateReqVO.getId());
        // 校验数据
        validateItemSaveData(updateReqVO);

        // 更新商品
        WmsItemDO updateObj = BeanUtils.toBean(updateReqVO, WmsItemDO.class);
        itemMapper.updateById(updateObj);
        // 更新 SKU
        itemSkuService.updateItemSkuList(updateReqVO.getId(), updateReqVO.getSkus());
    }

    private void validateItemSaveData(WmsItemSaveReqVO reqVO) {
        // 校验商品编号唯一
        validateItemCodeUnique(reqVO.getId(), reqVO.getCode());
        // 校验商品名称唯一
        validateItemNameUnique(reqVO.getId(), reqVO.getName());
        // 校验商品分类存在
        validateCategoryExists(reqVO.getCategoryId());
        // 校验商品品牌存在
        validateBrandExists(reqVO.getBrandId());
    }

    private void validateItemCodeUnique(Long id, String code) {
        WmsItemDO item = itemMapper.selectByCode(code);
        if (item == null) {
            return;
        }
        if (id == null || ObjectUtil.notEqual(item.getId(), id)) {
            throw exception(ITEM_CODE_DUPLICATE);
        }
    }

    private void validateItemNameUnique(Long id, String name) {
        WmsItemDO item = itemMapper.selectByName(name);
        if (item == null) {
            return;
        }
        if (id == null || ObjectUtil.notEqual(item.getId(), id)) {
            throw exception(ITEM_NAME_DUPLICATE);
        }
    }

    private void validateCategoryExists(Long categoryId) {
        categoryService.validateItemCategoryExists(categoryId);
    }

    private void validateBrandExists(Long brandId) {
        if (brandId == null) {
            return;
        }
        brandService.validateItemBrandExists(brandId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteItem(Long id) {
        // 校验存在
        validateItemExists(id);

        // 删除商品和 SKU
        itemSkuService.deleteItemSkuListByItemId(id);
        itemMapper.deleteById(id);
    }

    @Override
    public WmsItemDO validateItemExists(Long id) {
        WmsItemDO item = itemMapper.selectById(id);
        if (item == null) {
            throw exception(ITEM_NOT_EXISTS);
        }
        return item;
    }

    @Override
    public WmsItemDO getItem(Long id) {
        return itemMapper.selectById(id);
    }

    @Override
    public PageResult<WmsItemDO> getItemPage(WmsItemPageReqVO pageReqVO) {
        return itemMapper.selectPage(pageReqVO,
                categoryService.getSelfAndChildItemCategoryIdList(pageReqVO.getCategoryId()));
    }

    @Override
    public List<WmsItemDO> getItemList(WmsItemListReqVO listReqVO) {
        return itemMapper.selectList(listReqVO,
                categoryService.getSelfAndChildItemCategoryIdList(listReqVO.getCategoryId()));
    }

    @Override
    public List<WmsItemDO> getItemList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return itemMapper.selectByIds(ids);
    }

    @Override
    public long getItemCountByCategoryId(Long categoryId) {
        return itemMapper.selectCountByCategoryId(categoryId);
    }

    @Override
    public long getItemCountByBrandId(Long brandId) {
        return itemMapper.selectCountByBrandId(brandId);
    }

}
