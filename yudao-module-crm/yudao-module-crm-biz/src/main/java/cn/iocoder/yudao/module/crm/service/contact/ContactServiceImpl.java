package cn.iocoder.yudao.module.crm.service.contact;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.crm.controller.admin.contact.vo.ContactBaseVO;
import cn.iocoder.yudao.module.crm.controller.admin.contact.vo.ContactCreateReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.contact.vo.ContactPageReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.contact.vo.ContactUpdateReqVO;
import cn.iocoder.yudao.module.crm.convert.contact.ContactConvert;
import cn.iocoder.yudao.module.crm.dal.dataobject.contact.ContactDO;
import cn.iocoder.yudao.module.crm.dal.mysql.contact.ContactMapper;
import cn.iocoder.yudao.module.crm.framework.core.annotations.CrmPermission;
import cn.iocoder.yudao.module.crm.framework.enums.CrmBizTypeEnum;
import cn.iocoder.yudao.module.crm.framework.enums.CrmPermissionLevelEnum;
import cn.iocoder.yudao.module.crm.service.customer.CrmCustomerService;
import cn.iocoder.yudao.module.crm.service.permission.CrmPermissionService;
import cn.iocoder.yudao.module.crm.service.permission.bo.CrmPermissionCreateReqBO;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.crm.enums.ErrorCodeConstants.CONTACT_NOT_EXISTS;
import static cn.iocoder.yudao.module.crm.enums.ErrorCodeConstants.CUSTOMER_NOT_EXISTS;
import static cn.iocoder.yudao.module.system.enums.ErrorCodeConstants.USER_NOT_EXISTS;

/**
 * crm联系人 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class ContactServiceImpl implements ContactService {

    @Resource
    private ContactMapper contactMapper;

    @Resource
    private CrmCustomerService customerService;

    @Resource
    private AdminUserApi adminUserApi;

    @Resource
    private CrmPermissionService crmPermissionService;

    @Override
    public Long createContact(ContactCreateReqVO createReqVO, Long userId) {
        //@todo
        //校验
        validateDataExist(createReqVO);
        // 插入
        ContactDO contact = ContactConvert.INSTANCE.convert(createReqVO);
        contactMapper.insert(contact);

        // 创建数据权限
        crmPermissionService.createPermission(new CrmPermissionCreateReqBO().setBizType(CrmBizTypeEnum.CRM_CONTACTS.getType())
                .setBizId(contact.getId()).setUserId(userId).setLevel(CrmPermissionLevelEnum.OWNER.getLevel())); // 设置当前操作的人为负责人

        // 返回
        return contact.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CrmPermission(bizType = CrmBizTypeEnum.CRM_CONTACTS, bizId = "#updateReqVO.id", level = CrmPermissionLevelEnum.WRITE)
    public void updateContact(ContactUpdateReqVO updateReqVO) {
        // 校验存在
        validateContactExists(updateReqVO.getId());
        validateDataExist(updateReqVO);
        // 更新
        ContactDO updateObj = ContactConvert.INSTANCE.convert(updateReqVO);
        contactMapper.updateById(updateObj);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CrmPermission(bizType = CrmBizTypeEnum.CRM_CONTACTS, bizId = "#id", level = CrmPermissionLevelEnum.WRITE)
    public void deleteContact(Long id) {
        // 校验存在
        validateContactExists(id);
        // 删除
        contactMapper.deleteById(id);
    }

    private void validateContactExists(Long id) {
        if (contactMapper.selectById(id) == null) {
            throw exception(CONTACT_NOT_EXISTS);
        }
    }

    @Override
    @CrmPermission(bizType = CrmBizTypeEnum.CRM_CONTACTS, bizId = "#id", level = CrmPermissionLevelEnum.READ)
    public ContactDO getContact(Long id) {
        return contactMapper.selectById(id);
    }

    @Override
    public List<ContactDO> getContactList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return ListUtil.empty();
        }
        return contactMapper.selectBatchIds(ids);
    }

    @Override
    public PageResult<ContactDO> getContactPage(ContactPageReqVO pageReqVO) {
        return contactMapper.selectPage(pageReqVO);
    }

    @Override
    public List<ContactDO> getContactList(ContactPageReqVO exportReqVO) {
        return contactMapper.selectList(exportReqVO);
    }

    @Override
    public List<ContactDO> getContactList() {
        return contactMapper.selectList();
    }

    private void validateDataExist(ContactBaseVO contactBaseVO){
        //1.校验客户
        if (contactBaseVO.getCustomerId() != null) {
            Optional.ofNullable(customerService.getCustomer(contactBaseVO.getCustomerId())).orElseThrow(() -> exception(CUSTOMER_NOT_EXISTS));
        }
        //2.校验负责人
        if (contactBaseVO.getOwnerUserId() != null) {
            Optional.ofNullable(adminUserApi.getUser(contactBaseVO.getOwnerUserId())).orElseThrow(() -> exception(USER_NOT_EXISTS));
        }
        //3.直属上级
        if (contactBaseVO.getParentId() != null) {
            Optional.ofNullable(contactMapper.selectById(contactBaseVO.getParentId())).orElseThrow(() -> exception(CONTACT_NOT_EXISTS));
        }
    }
}
