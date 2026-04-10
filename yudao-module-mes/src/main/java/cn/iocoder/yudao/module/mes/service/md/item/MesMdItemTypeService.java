package cn.iocoder.yudao.module.mes.service.md.item;

import cn.iocoder.yudao.module.mes.controller.admin.md.item.vo.type.MesMdItemTypeListReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.md.item.vo.type.MesMdItemTypeSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.item.MesMdItemTypeDO;
import jakarta.validation.Valid;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;

/**
 * MES 物料产品分类 Service 接口
 *
 * @author 芋道源码
 */
public interface MesMdItemTypeService {

    /**
     * 创建物料产品分类
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createItemType(@Valid MesMdItemTypeSaveReqVO createReqVO);

    /**
     * 更新物料产品分类
     *
     * @param updateReqVO 更新信息
     */
    void updateItemType(@Valid MesMdItemTypeSaveReqVO updateReqVO);

    /**
     * 删除物料产品分类
     *
     * @param id 编号
     */
    void deleteItemType(Long id);

    /**
     * 获得物料产品分类
     *
     * @param id 编号
     * @return 物料产品分类
     */
    MesMdItemTypeDO getItemType(Long id);

    /**
     * 获得物料产品分类列表
     *
     * @param listReqVO 查询条件
     * @return 物料产品分类列表
     */
    List<MesMdItemTypeDO> getItemTypeList(MesMdItemTypeListReqVO listReqVO);

    /**
     * 获得物料产品分类列表
     *
     * @param ids 编号数组
     * @return 物料产品分类列表
     */
    List<MesMdItemTypeDO> getItemTypeList(Collection<Long> ids);

    /**
     * 获得物料产品分类 Map
     *
     * @param ids 编号数组
     * @return 物料产品分类 Map
     */
    default Map<Long, MesMdItemTypeDO> getItemTypeMap(Collection<Long> ids) {
        return convertMap(getItemTypeList(ids), MesMdItemTypeDO::getId);
    }

    /**
     * 获得指定分类的所有子分类列表（递归）
     *
     * @param parentId 父分类编号
     * @return 子分类列表
     */
    List<MesMdItemTypeDO> getItemTypeChildrenList(Long parentId);

}
