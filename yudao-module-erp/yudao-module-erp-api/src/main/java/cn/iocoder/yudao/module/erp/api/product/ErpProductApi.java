package cn.iocoder.yudao.module.erp.api.product;


import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.module.erp.api.product.dto.ErpProductDTO;
import cn.iocoder.yudao.module.erp.api.product.dto.ErpProductRespDTO;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;

public interface ErpProductApi {
    //getProductDto方法
    ErpProductDTO getProductDto(Long id);

    /**
     * 获得所有产品DTO，根据ids，如果ids为null返回所有
     */
    List<ErpProductDTO> listProductDTOs(List<Long> ids);

    /**
     * 获得产品 DO Map
     *
     * @param ids 编号数组
     * @return 产品 DO Map
     */
    default Map<Long, ErpProductDTO> getProductMap(Collection<Long> ids) {
        if(CollectionUtils.isEmpty(ids)) {
            return new HashMap<>();
        }
        return convertMap(listProducts(ids), ErpProductDTO::getId);
    }


    /**
     * 获得产品 DO Map
     *
     * @param codes 产品代码组
     * @return 产品 DO Map
     */
    default Map<String, ErpProductDTO> getProductMapByCode(Collection<String> codes) {
        if(CollectionUtils.isEmpty(codes)) {
            return new HashMap<>();
        }
        return convertMap(listProductsByCodes(codes), ErpProductDTO::getCode);
    }


    /**
     * 获得产品 DO 列表
     *
     * @param codes 产品代码组
     * @return 产品 DO 列表
     */
    List<ErpProductDTO> listProductsByCodes(Collection<String> codes);

    /**
     * 获得产品 DO 列表
     *
     * @param ids 编号组
     * @return 产品 DO 列表
     */
    List<ErpProductDTO> listProducts(Collection<Long> ids);

    /**
     * 校验产品们的有效性
     *
     * @param ids 编号数组
     * @return 产品列表
     */
    List<ErpProductDTO> validProductList(Collection<Long> ids);

    /**
     * 获得指定状态的产品 DTO 列表
     *
     * @param status 状态
     * @return 产品 DTO 列表
     */
    List<ErpProductRespDTO> getProductDTOListByStatus(Boolean status);

    /**
     * (VO->DTO不要使用 -wdy)
     * 获得产品 VO 列表
     *
     * @param ids 编号数组
     * @return 产品 VO 列表
     */
    List<ErpProductRespDTO> getProductVOList(Collection<Long> ids);

    /**
     * 获得产品 DTO Map
     *
     * @param ids 编号数组
     * @return 产品 DTO Map
     */
    Map<Long, ErpProductRespDTO> getProductDTOMap(Collection<Long> ids);
}
