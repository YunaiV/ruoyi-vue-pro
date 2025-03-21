package cn.iocoder.yudao.module.srm.service.product;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.spring.SpringUtils;
import cn.iocoder.yudao.module.srm.api.product.dto.ErpProductRespDTO;
import cn.iocoder.yudao.module.srm.controller.admin.product.vo.product.ErpProductPageReqVO;
import cn.iocoder.yudao.module.srm.controller.admin.product.vo.product.ErpProductRespVO;
import cn.iocoder.yudao.module.srm.controller.admin.product.vo.product.ErpProductSaveReqVO;
import cn.iocoder.yudao.module.srm.dal.dataobject.product.ErpProductDO;
import cn.iocoder.yudao.module.srm.service.product.bedsidecabinet.ErpProductBedsideCabinetServiceImpl;
import cn.iocoder.yudao.module.srm.service.product.bookcase.ErpProductBookcaseServiceImpl;
import cn.iocoder.yudao.module.srm.service.product.desktopstoragerack.ErpProductDesktopStorageRackServiceImpl;
import cn.iocoder.yudao.module.srm.service.product.floatingshelf.ErpProductFloatingShelfServiceImpl;
import cn.iocoder.yudao.module.srm.service.product.floortvstand.ErpProductFloorTVStandServiceImpl;
import cn.iocoder.yudao.module.srm.service.product.ironfilingcabinet.ErpProductIronFilingCabinetServiceImpl;
import cn.iocoder.yudao.module.srm.service.product.keyboardtray.ErpProductKeyboardTrayServiceImpl;
import cn.iocoder.yudao.module.srm.service.product.mediastand.ErpProductMediaStandServiceImpl;
import cn.iocoder.yudao.module.srm.service.product.monitorriser.ErpProductMonitorRiserServiceImpl;
import cn.iocoder.yudao.module.srm.service.product.officedesk.ErpProductOfficeDeskServiceImpl;
import cn.iocoder.yudao.module.srm.service.product.speakerstand.ErpProductSpeakerStandServiceImpl;
import cn.iocoder.yudao.module.srm.service.product.tabletoptvstand.ErpProductTableTopTVStandServiceImpl;
import cn.iocoder.yudao.module.srm.service.product.tvstandwithmount.ErpProductTVStandWithMountServiceImpl;
import cn.iocoder.yudao.module.srm.service.product.wallmountedtvmount.ErpProductWallMountedTVMountServiceImpl;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;


/**
 * ERP 产品 Service 实现类
 *
 * @author 芋道源码
 */
@Primary
@Service
public class ErpProductServiceDelegator implements ErpProductService {

    // 分类Id映射ProductService的实现类
    private static final Map<Long, Class<?>> SERVICE_MAP = Map.ofEntries(
        // 1L对应落地电视支架的服务实现类
        Map.entry(1L, ErpProductFloorTVStandServiceImpl.class),
        // 2L对应床头柜的服务实现类
        Map.entry(2L, ErpProductBedsideCabinetServiceImpl.class),
        // 3L对应多媒体支架的服务实现类
        Map.entry(3L, ErpProductMediaStandServiceImpl.class),
        // 5L对应电视柜支架的服务实现类
        Map.entry(5L, ErpProductTVStandWithMountServiceImpl.class),
        // 6L对应挂墙电视支架的服务实现类
        Map.entry(6L, ErpProductWallMountedTVMountServiceImpl.class),
        // 7L对应音响支架的服务实现类
        Map.entry(7L, ErpProductSpeakerStandServiceImpl.class),
        // 9L对应多媒体挂墙支架的服务实现类
        Map.entry(9L, ErpProductFloatingShelfServiceImpl.class),
        // 10L对应桌面电视架的服务实现类
        Map.entry(10L, ErpProductTableTopTVStandServiceImpl.class),
        // 11L对应办公桌的服务实现类
        Map.entry(11L, ErpProductOfficeDeskServiceImpl.class),
        // 13L对应显示器增高架的服务实现类
        Map.entry(13L, ErpProductMonitorRiserServiceImpl.class),
        // 14L对应书架的服务实现类
        Map.entry(14L, ErpProductBookcaseServiceImpl.class),
        // 15L对应键盘托的服务实现类
        Map.entry(15L, ErpProductKeyboardTrayServiceImpl.class),
        // 16L对应铁质文件柜的服务实现类
        Map.entry(16L, ErpProductIronFilingCabinetServiceImpl.class),
        // 17L对应桌面置物架的服务实现类
        Map.entry(17L, ErpProductDesktopStorageRackServiceImpl.class)
    );

    private ErpProductService getDefaultService() {
        return getService(null);
    }

    private ErpProductService getService(Long categoryId) {
        Class<?> serviceClass = ErpProductServiceImpl.class;
        if (categoryId != null) {
            serviceClass = SERVICE_MAP.getOrDefault(categoryId,serviceClass);
        }
        Object serviceImpl = SpringUtils.getBeanByExactType(serviceClass);
        return (ErpProductService) serviceImpl;
    }

    @Override
    public Long createProduct(ErpProductSaveReqVO createReqVO) {
        ErpProductService service = getService(createReqVO.getCategoryId());
        return service.createProduct(createReqVO);
    }

    @Override
    public void updateProduct(ErpProductSaveReqVO updateReqVO) {
        ErpProductService service = getService(updateReqVO.getCategoryId());
        service.updateProduct(updateReqVO);
    }

    @Override
    public void deleteProduct(Long id) {
        ErpProductService service = getDefaultService();
        service.deleteProduct(id);
    }

    @Override
    public List<ErpProductDO> validProductList(Collection<Long> ids) {
        ErpProductService service = getDefaultService();
        return service.validProductList(ids);
    }

    @Override
    public ErpProductRespVO getProduct(Long id) {
        ErpProductService service = getDefaultService();
        return service.getProduct(id);
    }

    @Override
    public List<ErpProductRespVO> getProductVOListByStatus(Boolean status) {
        ErpProductService service = getDefaultService();
        return service.getProductVOListByStatus(status);
    }

    @Override
    public List<ErpProductRespDTO> getProductDTOListByStatus(Boolean status) {
        ErpProductService service = getDefaultService();
        return service.getProductDTOListByStatus(status);
    }

    @Override
    public List<ErpProductRespVO> getProductVOList(Collection<Long> ids) {
        ErpProductService service = getDefaultService();
        return service.getProductVOList(ids);
    }
    @Override
    public List<ErpProductDO> listProducts(Collection<Long> ids) {
        ErpProductService service = getDefaultService();
        return service.listProducts(ids);
    }
    /**
     * 获得产品 DO Map
     *
     * @param ids 编号数组
     * @return 产品 DO Map
     */
    @Override
    public Map<Long, ErpProductDO> getProductMap(Collection<Long> ids) {
        return ErpProductService.super.getProductMap(ids);
    }
    /**
     * 获得产品 VO Map
     *
     * @param ids 编号数组
     * @return 产品 VO Map
     */
    @Override
    public Map<Long, ErpProductRespVO> getProductVOMap(Collection<Long> ids) {
        return ErpProductService.super.getProductVOMap(ids);
    }

    @Override
    public PageResult<ErpProductRespVO> getProductVOPage(ErpProductPageReqVO pageReqVO) {
        ErpProductService service = getService(pageReqVO.getCategoryId());
        return service.getProductVOPage(pageReqVO);
    }

    @Override
    public Long getProductCountByCategoryId(Long categoryId) {
        ErpProductService service = getService(categoryId);
        return service.getProductCountByCategoryId(categoryId);
    }

    @Override
    public Long getProductCountByUnitId(Long unitId) {
        ErpProductService service = getDefaultService();
        return service.getProductCountByUnitId(unitId);
    }

    @Override
    public List<Long> listProductIdByBarCode(String barCode) {
        ErpProductService service = getDefaultService();
        return service.listProductIdByBarCode(barCode);
    }
}