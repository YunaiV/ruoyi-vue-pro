package cn.iocoder.yudao.adminserver.modules.infra.service.logger;

import cn.iocoder.yudao.adminserver.BaseDbUnitTest;
import cn.iocoder.yudao.adminserver.modules.infra.controller.logger.vo.apierrorlog.InfApiErrorLogExportReqVO;
import cn.iocoder.yudao.adminserver.modules.infra.controller.logger.vo.apierrorlog.InfApiErrorLogPageReqVO;
import cn.iocoder.yudao.adminserver.modules.infra.dal.mysql.logger.InfApiErrorLogMapper;
import cn.iocoder.yudao.adminserver.modules.infra.enums.logger.InfApiErrorLogProcessStatusEnum;
import cn.iocoder.yudao.adminserver.modules.infra.service.logger.impl.InfApiErrorLogServiceImpl;
import cn.iocoder.yudao.coreservice.modules.infra.dal.dataobject.logger.InfApiErrorLogDO;
import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.ObjectUtils;
import cn.iocoder.yudao.framework.test.core.util.RandomUtils;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

import static cn.iocoder.yudao.adminserver.modules.infra.enums.InfErrorCodeConstants.API_ERROR_LOG_NOT_FOUND;
import static cn.iocoder.yudao.adminserver.modules.infra.enums.InfErrorCodeConstants.API_ERROR_LOG_PROCESSED;
import static cn.iocoder.yudao.framework.common.util.date.DateUtils.buildTime;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertPojoEquals;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@link InfApiErrorLogServiceImpl} 单元测试
 */
@Import(InfApiErrorLogServiceImpl.class)
public class InfApiErrorLogServiceImplTest extends BaseDbUnitTest {

    @Resource
    private InfApiErrorLogService infApiErrorLogServiceImpl;

    @Resource
    private InfApiErrorLogMapper infApiErrorLogMapper;

    @Test
    public void testGetApiErrorLogPage() {
        // 构造测试数据
        long userId = 2233L;
        int userType = UserTypeEnum.ADMIN.getValue();
        String applicationName = "yudao-test";
        String requestUrl = "foo";
        Date beginTime = buildTime(2021, 3, 13);
        int progressStatus = InfApiErrorLogProcessStatusEnum.INIT.getStatus();

        InfApiErrorLogDO infApiErrorLogDO = RandomUtils.randomPojo(InfApiErrorLogDO.class, logDO -> {
            logDO.setUserId(userId);
            logDO.setUserType(userType);
            logDO.setApplicationName(applicationName);
            logDO.setRequestUrl(requestUrl);
            logDO.setExceptionTime(beginTime);
            logDO.setProcessStatus(progressStatus);
        });
        infApiErrorLogMapper.insert(infApiErrorLogDO);

        // 下面几个都是不匹配的数据
        // userId 不同的
        infApiErrorLogMapper.insert(ObjectUtils.cloneIgnoreId(infApiErrorLogDO, logDO -> logDO.setUserId(3344L)));
        // userType
        infApiErrorLogMapper.insert(ObjectUtils.cloneIgnoreId(infApiErrorLogDO, logDO -> logDO.setUserType(UserTypeEnum.MEMBER.getValue())));
        // applicationName 不同的
        infApiErrorLogMapper.insert(ObjectUtils.cloneIgnoreId(infApiErrorLogDO, logDO -> logDO.setApplicationName("test")));
        // requestUrl 不同的
        infApiErrorLogMapper.insert(ObjectUtils.cloneIgnoreId(infApiErrorLogDO, logDO -> logDO.setRequestUrl("bar")));
        // 构造一个早期时间 2021-02-06 00:00:00
        infApiErrorLogMapper.insert(ObjectUtils.cloneIgnoreId(infApiErrorLogDO, logDO -> logDO.setExceptionTime(buildTime(2021, 2, 6))));
        // progressStatus 不同的
        infApiErrorLogMapper.insert(ObjectUtils.cloneIgnoreId(infApiErrorLogDO, logDO -> logDO.setProcessStatus(InfApiErrorLogProcessStatusEnum.DONE.getStatus())));

        // 构造调用参数
        InfApiErrorLogPageReqVO reqVO = new InfApiErrorLogPageReqVO();
        reqVO.setUserId(userId);
        reqVO.setUserType(userType);
        reqVO.setApplicationName(applicationName);
        reqVO.setRequestUrl(requestUrl);
        reqVO.setBeginExceptionTime(buildTime(2021, 3, 12));
        reqVO.setEndExceptionTime(buildTime(2021, 3, 14));
        reqVO.setProcessStatus(progressStatus);

        // 调用service方法
        PageResult<InfApiErrorLogDO> pageResult = infApiErrorLogServiceImpl.getApiErrorLogPage(reqVO);

        // 断言，只查到了一条符合条件的
        assertEquals(1, pageResult.getTotal());
        assertEquals(1, pageResult.getList().size());
        assertPojoEquals(infApiErrorLogDO, pageResult.getList().get(0));
    }

    @Test
    public void testGetApiErrorLogList() {
        // 构造测试数据
        long userId = 2233L;
        int userType = UserTypeEnum.ADMIN.getValue();
        String applicationName = "yudao-test";
        String requestUrl = "foo";
        Date beginTime = buildTime(2021, 3, 13);
        int progressStatus = InfApiErrorLogProcessStatusEnum.INIT.getStatus();

        InfApiErrorLogDO infApiErrorLogDO = RandomUtils.randomPojo(InfApiErrorLogDO.class, logDO -> {
            logDO.setUserId(userId);
            logDO.setUserType(userType);
            logDO.setApplicationName(applicationName);
            logDO.setRequestUrl(requestUrl);
            logDO.setExceptionTime(beginTime);
            logDO.setProcessStatus(progressStatus);
        });
        infApiErrorLogMapper.insert(infApiErrorLogDO);

        // 下面几个都是不匹配的数据
        // userId 不同的
        infApiErrorLogMapper.insert(ObjectUtils.cloneIgnoreId(infApiErrorLogDO, logDO -> logDO.setUserId(3344L)));
        // userType
        infApiErrorLogMapper.insert(ObjectUtils.cloneIgnoreId(infApiErrorLogDO, logDO -> logDO.setUserType(UserTypeEnum.MEMBER.getValue())));
        // applicationName 不同的
        infApiErrorLogMapper.insert(ObjectUtils.cloneIgnoreId(infApiErrorLogDO, logDO -> logDO.setApplicationName("test")));
        // requestUrl 不同的
        infApiErrorLogMapper.insert(ObjectUtils.cloneIgnoreId(infApiErrorLogDO, logDO -> logDO.setRequestUrl("bar")));
        // 构造一个早期时间 2021-02-06 00:00:00
        infApiErrorLogMapper.insert(ObjectUtils.cloneIgnoreId(infApiErrorLogDO, logDO -> logDO.setExceptionTime(buildTime(2021, 2, 6))));
        // progressStatus 不同的
        infApiErrorLogMapper.insert(ObjectUtils.cloneIgnoreId(infApiErrorLogDO, logDO -> logDO.setProcessStatus(InfApiErrorLogProcessStatusEnum.DONE.getStatus())));

        // 构造调用参数
        InfApiErrorLogExportReqVO reqVO = new InfApiErrorLogExportReqVO();
        reqVO.setUserId(userId);
        reqVO.setUserType(userType);
        reqVO.setApplicationName(applicationName);
        reqVO.setRequestUrl(requestUrl);
        reqVO.setBeginExceptionTime(buildTime(2021, 3, 12));
        reqVO.setEndExceptionTime(buildTime(2021, 3, 14));
        reqVO.setProcessStatus(progressStatus);

        // 调用service方法
        List<InfApiErrorLogDO> list = infApiErrorLogServiceImpl.getApiErrorLogList(reqVO);

        // 断言，只查到了一条符合条件的
        assertEquals(1, list.size());
        assertPojoEquals(infApiErrorLogDO, list.get(0));
    }


    @Test
    public void testUpdateApiErrorLogProcess() {
        // 先构造两条数据，第一条用于抛出异常，第二条用于正常的执行update操作
        Long processUserId = 2233L;

        InfApiErrorLogDO first = RandomUtils.randomPojo(InfApiErrorLogDO.class, logDO -> {
            logDO.setProcessUserId(processUserId);
            logDO.setUserType(UserTypeEnum.ADMIN.getValue());
            logDO.setProcessStatus(InfApiErrorLogProcessStatusEnum.DONE.getStatus());
        });
        infApiErrorLogMapper.insert(first);

        InfApiErrorLogDO second = RandomUtils.randomPojo(InfApiErrorLogDO.class, logDO -> {
            logDO.setProcessUserId(1122L);
            logDO.setUserType(UserTypeEnum.ADMIN.getValue());
            logDO.setProcessStatus(InfApiErrorLogProcessStatusEnum.INIT.getStatus());
        });
        infApiErrorLogMapper.insert(second);

        Long firstId = first.getId();
        Long secondId = second.getId();

        // 执行正常的 update 操作
        infApiErrorLogServiceImpl.updateApiErrorLogProcess(secondId, InfApiErrorLogProcessStatusEnum.DONE.getStatus(), processUserId);
        InfApiErrorLogDO secondSelect = infApiErrorLogMapper.selectOne("id", secondId);

        // id 为 0 查询不到，应该抛出异常 API_ERROR_LOG_NOT_FOUND
        assertServiceException(() -> infApiErrorLogServiceImpl.updateApiErrorLogProcess(0L, InfApiErrorLogProcessStatusEnum.DONE.getStatus(), processUserId), API_ERROR_LOG_NOT_FOUND);
        // id 为 first 的 progressStatus 为 DONE ，应该抛出 API_ERROR_LOG_PROCESSED
        assertServiceException(() -> infApiErrorLogServiceImpl.updateApiErrorLogProcess(firstId, InfApiErrorLogProcessStatusEnum.DONE.getStatus(), processUserId), API_ERROR_LOG_PROCESSED);
        // 验证 progressStatus 是否修改成功
        assertEquals(InfApiErrorLogProcessStatusEnum.DONE.getStatus(), secondSelect.getProcessStatus());
        // 验证 progressUserId 是否修改成功
        assertEquals(processUserId, secondSelect.getProcessUserId());
    }

}
