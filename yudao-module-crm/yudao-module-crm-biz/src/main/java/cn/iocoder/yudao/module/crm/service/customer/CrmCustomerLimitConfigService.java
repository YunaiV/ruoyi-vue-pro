package cn.iocoder.yudao.module.crm.service.customer;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.crm.controller.admin.customer.vo.limitconfig.CrmCustomerLimitConfigPageReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.customer.vo.limitconfig.CrmCustomerLimitConfigSaveReqVO;
import cn.iocoder.yudao.module.crm.dal.dataobject.customer.CrmCustomerLimitConfigDO;

import javax.validation.Valid;
import java.util.List;

/**
 * 客户限制配置 Service 接口
 *
 * @author Wanwan
 */
public interface CrmCustomerLimitConfigService {

    /**
     * 创建客户限制配置
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createCustomerLimitConfig(@Valid CrmCustomerLimitConfigSaveReqVO createReqVO);

    /**
     * 更新客户限制配置
     *
     * @param updateReqVO 更新信息
     */
    void updateCustomerLimitConfig(@Valid CrmCustomerLimitConfigSaveReqVO updateReqVO);

    /**
     * 删除客户限制配置
     *
     * @param id 编号
     */
    void deleteCustomerLimitConfig(Long id);

    /**
     * 获得客户限制配置
     *
     * @param id 编号
     * @return 客户限制配置
     */
    CrmCustomerLimitConfigDO getCustomerLimitConfig(Long id);

    /**
     * 获得客户限制配置分页
     *
     * @param pageReqVO 分页查询
     * @return 客户限制配置分页
     */
    PageResult<CrmCustomerLimitConfigDO> getCustomerLimitConfigPage(CrmCustomerLimitConfigPageReqVO pageReqVO);

    /**
     * 查询用户对应的配置列表
     *
     * @param type   类型
     * @param userId 用户类型
     */
    List<CrmCustomerLimitConfigDO> getCustomerLimitConfigListByUserId(Integer type, Long userId);

}
