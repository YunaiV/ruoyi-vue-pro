package cn.iocoder.yudao.module.promotion.service.point;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.product.api.sku.ProductSkuApi;
import cn.iocoder.yudao.module.product.api.sku.dto.ProductSkuRespDTO;
import cn.iocoder.yudao.module.product.api.spu.ProductSpuApi;
import cn.iocoder.yudao.module.product.api.spu.dto.ProductSpuRespDTO;
import cn.iocoder.yudao.module.promotion.controller.admin.point.vo.activity.PointActivityPageReqVO;
import cn.iocoder.yudao.module.promotion.controller.admin.point.vo.activity.PointActivitySaveReqVO;
import cn.iocoder.yudao.module.promotion.controller.admin.point.vo.product.PointProductSaveReqVO;
import cn.iocoder.yudao.module.promotion.dal.dataobject.point.PointActivityDO;
import cn.iocoder.yudao.module.promotion.dal.mysql.point.PointActivityMapper;
import cn.iocoder.yudao.module.promotion.dal.mysql.point.PointProductMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;
import static cn.iocoder.yudao.module.product.enums.ErrorCodeConstants.SKU_NOT_EXISTS;
import static cn.iocoder.yudao.module.product.enums.ErrorCodeConstants.SPU_NOT_EXISTS;
import static cn.iocoder.yudao.module.promotion.enums.ErrorCodeConstants.POINT_ACTIVITY_NOT_EXISTS;
import static java.util.Collections.singletonList;

// TODO @puhui999: 下次提交完善

/**
 * 积分商城活动 Service 实现类
 *
 * @author HUIHUI
 */
@Service
@Validated
public class PointActivityServiceImpl implements PointActivityService {

    @Resource
    private PointActivityMapper pointActivityMapper;
    @Resource
    private PointProductMapper pointProductMapper;

    @Resource
    private ProductSpuApi productSpuApi;
    @Resource
    private ProductSkuApi productSkuApi;

    @Override
    public Long createPointActivity(PointActivitySaveReqVO createReqVO) {
        // 1. 校验商品是否存在
        validateProductExists(createReqVO.getSpuId(), createReqVO.getProducts());
        // 插入
        PointActivityDO pointActivity = BeanUtils.toBean(createReqVO, PointActivityDO.class);
        pointActivityMapper.insert(pointActivity);
        // 返回
        return pointActivity.getId();
    }

    @Override
    public void updatePointActivity(PointActivitySaveReqVO updateReqVO) {
        // 1.1 校验存在
        validatePointActivityExists(updateReqVO.getId());
        // 1.2 校验商品是否存在
        validateProductExists(updateReqVO.getSpuId(), updateReqVO.getProducts());

        // 更新
        PointActivityDO updateObj = BeanUtils.toBean(updateReqVO, PointActivityDO.class);
        pointActivityMapper.updateById(updateObj);
    }

    @Override
    public void deletePointActivity(Long id) {
        // 校验存在
        validatePointActivityExists(id);
        // 删除
        pointActivityMapper.deleteById(id);
    }

    private void validatePointActivityExists(Long id) {
        if (pointActivityMapper.selectById(id) == null) {
            throw exception(POINT_ACTIVITY_NOT_EXISTS);
        }
    }

    /**
     * 校验秒杀商品是否都存在
     *
     * @param spuId    商品 SPU 编号
     * @param products 秒杀商品
     */
    private void validateProductExists(Long spuId, List<PointProductSaveReqVO> products) {
        // 1. 校验商品 spu 是否存在
        ProductSpuRespDTO spu = productSpuApi.getSpu(spuId);
        if (spu == null) {
            throw exception(SPU_NOT_EXISTS);
        }

        // 2. 校验商品 sku 都存在
        List<ProductSkuRespDTO> skus = productSkuApi.getSkuListBySpuId(singletonList(spuId));
        Map<Long, ProductSkuRespDTO> skuMap = convertMap(skus, ProductSkuRespDTO::getId);
        products.forEach(product -> {
            if (!skuMap.containsKey(product.getSkuId())) {
                throw exception(SKU_NOT_EXISTS);
            }
        });
    }

    @Override
    public PointActivityDO getPointActivity(Long id) {
        return pointActivityMapper.selectById(id);
    }

    @Override
    public PageResult<PointActivityDO> getPointActivityPage(PointActivityPageReqVO pageReqVO) {
        return pointActivityMapper.selectPage(pageReqVO);
    }

}