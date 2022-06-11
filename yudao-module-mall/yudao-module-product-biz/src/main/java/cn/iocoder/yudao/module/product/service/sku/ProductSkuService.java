package cn.iocoder.yudao.module.product.service.sku;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.product.controller.admin.sku.vo.ProductSkuCreateReqVO;
import cn.iocoder.yudao.module.product.controller.admin.sku.vo.ProductSkuExportReqVO;
import cn.iocoder.yudao.module.product.controller.admin.sku.vo.ProductSkuPageReqVO;
import cn.iocoder.yudao.module.product.controller.admin.sku.vo.ProductSkuUpdateReqVO;
import cn.iocoder.yudao.module.product.dal.dataobject.sku.ProductSkuDO;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;

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
     * 对 sku 的组合的属性等进行合法性校验
     *
     * @param list sku组合的集合
     */
    void validateSkus(List<ProductSkuCreateReqVO> list);

    /**
     * 批量保存 sku
     *
     * @param list sku对象集合
     */
    void createSkus(List<ProductSkuDO> list);

    /**
     * 获得商品 sku 集合
     *
     * @param spuId spu 编号
     * @return 商品sku 集合
     */
    List<ProductSkuDO> getSkusBySpuId(Long spuId);

    /**
     * 获得 spu 对应的 sku 集合
     *
     * @param spuIds spu 编码集合
     * @return  商品 sku 集合
     */
    List<ProductSkuDO> getSkusBySpuIds(List<Long> spuIds);

    /**
     * 通过 spuId 删除 sku 信息
     *
     * @param spuId spu 编码
     */
    void deleteSkuBySpuId(Long spuId);

    /**
     * 根据 spuId 更新 spu 下的 sku 信息
     *
     * @param spuId spu 编码
     * @param skus sku 的集合
     */
    void updateSkus(Long spuId, List<ProductSkuCreateReqVO> skus);
}
