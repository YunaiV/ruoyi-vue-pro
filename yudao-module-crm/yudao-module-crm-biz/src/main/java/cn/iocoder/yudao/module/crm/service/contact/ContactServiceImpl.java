package cn.iocoder.yudao.module.crm.service.contact;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.crm.controller.admin.contact.vo.*;
import cn.iocoder.yudao.module.crm.convert.contact.ContactConvert;
import cn.iocoder.yudao.module.crm.dal.dataobject.contact.ContactDO;
import cn.iocoder.yudao.module.crm.dal.mysql.contact.ContactMapper;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.crm.enums.ErrorCodeConstants.*;
import static cn.iocoder.yudao.module.crm.framework.utils.CrmPermissionUtils.isReadAndWrite;

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
    private AdminUserApi adminUserApi;

    @Override
    public Long createContact(ContactCreateReqVO createReqVO) {
        // TODO @customerId：需要校验存在
        // 插入
        ContactDO contact = ContactConvert.INSTANCE.convert(createReqVO);
        contactMapper.insert(contact);
        // 返回
        return contact.getId();
    }

    @Override
    public void updateContact(ContactUpdateReqVO updateReqVO) {
        // 校验存在
        validateContactExists(updateReqVO.getId());
        // TODO @customerId：需要校验存在

        // 更新
        ContactDO updateObj = ContactConvert.INSTANCE.convert(updateReqVO);
        contactMapper.updateById(updateObj);
    }

    @Override
    public void deleteContact(Long id) {
        // 校验存在
        validateContactExists(id);
        // 删除
        contactMapper.deleteById(id);
    }

    private ContactDO validateContactExists(Long id) {
        ContactDO contact = contactMapper.selectById(id);
        if (contact == null) {
            throw exception(CONTACT_NOT_EXISTS);
        }
        return contact;
    }

    @Override
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
    public List<ContactDO> getContactList(ContactExportReqVO exportReqVO) {
        return contactMapper.selectList(exportReqVO);
    }

    // TODO @puhui999：参考 CrmBusinessServiceImpl 修改建议
    @Override
    public void contactTransfer(CrmContactTransferReqVO reqVO, Long userId) {
        // 1. 校验联系人是否存在
        ContactDO contact = validateContactExists(reqVO.getId());
        // 1.2. 校验用户是否拥有读写权限
        if (!isReadAndWrite(contact.getRwUserIds(), userId)) {
            throw exception(CONTACT_TRANSFER_FAIL_PERMISSION_DENIED);
        }
        // 2. 校验新负责人是否存在
        AdminUserRespDTO user = adminUserApi.getUser(reqVO.getOwnerUserId());
        if (user == null) {
            throw exception(CONTACT_TRANSFER_FAIL_OWNER_USER_NOT_EXISTS);
        }

        // 3. 更新新的负责人
        ContactDO updateContact = ContactConvert.INSTANCE.convert(contact, reqVO, userId);
        contactMapper.updateById(updateContact);

        // 4. TODO 记录联系人转移日志

    }

}
