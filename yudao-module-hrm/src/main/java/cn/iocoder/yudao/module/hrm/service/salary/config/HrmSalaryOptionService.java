package cn.iocoder.yudao.module.hrm.service.salary.config;

import cn.iocoder.yudao.module.hrm.controller.admin.salary.vo.option.HrmSalaryOptionSaveReqVO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.salary.config.HrmSalaryOptionDO;

import java.util.List;

/**
 * HRM 工资表薪资项 Service 接口
 *
 * @author 芋道源码
 */
public interface HrmSalaryOptionService {

    /**
     * 创建自定义薪资项
     *
     * @param createReqVO 创建信息
     * @return 薪资项编号
     */
    Long createSalaryOption(HrmSalaryOptionSaveReqVO createReqVO);

    /**
     * 更新薪资项启用状态
     *
     * @param id      薪资项编号
     * @param enabled 是否启用
     */
    void updateSalaryOptionEnabled(Long id, Boolean enabled);

    /**
     * 更新薪资项显示状态
     *
     * @param id      薪资项编号
     * @param visible 是否显示
     */
    void updateSalaryOptionVisible(Long id, Boolean visible);

    /**
     * 删除自定义薪资项
     *
     * @param id 薪资项编号
     */
    void deleteSalaryOption(Long id);

    /**
     * 同步标准薪资项
     */
    void syncSalaryOption();

    /**
     * 获得已启用的薪资项列表
     *
     * @param adjustable 是否只查询可调薪项
     * @return 薪资项列表
     */
    default List<HrmSalaryOptionDO> getSalaryOptionList(Boolean adjustable) {
        return getSalaryOptionList(adjustable, null);
    }

    /**
     * 获得已启用的薪资项列表
     *
     * @param adjustable 是否只查询可调薪项
     * @param visible 是否只查询显示项
     * @return 薪资项列表
     */
    List<HrmSalaryOptionDO> getSalaryOptionList(Boolean adjustable, Boolean visible);

    /**
     * 获得薪资项列表
     *
     * @return 薪资项列表
     */
    List<HrmSalaryOptionDO> getSalaryOptionList();

}
