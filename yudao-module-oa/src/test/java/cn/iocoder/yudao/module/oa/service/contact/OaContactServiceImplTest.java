package cn.iocoder.yudao.module.oa.service.contact;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.security.core.LoginUser;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.oa.controller.admin.contact.vo.OaContactPageReqVO;
import cn.iocoder.yudao.module.oa.controller.admin.contact.vo.OaContactSaveReqVO;
import cn.iocoder.yudao.module.oa.dal.dataobject.contact.OaContactDO;
import cn.iocoder.yudao.module.oa.dal.dataobject.contact.OaContactShareDO;
import cn.iocoder.yudao.module.oa.dal.mysql.contact.OaContactMapper;
import cn.iocoder.yudao.module.oa.dal.mysql.contact.OaContactShareMapper;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomLongId;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static cn.iocoder.yudao.module.oa.enums.ErrorCodeConstants.CONTACT_ACCESS_DENIED;
import static cn.iocoder.yudao.module.oa.enums.ErrorCodeConstants.CONTACT_CATEGORY_NOT_EXISTS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;

/**
 * {@link OaContactServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
@Import(OaContactServiceImpl.class)
public class OaContactServiceImplTest extends BaseDbUnitTest {

    @Resource
    private OaContactServiceImpl contactService;

    @Resource
    private OaContactMapper contactMapper;
    @Resource
    private OaContactShareMapper contactShareMapper;

    @MockBean
    private AdminUserApi adminUserApi;
    @MockBean
    private OaContactCategoryService contactCategoryService;

    @AfterEach
    public void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    public void testCreateContact_autoFillCreator() {
        // 准备参数
        Long userId = randomLongId();
        OaContactSaveReqVO reqVO = new OaContactSaveReqVO().setName("张三");
        SecurityFrameworkUtils.setLoginUser(new LoginUser().setId(userId), new MockHttpServletRequest());

        // 调用
        Long id = contactService.createContact(reqVO, userId);

        // 断言：创建人由平台填充，随后可按创建人查询和访问
        assertEquals(userId.toString(), contactMapper.selectById(id).getCreator());
        assertEquals(id, contactService.getContact(id, userId).getId());
        assertEquals(id, CollUtil.getFirst(contactMapper.selectListByCreator(userId.toString())).getId());
    }

    @Test
    public void testDeleteContact_notCreator() {
        // mock 数据
        OaContactDO contact = randomContactDO(randomLongId());
        contactMapper.insert(contact);
        contactShareMapper.insert(randomPojo(OaContactShareDO.class, share -> {
            share.setId(null).setContactId(contact.getId())
                    .setUserId(Long.valueOf(contact.getCreator())).setCategoryId(contact.getCategoryId())
                    .setHandleStatus(true);
            share.setCreator(contact.getCreator());
        }));

        // 调用，并断言异常
        assertServiceException(() -> contactService.deleteContact(contact.getId(), randomLongId()),
                CONTACT_ACCESS_DENIED);
        assertEquals(contact.getCreator(), contactMapper.selectById(contact.getId()).getCreator());
    }

    @Test
    public void testUpdateContact_clearNullableFields() {
        // mock 数据
        Long userId = randomLongId();
        OaContactDO contact = randomContactDO(userId).setCategoryId(10L).setMobile("15600000000")
                .setRemark("重点客户");
        contactMapper.insert(contact);
        contactShareMapper.insert(randomPojo(OaContactShareDO.class, share -> {
            share.setId(null).setContactId(contact.getId())
                    .setUserId(Long.valueOf(contact.getCreator())).setCategoryId(contact.getCategoryId())
                    .setHandleStatus(true);
            share.setCreator(contact.getCreator());
        }));
        // 准备参数
        OaContactSaveReqVO reqVO = new OaContactSaveReqVO().setId(contact.getId()).setName("李四");

        // 调用
        contactService.updateContact(reqVO, userId);

        // 断言
        OaContactDO updatedContact = contactMapper.selectById(contact.getId());
        assertEquals("李四", updatedContact.getName());
        assertEquals(userId.toString(), updatedContact.getCreator());
        assertNull(updatedContact.getCategoryId());
        assertNull(updatedContact.getMobile());
        assertNull(updatedContact.getRemark());
    }

    @Test
    public void testShareContact_onlyCreatesNewReceivers() {
        // mock 数据
        Long userId = randomLongId();
        Long existingUserId = randomLongId();
        Long newUserId = randomLongId();
        OaContactDO contact = randomContactDO(userId);
        contactMapper.insert(contact);
        contactShareMapper.insert(randomPojo(OaContactShareDO.class, share -> {
            share.setId(null).setContactId(contact.getId())
                    .setUserId(Long.valueOf(contact.getCreator())).setCategoryId(contact.getCategoryId())
                    .setHandleStatus(true);
            share.setCreator(contact.getCreator());
        }));
        contactShareMapper.insert(new OaContactShareDO().setContactId(contact.getId())
                .setUserId(existingUserId).setHandleStatus(false));

        // 调用
        contactService.shareContact(contact.getId(), Arrays.asList(existingUserId, newUserId), userId);

        // 断言
        List<OaContactShareDO> shares = contactShareMapper.selectListByContactIds(
                Collections.singleton(contact.getId()));
        assertEquals(3, shares.size());
    }

    @Test
    public void testHandleContactShare_success() {
        // mock 数据
        Long receiverUserId = randomLongId();
        OaContactDO contact = randomContactDO(randomLongId());
        contactMapper.insert(contact);
        contactShareMapper.insert(randomPojo(OaContactShareDO.class, share -> {
            share.setId(null).setContactId(contact.getId())
                    .setUserId(Long.valueOf(contact.getCreator())).setCategoryId(contact.getCategoryId())
                    .setHandleStatus(true);
            share.setCreator(contact.getCreator());
        }));
        OaContactShareDO share = new OaContactShareDO().setContactId(contact.getId())
                .setUserId(receiverUserId).setHandleStatus(false);
        contactShareMapper.insert(share);

        // 调用
        contactService.handleContactShare(contact.getId(), 20L, receiverUserId);

        // 断言
        OaContactShareDO updatedShare = contactShareMapper.selectById(share.getId());
        assertTrue(updatedShare.getHandleStatus());
        assertEquals(20L, updatedShare.getCategoryId());
    }

    @Test
    public void testDeleteReceivedContact_success() {
        // mock 数据
        Long receiverUserId = randomLongId();
        OaContactDO contact = randomContactDO(randomLongId());
        contactMapper.insert(contact);
        contactShareMapper.insert(randomPojo(OaContactShareDO.class, share -> {
            share.setId(null).setContactId(contact.getId())
                    .setUserId(Long.valueOf(contact.getCreator())).setCategoryId(contact.getCategoryId())
                    .setHandleStatus(true);
            share.setCreator(contact.getCreator());
        }));
        contactShareMapper.insert(new OaContactShareDO().setContactId(contact.getId())
                .setUserId(receiverUserId).setHandleStatus(true));

        // 调用
        contactService.deleteReceivedContact(contact.getId(), receiverUserId);

        // 断言
        assertNull(contactShareMapper.selectByContactIdAndUserId(contact.getId(), receiverUserId));
        assertEquals(contact.getName(), contactMapper.selectById(contact.getId()).getName());
    }

    @Test
    public void testGetContactPage_mineIncludesHandledReceivedContact() {
        // mock 数据
        Long userId = randomLongId();
        OaContactDO ownedContact = randomContactDO(userId);
        contactMapper.insert(ownedContact);
        contactShareMapper.insert(randomPojo(OaContactShareDO.class, share -> {
            share.setId(null).setContactId(ownedContact.getId())
                    .setUserId(Long.valueOf(ownedContact.getCreator())).setCategoryId(ownedContact.getCategoryId())
                    .setHandleStatus(true);
            share.setCreator(ownedContact.getCreator());
        }));
        OaContactDO receivedContact = randomContactDO(randomLongId());
        contactMapper.insert(receivedContact);
        contactShareMapper.insert(randomPojo(OaContactShareDO.class, share -> {
            share.setId(null).setContactId(receivedContact.getId())
                    .setUserId(Long.valueOf(receivedContact.getCreator())).setCategoryId(receivedContact.getCategoryId())
                    .setHandleStatus(true);
            share.setCreator(receivedContact.getCreator());
        }));
        contactShareMapper.insert(new OaContactShareDO().setContactId(receivedContact.getId())
                .setUserId(userId).setHandleStatus(true));
        OaContactDO pendingContact = randomContactDO(randomLongId());
        contactMapper.insert(pendingContact);
        contactShareMapper.insert(randomPojo(OaContactShareDO.class, share -> {
            share.setId(null).setContactId(pendingContact.getId())
                    .setUserId(Long.valueOf(pendingContact.getCreator())).setCategoryId(pendingContact.getCategoryId())
                    .setHandleStatus(true);
            share.setCreator(pendingContact.getCreator());
        }));
        contactShareMapper.insert(new OaContactShareDO().setContactId(pendingContact.getId())
                .setUserId(userId).setHandleStatus(false));

        // 准备参数
        OaContactPageReqVO pageReqVO = new OaContactPageReqVO();

        // 调用
        List<OaContactDO> contacts = contactService.getMyContactPage(pageReqVO, userId).getList();

        // 断言
        assertEquals(2, contacts.size());
        assertTrue(contacts.stream().anyMatch(contact -> contact.getId().equals(ownedContact.getId())));
        assertTrue(contacts.stream().anyMatch(contact -> contact.getId().equals(receivedContact.getId())));
        assertFalse(contacts.stream().anyMatch(contact -> contact.getId().equals(pendingContact.getId())));
    }

    @Test
    public void testGetSharedContactPage_receiverRowsAndFilters() {
        // mock 数据：同一联系人发给两个人，以及他人转发的关系
        Long ownerId = randomLongId();
        OaContactDO contact = randomContactDO(ownerId).setName("Alice").setPinyin("alice");
        contactMapper.insert(contact);
        OaContactShareDO ownerShare = randomPojo(OaContactShareDO.class, o -> {
            o.setId(null).setContactId(contact.getId()).setUserId(ownerId).setCategoryId(null).setHandleStatus(true);
            o.setCreator(ownerId.toString());
        });
        contactShareMapper.insert(ownerShare);
        OaContactShareDO first = randomPojo(OaContactShareDO.class, o -> {
            o.setId(null).setContactId(contact.getId()).setUserId(randomLongId()).setCategoryId(null).setHandleStatus(false);
            o.setCreator(ownerId.toString());
        });
        OaContactShareDO second = randomPojo(OaContactShareDO.class, o -> {
            o.setId(null).setContactId(contact.getId()).setUserId(randomLongId()).setCategoryId(null).setHandleStatus(true);
            o.setCreator(ownerId.toString());
        });
        contactShareMapper.insert(first);
        contactShareMapper.insert(second);
        OaContactShareDO otherShare = randomPojo(OaContactShareDO.class, o -> {
            o.setId(null).setContactId(contact.getId()).setUserId(randomLongId()).setCategoryId(null).setHandleStatus(false);
            o.setCreator(randomLongId().toString());
        });
        contactShareMapper.insert(otherShare);
        // 准备参数
        OaContactPageReqVO reqVO = new OaContactPageReqVO().setKeyword("Alice").setAlphabet("A");
        reqVO.setPageSize(1);

        // 调用
        PageResult<OaContactShareDO> page =
                contactService.getSharedContactPage(reqVO, ownerId);

        // 断言：按关系分页，同一联系人保留两行，排除本人初始化行和他人转发
        assertEquals(2L, page.getTotal());
        assertEquals(1, page.getList().size());
        Long firstPageId = CollUtil.getFirst(page.getList()).getId();
        reqVO.setPageNo(2);
        assertFalse(firstPageId.equals(CollUtil.getFirst(contactService.getSharedContactPage(reqVO, ownerId).getList()).getId()));
        reqVO.setPageNo(1);
        reqVO.setHandleStatus(false);
        assertEquals(first.getId(), CollUtil.getFirst(contactService.getSharedContactPage(reqVO, ownerId).getList()).getId());
        reqVO.setKeyword("不存在");
        assertEquals(0L, contactService.getSharedContactPage(reqVO, ownerId).getTotal());
        reqVO.setKeyword("Alice");
        contactMapper.deleteById(contact.getId());
        assertEquals(0L, contactService.getSharedContactPage(reqVO, ownerId).getTotal());
    }

    @Test
    public void testGetContactPage_sentOnlyIncludesSharedContacts() {
        // mock 数据
        Long userId = randomLongId();
        OaContactDO sharedContact = randomContactDO(userId);
        contactMapper.insert(sharedContact);
        contactShareMapper.insert(randomPojo(OaContactShareDO.class, share -> {
            share.setId(null).setContactId(sharedContact.getId())
                    .setUserId(Long.valueOf(sharedContact.getCreator())).setCategoryId(sharedContact.getCategoryId())
                    .setHandleStatus(true);
            share.setCreator(sharedContact.getCreator());
        }));
        contactShareMapper.insert(randomPojo(OaContactShareDO.class, share -> {
            share.setId(null).setContactId(sharedContact.getId()).setUserId(randomLongId()).setHandleStatus(false);
            share.setCreator(userId.toString());
        }));
        OaContactDO notSharedContact = randomContactDO(userId);
        contactMapper.insert(notSharedContact);
        contactShareMapper.insert(randomPojo(OaContactShareDO.class, share -> {
            share.setId(null).setContactId(notSharedContact.getId())
                    .setUserId(Long.valueOf(notSharedContact.getCreator())).setCategoryId(notSharedContact.getCategoryId())
                    .setHandleStatus(true);
            share.setCreator(notSharedContact.getCreator());
        }));

        // 准备参数
        OaContactPageReqVO pageReqVO = new OaContactPageReqVO();

        // 调用
        List<OaContactShareDO> contacts = contactService.getSharedContactPage(pageReqVO, userId).getList();

        // 断言
        assertEquals(1, contacts.size());
        assertEquals(sharedContact.getId(), CollUtil.getFirst(contacts).getContactId());
    }

    @Test
    public void testGetContactPage_receivedWithoutHandleStatus() {
        // mock 数据
        Long userId = randomLongId();
        OaContactDO pendingContact = randomContactDO(randomLongId());
        contactMapper.insert(pendingContact);
        contactShareMapper.insert(randomPojo(OaContactShareDO.class, share -> {
            share.setId(null).setContactId(pendingContact.getId())
                    .setUserId(Long.valueOf(pendingContact.getCreator())).setCategoryId(pendingContact.getCategoryId())
                    .setHandleStatus(true);
            share.setCreator(pendingContact.getCreator());
        }));
        contactShareMapper.insert(new OaContactShareDO().setContactId(pendingContact.getId())
                .setUserId(userId).setHandleStatus(false));
        OaContactDO handledContact = randomContactDO(randomLongId());
        contactMapper.insert(handledContact);
        contactShareMapper.insert(randomPojo(OaContactShareDO.class, share -> {
            share.setId(null).setContactId(handledContact.getId())
                    .setUserId(Long.valueOf(handledContact.getCreator())).setCategoryId(handledContact.getCategoryId())
                    .setHandleStatus(true);
            share.setCreator(handledContact.getCreator());
        }));
        contactShareMapper.insert(new OaContactShareDO().setContactId(handledContact.getId())
                .setUserId(userId).setHandleStatus(true));

        // 准备参数：未选择处理状态时应查询全部收到的共享
        OaContactPageReqVO pageReqVO = new OaContactPageReqVO();

        // 调用
        List<OaContactDO> contacts = contactService.getReceivedContactPage(pageReqVO, userId).getList();

        // 断言
        assertEquals(2, contacts.size());
        assertTrue(contacts.stream().anyMatch(contact -> contact.getId().equals(pendingContact.getId())));
        assertTrue(contacts.stream().anyMatch(contact -> contact.getId().equals(handledContact.getId())));
    }

    @Test
    public void testGetContactShareListMap_success() {
        // mock 数据
        OaContactShareDO share = new OaContactShareDO().setContactId(10L).setUserId(1L).setHandleStatus(false);
        contactShareMapper.insert(share);
        contactShareMapper.insert(new OaContactShareDO().setContactId(20L).setUserId(2L).setHandleStatus(false));

        // 调用
        Map<Long, List<OaContactShareDO>> shares = contactService.getContactShareListMap(Collections.singleton(10L));

        // 断言
        assertEquals(1, shares.size());
        assertEquals(share.getId(), CollUtil.getFirst(shares.get(10L)).getId());
    }

    @Test
    public void testGetContactShareListMap_empty() {
        // mock 数据
        contactShareMapper.insert(new OaContactShareDO().setContactId(10L).setUserId(1L).setHandleStatus(false));

        // 调用，并断言：空编号集合不能返回全部共享记录
        assertTrue(CollUtil.isEmpty(contactService.getContactShareListMap(Collections.emptyList())));
    }

    @Test
    public void testHandleContactShare_clearCategory() {
        // mock 数据
        Long userId = randomLongId();
        OaContactDO contact = randomContactDO(randomLongId());
        contactMapper.insert(contact);
        contactShareMapper.insert(randomPojo(OaContactShareDO.class, share -> {
            share.setId(null).setContactId(contact.getId())
                    .setUserId(Long.valueOf(contact.getCreator())).setCategoryId(contact.getCategoryId())
                    .setHandleStatus(true);
            share.setCreator(contact.getCreator());
        }));
        OaContactShareDO share = new OaContactShareDO().setContactId(contact.getId())
                .setUserId(userId).setCategoryId(10L).setHandleStatus(true);
        contactShareMapper.insert(share);

        // 调用
        contactService.handleContactShare(contact.getId(), null, userId);

        // 断言
        assertNull(contactShareMapper.selectById(share.getId()).getCategoryId());
        assertTrue(contactShareMapper.selectById(share.getId()).getHandleStatus());
    }

    @Test
    public void testClearContactCategoryId_onlySpecifiedCategory() {
        // mock 数据
        Long userId = randomLongId();
        OaContactDO contact = randomContactDO(userId).setCategoryId(10L);
        contactMapper.insert(contact);
        contactShareMapper.insert(randomPojo(OaContactShareDO.class, share -> {
            share.setId(null).setContactId(contact.getId())
                    .setUserId(Long.valueOf(contact.getCreator())).setCategoryId(contact.getCategoryId())
                    .setHandleStatus(true);
            share.setCreator(contact.getCreator());
        }));
        OaContactDO otherContact = randomContactDO(randomLongId()).setCategoryId(11L);
        contactMapper.insert(otherContact);
        contactShareMapper.insert(randomPojo(OaContactShareDO.class, share -> {
            share.setId(null).setContactId(otherContact.getId())
                    .setUserId(Long.valueOf(otherContact.getCreator())).setCategoryId(otherContact.getCategoryId())
                    .setHandleStatus(true);
            share.setCreator(otherContact.getCreator());
        }));
        OaContactShareDO share = new OaContactShareDO().setContactId(otherContact.getId())
                .setUserId(userId).setCategoryId(10L).setHandleStatus(true);
        contactShareMapper.insert(share);
        OaContactShareDO otherShare = new OaContactShareDO().setContactId(contact.getId())
                .setUserId(randomLongId()).setCategoryId(11L).setHandleStatus(true);
        contactShareMapper.insert(otherShare);

        // 调用
        contactService.clearContactCategoryId(10L);

        // 断言
        assertNull(contactMapper.selectById(contact.getId()).getCategoryId());
        assertNull(contactShareMapper.selectById(share.getId()).getCategoryId());
        assertEquals(11L, contactMapper.selectById(otherContact.getId()).getCategoryId());
        assertEquals(11L, contactShareMapper.selectById(otherShare.getId()).getCategoryId());
    }

    @Test
    public void testShareContact_notOwner() {
        // mock 数据
        OaContactDO contact = randomContactDO(randomLongId());
        contactMapper.insert(contact);
        contactShareMapper.insert(randomPojo(OaContactShareDO.class, share -> {
            share.setId(null).setContactId(contact.getId())
                    .setUserId(Long.valueOf(contact.getCreator())).setCategoryId(contact.getCategoryId())
                    .setHandleStatus(true);
            share.setCreator(contact.getCreator());
        }));

        // 调用，并断言异常
        assertServiceException(() -> contactService.shareContact(contact.getId(),
                Collections.singleton(randomLongId()), randomLongId()), CONTACT_ACCESS_DENIED);
    }

    @Test
    public void testHandleContactShare_ownerCannotChangeOnlyRelation() {
        // mock 数据
        Long userId = randomLongId();
        OaContactDO contact = randomContactDO(userId).setCategoryId(10L);
        contactMapper.insert(contact);
        OaContactShareDO share = new OaContactShareDO().setContactId(contact.getId())
                .setUserId(userId).setCategoryId(10L).setHandleStatus(true);
        contactShareMapper.insert(share);

        // 调用，并断言：本人分类应走联系人编辑，主表与关系表均不变
        assertServiceException(() -> contactService.handleContactShare(contact.getId(), 20L, userId),
                CONTACT_ACCESS_DENIED);
        assertEquals(10L, contactMapper.selectById(contact.getId()).getCategoryId());
        assertEquals(10L, contactShareMapper.selectById(share.getId()).getCategoryId());
    }

    @Test
    public void testHandleContactShare_notReceiver() {
        // mock 数据
        OaContactDO contact = randomContactDO(randomLongId());
        contactMapper.insert(contact);
        contactShareMapper.insert(randomPojo(OaContactShareDO.class, share -> {
            share.setId(null).setContactId(contact.getId())
                    .setUserId(Long.valueOf(contact.getCreator())).setCategoryId(contact.getCategoryId())
                    .setHandleStatus(true);
            share.setCreator(contact.getCreator());
        }));

        // 调用，并断言异常
        assertServiceException(() -> contactService.handleContactShare(contact.getId(),
                10L, randomLongId()), CONTACT_ACCESS_DENIED);
    }

    @Test
    public void testHandleContactShare_invalidCategory() {
        // mock 数据
        Long userId = randomLongId();
        Long categoryId = randomLongId();
        OaContactDO contact = randomContactDO(randomLongId());
        contactMapper.insert(contact);
        OaContactShareDO share = new OaContactShareDO().setContactId(contact.getId())
                .setUserId(userId).setHandleStatus(false);
        contactShareMapper.insert(share);
        // mock 方法
        doThrow(exception(
                CONTACT_CATEGORY_NOT_EXISTS))
                .when(contactCategoryService).validateContactCategory(categoryId, userId);

        // 调用，并断言异常
        assertServiceException(() -> contactService.handleContactShare(share.getContactId(), categoryId, userId),
                CONTACT_CATEGORY_NOT_EXISTS);
        assertFalse(contactShareMapper.selectById(share.getId()).getHandleStatus());
        assertNull(contactShareMapper.selectById(share.getId()).getCategoryId());
    }

    @Test
    public void testGetContactPage_receivedUsesReceiverCategory() {
        // mock 数据：创建人与接收人分别选择自己的分类
        Long userId = randomLongId();
        OaContactDO contact = randomContactDO(randomLongId()).setCategoryId(10L);
        contactMapper.insert(contact);
        contactShareMapper.insert(randomPojo(OaContactShareDO.class, share -> {
            share.setId(null).setContactId(contact.getId())
                    .setUserId(Long.valueOf(contact.getCreator())).setCategoryId(contact.getCategoryId())
                    .setHandleStatus(true);
            share.setCreator(contact.getCreator());
        }));
        contactShareMapper.insert(new OaContactShareDO().setContactId(contact.getId())
                .setUserId(userId).setCategoryId(20L).setHandleStatus(true));
        // 准备参数
        OaContactPageReqVO reqVO = new OaContactPageReqVO();
        reqVO.setCategoryId(20L);

        // 调用，并断言
        assertEquals(1L, contactService.getReceivedContactPage(reqVO, userId).getTotal());
        reqVO.setCategoryId(10L);
        assertEquals(0L, contactService.getReceivedContactPage(reqVO, userId).getTotal());
    }

    @Test
    public void testDeleteContact_preserveOtherHolderAndDeleteLastHolder() {
        // 准备参数
        Long ownerId = 101L;
        Long receiverId = 102L;
        SecurityFrameworkUtils.setLoginUser(new LoginUser().setId(ownerId), new MockHttpServletRequest());
        Long id = contactService.createContact(new OaContactSaveReqVO().setName("共享联系人"), ownerId);
        contactService.shareContact(id, Collections.singleton(receiverId), ownerId);

        // 调用：创建人退出，不影响其他持有人
        contactService.deleteContact(id, ownerId);

        // 断言
        assertServiceException(() -> contactService.getContact(id, ownerId), CONTACT_ACCESS_DENIED);
        assertEquals(id, contactService.getContact(id, receiverId).getId());
        assertEquals(ownerId.toString(), contactMapper.selectById(id).getCreator());
        contactService.deleteReceivedContact(id, receiverId);
        assertNull(contactMapper.selectById(id));
    }

    @Test
    public void testShareContact_receiverCanShareAndCreatorFilledAutomatically() {
        // 准备参数
        Long ownerId = 101L;
        Long receiverId = 102L;
        Long nextReceiverId = 103L;
        SecurityFrameworkUtils.setLoginUser(new LoginUser().setId(ownerId), new MockHttpServletRequest());
        Long id = contactService.createContact(new OaContactSaveReqVO().setName("共享联系人"), ownerId);
        assertEquals(0L, contactService.getReceivedContactPage(new OaContactPageReqVO(), ownerId).getTotal());
        assertEquals(0L, contactService.getSharedContactPage(new OaContactPageReqVO(), ownerId).getTotal());
        contactService.shareContact(id, Collections.singleton(receiverId), ownerId);
        SecurityFrameworkUtils.setLoginUser(new LoginUser().setId(receiverId), new MockHttpServletRequest());

        // 调用
        contactService.shareContact(id, Collections.singleton(nextReceiverId), receiverId);

        // 断言：关系 creator 是本次共享人，联系人 creator 不变
        assertEquals(receiverId.toString(), contactShareMapper.selectByContactIdAndUserId(id, nextReceiverId).getCreator());
        assertEquals(ownerId.toString(), contactMapper.selectById(id).getCreator());
        assertEquals(1L, contactService.getSharedContactPage(new OaContactPageReqVO(), receiverId).getTotal());
        contactService.deleteContact(id, ownerId);
        contactService.shareContact(id, Collections.singleton(ownerId), receiverId);
        assertEquals(id, contactService.getContact(id, ownerId).getId());
        assertEquals(1L, contactService.getReceivedContactPage(new OaContactPageReqVO(), ownerId).getTotal());
    }

    // ========== 随机对象 ==========

    /**
     * 构造性别在合法范围内的联系人。
     *
     * @param ownerUserId 创建人编号
     * @return 未入库的测试对象
     */
    private static OaContactDO randomContactDO(Long ownerUserId) {
        OaContactDO contact = randomPojo(OaContactDO.class, o -> o.setId(null)
                .setName("张三").setPinyin("zhangsan").setSex(1));
        contact.setCreator(ownerUserId.toString());
        return contact;
    }

}
