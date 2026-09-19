package cn.iocoder.yudao.module.oa.service.contact;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.number.NumberUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.common.util.string.StrUtils;
import cn.iocoder.yudao.module.oa.controller.admin.contact.vo.OaContactPageReqVO;
import cn.iocoder.yudao.module.oa.controller.admin.contact.vo.OaContactSaveReqVO;
import cn.iocoder.yudao.module.oa.dal.dataobject.contact.OaContactDO;
import cn.iocoder.yudao.module.oa.dal.dataobject.contact.OaContactShareDO;
import cn.iocoder.yudao.module.oa.dal.mysql.contact.OaContactMapper;
import cn.iocoder.yudao.module.oa.dal.mysql.contact.OaContactShareMapper;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.filterList;
import static cn.iocoder.yudao.module.oa.enums.ErrorCodeConstants.CONTACT_ACCESS_DENIED;
import static cn.iocoder.yudao.module.oa.enums.ErrorCodeConstants.CONTACT_NOT_EXISTS;

/**
 * OA 外部联系人 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class OaContactServiceImpl implements OaContactService {

    @Resource
    private OaContactMapper contactMapper;
    @Resource
    private OaContactShareMapper contactShareMapper;

    @Resource
    @Lazy // 延迟加载，避免循环依赖
    private OaContactCategoryService contactCategoryService;

    @Resource
    private AdminUserApi adminUserApi;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createContact(OaContactSaveReqVO createReqVO, Long userId) {
        // 1. 校验联系人分类归属
        if (createReqVO.getCategoryId() != null) {
            contactCategoryService.validateContactCategory(createReqVO.getCategoryId(), userId);
        }

        // 2. 新增联系人
        OaContactDO contact = BeanUtils.toBean(createReqVO, OaContactDO.class)
                .setPinyin(StrUtils.toPinyin(createReqVO.getName()));
        contactMapper.insert(contact);

        // 3. 创建人也保存持有关系
        // 特殊：创建人和接收人均通过关联持有联系人，删除时只移除本人关联，最后一人移除后才删除正文。
        //      因此创建时也需保存本人关联，并标记为已处理，使其直接进入“我的联系人”。
        contactShareMapper.insert(new OaContactShareDO().setContactId(contact.getId()).setUserId(userId)
                .setCategoryId(createReqVO.getCategoryId()).setHandleStatus(true));
        return contact.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateContact(OaContactSaveReqVO updateReqVO, Long userId) {
        // 1.1 校验联系人属于当前用户
        validateContactOwner(updateReqVO.getId(), userId);
        // 1.2 校验联系人分类归属
        if (updateReqVO.getCategoryId() != null) {
            contactCategoryService.validateContactCategory(updateReqVO.getCategoryId(), userId);
        }

        // 2. 更新联系人
        contactMapper.updateForSave(BeanUtils.toBean(updateReqVO, OaContactDO.class)
                .setPinyin(StrUtils.toPinyin(updateReqVO.getName())));

        // 3. 同步本人关联的分类
        OaContactShareDO share = contactShareMapper.selectByContactIdAndUserId(updateReqVO.getId(), userId);
        contactShareMapper.updateHandleStatusAndCategoryIdById(new OaContactShareDO().setId(share.getId())
                .setCategoryId(updateReqVO.getCategoryId()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteContact(Long id, Long userId) {
        // 1. 校验联系人属于当前用户
        validateContactOwner(id, userId);

        // 2. 移除本人关联，最后一个持有人离开时删除正文
        deleteReceivedContact(id, userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteReceivedContact(Long contactId, Long userId) {
        // 1.1 校验联系人存在
        validateContactExists(contactId);
        // 1.2 校验当前用户接收了该联系人
        OaContactShareDO contactShare = contactShareMapper.selectByContactIdAndUserId(contactId, userId);
        if (contactShare == null) {
            throw exception(CONTACT_ACCESS_DENIED);
        }

        // 2. 删除联系人共享关系
        contactShareMapper.deleteByContactIdAndUserId(contactId, userId);

        // 3. 最后一个持有人离开时才删除正文
        if (CollUtil.isEmpty(contactShareMapper.selectListByContactId(contactId))) {
            contactMapper.deleteById(contactId);
        }
    }

    @Override
    public OaContactDO getContact(Long id, Long userId) {
        // 1. 校验联系人存在
        OaContactDO contact = validateContactExists(id);

        // 2. 所有用户均通过本人关联校验访问权限
        if (contactShareMapper.selectByContactIdAndUserId(id, userId) == null) {
            throw exception(CONTACT_ACCESS_DENIED);
        }
        return contact;
    }

    @Override
    public PageResult<OaContactDO> getMyContactPage(OaContactPageReqVO pageReqVO, Long userId) {
        // 1. 查询本人已处理且符合本人分类的共享关系
        List<OaContactShareDO> shares = contactShareMapper.selectListByUserId(userId, true,
                pageReqVO.getCategoryId());
        List<Long> contactIds = convertList(shares, OaContactShareDO::getContactId);
        // 2. 查询本人创建及已处理接收的联系人
        return CollUtil.isEmpty(contactIds) ? PageResult.empty() : contactMapper.selectReceivedPage(pageReqVO, contactIds);
    }

    @Override
    public PageResult<OaContactDO> getReceivedContactPage(OaContactPageReqVO pageReqVO, Long userId) {
        // 1. 按接收人的分类和处理状态查询共享关系
        List<OaContactShareDO> shares = contactShareMapper.selectListByUserId(userId,
                pageReqVO.getHandleStatus(), pageReqVO.getCategoryId());
        // 创建人初始化的本人关联不是收到的共享
        shares = filterList(shares, share -> ObjectUtil.notEqual(NumberUtils.parseLong(share.getCreator()), userId));
        if (CollUtil.isEmpty(shares)) {
            return PageResult.empty();
        }
        // 2. 查询共享给本人的联系人
        List<Long> contactIds = convertList(shares, OaContactShareDO::getContactId);
        return contactMapper.selectReceivedPage(pageReqVO, contactIds);
    }

    @Override
    public PageResult<OaContactShareDO> getSharedContactPage(OaContactPageReqVO pageReqVO, Long userId) {
        // 1. 查询本人仍持有且符合本人分类的联系人
        List<Long> contactIds = convertList(contactShareMapper.selectListByUserId(userId, null,
                pageReqVO.getCategoryId()), OaContactShareDO::getContactId);
        if (CollUtil.isEmpty(contactIds)) {
            return PageResult.empty();
        }

        // 2. 按本人发出的共享关系分页，每个接收人一行
        return contactShareMapper.selectSharedPage(pageReqVO, userId, contactIds);
    }

    @Override
    public List<OaContactDO> getContactList(Collection<Long> ids) {
        return CollUtil.isEmpty(ids) ? Collections.emptyList() : contactMapper.selectByIds(ids);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void shareContact(Long contactId, Collection<Long> userIds, Long currentUserId) {
        // 1.1 校验当前用户持有联系人，接收人可以再共享
        getContact(contactId, currentUserId);
        // 1.2 校验共享接收人存在
        adminUserApi.validateUserList(userIds);
        // 1.3 查询已有共享接收人
        List<OaContactShareDO> shares = contactShareMapper.selectListByContactId(contactId);
        List<Long> sharedUserIds = convertList(shares, OaContactShareDO::getUserId);
        List<Long> createUserIds = filterList(convertSet(userIds, userId -> userId),
                userId -> ObjectUtil.notEqual(userId, currentUserId) && !sharedUserIds.contains(userId));
        if (CollUtil.isEmpty(createUserIds)) {
            return;
        }

        // 2. 新增尚未共享的接收人
        contactShareMapper.insertBatch(convertList(createUserIds, userId -> new OaContactShareDO()
                .setContactId(contactId).setUserId(userId).setHandleStatus(false)));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleContactShare(Long contactId, Long categoryId, Long userId) {
        // 1.1 校验共享关系存在
        OaContactShareDO contactShare = contactShareMapper.selectByContactIdAndUserId(contactId, userId);
        if (contactShare == null) {
            throw exception(CONTACT_ACCESS_DENIED);
        }
        // 1.2 创建人的分类通过联系人编辑维护，不能仅修改初始化的持有关系
        OaContactDO contact = validateContactExists(contactId);
        if (ObjectUtil.equal(contact.getCreator(), userId.toString())) {
            throw exception(CONTACT_ACCESS_DENIED);
        }
        // 1.3 校验接收人选择的分类属于本人
        if (categoryId != null) {
            contactCategoryService.validateContactCategory(categoryId, userId);
        }

        // 2. 更新处理状态和接收人分类
        OaContactShareDO updateObj = new OaContactShareDO().setId(contactShare.getId())
                .setHandleStatus(true).setCategoryId(categoryId);
        contactShareMapper.updateHandleStatusAndCategoryIdById(updateObj);
    }

    @Override
    public List<OaContactShareDO> getContactShareList(Collection<Long> contactIds) {
        if (CollUtil.isEmpty(contactIds)) {
            return Collections.emptyList();
        }
        return contactShareMapper.selectListByContactIds(contactIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void clearContactCategoryId(Long categoryId) {
        // 1. 清空创建人的联系人分类
        contactMapper.updateCategoryIdToNullByCategoryId(categoryId);
        // 2. 清空接收关系分类，不删除联系人或共享关系
        contactShareMapper.updateCategoryIdToNullByCategoryId(categoryId);
    }

    /**
     * 校验联系人存在且属于当前用户
     *
     * @param id 联系人编号
     * @param userId 用户编号
     * @return 联系人
     */
    private OaContactDO validateContactOwner(Long id, Long userId) {
        OaContactDO contact = getContact(id, userId);
        if (ObjectUtil.notEqual(NumberUtils.parseLong(contact.getCreator()), userId)) {
            throw exception(CONTACT_ACCESS_DENIED);
        }
        return contact;
    }

    /**
     * 校验联系人存在
     *
     * @param id 联系人编号
     * @return 联系人
     */
    private OaContactDO validateContactExists(Long id) {
        OaContactDO contact = contactMapper.selectById(id);
        if (contact == null) {
            throw exception(CONTACT_NOT_EXISTS);
        }
        return contact;
    }

}
