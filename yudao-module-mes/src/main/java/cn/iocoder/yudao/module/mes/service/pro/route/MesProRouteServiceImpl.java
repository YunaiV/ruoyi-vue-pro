package cn.iocoder.yudao.module.mes.service.pro.route;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.mes.controller.admin.pro.route.vo.MesProRoutePageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.pro.route.vo.MesProRouteSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.pro.route.MesProRouteDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.pro.route.MesProRouteProcessDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.pro.route.MesProRouteProductDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.pro.route.MesProRouteProductBomDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.item.MesMdItemDO;
import cn.iocoder.yudao.module.mes.dal.mysql.pro.route.MesProRouteMapper;
import cn.iocoder.yudao.module.mes.service.md.item.MesMdItemService;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.mes.enums.ErrorCodeConstants.*;

/**
 * MES 工艺路线 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class MesProRouteServiceImpl implements MesProRouteService {

    @Resource
    private MesProRouteMapper routeMapper;

    @Resource
    @Lazy
    private MesProRouteProcessService routeProcessService;
    @Resource
    @Lazy
    private MesProRouteProductService routeProductService;
    @Resource
    @Lazy
    private MesProRouteProductBomService routeProductBomService;
    @Resource
    @Lazy
    private MesMdItemService itemService;

    @Override
    public Long createRoute(MesProRouteSaveReqVO createReqVO) {
        // 1. 校验编码唯一性
        validateRouteCodeUnique(null, createReqVO.getCode());
        // 2. 插入
        MesProRouteDO route = BeanUtils.toBean(createReqVO, MesProRouteDO.class);
        route.setStatus(CommonStatusEnum.DISABLE.getStatus());
        routeMapper.insert(route);
        return route.getId();
    }

    @Override
    public void updateRoute(MesProRouteSaveReqVO updateReqVO) {
        // 1.1 校验存在
        validateRouteExists(updateReqVO.getId());
        // 1.2 校验编码唯一性
        validateRouteCodeUnique(updateReqVO.getId(), updateReqVO.getCode());

        // 2. 更新
        MesProRouteDO updateObj = BeanUtils.toBean(updateReqVO, MesProRouteDO.class);
        routeMapper.updateById(updateObj);
    }

    @Override
    public void updateRouteStatus(Long id, Integer status) {
        // 1.1 校验存在
        MesProRouteDO route = validateRouteExists(id);
        // 1.2 启用时的校验
        if (CommonStatusEnum.ENABLE.getStatus().equals(status)) {
            validateRouteEnable(id);
        }

        // 2. 更新状态
        routeMapper.updateById(new MesProRouteDO().setId(id).setStatus(status));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRoute(Long id) {
        // 1.1 校验存在
        validateRouteExists(id);
        // 1.2 已启用的工艺路线，不允许删除
        validateRouteNotEnable(id);

        // 2.1 级联删除
        routeProcessService.deleteRouteProcessByRouteId(id);
        routeProductService.deleteRouteProductByRouteId(id);
        routeProductBomService.deleteRouteProductBomByRouteId(id);
        // 2.2 删除工艺路线
        routeMapper.deleteById(id);
    }

    @Override
    public MesProRouteDO validateRouteExists(Long id) {
        MesProRouteDO route = routeMapper.selectById(id);
        if (route == null) {
            throw exception(PRO_ROUTE_NOT_EXISTS);
        }
        return route;
    }

    private void validateRouteCodeUnique(Long id, String code) {
        MesProRouteDO route = routeMapper.selectByCode(code);
        if (route == null) {
            return;
        }
        if (ObjUtil.notEqual(route.getId(), id)) {
            throw exception(PRO_ROUTE_CODE_DUPLICATE);
        }
    }

    /**
     * 启用工艺路线时的校验
     */
    private void validateRouteEnable(Long routeId) {
        // 1. 必须有工序
        List<MesProRouteProcessDO> processList = routeProcessService.getRouteProcessListByRouteId(routeId);
        if (CollUtil.isEmpty(processList)) {
            throw exception(PRO_ROUTE_ENABLE_NO_PROCESS);
        }
        // 2. 必须有关键工序
        boolean hasKeyProcess = processList.stream().anyMatch(MesProRouteProcessDO::getKeyFlag);
        if (BooleanUtil.isFalse(hasKeyProcess)) {
            throw exception(PRO_ROUTE_ENABLE_NO_KEY_PROCESS);
        }
        // 3. 所有产品必须配置了 BOM 消耗
        List<MesProRouteProductDO> productList = routeProductService.getRouteProductListByRouteId(routeId);
        for (MesProRouteProductDO product : productList) {
            List<MesProRouteProductBomDO> bomList = routeProductBomService
                    .getRouteProductBomList(routeId, null, product.getItemId());
            if (CollUtil.isEmpty(bomList)) {
                MesMdItemDO item = itemService.validateItemExists(product.getItemId());
                throw exception(PRO_ROUTE_ENABLE_PRODUCT_NO_BOM, item.getName());
            }
        }
    }

    @Override
    public MesProRouteDO getRoute(Long id) {
        return routeMapper.selectById(id);
    }

    @Override
    public PageResult<MesProRouteDO> getRoutePage(MesProRoutePageReqVO pageReqVO) {
        return routeMapper.selectPage(pageReqVO);
    }

    @Override
    public List<MesProRouteDO> getRouteListByStatus(Integer status) {
        return routeMapper.selectListByStatus(status);
    }

    @Override
    public void validateRouteNotEnable(Long routeId) {
        MesProRouteDO route = routeMapper.selectById(routeId);
        if (route != null && CommonStatusEnum.ENABLE.getStatus().equals(route.getStatus())) {
            throw exception(PRO_ROUTE_IS_ENABLE);
        }
    }

    @Override
    public List<MesProRouteDO> getRouteList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return routeMapper.selectByIds(ids);
    }

}
