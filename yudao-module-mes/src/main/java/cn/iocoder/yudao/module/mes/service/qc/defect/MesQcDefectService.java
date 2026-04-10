package cn.iocoder.yudao.module.mes.service.qc.defect;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.mes.controller.admin.qc.defect.vo.MesQcDefectPageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.qc.defect.vo.MesQcDefectSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.qc.defect.MesQcDefectDO;
import jakarta.validation.Valid;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;

/**
 * MES 缺陷类型 Service 接口
 *
 * @author 芋道源码
 */
public interface MesQcDefectService {

    /**
     * 创建缺陷类型
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createDefect(@Valid MesQcDefectSaveReqVO createReqVO);

    /**
     * 更新缺陷类型
     *
     * @param updateReqVO 更新信息
     */
    void updateDefect(@Valid MesQcDefectSaveReqVO updateReqVO);

    /**
     * 删除缺陷类型
     *
     * @param id 编号
     */
    void deleteDefect(Long id);

    /**
     * 获得缺陷类型
     *
     * @param id 编号
     * @return 缺陷类型
     */
    MesQcDefectDO getDefect(Long id);

    /**
     * 获得缺陷类型分页
     *
     * @param pageReqVO 分页查询
     * @return 缺陷类型分页
     */
    PageResult<MesQcDefectDO> getDefectPage(MesQcDefectPageReqVO pageReqVO);

    /**
     * 获得缺陷类型列表
     *
     * @return 缺陷类型列表
     */
    List<MesQcDefectDO> getDefectList();

    /**
     * 获得缺陷类型列表
     *
     * @param ids 编号数组
     * @return 缺陷类型列表
     */
    List<MesQcDefectDO> getDefectList(Collection<Long> ids);

    /**
     * 获得缺陷类型 Map
     *
     * @param ids 编号数组
     * @return 缺陷类型 Map
     */
    default Map<Long, MesQcDefectDO> getDefectMap(Collection<Long> ids) {
        return convertMap(getDefectList(ids), MesQcDefectDO::getId);
    }

}
