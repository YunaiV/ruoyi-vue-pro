package cn.iocoder.yudao.module.crm.framework.core.aop;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.core.KeyValue;
import cn.iocoder.yudao.module.crm.dal.dataobject.business.CrmBusinessDO;
import cn.iocoder.yudao.module.crm.dal.dataobject.contact.ContactDO;
import cn.iocoder.yudao.module.crm.dal.dataobject.contract.ContractDO;
import cn.iocoder.yudao.module.crm.dal.dataobject.customer.CrmCustomerDO;
import cn.iocoder.yudao.module.crm.framework.core.annotations.CrmPermission;
import cn.iocoder.yudao.module.crm.framework.enums.CrmEnum;
import cn.iocoder.yudao.module.crm.framework.enums.OperationTypeEnum;
import cn.iocoder.yudao.module.crm.framework.vo.CrmTransferBaseVO;
import cn.iocoder.yudao.module.crm.service.business.CrmBusinessService;
import cn.iocoder.yudao.module.crm.service.contact.ContactService;
import cn.iocoder.yudao.module.crm.service.contract.ContractService;
import cn.iocoder.yudao.module.crm.service.customer.CrmCustomerService;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Collection;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.json.JsonUtils.toJsonString;
import static cn.iocoder.yudao.module.crm.enums.ErrorCodeConstants.*;

/**
 * Crm 数据权限校验 AOP 切面
 *
 * @author HUIHUI
 */
@Component
@Aspect
@Slf4j
public class CrmPermissionAspect {

    /**
     * 用户编号
     */
    private static final ThreadLocal<Long> USER_ID = new ThreadLocal<>();
    /**
     * 用户类型
     */
    private static final ThreadLocal<Integer> USER_TYPE = new ThreadLocal<>();
    /**
     * 操作数据编号
     */
    private static final ThreadLocal<Long> DATA_ID = new ThreadLocal<>();
    /**
     * Crm 转换数据 VO 数据
     */
    private static final ThreadLocal<CrmTransferBaseVO> CRM_TRANSFER_VO = new ThreadLocal<>();

    @Resource
    private CrmBusinessService crmBusinessService;
    @Resource
    private ContactService contactService;
    @Resource
    private ContractService contractService;
    @Resource
    private CrmCustomerService crmCustomerService;

    public static void setCrmTransferInfo(Long userId, Integer userType, Object crmTransferBaseVO) {
        USER_ID.set(userId);
        USER_TYPE.set(userType);
        CRM_TRANSFER_VO.set((CrmTransferBaseVO) crmTransferBaseVO);
    }

    public static void setCrmTransferInfo(Long userId, Integer userType) {
        USER_ID.set(userId);
        USER_TYPE.set(userType);
    }

    private static void clear() {
        USER_ID.remove();
        USER_TYPE.remove();
        DATA_ID.remove();
        CRM_TRANSFER_VO.remove();
    }

    @Before("@annotation(crmPermission)")
    public void doBefore(JoinPoint joinPoint, CrmPermission crmPermission) {
        try {
            Integer crmType = crmPermission.crmType().getType();
            Integer operationType = crmPermission.operationType().getType();
            Long id = DATA_ID.get();// 获取操作数据的编号
            KeyValue<Collection<Long>, Collection<Long>> keyValue = new KeyValue<>(); // 数据权限 key 只读，value 读写
            // 客户
            if (ObjUtil.equal(crmType, CrmEnum.CRM_CUSTOMER.getType())) {
                CrmCustomerDO customer = crmCustomerService.getCustomer(id);
                if (customer == null) {
                    throw exception(CUSTOMER_NOT_EXISTS);
                }
                // 如果是自己则直接过
                if (ObjUtil.equal(customer.getOwnerUserId(), USER_ID.get())) {
                    return;
                }
                new KeyValue<>(customer.getRoUserIds(), customer.getRwUserIds());
            }
            // 联系人
            if (ObjUtil.equal(crmType, CrmEnum.CRM_CONTACTS.getType())) {
                ContactDO contact = contactService.getContact(id);
                if (contact == null) {
                    throw exception(CONTACT_NOT_EXISTS);
                }
                // 如果是自己则直接过
                if (ObjUtil.equal(contact.getOwnerUserId(), USER_ID.get())) {
                    return;
                }
                new KeyValue<>(contact.getRoUserIds(), contact.getRwUserIds());
            }
            // 商机
            if (ObjUtil.equal(crmType, CrmEnum.CRM_BUSINESS.getType())) {
                CrmBusinessDO business = crmBusinessService.getBusiness(id);
                if (business == null) {
                    throw exception(BUSINESS_NOT_EXISTS);
                }
                // 如果是自己则直接过
                if (ObjUtil.equal(business.getOwnerUserId(), USER_ID.get())) {
                    return;
                }
                new KeyValue<>(business.getRoUserIds(), business.getRwUserIds());
            }
            // 合同
            if (ObjUtil.equal(crmType, CrmEnum.CRM_CONTRACT.getType())) {
                ContractDO contract = contractService.getContract(id);
                if (contract == null) {
                    throw exception(CONTRACT_NOT_EXISTS);
                }
                // 如果是自己则直接过
                if (ObjUtil.equal(contract.getOwnerUserId(), USER_ID.get())) {
                    return;
                }
                new KeyValue<>(contract.getRoUserIds(), contract.getRwUserIds());
            }
            // 1. 校验是否有读权限
            if (OperationTypeEnum.isRead(operationType)) {
                // 校验该数据当前用户是否可读
                boolean isRead = CollUtil.contains(keyValue.getKey(), item -> ObjUtil.equal(id, USER_ID.get()))
                        || CollUtil.contains(keyValue.getValue(), item -> ObjUtil.equal(id, USER_ID.get()));
                if (isRead) {
                    return;
                }
                throw exception(CONTRACT_NOT_EXISTS);
            }
            // 2. 校验是否有编辑权限
            if (OperationTypeEnum.isEdit(operationType)) {
                // 校验该数据当前用户是否可读写
                if (CollUtil.contains(keyValue.getValue(), item -> ObjUtil.equal(id, USER_ID.get()))) {
                    return;
                }
                throw exception(CONTRACT_NOT_EXISTS);
            }
        } catch (Exception ex) {
            log.error("[doBefore][crmPermission({}) 数据校验错误]", toJsonString(crmPermission), ex);
        } finally {
            clear();
        }
    }

}
