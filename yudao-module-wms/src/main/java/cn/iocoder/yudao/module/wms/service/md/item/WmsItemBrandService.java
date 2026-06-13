package cn.iocoder.yudao.module.wms.service.md.item;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.wms.controller.admin.md.item.vo.brand.WmsItemBrandPageReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.md.item.vo.brand.WmsItemBrandSaveReqVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.md.item.WmsItemBrandDO;
import jakarta.validation.Valid;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;

/**
 * WMS 商品品牌 Service 接口
 *
 * @author 芋道源码
 */
public interface WmsItemBrandService {

    /**
     * 创建商品品牌
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createItemBrand(@Valid WmsItemBrandSaveReqVO createReqVO);

    /**
     * 更新商品品牌
     *
     * @param updateReqVO 更新信息
     */
    void updateItemBrand(@Valid WmsItemBrandSaveReqVO updateReqVO);

    /**
     * 删除商品品牌
     *
     * @param id 编号
     */
    void deleteItemBrand(Long id);

    /**
     * 校验商品品牌存在
     *
     * @param id 编号
     * @return 商品品牌
     */
    WmsItemBrandDO validateItemBrandExists(Long id);

    /**
     * 获得商品品牌
     *
     * @param id 编号
     * @return 商品品牌
     */
    WmsItemBrandDO getItemBrand(Long id);

    /**
     * 获得商品品牌分页
     *
     * @param pageReqVO 分页查询
     * @return 商品品牌分页
     */
    PageResult<WmsItemBrandDO> getItemBrandPage(WmsItemBrandPageReqVO pageReqVO);

    /**
     * 获得商品品牌列表
     *
     * @return 商品品牌列表
     */
    List<WmsItemBrandDO> getItemBrandList();

    /**
     * 按编号集合获得商品品牌列表
     *
     * @param ids 编号集合
     * @return 商品品牌列表
     */
    List<WmsItemBrandDO> getItemBrandList(Collection<Long> ids);

    /**
     * 按编号集合获得商品品牌 Map
     *
     * @param ids 编号集合
     * @return 商品品牌 Map
     */
    default Map<Long, WmsItemBrandDO> getItemBrandMap(Collection<Long> ids) {
        return convertMap(getItemBrandList(ids), WmsItemBrandDO::getId);
    }

}
