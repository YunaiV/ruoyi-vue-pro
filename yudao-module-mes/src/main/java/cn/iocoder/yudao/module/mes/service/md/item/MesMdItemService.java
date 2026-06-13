package cn.iocoder.yudao.module.mes.service.md.item;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.mes.controller.admin.md.item.vo.MesMdItemImportExcelVO;
import cn.iocoder.yudao.module.mes.controller.admin.md.item.vo.MesMdItemImportRespVO;
import cn.iocoder.yudao.module.mes.controller.admin.md.item.vo.MesMdItemPageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.md.item.vo.MesMdItemSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.item.MesMdItemDO;
import jakarta.validation.Valid;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;

/**
 * MES 物料产品 Service 接口
 *
 * @author 芋道源码
 */
public interface MesMdItemService {

    /**
     * 创建物料产品
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createItem(@Valid MesMdItemSaveReqVO createReqVO);

    /**
     * 更新物料产品
     *
     * @param updateReqVO 更新信息
     */
    void updateItem(@Valid MesMdItemSaveReqVO updateReqVO);

    /**
     * 更新物料产品状态
     *
     * @param id     编号
     * @param status 状态
     */
    void updateItemStatus(Long id, Integer status);

    /**
     * 删除物料产品
     *
     * @param id 编号
     */
    void deleteItem(Long id);

    /**
     * 获得物料产品
     *
     * @param id 编号
     * @return 物料产品
     */
    MesMdItemDO getItem(Long id);

    /**
     * 校验物料产品存在
     *
     * @param id 编号
     * @return 物料产品
     */
    MesMdItemDO validateItemExists(Long id);

    /**
     * 校验物料产品存在且启用
     *
     * @param id 编号
     * @return 物料产品
     */
    MesMdItemDO validateItemExistsAndEnable(Long id);

    /**
     * 获得物料产品分页
     *
     * @param pageReqVO 分页查询
     * @return 物料产品分页
     */
    PageResult<MesMdItemDO> getItemPage(MesMdItemPageReqVO pageReqVO);

    /**
     * 获得物料产品列表
     *
     * @param ids 编号数组
     * @return 物料产品列表
     */
    List<MesMdItemDO> getItemList(Collection<Long> ids);

    /**
     * 获得物料产品 Map
     *
     * @param ids 编号数组
     * @return 物料产品 Map
     */
    default Map<Long, MesMdItemDO> getItemMap(Collection<Long> ids) {
        return convertMap(getItemList(ids), MesMdItemDO::getId);
    }

    /**
     * 基于物料分类编号，获得物料数量
     *
     * @param itemTypeId 物料分类编号
     * @return 物料数量
     */
    Long getItemCountByItemTypeId(Long itemTypeId);

    /**
     * 批量导入物料
     *
     * @param importItems 导入物料列表
     * @param updateSupport 是否支持更新
     * @return 导入结果
     */
    MesMdItemImportRespVO importItemList(List<MesMdItemImportExcelVO> importItems, boolean updateSupport);

    /**
     * 基于计量单位编号，获得物料数量
     *
     * @param unitMeasureId 计量单位编号
     * @return 物料数量
     */
    Long getItemCountByUnitMeasureId(Long unitMeasureId);

}
