package cn.iocoder.yudao.module.mes.service.pro.route;

import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.mes.controller.admin.pro.route.vo.product.MesProRouteProductSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.pro.route.MesProRouteProductDO;
import cn.iocoder.yudao.module.mes.dal.mysql.pro.route.MesProRouteProductMapper;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.mes.enums.ErrorCodeConstants.*;

/**
 * MES 工艺路线产品 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class MesProRouteProductServiceImpl implements MesProRouteProductService {

    @Resource
    private MesProRouteProductMapper routeProductMapper;

    @Resource
    @Lazy
    private MesProRouteService routeService;
    @Resource
    @Lazy
    private MesProRouteProductBomService routeProductBomService;

    @Override
    public Long createRouteProduct(MesProRouteProductSaveReqVO createReqVO) {
        // 1.0 已启用的工艺路线，不允许操作
        routeService.validateRouteNotEnable(createReqVO.getRouteId());
        // 1.1 校验产品唯一性（一个产品只能关联一条工艺路线）
        validateItemUnique(null, createReqVO.getItemId());

        // 2. 插入
        MesProRouteProductDO routeProduct = BeanUtils.toBean(createReqVO, MesProRouteProductDO.class);
        routeProductMapper.insert(routeProduct);
        return routeProduct.getId();
    }

    @Override
    public void updateRouteProduct(MesProRouteProductSaveReqVO updateReqVO) {
        // 1.0 已启用的工艺路线，不允许操作
        routeService.validateRouteNotEnable(updateReqVO.getRouteId());
        // 1.1 校验存在
        validateRouteProductExists(updateReqVO.getId());
        // 1.2 校验产品唯一性
        validateItemUnique(updateReqVO.getId(), updateReqVO.getItemId());

        // 2. 更新
        MesProRouteProductDO updateObj = BeanUtils.toBean(updateReqVO, MesProRouteProductDO.class);
        routeProductMapper.updateById(updateObj);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRouteProduct(Long id) {
        // 1.1 校验存在
        MesProRouteProductDO routeProduct = routeProductMapper.selectById(id);
        validateRouteProductExists(routeProduct);
        // 1.2 已启用的工艺路线，不允许操作
        routeService.validateRouteNotEnable(routeProduct.getRouteId());

        // 2.1 级联删除关联的 BOM
        routeProductBomService.deleteRouteProductBomByRouteIdAndProductId(routeProduct.getRouteId(), routeProduct.getItemId());
        // 2.2 删除产品关联
        routeProductMapper.deleteById(id);
    }

    private void validateRouteProductExists(Long id) {
        if (routeProductMapper.selectById(id) == null) {
            throw exception(PRO_ROUTE_PRODUCT_NOT_EXISTS);
        }
    }

    private void validateRouteProductExists(MesProRouteProductDO routeProduct) {
        if (routeProduct == null) {
            throw exception(PRO_ROUTE_PRODUCT_NOT_EXISTS);
        }
    }

    private void validateItemUnique(Long id, Long itemId) {
        MesProRouteProductDO existing = routeProductMapper.selectByItemId(itemId);
        if (existing == null) {
            return;
        }
        if (ObjUtil.notEqual(existing.getId(), id)) {
            throw exception(PRO_ROUTE_PRODUCT_ITEM_DUPLICATE);
        }
    }

    @Override
    public MesProRouteProductDO getRouteProduct(Long id) {
        return routeProductMapper.selectById(id);
    }

    @Override
    public MesProRouteProductDO getRouteProductByItemId(Long itemId) {
        return routeProductMapper.selectByItemId(itemId);
    }

    @Override
    public List<MesProRouteProductDO> getRouteProductListByRouteId(Long routeId) {
        return routeProductMapper.selectListByRouteId(routeId);
    }

    @Override
    public void deleteRouteProductByRouteId(Long routeId) {
        routeProductMapper.deleteByRouteId(routeId);
    }

}
