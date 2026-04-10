package cn.iocoder.yudao.module.mes.service.pro.route;

import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.mes.controller.admin.pro.route.vo.productbom.MesProRouteProductBomSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.pro.route.MesProRouteProductBomDO;
import cn.iocoder.yudao.module.mes.dal.mysql.pro.route.MesProRouteProductBomMapper;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.mes.enums.ErrorCodeConstants.*;

/**
 * MES 工艺路线产品 BOM Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class MesProRouteProductBomServiceImpl implements MesProRouteProductBomService {

    @Resource
    private MesProRouteProductBomMapper routeProductBomMapper;

    @Resource
    @Lazy
    private MesProRouteService routeService;

    @Override
    public Long createRouteProductBom(MesProRouteProductBomSaveReqVO createReqVO) {
        // 1.0 已启用的工艺路线，不允许操作
        routeService.validateRouteNotEnable(createReqVO.getRouteId());
        // 1.1 校验唯一性
        validateBomUnique(null, createReqVO.getItemId(), createReqVO.getProcessId(), createReqVO.getProductId());
        // 2. 插入
        MesProRouteProductBomDO routeProductBom = BeanUtils.toBean(createReqVO, MesProRouteProductBomDO.class);
        routeProductBomMapper.insert(routeProductBom);
        return routeProductBom.getId();
    }

    @Override
    public void updateRouteProductBom(MesProRouteProductBomSaveReqVO updateReqVO) {
        // 1.0 已启用的工艺路线，不允许操作
        routeService.validateRouteNotEnable(updateReqVO.getRouteId());
        // 1.1 校验存在
        validateRouteProductBomExists(updateReqVO.getId());
        // 1.2 校验唯一性
        validateBomUnique(updateReqVO.getId(), updateReqVO.getItemId(), updateReqVO.getProcessId(), updateReqVO.getProductId());
        // 3. 更新
        MesProRouteProductBomDO updateObj = BeanUtils.toBean(updateReqVO, MesProRouteProductBomDO.class);
        routeProductBomMapper.updateById(updateObj);
    }

    @Override
    public void deleteRouteProductBom(Long id) {
        // 1.1 校验存在
        MesProRouteProductBomDO bom = routeProductBomMapper.selectById(id);
        if (bom == null) {
            throw exception(PRO_ROUTE_PRODUCT_BOM_NOT_EXISTS);
        }
        // 1.2 已启用的工艺路线，不允许操作
        routeService.validateRouteNotEnable(bom.getRouteId());
        // 2. 删除
        routeProductBomMapper.deleteById(id);
    }

    private void validateRouteProductBomExists(Long id) {
        if (routeProductBomMapper.selectById(id) == null) {
            throw exception(PRO_ROUTE_PRODUCT_BOM_NOT_EXISTS);
        }
    }

    private void validateBomUnique(Long id, Long itemId, Long processId, Long productId) {
        MesProRouteProductBomDO existing = routeProductBomMapper.selectByUnique(itemId, processId, productId);
        if (existing == null) {
            return;
        }
        if (ObjUtil.notEqual(existing.getId(), id)) {
            throw exception(PRO_ROUTE_PRODUCT_BOM_DUPLICATE);
        }
    }

    @Override
    public MesProRouteProductBomDO getRouteProductBom(Long id) {
        return routeProductBomMapper.selectById(id);
    }

    @Override
    public List<MesProRouteProductBomDO> getRouteProductBomList(Long routeId, Long processId, Long productId) {
        return routeProductBomMapper.selectList(routeId, processId, productId);
    }

    @Override
    public void deleteRouteProductBomByRouteId(Long routeId) {
        routeProductBomMapper.deleteByRouteId(routeId);
    }

    @Override
    public void deleteRouteProductBomByRouteIdAndProductId(Long routeId, Long productId) {
        routeProductBomMapper.deleteByRouteIdAndProductId(routeId, productId);
    }

}
