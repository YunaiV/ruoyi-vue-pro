package cn.iocoder.yudao.module.mes.service.dv.machinery;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.mes.controller.admin.dv.machinery.vo.MesDvMachineryImportExcelVO;
import cn.iocoder.yudao.module.mes.controller.admin.dv.machinery.vo.MesDvMachineryImportRespVO;
import cn.iocoder.yudao.module.mes.controller.admin.dv.machinery.vo.MesDvMachineryPageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.dv.machinery.vo.MesDvMachinerySaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.dv.machinery.MesDvMachineryDO;
import jakarta.validation.Valid;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;

/**
 * MES 设备台账 Service 接口
 *
 * @author 芋道源码
 */
public interface MesDvMachineryService {

    /**
     * 创建设备
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createMachinery(@Valid MesDvMachinerySaveReqVO createReqVO);

    /**
     * 更新设备
     *
     * @param updateReqVO 更新信息
     */
    void updateMachinery(@Valid MesDvMachinerySaveReqVO updateReqVO);

    /**
     * 删除设备
     *
     * @param id 编号
     */
    void deleteMachinery(Long id);

    /**
     * 获得设备
     *
     * @param id 编号
     * @return 设备
     */
    MesDvMachineryDO getMachinery(Long id);

    /**
     * 获得设备分页
     *
     * @param pageReqVO 分页查询
     * @return 设备分页
     */
    PageResult<MesDvMachineryDO> getMachineryPage(MesDvMachineryPageReqVO pageReqVO);

    /**
     * 获得指定设备类型下的设备数量
     *
     * @param machineryTypeId 设备类型编号
     * @return 设备数量
     */
    Long getMachineryCountByMachineryTypeId(Long machineryTypeId);

    /**
     * 校验设备存在
     *
     * @param id 编号
     */
    void validateMachineryExists(Long id);

    /**
     * 获得设备列表
     *
     * @return 设备列表
     */
    List<MesDvMachineryDO> getMachineryList();

    /**
     * 获得设备列表
     *
     * @param ids 编号数组
     * @return 设备列表
     */
    List<MesDvMachineryDO> getMachineryList(Collection<Long> ids);

    /**
     * 获得设备 Map
     *
     * @param ids 编号数组
     * @return 设备 Map
     */
    default Map<Long, MesDvMachineryDO> getMachineryMap(Collection<Long> ids) {
        return convertMap(getMachineryList(ids), MesDvMachineryDO::getId);
    }

    /**
     * 导入设备列表
     *
     * @param importMachineryList 导入设备列表
     * @param updateSupport 是否支持更新
     * @return 导入结果
     */
    MesDvMachineryImportRespVO importMachineryList(List<MesDvMachineryImportExcelVO> importMachineryList,
                                                    boolean updateSupport);

}
