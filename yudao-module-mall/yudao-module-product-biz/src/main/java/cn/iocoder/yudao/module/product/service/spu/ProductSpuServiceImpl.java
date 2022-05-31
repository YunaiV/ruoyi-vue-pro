package cn.iocoder.yudao.module.product.service.spu;

import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;

import java.util.*;
import cn.iocoder.yudao.module.product.controller.admin.spu.vo.*;
import cn.iocoder.yudao.module.product.dal.dataobject.spu.ProductSpuDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

import cn.iocoder.yudao.module.product.convert.spu.ProductSpuConvert;
import cn.iocoder.yudao.module.product.dal.mysql.spu.ProductSpuMapper;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.product.enums.ErrorCodeConstants.*;

/**
 * 商品spu Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class ProductSpuServiceImpl implements ProductSpuService {

    @Resource
    private ProductSpuMapper ProductSpuMapper;

    @Override
    public Integer createSpu(ProductSpuCreateReqVO createReqVO) {
        // 插入
        ProductSpuDO spu = ProductSpuConvert.INSTANCE.convert(createReqVO);
        ProductSpuMapper.insert(spu);
        // 返回
        return spu.getId();
    }

    @Override
    public void updateSpu(SpuUpdateReqVO updateReqVO) {
        // 校验存在
        this.validateSpuExists(updateReqVO.getId());
        // 更新
        ProductSpuDO updateObj = ProductSpuConvert.INSTANCE.convert(updateReqVO);
        ProductSpuMapper.updateById(updateObj);
    }

    @Override
    public void deleteSpu(Integer id) {
        // 校验存在
        this.validateSpuExists(id);
        // 删除
        ProductSpuMapper.deleteById(id);
    }

    private void validateSpuExists(Integer id) {
        if (ProductSpuMapper.selectById(id) == null) {
            throw exception(SPU_NOT_EXISTS);
        }
    }

    @Override
    public ProductSpuDO getSpu(Integer id) {
        return ProductSpuMapper.selectById(id);
    }

    @Override
    public List<ProductSpuDO> getSpuList(Collection<Integer> ids) {
        return ProductSpuMapper.selectBatchIds(ids);
    }

    @Override
    public PageResult<ProductSpuDO> getSpuPage(SpuPageReqVO pageReqVO) {
        return ProductSpuMapper.selectPage(pageReqVO);
    }

    @Override
    public List<ProductSpuDO> getSpuList(SpuExportReqVO exportReqVO) {
        return ProductSpuMapper.selectList(exportReqVO);
    }

}
