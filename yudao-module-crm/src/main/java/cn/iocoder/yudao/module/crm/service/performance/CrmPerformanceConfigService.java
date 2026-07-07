package cn.iocoder.yudao.module.crm.service.performance;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.crm.controller.admin.performance.vo.config.CrmPerformanceConfigPageReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.performance.vo.config.CrmPerformanceConfigSaveReqVO;
import cn.iocoder.yudao.module.crm.dal.dataobject.performance.CrmPerformanceConfigDO;
import javax.validation.Valid;

import java.util.Collection;
import java.util.List;

/**
 * CRM 业绩目标设置 Service 接口
 *
 * @author 芋道源码
 */
public interface CrmPerformanceConfigService {

    /**
     * 创建业绩目标
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createPerformanceConfig(@Valid CrmPerformanceConfigSaveReqVO createReqVO);

    /**
     * 更新业绩目标
     *
     * @param updateReqVO 更新信息
     */
    void updatePerformanceConfig(@Valid CrmPerformanceConfigSaveReqVO updateReqVO);

    /**
     * 删除业绩目标
     *
     * @param id 编号
     */
    void deletePerformanceConfig(Long id);

    /**
     * 获得业绩目标
     *
     * @param id 编号
     * @return 业绩目标
     */
    CrmPerformanceConfigDO getPerformanceConfig(Long id);

    /**
     * 获得业绩目标分页
     *
     * @param pageReqVO 分页查询
     * @return 业绩目标分页
     */
    PageResult<CrmPerformanceConfigDO> getPerformanceConfigPage(CrmPerformanceConfigPageReqVO pageReqVO);

    /**
     * 根据对象、年份、目标类型获得业绩目标配置
     *
     * @param objectId 对象编号
     * @param objectType 对象类型
     * @param year 年份
     * @param bizType 业务类型
     * @return 业绩目标配置
     */
    CrmPerformanceConfigDO getPerformanceConfig(Long objectId, Integer objectType, Integer year, Integer bizType);

    /**
     * 根据对象数组、年份、目标类型获得业绩目标配置列表
     *
     * @param objectIds 对象编号数组
     * @param objectType 对象类型
     * @param year 年份
     * @param bizType 业务类型
     * @return 业绩目标配置列表
     */
    List<CrmPerformanceConfigDO> getPerformanceConfigList(Collection<Long> objectIds, Integer objectType, Integer year,
                                                          Integer bizType);

}
