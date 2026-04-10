package cn.iocoder.yudao.module.mes.service.md.unitmeasure;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.mes.controller.admin.md.unitmeasure.vo.MesMdUnitMeasurePageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.md.unitmeasure.vo.MesMdUnitMeasureSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.unitmeasure.MesMdUnitMeasureDO;
import jakarta.validation.Valid;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;

/**
 * MES 计量单位 Service 接口
 *
 * @author 芋道源码
 */
public interface MesMdUnitMeasureService {

    /**
     * 创建计量单位
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createUnitMeasure(@Valid MesMdUnitMeasureSaveReqVO createReqVO);

    /**
     * 更新计量单位
     *
     * @param updateReqVO 更新信息
     */
    void updateUnitMeasure(@Valid MesMdUnitMeasureSaveReqVO updateReqVO);

    /**
     * 删除计量单位
     *
     * @param id 编号
     */
    void deleteUnitMeasure(Long id);

    /**
     * 获得计量单位
     *
     * @param id 编号
     * @return 计量单位
     */
    MesMdUnitMeasureDO getUnitMeasure(Long id);

    /**
     * 获得计量单位分页
     *
     * @param pageReqVO 分页查询
     * @return 计量单位分页
     */
    PageResult<MesMdUnitMeasureDO> getUnitMeasurePage(MesMdUnitMeasurePageReqVO pageReqVO);

    /**
     * 获得指定状态的计量单位列表
     *
     * @param status 状态
     * @return 计量单位列表
     */
    List<MesMdUnitMeasureDO> getUnitMeasureListByStatus(Integer status);

    /**
     * 获得计量单位列表
     *
     * @param ids 编号数组
     * @return 计量单位列表
     */
    List<MesMdUnitMeasureDO> getUnitMeasureList(Collection<Long> ids);

    /**
     * 获得计量单位 Map
     *
     * @param ids 编号数组
     * @return 计量单位 Map
     */
    default Map<Long, MesMdUnitMeasureDO> getUnitMeasureMap(Collection<Long> ids) {
        return convertMap(getUnitMeasureList(ids), MesMdUnitMeasureDO::getId);
    }

    /**
     * 根据编码获得计量单位
     *
     * @param code 单位编码
     * @return 计量单位
     */
    MesMdUnitMeasureDO getUnitMeasureByCode(String code);

}
