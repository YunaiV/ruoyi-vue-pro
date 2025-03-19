package cn.iocoder.yudao.module.erp.service.product;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.erp.api.product.dto.ErpProductRespDTO;
import cn.iocoder.yudao.module.erp.controller.admin.product.vo.product.ErpProductPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.product.vo.product.ErpProductRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.product.vo.product.ErpProductSaveReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.product.ErpProductDO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;

/**
 * ERP 产品 Service 接口
 *
 * @author 芋道源码
 */
public interface ErpProductService {

    /**
     * 创建产品
     *
     * @param saveReqVO 创建信息
     * @return 编号
     */
    Long createProduct(@Valid ErpProductSaveReqVO saveReqVO);

    /**
     * 更新产品
     *
     * @param updateReqVO 更新信息
     */
    void updateProduct(@Valid ErpProductSaveReqVO updateReqVO);

    /**
     * 删除产品
     *
     * @param id 编号
     */
    void deleteProduct(Long id);

    /**
     * 校验产品们的有效性
     *
     * @param ids 编号数组
     * @return 产品列表
     */
    List<ErpProductDO> validProductList(Collection<Long> ids);

    /**
     * 获得产品
     *
     * @param id 编号
     * @return 产品
     */
    ErpProductRespVO getProduct(Long id);

    /**
     * 获得指定状态的产品 VO 列表
     *
     * @param status 状态
     * @return 产品 VO 列表
     */
    List<ErpProductRespVO> getProductVOListByStatus(Boolean status);

    /**
     * 获得指定状态的产品 DTO 列表
     *
     * @param status 状态
     * @return 产品 DTO 列表
     */
    List<ErpProductRespDTO> getProductDTOListByStatus(Boolean status);
    /**
     * 获得产品 VO 列表
     *
     * @param ids 编号数组
     * @return 产品 VO 列表
     */
    List<ErpProductRespVO> getProductVOList(Collection<Long> ids);

    /**
     * 获得产品 DO 列表
     *
     * @param ids 编号组
     * @return 产品 DO 列表
     */
    List<ErpProductDO> listProducts(@NotNull Collection<Long> ids);

    /**
     * 获得产品 DO Map
     *
     * @param ids 编号数组
     * @return 产品 DO Map
     */
    default Map<Long, ErpProductDO> getProductMap(Collection<Long> ids) {
        return convertMap(listProducts(ids), ErpProductDO::getId);
    }

    /**
     * 获得产品 VO Map
     *
     * @param ids 编号数组
     * @return 产品 VO Map
     */
    default Map<Long, ErpProductRespVO> getProductVOMap(Collection<Long> ids) {
        return convertMap(getProductVOList(ids), ErpProductRespVO::getId);
    }

    /**
     * 获得产品 VO 分页
     *
     * @param pageReqVO 分页查询
     * @return 产品分页
     */
    PageResult<ErpProductRespVO> getProductVOPage(ErpProductPageReqVO pageReqVO);

    /**
     * 基于产品分类编号，获得产品数量
     *
     * @param categoryId 产品分类编号
     * @return 产品数量
     */
    Long getProductCountByCategoryId(Long categoryId);

    /**
     * 基于产品单位编号，获得产品数量
     *
     * @param unitId 产品单位编号
     * @return 产品数量
     */
    Long getProductCountByUnitId(Long unitId);


    /**
     * 根据barCode模糊查询productId集合
     */
    List<Long> listProductIdByBarCode(String barCode);
}