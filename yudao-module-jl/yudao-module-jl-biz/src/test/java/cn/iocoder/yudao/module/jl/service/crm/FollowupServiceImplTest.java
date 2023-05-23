package cn.iocoder.yudao.module.jl.service.crm;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.annotation.Resource;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;

import cn.iocoder.yudao.module.jl.controller.admin.crm.vo.*;
import cn.iocoder.yudao.module.jl.dal.dataobject.crm.FollowupDO;
import cn.iocoder.yudao.module.jl.dal.mysql.crm.FollowupMapper;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

import javax.annotation.Resource;
import org.springframework.context.annotation.Import;
import java.util.*;
import java.time.LocalDateTime;

import static cn.hutool.core.util.RandomUtil.*;
import static cn.iocoder.yudao.module.jl.enums.ErrorCodeConstants.*;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.*;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.*;
import static cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils.*;
import static cn.iocoder.yudao.framework.common.util.object.ObjectUtils.*;
import static cn.iocoder.yudao.framework.common.util.date.DateUtils.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * {@link FollowupServiceImpl} 的单元测试类
 *
 * @author 惟象科技
 */
@Import(FollowupServiceImpl.class)
public class FollowupServiceImplTest extends BaseDbUnitTest {

    @Resource
    private FollowupServiceImpl followupService;

    @Resource
    private FollowupMapper followupMapper;

    @Test
    public void testCreateFollowup_success() {
        // 准备参数
        FollowupCreateReqVO reqVO = randomPojo(FollowupCreateReqVO.class);

        // 调用
        Long followupId = followupService.createFollowup(reqVO);
        // 断言
        assertNotNull(followupId);
        // 校验记录的属性是否正确
        FollowupDO followup = followupMapper.selectById(followupId);
        assertPojoEquals(reqVO, followup);
    }

    @Test
    public void testUpdateFollowup_success() {
        // mock 数据
        FollowupDO dbFollowup = randomPojo(FollowupDO.class);
        followupMapper.insert(dbFollowup);// @Sql: 先插入出一条存在的数据
        // 准备参数
        FollowupUpdateReqVO reqVO = randomPojo(FollowupUpdateReqVO.class, o -> {
            o.setId(dbFollowup.getId()); // 设置更新的 ID
        });

        // 调用
        followupService.updateFollowup(reqVO);
        // 校验是否更新正确
        FollowupDO followup = followupMapper.selectById(reqVO.getId()); // 获取最新的
        assertPojoEquals(reqVO, followup);
    }

    @Test
    public void testUpdateFollowup_notExists() {
        // 准备参数
        FollowupUpdateReqVO reqVO = randomPojo(FollowupUpdateReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> followupService.updateFollowup(reqVO), FOLLOWUP_NOT_EXISTS);
    }

    @Test
    public void testDeleteFollowup_success() {
        // mock 数据
        FollowupDO dbFollowup = randomPojo(FollowupDO.class);
        followupMapper.insert(dbFollowup);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbFollowup.getId();

        // 调用
        followupService.deleteFollowup(id);
       // 校验数据不存在了
       assertNull(followupMapper.selectById(id));
    }

    @Test
    public void testDeleteFollowup_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用, 并断言异常
        assertServiceException(() -> followupService.deleteFollowup(id), FOLLOWUP_NOT_EXISTS);
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetFollowupPage() {
       // mock 数据
       FollowupDO dbFollowup = randomPojo(FollowupDO.class, o -> { // 等会查询到
           o.setCreateTime(null);
           o.setContent(null);
           o.setCustomerId(null);
           o.setRefId(null);
           o.setType(null);
       });
       followupMapper.insert(dbFollowup);
       // 测试 createTime 不匹配
       followupMapper.insert(cloneIgnoreId(dbFollowup, o -> o.setCreateTime(null)));
       // 测试 content 不匹配
       followupMapper.insert(cloneIgnoreId(dbFollowup, o -> o.setContent(null)));
       // 测试 customerId 不匹配
       followupMapper.insert(cloneIgnoreId(dbFollowup, o -> o.setCustomerId(null)));
       // 测试 refId 不匹配
       followupMapper.insert(cloneIgnoreId(dbFollowup, o -> o.setRefId(null)));
       // 测试 type 不匹配
       followupMapper.insert(cloneIgnoreId(dbFollowup, o -> o.setType(null)));
       // 准备参数
       FollowupPageReqVO reqVO = new FollowupPageReqVO();
       reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));
       reqVO.setContent(null);
       reqVO.setCustomerId(null);
       reqVO.setRefId(null);
       reqVO.setType(null);

       // 调用
       PageResult<FollowupDO> pageResult = followupService.getFollowupPage(reqVO);
       // 断言
       assertEquals(1, pageResult.getTotal());
       assertEquals(1, pageResult.getList().size());
       assertPojoEquals(dbFollowup, pageResult.getList().get(0));
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetFollowupList() {
       // mock 数据
       FollowupDO dbFollowup = randomPojo(FollowupDO.class, o -> { // 等会查询到
           o.setCreateTime(null);
           o.setContent(null);
           o.setCustomerId(null);
           o.setRefId(null);
           o.setType(null);
       });
       followupMapper.insert(dbFollowup);
       // 测试 createTime 不匹配
       followupMapper.insert(cloneIgnoreId(dbFollowup, o -> o.setCreateTime(null)));
       // 测试 content 不匹配
       followupMapper.insert(cloneIgnoreId(dbFollowup, o -> o.setContent(null)));
       // 测试 customerId 不匹配
       followupMapper.insert(cloneIgnoreId(dbFollowup, o -> o.setCustomerId(null)));
       // 测试 refId 不匹配
       followupMapper.insert(cloneIgnoreId(dbFollowup, o -> o.setRefId(null)));
       // 测试 type 不匹配
       followupMapper.insert(cloneIgnoreId(dbFollowup, o -> o.setType(null)));
       // 准备参数
       FollowupExportReqVO reqVO = new FollowupExportReqVO();
       reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));
       reqVO.setContent(null);
       reqVO.setCustomerId(null);
       reqVO.setRefId(null);
       reqVO.setType(null);

       // 调用
       List<FollowupDO> list = followupService.getFollowupList(reqVO);
       // 断言
       assertEquals(1, list.size());
       assertPojoEquals(dbFollowup, list.get(0));
    }

}
