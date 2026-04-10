package cn.iocoder.yudao.module.mes.service.md.item;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.mes.controller.admin.md.item.vo.bom.MesMdProductBomPageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.md.item.vo.bom.MesMdProductBomSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.item.MesMdProductBomDO;
import jakarta.validation.Valid;

import java.util.Collection;
import java.util.List;

/**
 * MES 产品BOM Service 接口
 *
 * @author 芋道源码
 */
public interface MesMdProductBomService {

    /**
     * 创建产品BOM
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createProductBom(@Valid MesMdProductBomSaveReqVO createReqVO);

    /**
     * 更新产品BOM
     *
     * @param updateReqVO 更新信息
     */
    void updateProductBom(@Valid MesMdProductBomSaveReqVO updateReqVO);

    /**
     * 删除产品BOM
     *
     * @param id 编号
     */
    void deleteProductBom(Long id);

    /**
     * 获得产品BOM
     *
     * @param id 编号
     * @return 产品BOM
     */
    MesMdProductBomDO getProductBom(Long id);

    /**
     * 获得产品BOM分页
     *
     * @param pageReqVO 分页查询
     * @return 产品BOM分页
     */
    PageResult<MesMdProductBomDO> getProductBomPage(MesMdProductBomPageReqVO pageReqVO);

    /**
     * 根据物料产品编号获得产品BOM列表
     *
     * @param itemId 物料产品编号
     * @return 产品BOM列表
     */
    List<MesMdProductBomDO> getProductBomListByItemId(Long itemId);

    /**
     * 根据物料产品编号批量获得产品BOM列表
     *
     * @param itemIds 物料产品编号数组
     * @return 产品BOM列表
     */
    List<MesMdProductBomDO> getProductBomListByItemIds(Collection<Long> itemIds);

    /**
     * 根据物料产品编号删除产品BOM
     *
     * @param itemId 物料产品编号
     */
    void deleteProductBomByItemId(Long itemId);

}
