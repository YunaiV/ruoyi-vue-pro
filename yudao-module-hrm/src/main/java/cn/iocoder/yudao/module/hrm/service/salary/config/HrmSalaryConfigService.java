package cn.iocoder.yudao.module.hrm.service.salary.config;

import cn.iocoder.yudao.module.hrm.controller.admin.salary.vo.config.HrmSalaryConfigCreateReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.salary.vo.config.HrmSalaryConfigUpdateReqVO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.salary.config.HrmSalaryConfigDO;

/**
 * HRM 计薪配置 Service 接口
 *
 * @author 芋道源码
 */
public interface HrmSalaryConfigService {

    /**
     * 创建计薪配置
     *
     * @param createReqVO 计薪配置信息
     * @return 配置编号
     */
    Long createSalaryConfig(HrmSalaryConfigCreateReqVO createReqVO);

    /**
     * 更新计薪配置
     *
     * @param updateReqVO 计薪配置信息
     */
    void updateSalaryConfig(HrmSalaryConfigUpdateReqVO updateReqVO);

    /**
     * 获得计薪配置
     *
     * @return 计薪配置
     */
    HrmSalaryConfigDO getSalaryConfig();

}
