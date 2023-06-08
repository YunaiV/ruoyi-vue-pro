package cn.iocoder.yudao.module.infra.service.logger;

import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.infra.api.logger.dto.ApiErrorLogCreateReqDTO;
import cn.iocoder.yudao.module.infra.controller.admin.logger.vo.apierrorlog.ApiErrorLogExportReqVO;
import cn.iocoder.yudao.module.infra.controller.admin.logger.vo.apierrorlog.ApiErrorLogPageReqVO;
import cn.iocoder.yudao.module.infra.dal.dataobject.logger.ApiErrorLogDO;
import cn.iocoder.yudao.module.infra.dal.mysql.logger.ApiErrorLogMapper;
import cn.iocoder.yudao.module.infra.enums.logger.ApiErrorLogProcessStatusEnum;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;
import java.util.List;

import static cn.hutool.core.util.RandomUtil.randomEle;
import static cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils.buildBetweenTime;
import static cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils.buildTime;
import static cn.iocoder.yudao.framework.common.util.object.ObjectUtils.cloneIgnoreId;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertPojoEquals;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomLongId;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static cn.iocoder.yudao.module.infra.enums.ErrorCodeConstants.API_ERROR_LOG_NOT_FOUND;
import static cn.iocoder.yudao.module.infra.enums.ErrorCodeConstants.API_ERROR_LOG_PROCESSED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Import(ApiErrorLogServiceImpl.class)
public class ApiErrorLogServiceImplTest extends BaseDbUnitTest {

    @Resource
    private ApiErrorLogServiceImpl apiErrorLogService;

    @Resource
    private ApiErrorLogMapper apiErrorLogMapper;

    @Test
    public void testGetApiErrorLogPage() {
        // mock 数据
        ApiErrorLogDO apiErrorLogDO = randomPojo(ApiErrorLogDO.class, o -> {
            o.setUserId(2233L);
            o.setUserType(UserTypeEnum.ADMIN.getValue());
            o.setApplicationName("yudao-test");
            o.setRequestUrl("foo");
            o.setExceptionTime(buildTime(2021, 3, 13));
            o.setProcessStatus(ApiErrorLogProcessStatusEnum.INIT.getStatus());
        });
        apiErrorLogMapper.insert(apiErrorLogDO);
        // 测试 userId 不匹配
        apiErrorLogMapper.insert(cloneIgnoreId(apiErrorLogDO, o -> o.setUserId(3344L)));
        // 测试 userType 不匹配
        apiErrorLogMapper.insert(cloneIgnoreId(apiErrorLogDO, logDO -> logDO.setUserType(UserTypeEnum.MEMBER.getValue())));
        // 测试 applicationName 不匹配
        apiErrorLogMapper.insert(cloneIgnoreId(apiErrorLogDO, logDO -> logDO.setApplicationName("test")));
        // 测试 requestUrl 不匹配
        apiErrorLogMapper.insert(cloneIgnoreId(apiErrorLogDO, logDO -> logDO.setRequestUrl("bar")));
        // 测试 exceptionTime 不匹配：构造一个早期时间 2021-02-06 00:00:00
        apiErrorLogMapper.insert(cloneIgnoreId(apiErrorLogDO, logDO -> logDO.setExceptionTime(buildTime(2021, 2, 6))));
        // 测试 progressStatus 不匹配
        apiErrorLogMapper.insert(cloneIgnoreId(apiErrorLogDO, logDO -> logDO.setProcessStatus(ApiErrorLogProcessStatusEnum.DONE.getStatus())));
        // 准备参数
        ApiErrorLogPageReqVO reqVO = new ApiErrorLogPageReqVO();
        reqVO.setUserId(2233L);
        reqVO.setUserType(UserTypeEnum.ADMIN.getValue());
        reqVO.setApplicationName("yudao-test");
        reqVO.setRequestUrl("foo");
        reqVO.setExceptionTime(buildBetweenTime(2021, 3, 1, 2021, 3, 31));
        reqVO.setProcessStatus(ApiErrorLogProcessStatusEnum.INIT.getStatus());

        // 调用
        PageResult<ApiErrorLogDO> pageResult = apiErrorLogService.getApiErrorLogPage(reqVO);
        // 断言，只查到了一条符合条件的
        assertEquals(1, pageResult.getTotal());
        assertEquals(1, pageResult.getList().size());
        assertPojoEquals(apiErrorLogDO, pageResult.getList().get(0));
    }

    @Test
    public void testGetApiErrorLogList() {
        // mock 数据
        ApiErrorLogDO apiErrorLogDO = randomPojo(ApiErrorLogDO.class, o -> {
            o.setUserId(2233L);
            o.setUserType(UserTypeEnum.ADMIN.getValue());
            o.setApplicationName("yudao-test");
            o.setRequestUrl("foo");
            o.setExceptionTime(buildTime(2021, 3, 13));
            o.setProcessStatus(ApiErrorLogProcessStatusEnum.INIT.getStatus());
        });
        apiErrorLogMapper.insert(apiErrorLogDO);
        // 测试 userId 不匹配
        apiErrorLogMapper.insert(cloneIgnoreId(apiErrorLogDO, o -> o.setUserId(3344L)));
        // 测试 userType 不匹配
        apiErrorLogMapper.insert(cloneIgnoreId(apiErrorLogDO, logDO -> logDO.setUserType(UserTypeEnum.MEMBER.getValue())));
        // 测试 applicationName 不匹配
        apiErrorLogMapper.insert(cloneIgnoreId(apiErrorLogDO, logDO -> logDO.setApplicationName("test")));
        // 测试 requestUrl 不匹配
        apiErrorLogMapper.insert(cloneIgnoreId(apiErrorLogDO, logDO -> logDO.setRequestUrl("bar")));
        // 测试 exceptionTime 不匹配：构造一个早期时间 2021-02-06 00:00:00
        apiErrorLogMapper.insert(cloneIgnoreId(apiErrorLogDO, logDO -> logDO.setExceptionTime(buildTime(2021, 2, 6))));
        // 测试 progressStatus 不匹配
        apiErrorLogMapper.insert(cloneIgnoreId(apiErrorLogDO, logDO -> logDO.setProcessStatus(ApiErrorLogProcessStatusEnum.DONE.getStatus())));
        // 准备参数
        ApiErrorLogExportReqVO reqVO = new ApiErrorLogExportReqVO();
        reqVO.setUserId(2233L);
        reqVO.setUserType(UserTypeEnum.ADMIN.getValue());
        reqVO.setApplicationName("yudao-test");
        reqVO.setRequestUrl("foo");
        reqVO.setExceptionTime(buildBetweenTime(2021, 3, 1, 2021, 3, 31));
        reqVO.setProcessStatus(ApiErrorLogProcessStatusEnum.INIT.getStatus());

        // 调用
        List<ApiErrorLogDO> list = apiErrorLogService.getApiErrorLogList(reqVO);
        // 断言，只查到了一条符合条件的
        assertEquals(1, list.size());
        assertPojoEquals(apiErrorLogDO, list.get(0));
    }

    @Test
    public void testCreateApiErrorLog() {
        // 准备参数
        ApiErrorLogCreateReqDTO createDTO = randomPojo(ApiErrorLogCreateReqDTO.class);

        // 调用
        apiErrorLogService.createApiErrorLog(createDTO);
        // 断言
        ApiErrorLogDO apiErrorLogDO = apiErrorLogMapper.selectOne(null);
        assertPojoEquals(createDTO, apiErrorLogDO);
        assertEquals(ApiErrorLogProcessStatusEnum.INIT.getStatus(), apiErrorLogDO.getProcessStatus());
    }

    @Test
    public void testUpdateApiErrorLogProcess_success() {
        // 准备参数
        ApiErrorLogDO apiErrorLogDO = randomPojo(ApiErrorLogDO.class,
                o -> o.setProcessStatus(ApiErrorLogProcessStatusEnum.INIT.getStatus()));
        apiErrorLogMapper.insert(apiErrorLogDO);
        // 准备参数
        Long id = apiErrorLogDO.getId();
        Integer processStatus = randomEle(ApiErrorLogProcessStatusEnum.values()).getStatus();
        Long processUserId = randomLongId();

        // 调用
        apiErrorLogService.updateApiErrorLogProcess(id, processStatus, processUserId);
        // 断言
        ApiErrorLogDO dbApiErrorLogDO = apiErrorLogMapper.selectById(apiErrorLogDO.getId());
        assertEquals(processStatus, dbApiErrorLogDO.getProcessStatus());
        assertEquals(processUserId, dbApiErrorLogDO.getProcessUserId());
        assertNotNull(dbApiErrorLogDO.getProcessTime());
    }

    @Test
    public void testUpdateApiErrorLogProcess_processed() {
        // 准备参数
        ApiErrorLogDO apiErrorLogDO = randomPojo(ApiErrorLogDO.class,
                o -> o.setProcessStatus(ApiErrorLogProcessStatusEnum.DONE.getStatus()));
        apiErrorLogMapper.insert(apiErrorLogDO);
        // 准备参数
        Long id = apiErrorLogDO.getId();
        Integer processStatus = randomEle(ApiErrorLogProcessStatusEnum.values()).getStatus();
        Long processUserId = randomLongId();

        // 调用，并断言异常
        assertServiceException(() ->
                apiErrorLogService.updateApiErrorLogProcess(id, processStatus, processUserId),
                API_ERROR_LOG_PROCESSED);
    }

    @Test
    public void testUpdateApiErrorLogProcess_notFound() {
        // 准备参数
        Long id = randomLongId();
        Integer processStatus = randomEle(ApiErrorLogProcessStatusEnum.values()).getStatus();
        Long processUserId = randomLongId();

        // 调用，并断言异常
        assertServiceException(() ->
                apiErrorLogService.updateApiErrorLogProcess(id, processStatus, processUserId),
                API_ERROR_LOG_NOT_FOUND);
    }

}
