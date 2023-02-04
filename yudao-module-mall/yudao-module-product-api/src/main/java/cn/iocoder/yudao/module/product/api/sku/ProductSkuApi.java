package cn.iocoder.yudao.module.product.api.sku;

import cn.iocoder.yudao.module.product.api.sku.dto.ProductSkuUpdateStockReqDTO;
import cn.iocoder.yudao.module.product.api.sku.dto.ProductSkuRespDTO;

import java.util.Collection;
import java.util.List;

/**
 * 商品 SKU API 接口
 *
 * @author LeeYan9
 * @since 2022-08-26
 */
public interface ProductSkuApi {

    /**
     * 查询 SKU 信息
     *
     * @param id SKU 编号
     * @return SKU 信息
     */
    ProductSkuRespDTO getSku(Long id);

    /**
     * 批量查询 SKU 数组
     *
     * @param ids SKU 编号列表
     * @return SKU 数组
     */
    List<ProductSkuRespDTO> getSkuList(Collection<Long> ids);

    /**
     * 更新 SKU 库存
     *
     * @param updateStockReqDTO 更新请求
     */
    void updateSkuStock(ProductSkuUpdateStockReqDTO updateStockReqDTO);

}
