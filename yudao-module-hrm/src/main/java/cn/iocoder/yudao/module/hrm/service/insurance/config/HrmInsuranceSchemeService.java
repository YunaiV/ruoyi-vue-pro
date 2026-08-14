package cn.iocoder.yudao.module.hrm.service.insurance.config;

import cn.iocoder.yudao.module.hrm.controller.admin.insurance.vo.scheme.HrmInsuranceSchemeSaveReqVO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.insurance.config.HrmInsuranceSchemeProjectDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.insurance.config.HrmInsuranceSchemeDO;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;

/**
 * HRM 社保方案 Service 接口
 *
 * @author 芋道源码
 */
public interface HrmInsuranceSchemeService {

    /**
     * 创建社保方案
     *
     * @param reqVO 社保方案信息
     * @return 社保方案编号
     */
    Long createScheme(HrmInsuranceSchemeSaveReqVO reqVO);

    /**
     * 更新社保方案
     *
     * @param reqVO 社保方案信息
     */
    void updateScheme(HrmInsuranceSchemeSaveReqVO reqVO);

    /**
     * 删除社保方案
     *
     * @param id 社保方案编号
     */
    void deleteScheme(Long id);

    /**
     * 获得社保方案
     *
     * @param id 社保方案编号
     * @return 社保方案
     */
    HrmInsuranceSchemeDO getScheme(Long id);

    /**
     * 根据名称获得社保方案
     *
     * @param name 方案名称
     * @return 社保方案
     */
    HrmInsuranceSchemeDO getSchemeByName(String name);

    /**
     * 校验社保方案是否存在
     *
     * @param id 社保方案编号
     * @return 社保方案
     */
    HrmInsuranceSchemeDO validateSchemeExists(Long id);

    /**
     * 获得社保方案列表
     *
     * @return 社保方案列表
     */
    List<HrmInsuranceSchemeDO> getSchemeList();

    /**
     * 获得指定编号的社保方案列表
     *
     * @param ids 社保方案编号集合
     * @return 社保方案列表
     */
    List<HrmInsuranceSchemeDO> getSchemeListByIds(Collection<Long> ids);

    /**
     * 获得指定编号的社保方案 Map
     *
     * @param ids 社保方案编号集合
     * @return 社保方案 Map
     */
    default Map<Long, HrmInsuranceSchemeDO> getSchemeMap(Collection<Long> ids) {
        return convertMap(getSchemeListByIds(ids), HrmInsuranceSchemeDO::getId);
    }

    /**
     * 获得指定地区的社保方案列表
     *
     * @param areaId 地区编号
     * @return 社保方案列表
     */
    List<HrmInsuranceSchemeDO> getSchemeListByAreaId(Integer areaId);

    /**
     * 获得社保方案项目列表
     *
     * @param schemeId 社保方案编号
     * @return 社保方案项目列表
     */
    List<HrmInsuranceSchemeProjectDO> getSchemeProjectList(Long schemeId);

    /**
     * 获得指定方案的项目 Map
     *
     * @param schemeIds 社保方案编号集合
     * @return 社保方案编号与项目列表的映射
     */
    Map<Long, List<HrmInsuranceSchemeProjectDO>> getSchemeProjectListMap(Collection<Long> schemeIds);

}
