package cn.iocoder.yudao.module.crm.service.contract;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.crm.controller.admin.contract.vo.CrmContractCreateReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.contract.vo.CrmContractPageReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.contract.vo.CrmContractUpdateReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.contract.vo.CrmContractTransferReqVO;
import cn.iocoder.yudao.module.crm.dal.dataobject.contract.CrmContractDO;
import cn.iocoder.yudao.module.crm.dal.dataobject.customer.CrmCustomerDO;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;

/**
 * CRM 合同 Service 接口
 *
 * @author dhb52
 */
public interface CrmContractService {

    /**
     * 创建合同
     *
     * @param createReqVO 创建信息
     * @param userId      用户编号
     * @return 编号
     */
    Long createContract(@Valid CrmContractCreateReqVO createReqVO, Long userId);

    /**
     * 更新合同
     *
     * @param updateReqVO 更新信息
     */
    void updateContract(@Valid CrmContractUpdateReqVO updateReqVO);

    /**
     * 删除合同
     *
     * @param id 编号
     */
    void deleteContract(Long id);

    /**
     * 获得合同
     *
     * @param id 编号
     * @return 合同
     */
    CrmContractDO getContract(Long id);

    /**
     * 获得合同列表
     *
     * @param ids 编号
     * @return 合同列表
     */
    List<CrmContractDO> getContractList(Collection<Long> ids);

    /**
     * 获得合同分页
     *
     * 数据权限：基于 {@link CrmContractDO} 读取
     *
     * @param pageReqVO 分页查询
     * @return 合同分页
     */
    PageResult<CrmContractDO> getContractPage(CrmContractPageReqVO pageReqVO);

    /**
     * 获得合同分页，基于指定客户
     *
     * 数据权限：基于 {@link CrmCustomerDO} 读取
     *
     * @param pageReqVO 分页查询
     * @return 联系人分页
     */
    PageResult<CrmContractDO> getContractPageByCustomer(CrmContractPageReqVO pageReqVO);

    /**
     * 合同转移
     *
     * @param reqVO  请求
     * @param userId 用户编号
     */
    void transferContract(CrmContractTransferReqVO reqVO, Long userId);

}
