package cn.iocoder.yudao.module.system.service.logger;

import cn.hutool.core.map.MapUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.monitor.TracerUtils;
import cn.iocoder.yudao.framework.common.util.object.ObjectUtils;
import cn.iocoder.yudao.framework.operatelog.core.enums.OperateTypeEnum;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.framework.test.core.util.RandomUtils;
import cn.iocoder.yudao.module.system.api.logger.dto.OperateLogCreateReqDTO;
import cn.iocoder.yudao.module.system.controller.admin.logger.vo.operatelog.OperateLogExportReqVO;
import cn.iocoder.yudao.module.system.controller.admin.logger.vo.operatelog.OperateLogPageReqVO;
import cn.iocoder.yudao.module.system.dal.dataobject.logger.OperateLogDO;
import cn.iocoder.yudao.module.system.dal.dataobject.user.AdminUserDO;
import cn.iocoder.yudao.module.system.dal.mysql.logger.OperateLogMapper;
import cn.iocoder.yudao.module.system.enums.common.SexEnum;
import cn.iocoder.yudao.module.system.service.user.AdminUserService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static cn.hutool.core.util.RandomUtil.randomEle;
import static cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants.BAD_REQUEST;
import static cn.iocoder.yudao.framework.common.util.date.DateUtils.buildTime;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertPojoEquals;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomLongId;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@Import({OperateLogServiceImpl.class})
public class OperateLogServiceImplTest extends BaseDbUnitTest {

    @Resource
    private OperateLogService operateLogServiceImpl;

    @Resource
    private OperateLogMapper operateLogMapper;

    @MockBean
    private AdminUserService userService;

    @Test
    public void testCreateOperateLogAsync() {
        String traceId = TracerUtils.getTraceId();
        OperateLogCreateReqDTO reqVO = RandomUtils.randomPojo(OperateLogCreateReqDTO.class, o -> {
            o.setTraceId(traceId);
            o.setUserId(randomLongId());
            o.setUserType(randomEle(UserTypeEnum.values()).getValue());
            o.setExts(MapUtil.<String, Object>builder("orderId", randomLongId()).build());
        });

        // 执行service方法
        operateLogServiceImpl.createOperateLog(reqVO);
        // 断言插入是否正确
        OperateLogDO sysOperateLogDO = operateLogMapper.selectOne("trace_id", traceId);
        assertPojoEquals(reqVO, sysOperateLogDO);
    }

    @Test
    public void testGetOperateLogPage() {
        // 构造测试数据
        // 先构造用户
        AdminUserDO user = RandomUtils.randomPojo(AdminUserDO.class, o -> {
            o.setNickname("wangkai");
            o.setSex(SexEnum.MALE.getSex());
            o.setStatus(CommonStatusEnum.ENABLE.getStatus());
        });
        when(userService.getUsersByNickname("wangkai")).thenReturn(Collections.singletonList(user));
        Long userId = user.getId();
        // 构造操作日志
        OperateLogDO sysOperateLogDO = RandomUtils.randomPojo(OperateLogDO.class, o -> {
            o.setUserId(userId);
            o.setUserType(randomEle(UserTypeEnum.values()).getValue());
            o.setModule("order");
            o.setType(OperateTypeEnum.CREATE.getType());
            o.setStartTime(buildTime(2021, 3, 6));
            o.setResultCode(GlobalErrorCodeConstants.SUCCESS.getCode());
            o.setExts(MapUtil.<String, Object>builder("orderId", randomLongId()).build());
        });
        operateLogMapper.insert(sysOperateLogDO);

        // 下面几个是不匹配的数据
        // 随机 userId
        operateLogMapper.insert(ObjectUtils.cloneIgnoreId(sysOperateLogDO, logDO -> logDO.setUserId(userId + 1)));
        // module 不同
        operateLogMapper.insert(ObjectUtils.cloneIgnoreId(sysOperateLogDO, logDO -> logDO.setModule("user")));
        // type 不同
        operateLogMapper.insert(ObjectUtils.cloneIgnoreId(sysOperateLogDO, logDO -> logDO.setType(OperateTypeEnum.IMPORT.getType())));
        // createTime 不同
        operateLogMapper.insert(ObjectUtils.cloneIgnoreId(sysOperateLogDO, logDO -> logDO.setStartTime(buildTime(2021, 2, 6))));
        // resultCode 不同
        operateLogMapper.insert(ObjectUtils.cloneIgnoreId(sysOperateLogDO, logDO -> logDO.setResultCode(BAD_REQUEST.getCode())));

        // 构造调用参数
        OperateLogPageReqVO reqVO = new OperateLogPageReqVO();
        reqVO.setUserNickname("wangkai");
        reqVO.setModule("order");
        reqVO.setType(OperateTypeEnum.CREATE.getType());
        reqVO.setStartTime((new Date[]{buildTime(2021, 3, 5),buildTime(2021, 3, 7)}));
        reqVO.setSuccess(true);

        // 调用service方法
        PageResult<OperateLogDO> pageResult = operateLogServiceImpl.getOperateLogPage(reqVO);
        // 断言，只查到了一条符合条件的
        assertEquals(1, pageResult.getTotal());
        assertEquals(1, pageResult.getList().size());
        assertPojoEquals(sysOperateLogDO, pageResult.getList().get(0));
    }

    @Test
    public void testGetOperateLogs() {
        // 构造测试数据
        // 先构造用户
        AdminUserDO user = RandomUtils.randomPojo(AdminUserDO.class, o -> {
            o.setNickname("wangkai");
            o.setSex(SexEnum.MALE.getSex());
            o.setStatus(CommonStatusEnum.ENABLE.getStatus());
        });
        when(userService.getUsersByNickname("wangkai")).thenReturn(Collections.singletonList(user));
        Long userId = user.getId();
        // 构造操作日志
        OperateLogDO sysOperateLogDO = RandomUtils.randomPojo(OperateLogDO.class, o -> {
            o.setUserId(userId);
            o.setUserType(randomEle(UserTypeEnum.values()).getValue());
            o.setModule("order");
            o.setType(OperateTypeEnum.CREATE.getType());
            o.setStartTime(buildTime(2021, 3, 6));
            o.setResultCode(GlobalErrorCodeConstants.SUCCESS.getCode());
            o.setExts(MapUtil.<String, Object>builder("orderId", randomLongId()).build());
        });
        operateLogMapper.insert(sysOperateLogDO);

        // 下面几个是不匹配的数据
        // 随机 userId
        operateLogMapper.insert(ObjectUtils.cloneIgnoreId(sysOperateLogDO, logDO -> logDO.setUserId(userId + 1)));
        // module 不同
        operateLogMapper.insert(ObjectUtils.cloneIgnoreId(sysOperateLogDO, logDO -> logDO.setModule("user")));
        // type 不同
        operateLogMapper.insert(ObjectUtils.cloneIgnoreId(sysOperateLogDO, logDO -> logDO.setType(OperateTypeEnum.IMPORT.getType())));
        // createTime 不同
        operateLogMapper.insert(ObjectUtils.cloneIgnoreId(sysOperateLogDO, logDO -> logDO.setStartTime(buildTime(2021, 2, 6))));
        // resultCode 不同
        operateLogMapper.insert(ObjectUtils.cloneIgnoreId(sysOperateLogDO, logDO -> logDO.setResultCode(BAD_REQUEST.getCode())));

        // 构造调用参数
        OperateLogExportReqVO reqVO = new OperateLogExportReqVO();
        reqVO.setUserNickname("wangkai");
        reqVO.setModule("order");
        reqVO.setType(OperateTypeEnum.CREATE.getType());
        reqVO.setStartTime((new Date[]{buildTime(2021, 3, 5),buildTime(2021, 3, 7)}));
        reqVO.setSuccess(true);

        // 调用 service 方法
        List<OperateLogDO> list = operateLogServiceImpl.getOperateLogs(reqVO);
        // 断言，只查到了一条符合条件的
        assertEquals(1, list.size());
        assertPojoEquals(sysOperateLogDO, list.get(0));
    }

}
