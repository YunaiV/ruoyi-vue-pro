package cn.iocoder.yudao.module.crm.service.contact;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.crm.controller.admin.contact.vo.ContactCreateReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.contact.vo.ContactExportReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.contact.vo.ContactPageReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.contact.vo.ContactUpdateReqVO;
import cn.iocoder.yudao.module.crm.convert.contact.ContactConvert;
import cn.iocoder.yudao.module.crm.dal.dataobject.contact.ContactDO;
import cn.iocoder.yudao.module.crm.dal.mysql.contact.ContactMapper;
import cn.iocoder.yudao.module.crm.framework.core.annotations.CrmPermission;
import cn.iocoder.yudao.module.crm.framework.enums.CrmBizTypeEnum;
import cn.iocoder.yudao.module.crm.framework.enums.CrmPermissionLevelEnum;
import cn.iocoder.yudao.module.crm.service.permission.CrmPermissionService;
import cn.iocoder.yudao.module.crm.service.permission.bo.CrmPermissionCreateReqBO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.crm.enums.ErrorCodeConstants.CONTACT_NOT_EXISTS;

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
    private CrmPermissionService crmPermissionService;

    @Override // TODO @zyna：新增和修改时，关联字段要校验，例如说 直属上级，是不是真的存在；
    public Long createContact(ContactCreateReqVO createReqVO, Long userId) {
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
    @CrmPermission(bizType = CrmBizTypeEnum.CRM_CONTACTS, level = CrmPermissionLevelEnum.WRITE)
    public void updateContact(ContactUpdateReqVO updateReqVO) {
        // 校验存在
        validateContactExists(updateReqVO.getId());
        // 更新
        ContactDO updateObj = ContactConvert.INSTANCE.convert(updateReqVO);
        contactMapper.updateById(updateObj);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CrmPermission(bizType = CrmBizTypeEnum.CRM_CONTACTS, level = CrmPermissionLevelEnum.WRITE)
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
    @CrmPermission(bizType = CrmBizTypeEnum.CRM_CONTACTS, level = CrmPermissionLevelEnum.READ)
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

    @Override
    public List<ContactDO> allContactList() {
        return contactMapper.selectList();
    }

}
