package cn.iocoder.yudao.module.mes.service.dv.machinery;

import cn.iocoder.yudao.module.mes.controller.admin.dv.machinery.vo.type.MesDvMachineryTypeListReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.dv.machinery.vo.type.MesDvMachineryTypeSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.dv.machinery.MesDvMachineryTypeDO;
import jakarta.validation.Valid;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;

/**
 * MES 设备类型 Service 接口
 *
 * @author 芋道源码
 */
public interface MesDvMachineryTypeService {

    /**
     * 创建设备类型
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createMachineryType(@Valid MesDvMachineryTypeSaveReqVO createReqVO);

    /**
     * 更新设备类型
     *
     * @param updateReqVO 更新信息
     */
    void updateMachineryType(@Valid MesDvMachineryTypeSaveReqVO updateReqVO);

    /**
     * 删除设备类型
     *
     * @param id 编号
     */
    void deleteMachineryType(Long id);

    /**
     * 获得设备类型
     *
     * @param id 编号
     * @return 设备类型
     */
    MesDvMachineryTypeDO getMachineryType(Long id);

    /**
     * 获得设备类型列表
     *
     * @param listReqVO 列表查询
     * @return 设备类型列表
     */
    List<MesDvMachineryTypeDO> getMachineryTypeList(MesDvMachineryTypeListReqVO listReqVO);

    /**
     * 获得设备类型列表
     *
     * @param ids 编号数组
     * @return 设备类型列表
     */
    List<MesDvMachineryTypeDO> getMachineryTypeList(Collection<Long> ids);

    /**
     * 获得设备类型 Map
     *
     * @param ids 编号数组
     * @return 设备类型 Map
     */
    default Map<Long, MesDvMachineryTypeDO> getMachineryTypeMap(Collection<Long> ids) {
        return convertMap(getMachineryTypeList(ids), MesDvMachineryTypeDO::getId);
    }

    /**
     * 获得指定父类型下的所有子类型（递归）
     *
     * @param parentId 父类型编号
     * @return 子类型列表
     */
    List<MesDvMachineryTypeDO> getMachineryTypeChildrenList(Long parentId);

}
