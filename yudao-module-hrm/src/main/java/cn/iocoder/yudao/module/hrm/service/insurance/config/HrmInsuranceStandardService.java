package cn.iocoder.yudao.module.hrm.service.insurance.config;

import cn.iocoder.yudao.module.hrm.controller.admin.insurance.vo.standard.HrmInsuranceStandardProjectRespVO;
import cn.iocoder.yudao.module.hrm.controller.admin.insurance.vo.standard.HrmInsuranceStandardTypeRespVO;

import java.util.List;

/**
 * HRM 标准参保数据 Service 接口
 *
 * @author 芋道源码
 */
public interface HrmInsuranceStandardService {

    /**
     * 获得指定地区的标准参保方案类型
     *
     * @param areaId 地区编号
     * @return 标准参保方案类型列表
     */
    List<HrmInsuranceStandardTypeRespVO> getStandardTypeList(Integer areaId);

    /**
     * 获得指定地区和方案类型的标准参保项目
     *
     * @param areaId 地区编号
     * @param typeCode 方案类型编码
     * @return 标准参保项目列表
     */
    List<HrmInsuranceStandardProjectRespVO> getStandardProjectList(Integer areaId, String typeCode);

}
