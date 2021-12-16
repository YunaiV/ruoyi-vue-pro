package cn.iocoder.yudao.adminserver.modules.infra.service.logger;

import cn.iocoder.yudao.adminserver.BaseDbUnitTest;
import cn.iocoder.yudao.coreservice.modules.infra.dal.dataobject.logger.InfApiAccessLogDO;
import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.adminserver.modules.infra.controller.logger.vo.apiaccesslog.InfApiAccessLogExportReqVO;
import cn.iocoder.yudao.adminserver.modules.infra.controller.logger.vo.apiaccesslog.InfApiAccessLogPageReqVO;
import cn.iocoder.yudao.adminserver.modules.infra.dal.mysql.logger.InfApiAccessLogMapper;
import cn.iocoder.yudao.adminserver.modules.infra.service.logger.impl.InfApiAccessLogServiceImpl;
import cn.iocoder.yudao.framework.test.core.util.RandomUtils;
import cn.iocoder.yudao.framework.common.util.object.ObjectUtils;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertPojoEquals;
import static cn.iocoder.yudao.framework.common.util.date.DateUtils.buildTime;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * {@link InfApiAccessLogServiceImpl} 单元测试
 */
@Import(InfApiAccessLogServiceImpl.class)
public class InfApiAccessLogServiceImplTest extends BaseDbUnitTest {

    @Resource
    private InfApiAccessLogService infApiAccessLogServiceImpl;

    @Resource
    private InfApiAccessLogMapper infApiAccessLogMapper;

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

        InfApiAccessLogDO infApiAccessLogDO = RandomUtils.randomPojo(InfApiAccessLogDO.class, dto -> {
            dto.setUserId(userId);
            dto.setUserType(userType);
            dto.setApplicationName(applicationName);
            dto.setRequestUrl(requestUrl);
            dto.setBeginTime(beginTime);
            dto.setDuration(duration);
            dto.setResultCode(resultCode);
        });
        infApiAccessLogMapper.insert(infApiAccessLogDO);

        // 下面几个都是不匹配的数据
        // userId 不同的
        infApiAccessLogMapper.insert(ObjectUtils.cloneIgnoreId(infApiAccessLogDO, logDO -> logDO.setUserId(3344L)));
        // userType
        infApiAccessLogMapper.insert(ObjectUtils.cloneIgnoreId(infApiAccessLogDO, logDO -> logDO.setUserType(UserTypeEnum.MEMBER.getValue())));
        // applicationName 不同的
        infApiAccessLogMapper.insert(ObjectUtils.cloneIgnoreId(infApiAccessLogDO, logDO -> logDO.setApplicationName("test")));
        // requestUrl 不同的
        infApiAccessLogMapper.insert(ObjectUtils.cloneIgnoreId(infApiAccessLogDO, logDO -> logDO.setRequestUrl("bar")));
        // 构造一个早期时间 2021-02-06 00:00:00
        infApiAccessLogMapper.insert(ObjectUtils.cloneIgnoreId(infApiAccessLogDO, logDO -> logDO.setBeginTime(buildTime(2021, 2, 6))));
        // duration 不同的
        infApiAccessLogMapper.insert(ObjectUtils.cloneIgnoreId(infApiAccessLogDO, logDO -> logDO.setDuration(100)));
        // resultCode 不同的
        infApiAccessLogMapper.insert(ObjectUtils.cloneIgnoreId(infApiAccessLogDO, logDO -> logDO.setResultCode(2)));

        // 构造调用参数
        InfApiAccessLogPageReqVO reqVO = new InfApiAccessLogPageReqVO();
        reqVO.setUserId(userId);
        reqVO.setUserType(userType);
        reqVO.setApplicationName(applicationName);
        reqVO.setRequestUrl(requestUrl);
        reqVO.setBeginBeginTime(buildTime(2021, 3, 12));
        reqVO.setEndBeginTime(buildTime(2021, 3, 14));
        reqVO.setDuration(duration);
        reqVO.setResultCode(resultCode);

        // 调用service方法
        PageResult<InfApiAccessLogDO> pageResult = infApiAccessLogServiceImpl.getApiAccessLogPage(reqVO);

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

        InfApiAccessLogDO infApiAccessLogDO = RandomUtils.randomPojo(InfApiAccessLogDO.class, dto -> {
            dto.setUserId(userId);
            dto.setUserType(userType);
            dto.setApplicationName(applicationName);
            dto.setRequestUrl(requestUrl);
            dto.setBeginTime(beginTime);
            dto.setDuration(duration);
            dto.setResultCode(resultCode);
        });
        infApiAccessLogMapper.insert(infApiAccessLogDO);

        // 下面几个都是不匹配的数据
        // userId 不同的
        infApiAccessLogMapper.insert(ObjectUtils.cloneIgnoreId(infApiAccessLogDO, logDO -> logDO.setUserId(3344L)));
        // userType
        infApiAccessLogMapper.insert(ObjectUtils.cloneIgnoreId(infApiAccessLogDO, logDO -> logDO.setUserType(UserTypeEnum.MEMBER.getValue())));
        // applicationName 不同的
        infApiAccessLogMapper.insert(ObjectUtils.cloneIgnoreId(infApiAccessLogDO, logDO -> logDO.setApplicationName("test")));
        // requestUrl 不同的
        infApiAccessLogMapper.insert(ObjectUtils.cloneIgnoreId(infApiAccessLogDO, logDO -> logDO.setRequestUrl("bar")));
        // 构造一个早期时间 2021-02-06 00:00:00
        infApiAccessLogMapper.insert(ObjectUtils.cloneIgnoreId(infApiAccessLogDO, logDO -> logDO.setBeginTime(buildTime(2021, 2, 6))));
        // duration 不同的
        infApiAccessLogMapper.insert(ObjectUtils.cloneIgnoreId(infApiAccessLogDO, logDO -> logDO.setDuration(100)));
        // resultCode 不同的
        infApiAccessLogMapper.insert(ObjectUtils.cloneIgnoreId(infApiAccessLogDO, logDO -> logDO.setResultCode(2)));

        // 构造调用参数
        InfApiAccessLogExportReqVO reqVO = new InfApiAccessLogExportReqVO();
        reqVO.setUserId(userId);
        reqVO.setUserType(userType);
        reqVO.setApplicationName(applicationName);
        reqVO.setRequestUrl(requestUrl);
        reqVO.setBeginBeginTime(buildTime(2021, 3, 12));
        reqVO.setEndBeginTime(buildTime(2021, 3, 14));
        reqVO.setDuration(duration);
        reqVO.setResultCode(resultCode);

        // 调用service方法
        List<InfApiAccessLogDO> list = infApiAccessLogServiceImpl.getApiAccessLogList(reqVO);

        // 断言，只查到了一条符合条件的
        assertEquals(1, list.size());
        assertPojoEquals(infApiAccessLogDO, list.get(0));
    }

}
