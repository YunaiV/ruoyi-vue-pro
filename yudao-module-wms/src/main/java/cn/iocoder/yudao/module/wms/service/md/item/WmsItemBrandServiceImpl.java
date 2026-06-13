package cn.iocoder.yudao.module.wms.service.md.item;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.wms.controller.admin.md.item.vo.brand.WmsItemBrandPageReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.md.item.vo.brand.WmsItemBrandSaveReqVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.md.item.WmsItemBrandDO;
import cn.iocoder.yudao.module.wms.dal.mysql.md.item.WmsItemBrandMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.ITEM_BRAND_CODE_DUPLICATE;
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.ITEM_BRAND_HAS_ITEM;
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.ITEM_BRAND_NAME_DUPLICATE;
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.ITEM_BRAND_NOT_EXISTS;

/**
 * WMS 商品品牌 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class WmsItemBrandServiceImpl implements WmsItemBrandService {

    @Resource
    private WmsItemBrandMapper brandMapper;
    @Resource
    private WmsItemService itemService;

    @Override
    public Long createItemBrand(WmsItemBrandSaveReqVO createReqVO) {
        validateBrandSaveData(null, createReqVO);

        // 新增
        WmsItemBrandDO brand = BeanUtils.toBean(createReqVO, WmsItemBrandDO.class);
        brandMapper.insert(brand);
        return brand.getId();
    }

    @Override
    public void updateItemBrand(WmsItemBrandSaveReqVO updateReqVO) {
        // 校验存在
        validateItemBrandExists(updateReqVO.getId());
        validateBrandSaveData(updateReqVO.getId(), updateReqVO);

        // 更新
        WmsItemBrandDO updateObj = BeanUtils.toBean(updateReqVO, WmsItemBrandDO.class);
        brandMapper.updateById(updateObj);
    }

    private void validateBrandSaveData(Long id, WmsItemBrandSaveReqVO reqVO) {
        validateBrandCodeUnique(id, reqVO.getCode());
        validateBrandNameUnique(id, reqVO.getName());
    }

    private void validateBrandCodeUnique(Long id, String code) {
        WmsItemBrandDO brand = brandMapper.selectByCode(code);
        if (brand == null) {
            return;
        }
        // 如果 id 为空，说明新增；和数据库已有 code 重复
        if (id == null) {
            throw exception(ITEM_BRAND_CODE_DUPLICATE);
        }
        if (ObjectUtil.notEqual(brand.getId(), id)) {
            throw exception(ITEM_BRAND_CODE_DUPLICATE);
        }
    }

    private void validateBrandNameUnique(Long id, String name) {
        WmsItemBrandDO brand = brandMapper.selectByName(name);
        if (brand == null) {
            return;
        }
        if (id == null) {
            throw exception(ITEM_BRAND_NAME_DUPLICATE);
        }
        if (ObjectUtil.notEqual(brand.getId(), id)) {
            throw exception(ITEM_BRAND_NAME_DUPLICATE);
        }
    }

    @Override
    public void deleteItemBrand(Long id) {
        // 校验存在
        validateItemBrandExists(id);
        // 校验品牌下不存在商品
        if (itemService.getItemCountByBrandId(id) > 0) {
            throw exception(ITEM_BRAND_HAS_ITEM);
        }

        // 删除
        brandMapper.deleteById(id);
    }

    @Override
    public WmsItemBrandDO validateItemBrandExists(Long id) {
        WmsItemBrandDO brand = brandMapper.selectById(id);
        if (brand == null) {
            throw exception(ITEM_BRAND_NOT_EXISTS);
        }
        return brand;
    }

    @Override
    public WmsItemBrandDO getItemBrand(Long id) {
        return brandMapper.selectById(id);
    }

    @Override
    public PageResult<WmsItemBrandDO> getItemBrandPage(WmsItemBrandPageReqVO pageReqVO) {
        return brandMapper.selectPage(pageReqVO);
    }

    @Override
    public List<WmsItemBrandDO> getItemBrandList() {
        return brandMapper.selectList();
    }

    @Override
    public List<WmsItemBrandDO> getItemBrandList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return brandMapper.selectByIds(ids);
    }

}
