package cn.iocoder.yudao.module.product.service.sku;

import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;

import java.util.*;
import cn.iocoder.yudao.module.product.controller.admin.sku.vo.*;
import cn.iocoder.yudao.module.product.dal.dataobject.sku.SkuDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

import cn.iocoder.yudao.module.product.convert.sku.SkuConvert;
import cn.iocoder.yudao.module.product.dal.mysql.sku.SkuMapper;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.product.enums.ErrorCodeConstants.*;

/**
 * 商品sku Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class SkuServiceImpl implements SkuService {

    @Resource
    private SkuMapper skuMapper;

    @Override
    public Integer createSku(SkuCreateReqVO createReqVO) {
        // 插入
        SkuDO sku = SkuConvert.INSTANCE.convert(createReqVO);
        skuMapper.insert(sku);
        // 返回
        return sku.getId();
    }

    @Override
    public void updateSku(SkuUpdateReqVO updateReqVO) {
        // 校验存在
        this.validateSkuExists(updateReqVO.getId());
        // 更新
        SkuDO updateObj = SkuConvert.INSTANCE.convert(updateReqVO);
        skuMapper.updateById(updateObj);
    }

    @Override
    public void deleteSku(Integer id) {
        // 校验存在
        this.validateSkuExists(id);
        // 删除
        skuMapper.deleteById(id);
    }

    private void validateSkuExists(Integer id) {
        if (skuMapper.selectById(id) == null) {
            throw exception(SKU_NOT_EXISTS);
        }
    }

    @Override
    public SkuDO getSku(Integer id) {
        return skuMapper.selectById(id);
    }

    @Override
    public List<SkuDO> getSkuList(Collection<Integer> ids) {
        return skuMapper.selectBatchIds(ids);
    }

    @Override
    public PageResult<SkuDO> getSkuPage(SkuPageReqVO pageReqVO) {
        return skuMapper.selectPage(pageReqVO);
    }

    @Override
    public List<SkuDO> getSkuList(SkuExportReqVO exportReqVO) {
        return skuMapper.selectList(exportReqVO);
    }

}
