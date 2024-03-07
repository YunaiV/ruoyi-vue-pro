package cn.iocoder.yudao.module.system.service.notify;

import cn.hutool.core.map.MapUtil;
import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.enums.SqlConstants;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.system.controller.admin.notify.vo.message.NotifyMessageMyPageReqVO;
import cn.iocoder.yudao.module.system.controller.admin.notify.vo.message.NotifyMessagePageReqVO;
import cn.iocoder.yudao.module.system.dal.dataobject.notify.NotifyMessageDO;
import cn.iocoder.yudao.module.system.dal.dataobject.notify.NotifyTemplateDO;
import cn.iocoder.yudao.module.system.dal.mysql.notify.NotifyMessageMapper;
import com.baomidou.mybatisplus.annotation.DbType;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static cn.hutool.core.util.RandomUtil.randomEle;
import static cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils.buildBetweenTime;
import static cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils.buildTime;
import static cn.iocoder.yudao.framework.common.util.object.ObjectUtils.cloneIgnoreId;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertPojoEquals;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.*;
import static org.junit.jupiter.api.Assertions.*;

/**
* {@link NotifyMessageServiceImpl} 的单元测试类
*
* @author 芋道源码
*/
@Import(NotifyMessageServiceImpl.class)
public class NotifyMessageServiceImplTest extends BaseDbUnitTest {

    @Resource
    private NotifyMessageServiceImpl notifyMessageService;

    @Resource
    private NotifyMessageMapper notifyMessageMapper;

    @Test
    public void testCreateNotifyMessage_success() {
        // 准备参数
        Long userId = randomLongId();
        Integer userType = randomEle(UserTypeEnum.values()).getValue();
        NotifyTemplateDO template = randomPojo(NotifyTemplateDO.class);
        String templateContent = randomString();
        Map<String, Object> templateParams = randomTemplateParams();
        // mock 方法

        // 调用
        Long messageId = notifyMessageService.createNotifyMessage(userId, userType,
                template, templateContent, templateParams);
        // 断言
        NotifyMessageDO message = notifyMessageMapper.selectById(messageId);
        assertNotNull(message);
        assertEquals(userId, message.getUserId());
        assertEquals(userType, message.getUserType());
        assertEquals(template.getId(), message.getTemplateId());
        assertEquals(template.getCode(), message.getTemplateCode());
        assertEquals(template.getType(), message.getTemplateType());
        assertEquals(template.getNickname(), message.getTemplateNickname());
        assertEquals(templateContent, message.getTemplateContent());
        assertEquals(templateParams, message.getTemplateParams());
        assertEquals(false, message.getReadStatus());
        assertNull(message.getReadTime());
    }

    @Test
    public void testGetNotifyMessagePage() {
       // mock 数据
       NotifyMessageDO dbNotifyMessage = randomPojo(NotifyMessageDO.class, o -> { // 等会查询到
           o.setUserId(1L);
           o.setUserType(UserTypeEnum.ADMIN.getValue());
           o.setTemplateCode("test_01");
           o.setTemplateType(10);
           o.setCreateTime(buildTime(2022, 1, 2));
           o.setTemplateParams(randomTemplateParams());
       });
       notifyMessageMapper.insert(dbNotifyMessage);
       // 测试 userId 不匹配
       notifyMessageMapper.insert(cloneIgnoreId(dbNotifyMessage, o -> o.setUserId(2L)));
       // 测试 userType 不匹配
       notifyMessageMapper.insert(cloneIgnoreId(dbNotifyMessage, o -> o.setUserType(UserTypeEnum.MEMBER.getValue())));
       // 测试 templateCode 不匹配
       notifyMessageMapper.insert(cloneIgnoreId(dbNotifyMessage, o -> o.setTemplateCode("test_11")));
       // 测试 templateType 不匹配
       notifyMessageMapper.insert(cloneIgnoreId(dbNotifyMessage, o -> o.setTemplateType(20)));
       // 测试 createTime 不匹配
       notifyMessageMapper.insert(cloneIgnoreId(dbNotifyMessage, o -> o.setCreateTime(buildTime(2022, 2, 1))));
       // 准备参数
       NotifyMessagePageReqVO reqVO = new NotifyMessagePageReqVO();
       reqVO.setUserId(1L);
       reqVO.setUserType(UserTypeEnum.ADMIN.getValue());
       reqVO.setTemplateCode("est_01");
       reqVO.setTemplateType(10);
       reqVO.setCreateTime(buildBetweenTime(2022, 1, 1, 2022, 1, 10));

       // 调用
       PageResult<NotifyMessageDO> pageResult = notifyMessageService.getNotifyMessagePage(reqVO);
       // 断言
       assertEquals(1, pageResult.getTotal());
       assertEquals(1, pageResult.getList().size());
       assertPojoEquals(dbNotifyMessage, pageResult.getList().get(0));
    }

    @Test
    public void testGetNotifyMessage() {
        // mock 数据
        NotifyMessageDO dbNotifyMessage = randomPojo(NotifyMessageDO.class,
                o -> o.setTemplateParams(randomTemplateParams()));
        notifyMessageMapper.insert(dbNotifyMessage);
        // 准备参数
        Long id = dbNotifyMessage.getId();

        // 调用
        NotifyMessageDO notifyMessage = notifyMessageService.getNotifyMessage(id);
        assertPojoEquals(dbNotifyMessage, notifyMessage);
    }

    @Test
    public void testGetMyNotifyMessagePage() {
        // mock 数据
        NotifyMessageDO dbNotifyMessage = randomPojo(NotifyMessageDO.class, o -> { // 等会查询到
            o.setUserId(1L);
            o.setUserType(UserTypeEnum.ADMIN.getValue());
            o.setReadStatus(true);
            o.setCreateTime(buildTime(2022, 1, 2));
            o.setTemplateParams(randomTemplateParams());
        });
        notifyMessageMapper.insert(dbNotifyMessage);
        // 测试 userId 不匹配
        notifyMessageMapper.insert(cloneIgnoreId(dbNotifyMessage, o -> o.setUserId(2L)));
        // 测试 userType 不匹配
        notifyMessageMapper.insert(cloneIgnoreId(dbNotifyMessage, o -> o.setUserType(UserTypeEnum.MEMBER.getValue())));
        // 测试 readStatus 不匹配
        notifyMessageMapper.insert(cloneIgnoreId(dbNotifyMessage, o -> o.setReadStatus(false)));
        // 测试 createTime 不匹配
        notifyMessageMapper.insert(cloneIgnoreId(dbNotifyMessage, o -> o.setCreateTime(buildTime(2022, 2, 1))));
        // 准备参数
        Long userId = 1L;
        Integer userType = UserTypeEnum.ADMIN.getValue();
        NotifyMessageMyPageReqVO reqVO = new NotifyMessageMyPageReqVO();
        reqVO.setReadStatus(true);
        reqVO.setCreateTime(buildBetweenTime(2022, 1, 1, 2022, 1, 10));

        // 调用
        PageResult<NotifyMessageDO> pageResult = notifyMessageService.getMyMyNotifyMessagePage(reqVO, userId, userType);
        // 断言
        assertEquals(1, pageResult.getTotal());
        assertEquals(1, pageResult.getList().size());
        assertPojoEquals(dbNotifyMessage, pageResult.getList().get(0));
    }

    @Test
    public void testGetUnreadNotifyMessageList() {
        SqlConstants.init(DbType.MYSQL);
        // mock 数据
        NotifyMessageDO dbNotifyMessage = randomPojo(NotifyMessageDO.class, o -> { // 等会查询到
            o.setUserId(1L);
            o.setUserType(UserTypeEnum.ADMIN.getValue());
            o.setReadStatus(false);
            o.setTemplateParams(randomTemplateParams());
        });
        notifyMessageMapper.insert(dbNotifyMessage);
        // 测试 userId 不匹配
        notifyMessageMapper.insert(cloneIgnoreId(dbNotifyMessage, o -> o.setUserId(2L)));
        // 测试 userType 不匹配
        notifyMessageMapper.insert(cloneIgnoreId(dbNotifyMessage, o -> o.setUserType(UserTypeEnum.MEMBER.getValue())));
        // 测试 readStatus 不匹配
        notifyMessageMapper.insert(cloneIgnoreId(dbNotifyMessage, o -> o.setReadStatus(true)));
        // 准备参数
        Long userId = 1L;
        Integer userType = UserTypeEnum.ADMIN.getValue();
        Integer size = 10;

        // 调用
        List<NotifyMessageDO> list = notifyMessageService.getUnreadNotifyMessageList(userId, userType, size);
        // 断言
        assertEquals(1, list.size());
        assertPojoEquals(dbNotifyMessage, list.get(0));
    }

    @Test
    public void testGetUnreadNotifyMessageCount() {
        SqlConstants.init(DbType.MYSQL);
        // mock 数据
        NotifyMessageDO dbNotifyMessage = randomPojo(NotifyMessageDO.class, o -> { // 等会查询到
            o.setUserId(1L);
            o.setUserType(UserTypeEnum.ADMIN.getValue());
            o.setReadStatus(false);
            o.setTemplateParams(randomTemplateParams());
        });
        notifyMessageMapper.insert(dbNotifyMessage);
        // 测试 userId 不匹配
        notifyMessageMapper.insert(cloneIgnoreId(dbNotifyMessage, o -> o.setUserId(2L)));
        // 测试 userType 不匹配
        notifyMessageMapper.insert(cloneIgnoreId(dbNotifyMessage, o -> o.setUserType(UserTypeEnum.MEMBER.getValue())));
        // 测试 readStatus 不匹配
        notifyMessageMapper.insert(cloneIgnoreId(dbNotifyMessage, o -> o.setReadStatus(true)));
        // 准备参数
        Long userId = 1L;
        Integer userType = UserTypeEnum.ADMIN.getValue();

        // 调用，并断言
        assertEquals(1, notifyMessageService.getUnreadNotifyMessageCount(userId, userType));
    }

    @Test
    public void testUpdateNotifyMessageRead() {
        // mock 数据
        NotifyMessageDO dbNotifyMessage = randomPojo(NotifyMessageDO.class, o -> { // 等会查询到
            o.setUserId(1L);
            o.setUserType(UserTypeEnum.ADMIN.getValue());
            o.setReadStatus(false);
            o.setReadTime(null);
            o.setTemplateParams(randomTemplateParams());
        });
        notifyMessageMapper.insert(dbNotifyMessage);
        // 测试 userId 不匹配
        notifyMessageMapper.insert(cloneIgnoreId(dbNotifyMessage, o -> o.setUserId(2L)));
        // 测试 userType 不匹配
        notifyMessageMapper.insert(cloneIgnoreId(dbNotifyMessage, o -> o.setUserType(UserTypeEnum.MEMBER.getValue())));
        // 测试 readStatus 不匹配
        notifyMessageMapper.insert(cloneIgnoreId(dbNotifyMessage, o -> o.setReadStatus(true)));
        // 准备参数
        Collection<Long> ids = Arrays.asList(dbNotifyMessage.getId(), dbNotifyMessage.getId() + 1,
                dbNotifyMessage.getId() + 2, dbNotifyMessage.getId() + 3);
        Long userId = 1L;
        Integer userType = UserTypeEnum.ADMIN.getValue();

        // 调用
        int updateCount = notifyMessageService.updateNotifyMessageRead(ids, userId, userType);
        // 断言
        assertEquals(1, updateCount);
        NotifyMessageDO notifyMessage = notifyMessageMapper.selectById(dbNotifyMessage.getId());
        assertTrue(notifyMessage.getReadStatus());
        assertNotNull(notifyMessage.getReadTime());
    }

    @Test
    public void testUpdateAllNotifyMessageRead() {
        // mock 数据
        NotifyMessageDO dbNotifyMessage = randomPojo(NotifyMessageDO.class, o -> { // 等会查询到
            o.setUserId(1L);
            o.setUserType(UserTypeEnum.ADMIN.getValue());
            o.setReadStatus(false);
            o.setReadTime(null);
            o.setTemplateParams(randomTemplateParams());
        });
        notifyMessageMapper.insert(dbNotifyMessage);
        // 测试 userId 不匹配
        notifyMessageMapper.insert(cloneIgnoreId(dbNotifyMessage, o -> o.setUserId(2L)));
        // 测试 userType 不匹配
        notifyMessageMapper.insert(cloneIgnoreId(dbNotifyMessage, o -> o.setUserType(UserTypeEnum.MEMBER.getValue())));
        // 测试 readStatus 不匹配
        notifyMessageMapper.insert(cloneIgnoreId(dbNotifyMessage, o -> o.setReadStatus(true)));
        // 准备参数
        Long userId = 1L;
        Integer userType = UserTypeEnum.ADMIN.getValue();

        // 调用
        int updateCount = notifyMessageService.updateAllNotifyMessageRead(userId, userType);
        // 断言
        assertEquals(1, updateCount);
        NotifyMessageDO notifyMessage = notifyMessageMapper.selectById(dbNotifyMessage.getId());
        assertTrue(notifyMessage.getReadStatus());
        assertNotNull(notifyMessage.getReadTime());
    }

    private static Map<String, Object> randomTemplateParams() {
        return MapUtil.<String, Object>builder().put(randomString(), randomString())
                .put(randomString(), randomString()).build();
    }

}
