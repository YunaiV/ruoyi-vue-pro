package cn.iocoder.yudao.module.product.service.sku;

import java.util.*;
import javax.validation.*;
import cn.iocoder.yudao.module.product.controller.admin.sku.vo.*;
import cn.iocoder.yudao.module.product.dal.dataobject.sku.SkuDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

/**
 * 商品sku Service 接口
 *
 * @author 芋道源码
 */
public interface SkuService {

    /**
     * 创建商品sku
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Integer createSku(@Valid SkuCreateReqVO createReqVO);

    /**
     * 更新商品sku
     *
     * @param updateReqVO 更新信息
     */
    void updateSku(@Valid SkuUpdateReqVO updateReqVO);

    /**
     * 删除商品sku
     *
     * @param id 编号
     */
    void deleteSku(Integer id);

    /**
     * 获得商品sku
     *
     * @param id 编号
     * @return 商品sku
     */
    SkuDO getSku(Integer id);

    /**
     * 获得商品sku列表
     *
     * @param ids 编号
     * @return 商品sku列表
     */
    List<SkuDO> getSkuList(Collection<Integer> ids);

    /**
     * 获得商品sku分页
     *
     * @param pageReqVO 分页查询
     * @return 商品sku分页
     */
    PageResult<SkuDO> getSkuPage(SkuPageReqVO pageReqVO);

    /**
     * 获得商品sku列表, 用于 Excel 导出
     *
     * @param exportReqVO 查询条件
     * @return 商品sku列表
     */
    List<SkuDO> getSkuList(SkuExportReqVO exportReqVO);

}
