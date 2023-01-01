package cn.iocoder.yudao.module.infra.service.logger;

import cn.hutool.core.util.RandomUtil;
import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.ObjectUtils;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.framework.test.core.util.RandomUtils;
import cn.iocoder.yudao.module.infra.api.logger.dto.ApiErrorLogCreateReqDTO;
import cn.iocoder.yudao.module.infra.controller.admin.logger.vo.apierrorlog.ApiErrorLogExportReqVO;
import cn.iocoder.yudao.module.infra.controller.admin.logger.vo.apierrorlog.ApiErrorLogPageReqVO;
import cn.iocoder.yudao.module.infra.dal.dataobject.logger.ApiErrorLogDO;
import cn.iocoder.yudao.module.infra.dal.mysql.logger.ApiErrorLogMapper;
import cn.iocoder.yudao.module.infra.enums.logger.ApiErrorLogProcessStatusEnum;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

import static cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils.buildTime;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertPojoEquals;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.module.infra.enums.ErrorCodeConstants.API_ERROR_LOG_NOT_FOUND;
import static cn.iocoder.yudao.module.infra.enums.ErrorCodeConstants.API_ERROR_LOG_PROCESSED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Import(ApiErrorLogServiceImpl.class)
public class ApiErrorLogServiceImplTest extends BaseDbUnitTest {

    @Resource
    private ApiErrorLogServiceImpl apiErrorLogService;

    @Resource
    private ApiErrorLogMapper infApiErrorLogMapper;

    @Test
    public void testGetApiErrorLogPage() {
        // 构造测试数据
        long userId = 2233L;
        int userType = UserTypeEnum.ADMIN.getValue();
        String applicationName = "yudao-test";
        String requestUrl = "foo";
        LocalDateTime beginTime = buildTime(2021, 3, 13);
        int progressStatus = ApiErrorLogProcessStatusEnum.INIT.getStatus();

        ApiErrorLogDO infApiErrorLogDO = RandomUtils.randomPojo(ApiErrorLogDO.class, logDO -> {
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
        infApiErrorLogMapper.insert(ObjectUtils.cloneIgnoreId(infApiErrorLogDO, logDO -> logDO.setProcessStatus(ApiErrorLogProcessStatusEnum.DONE.getStatus())));

        // 构造调用参数
        ApiErrorLogPageReqVO reqVO = new ApiErrorLogPageReqVO();
        reqVO.setUserId(userId);
        reqVO.setUserType(userType);
        reqVO.setApplicationName(applicationName);
        reqVO.setRequestUrl(requestUrl);
        reqVO.setExceptionTime((new LocalDateTime[]{buildTime(2021, 3, 12),buildTime(2021, 3, 14)}));
        reqVO.setProcessStatus(progressStatus);

        // 调用service方法
        PageResult<ApiErrorLogDO> pageResult = apiErrorLogService.getApiErrorLogPage(reqVO);

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
        LocalDateTime beginTime = buildTime(2021, 3, 13);
        int progressStatus = ApiErrorLogProcessStatusEnum.INIT.getStatus();

        ApiErrorLogDO infApiErrorLogDO = RandomUtils.randomPojo(ApiErrorLogDO.class, logDO -> {
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
        infApiErrorLogMapper.insert(ObjectUtils.cloneIgnoreId(infApiErrorLogDO, logDO -> logDO.setProcessStatus(ApiErrorLogProcessStatusEnum.DONE.getStatus())));

        // 构造调用参数
        ApiErrorLogExportReqVO reqVO = new ApiErrorLogExportReqVO();
        reqVO.setUserId(userId);
        reqVO.setUserType(userType);
        reqVO.setApplicationName(applicationName);
        reqVO.setRequestUrl(requestUrl);
        reqVO.setExceptionTime((new LocalDateTime[]{buildTime(2021, 3, 12),buildTime(2021, 3, 14)}));
        reqVO.setProcessStatus(progressStatus);

        // 调用service方法
        List<ApiErrorLogDO> list = apiErrorLogService.getApiErrorLogList(reqVO);

        // 断言，只查到了一条符合条件的
        assertEquals(1, list.size());
        assertPojoEquals(infApiErrorLogDO, list.get(0));
    }


    // TODO 芋艿：单元测试，可以拆小一点
    @Test
    public void testUpdateApiErrorLogProcess() {
        // 先构造两条数据，第一条用于抛出异常，第二条用于正常的执行update操作
        Long processUserId = 2233L;

        ApiErrorLogDO first = RandomUtils.randomPojo(ApiErrorLogDO.class, logDO -> {
            logDO.setProcessUserId(processUserId);
            logDO.setUserType(UserTypeEnum.ADMIN.getValue());
            logDO.setProcessStatus(ApiErrorLogProcessStatusEnum.DONE.getStatus());
        });
        infApiErrorLogMapper.insert(first);

        ApiErrorLogDO second = RandomUtils.randomPojo(ApiErrorLogDO.class, logDO -> {
            logDO.setProcessUserId(1122L);
            logDO.setUserType(UserTypeEnum.ADMIN.getValue());
            logDO.setProcessStatus(ApiErrorLogProcessStatusEnum.INIT.getStatus());
        });
        infApiErrorLogMapper.insert(second);

        Long firstId = first.getId();
        Long secondId = second.getId();

        // 执行正常的 update 操作
        apiErrorLogService.updateApiErrorLogProcess(secondId, ApiErrorLogProcessStatusEnum.DONE.getStatus(), processUserId);
        ApiErrorLogDO secondSelect = infApiErrorLogMapper.selectOne("id", secondId);

        // id 为 0 查询不到，应该抛出异常 API_ERROR_LOG_NOT_FOUND
        assertServiceException(() -> apiErrorLogService.updateApiErrorLogProcess(0L, ApiErrorLogProcessStatusEnum.DONE.getStatus(), processUserId), API_ERROR_LOG_NOT_FOUND);
        // id 为 first 的 progressStatus 为 DONE ，应该抛出 API_ERROR_LOG_PROCESSED
        assertServiceException(() -> apiErrorLogService.updateApiErrorLogProcess(firstId, ApiErrorLogProcessStatusEnum.DONE.getStatus(), processUserId), API_ERROR_LOG_PROCESSED);
        // 验证 progressStatus 是否修改成功
        Assertions.assertEquals(ApiErrorLogProcessStatusEnum.DONE.getStatus(), secondSelect.getProcessStatus());
        // 验证 progressUserId 是否修改成功
        Assertions.assertEquals(processUserId, secondSelect.getProcessUserId());
    }

    @Test
    public void testCreateApiErrorLogAsync() {
        // 准备参数
        ApiErrorLogCreateReqDTO createDTO = RandomUtils.randomPojo(ApiErrorLogCreateReqDTO.class,
                dto -> dto.setUserType(RandomUtil.randomEle(UserTypeEnum.values()).getValue()));

        // 调用
        apiErrorLogService.createApiErrorLog(createDTO);
        // 断言
        ApiErrorLogDO infApiErrorLogDO = infApiErrorLogMapper.selectOne(null);
        assertNotNull(infApiErrorLogDO);
        assertPojoEquals(createDTO, infApiErrorLogDO);
    }

}
