package cn.iocoder.yudao.module.oms.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.collection.StreamX;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.oms.api.dto.OmsShopProductSaveReqDTO;
import cn.iocoder.yudao.module.oms.controller.admin.product.vo.OmsShopProductPageReqVO;
import cn.iocoder.yudao.module.oms.controller.admin.product.vo.OmsShopProductRespVO;
import cn.iocoder.yudao.module.oms.controller.admin.shop.vo.OmsShopRespVO;
import cn.iocoder.yudao.module.oms.convert.OmsShopProductConvert;
import cn.iocoder.yudao.module.oms.dal.dataobject.OmsShopProductDO;
import cn.iocoder.yudao.module.oms.dal.mysql.OmsShopProductMapper;
import cn.iocoder.yudao.module.oms.service.OmsShopProductService;
import cn.iocoder.yudao.module.oms.service.OmsShopService;
import cn.iocoder.yudao.module.system.api.dept.DeptApi;
import cn.iocoder.yudao.module.system.api.dept.dto.DeptRespDTO;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.module.oms.api.enums.OmsErrorCodeConstants.OMS_SYNC_SHOP_PRODUCT_LACK;

/**
 * OMS店铺产品 Service 实现类
 */
@Service
@Slf4j
public class OmsShopProductServiceImpl extends ServiceImpl<OmsShopProductMapper, OmsShopProductDO> implements OmsShopProductService {

    @Resource
    private OmsShopProductMapper shopProductMapper;

    @Resource
    private OmsShopService omsShopService;

    @Resource
    private DeptApi deptApi;


    @Override
    public List<OmsShopProductDO> getByShopId(Long shopId) {
        return shopProductMapper.selectList(OmsShopProductDO::getShopId, shopId);
    }

    @Override
    public void createOrUpdateShopProductByPlatform(List<OmsShopProductSaveReqDTO> saveReqDTOs) {
        if (CollectionUtils.isEmpty(saveReqDTOs)) {
            throw exception(OMS_SYNC_SHOP_PRODUCT_LACK);
        }

        List<OmsShopProductDO> shopProductDOs = OmsShopProductConvert.INSTANCE.toOmsShopProductDO(saveReqDTOs);

        List<OmsShopProductDO> createShopProducts = new ArrayList<>();
        List<OmsShopProductDO> updateShopProducts = new ArrayList<>();
        //同批次的产品都是一个店铺，所以取第一个即可
        List<OmsShopProductDO> existShopProducts = getByShopId(shopProductDOs.get(0).getShopId());

        // 使用Map存储已存在的店铺产品信息，key=sourceId, value=OmsShopProductDO
        Map<String, OmsShopProductDO> existShopProductMap = Optional.ofNullable(existShopProducts)
            .orElse(Collections.emptyList())
            .stream()
            .collect(Collectors.toMap(omsShopProductDO -> omsShopProductDO.getSourceId(), omsShopProductDO -> omsShopProductDO));

        shopProductDOs.forEach(shopProductDO -> {
            OmsShopProductDO existShopProDuctDO = existShopProductMap.get(shopProductDO.getSourceId());
            if (existShopProDuctDO != null) {
                shopProductDO.setId(existShopProDuctDO.getId());
                updateShopProducts.add(shopProductDO);
            } else {
                // 新增
                createShopProducts.add(shopProductDO);
            }
        });

        if (CollectionUtil.isNotEmpty(createShopProducts)) {
            saveBatch(createShopProducts);
        }

        if (CollectionUtil.isNotEmpty(updateShopProducts)) {
            updateBatchById(updateShopProducts);
        }
        log.info("sync shop product success,salesShopId:{},shopProductCount:{}", shopProductDOs.get(0).getShopId(), shopProductDOs.size());
    }

    @Override
    public CommonResult<PageResult<OmsShopProductRespVO>> getShopProductPageVO(OmsShopProductPageReqVO pageReqVO) {

        PageResult<OmsShopProductDO> pageResult = this.getShopProductPage(pageReqVO);
        PageResult<OmsShopProductRespVO> pageResultVO = BeanUtils.toBean(pageResult, OmsShopProductRespVO.class);
        Set<Long> shopIds = StreamX.from(pageResultVO.getList()).toSet(OmsShopProductRespVO::getShopId);
        Map<Long, OmsShopRespVO> shopVoMap = omsShopService.getShopMapByIds(shopIds);
        // 装配对象
        StreamX.from(pageResultVO.getList()).assemble(shopVoMap, OmsShopProductRespVO::getShopId, OmsShopProductRespVO::setShop);

        List<Long> deptIds = StreamX.from(pageResultVO.getList()).filter(ObjectUtil::isNotNull).toList(OmsShopProductRespVO::getDeptId);
        List<DeptRespDTO> deptDTOList = deptApi.getDeptList(deptIds);
        deptDTOList = StreamX.from(deptDTOList).filter(ObjectUtil::isNotNull).toList();
        StreamX.from(pageResultVO.getList()).assemble(deptDTOList, DeptRespDTO::getId, OmsShopProductRespVO::getDeptId, (prod, dept) -> {
            if (dept != null) {
                prod.setDeptName(dept.getName());
            }
        });
        
        return success(pageResultVO);
    }

    @Override
    public PageResult<OmsShopProductDO> getShopProductPage(OmsShopProductPageReqVO pageReqVO) {
        return shopProductMapper.selectPage(pageReqVO);
    }

}