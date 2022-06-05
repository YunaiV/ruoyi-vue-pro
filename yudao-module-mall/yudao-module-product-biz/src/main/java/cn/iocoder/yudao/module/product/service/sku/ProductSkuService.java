package cn.iocoder.yudao.module.product.service.sku;

import java.util.*;
import javax.validation.*;
import cn.iocoder.yudao.module.product.controller.admin.sku.vo.*;
import cn.iocoder.yudao.module.product.dal.dataobject.sku.ProductSkuDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

/**
 * 商品sku Service 接口
 *
 * @author 芋道源码
 */
public interface ProductSkuService {

    /**
     * 创建商品sku
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createSku(@Valid ProductSkuCreateReqVO createReqVO);

    /**
     * 更新商品sku
     *
     * @param updateReqVO 更新信息
     */
    void updateSku(@Valid ProductSkuUpdateReqVO updateReqVO);

    /**
     * 删除商品sku
     *
     * @param id 编号
     */
    void deleteSku(Long id);

    /**
     * 获得商品sku
     *
     * @param id 编号
     * @return 商品sku
     */
    ProductSkuDO getSku(Long id);

    /**
     * 获得商品sku列表
     *
     * @param ids 编号
     * @return 商品sku列表
     */
    List<ProductSkuDO> getSkuList(Collection<Long> ids);

    /**
     * 获得商品sku分页
     *
     * @param pageReqVO 分页查询
     * @return 商品sku分页
     */
    PageResult<ProductSkuDO> getSkuPage(ProductSkuPageReqVO pageReqVO);

    /**
     * 获得商品sku列表, 用于 Excel 导出
     *
     * @param exportReqVO 查询条件
     * @return 商品sku列表
     */
    List<ProductSkuDO> getSkuList(ProductSkuExportReqVO exportReqVO);

    /**
     *对sku的组合的属性等进行合法性校验
     * @param skuCreateReqList sku组合的集合
     */
    void validatedSkuReq(List<ProductSkuCreateReqVO> skuCreateReqList);
}
