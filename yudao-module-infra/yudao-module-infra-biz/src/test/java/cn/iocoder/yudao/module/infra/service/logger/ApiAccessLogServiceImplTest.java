package cn.iocoder.yudao.module.infra.service.logger;

import cn.hutool.core.util.RandomUtil;
import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.ObjectUtils;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.framework.test.core.util.RandomUtils;
import cn.iocoder.yudao.module.infra.api.logger.dto.ApiAccessLogCreateReqDTO;
import cn.iocoder.yudao.module.infra.controller.admin.logger.vo.apiaccesslog.ApiAccessLogExportReqVO;
import cn.iocoder.yudao.module.infra.controller.admin.logger.vo.apiaccesslog.ApiAccessLogPageReqVO;
import cn.iocoder.yudao.module.infra.dal.dataobject.logger.ApiAccessLogDO;
import cn.iocoder.yudao.module.infra.dal.mysql.logger.ApiAccessLogMapper;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.buildTime;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertPojoEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Import(ApiAccessLogServiceImpl.class)
public class ApiAccessLogServiceImplTest extends BaseDbUnitTest {

    @Resource
    private ApiAccessLogService apiAccessLogService;

    @Resource
    private ApiAccessLogMapper apiAccessLogMapper;

    @Test
    public void testGetApiAccessLogPage() {
        // 构造测试数据
        long userId = 2233L;
        int userType = UserTypeEnum.ADMIN.getValue();
        String applicationName = "yudao-test";
        String requestUrl = "foo";
        Date beginTime = buildTime(2021, 3, 13);
        int duration = 1000;
        int resultCode = GlobalErrorCodeConstants.SUCCESS.getCode();

        ApiAccessLogDO infApiAccessLogDO = RandomUtils.randomPojo(ApiAccessLogDO.class, dto -> {
            dto.setUserId(userId);
            dto.setUserType(userType);
            dto.setApplicationName(applicationName);
            dto.setRequestUrl(requestUrl);
            dto.setBeginTime(beginTime);
            dto.setDuration(duration);
            dto.setResultCode(resultCode);
        });
        apiAccessLogMapper.insert(infApiAccessLogDO);

        // 下面几个都是不匹配的数据
        // userId 不同的
        apiAccessLogMapper.insert(ObjectUtils.cloneIgnoreId(infApiAccessLogDO, logDO -> logDO.setUserId(3344L)));
        // userType
        apiAccessLogMapper.insert(ObjectUtils.cloneIgnoreId(infApiAccessLogDO, logDO -> logDO.setUserType(UserTypeEnum.MEMBER.getValue())));
        // applicationName 不同的
        apiAccessLogMapper.insert(ObjectUtils.cloneIgnoreId(infApiAccessLogDO, logDO -> logDO.setApplicationName("test")));
        // requestUrl 不同的
        apiAccessLogMapper.insert(ObjectUtils.cloneIgnoreId(infApiAccessLogDO, logDO -> logDO.setRequestUrl("bar")));
        // 构造一个早期时间 2021-02-06 00:00:00
        apiAccessLogMapper.insert(ObjectUtils.cloneIgnoreId(infApiAccessLogDO, logDO -> logDO.setBeginTime(buildTime(2021, 2, 6))));
        // duration 不同的
        apiAccessLogMapper.insert(ObjectUtils.cloneIgnoreId(infApiAccessLogDO, logDO -> logDO.setDuration(100)));
        // resultCode 不同的
        apiAccessLogMapper.insert(ObjectUtils.cloneIgnoreId(infApiAccessLogDO, logDO -> logDO.setResultCode(2)));

        // 构造调用参数
        ApiAccessLogPageReqVO reqVO = new ApiAccessLogPageReqVO();
        reqVO.setUserId(userId);
        reqVO.setUserType(userType);
        reqVO.setApplicationName(applicationName);
        reqVO.setRequestUrl(requestUrl);
        reqVO.setBeginBeginTime(buildTime(2021, 3, 12));
        reqVO.setEndBeginTime(buildTime(2021, 3, 14));
        reqVO.setDuration(duration);
        reqVO.setResultCode(resultCode);

        // 调用service方法
        PageResult<ApiAccessLogDO> pageResult = apiAccessLogService.getApiAccessLogPage(reqVO);

        // 断言，只查到了一条符合条件的
        assertEquals(1, pageResult.getTotal());
        assertEquals(1, pageResult.getList().size());
        assertPojoEquals(infApiAccessLogDO, pageResult.getList().get(0));
    }

    @Test
    public void testGetApiAccessLogList() {
        // 构造测试数据
        long userId = 2233L;
        int userType = UserTypeEnum.ADMIN.getValue();
        String applicationName = "yudao-test";
        String requestUrl = "foo";
        Date beginTime = buildTime(2021, 3, 13);
        int duration = 1000;
        int resultCode = GlobalErrorCodeConstants.SUCCESS.getCode();

        ApiAccessLogDO infApiAccessLogDO = RandomUtils.randomPojo(ApiAccessLogDO.class, dto -> {
            dto.setUserId(userId);
            dto.setUserType(userType);
            dto.setApplicationName(applicationName);
            dto.setRequestUrl(requestUrl);
            dto.setBeginTime(beginTime);
            dto.setDuration(duration);
            dto.setResultCode(resultCode);
        });
        apiAccessLogMapper.insert(infApiAccessLogDO);

        // 下面几个都是不匹配的数据
        // userId 不同的
        apiAccessLogMapper.insert(ObjectUtils.cloneIgnoreId(infApiAccessLogDO, logDO -> logDO.setUserId(3344L)));
        // userType
        apiAccessLogMapper.insert(ObjectUtils.cloneIgnoreId(infApiAccessLogDO, logDO -> logDO.setUserType(UserTypeEnum.MEMBER.getValue())));
        // applicationName 不同的
        apiAccessLogMapper.insert(ObjectUtils.cloneIgnoreId(infApiAccessLogDO, logDO -> logDO.setApplicationName("test")));
        // requestUrl 不同的
        apiAccessLogMapper.insert(ObjectUtils.cloneIgnoreId(infApiAccessLogDO, logDO -> logDO.setRequestUrl("bar")));
        // 构造一个早期时间 2021-02-06 00:00:00
        apiAccessLogMapper.insert(ObjectUtils.cloneIgnoreId(infApiAccessLogDO, logDO -> logDO.setBeginTime(buildTime(2021, 2, 6))));
        // duration 不同的
        apiAccessLogMapper.insert(ObjectUtils.cloneIgnoreId(infApiAccessLogDO, logDO -> logDO.setDuration(100)));
        // resultCode 不同的
        apiAccessLogMapper.insert(ObjectUtils.cloneIgnoreId(infApiAccessLogDO, logDO -> logDO.setResultCode(2)));

        // 构造调用参数
        ApiAccessLogExportReqVO reqVO = new ApiAccessLogExportReqVO();
        reqVO.setUserId(userId);
        reqVO.setUserType(userType);
        reqVO.setApplicationName(applicationName);
        reqVO.setRequestUrl(requestUrl);
        reqVO.setBeginBeginTime(buildTime(2021, 3, 12));
        reqVO.setEndBeginTime(buildTime(2021, 3, 14));
        reqVO.setDuration(duration);
        reqVO.setResultCode(resultCode);

        // 调用service方法
        List<ApiAccessLogDO> list = apiAccessLogService.getApiAccessLogList(reqVO);

        // 断言，只查到了一条符合条件的
        assertEquals(1, list.size());
        assertPojoEquals(infApiAccessLogDO, list.get(0));
    }

    @Test
    public void testCreateApiAccessLogAsync() {
        // 准备参数
        ApiAccessLogCreateReqDTO createDTO = RandomUtils.randomPojo(ApiAccessLogCreateReqDTO.class,
                dto -> dto.setUserType(RandomUtil.randomEle(UserTypeEnum.values()).getValue()));

        // 调用
        apiAccessLogService.createApiAccessLog(createDTO);
        // 断言
        ApiAccessLogDO infApiAccessLogDO = apiAccessLogMapper.selectOne(null);
        assertNotNull(infApiAccessLogDO);
        assertPojoEquals(createDTO, infApiAccessLogDO);
    }


}
