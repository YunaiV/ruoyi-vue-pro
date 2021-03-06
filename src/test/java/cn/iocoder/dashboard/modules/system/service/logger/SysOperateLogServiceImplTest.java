package cn.iocoder.dashboard.modules.system.service.logger;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.iocoder.dashboard.BaseSpringBootUnitTest;
import cn.iocoder.dashboard.common.enums.CommonStatusEnum;
import cn.iocoder.dashboard.common.exception.enums.GlobalErrorCodeConstants;
import cn.iocoder.dashboard.common.pojo.PageResult;
import cn.iocoder.dashboard.framework.logger.operatelog.core.enums.OperateTypeEnum;
import cn.iocoder.dashboard.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.dashboard.framework.tracer.core.util.TracerUtils;
import cn.iocoder.dashboard.modules.system.controller.logger.vo.operatelog.SysOperateLogCreateReqVO;
import cn.iocoder.dashboard.modules.system.controller.logger.vo.operatelog.SysOperateLogExportReqVO;
import cn.iocoder.dashboard.modules.system.controller.logger.vo.operatelog.SysOperateLogPageReqVO;
import cn.iocoder.dashboard.modules.system.convert.logger.SysOperateLogConvert;
import cn.iocoder.dashboard.modules.system.dal.dataobject.logger.SysOperateLogDO;
import cn.iocoder.dashboard.modules.system.dal.dataobject.user.SysUserDO;
import cn.iocoder.dashboard.modules.system.dal.mysql.logger.SysOperateLogMapper;
import cn.iocoder.dashboard.modules.system.dal.mysql.user.SysUserMapper;
import cn.iocoder.dashboard.modules.system.enums.common.SysSexEnum;
import cn.iocoder.dashboard.util.RandomUtils;
import cn.iocoder.dashboard.util.object.ObjectUtils;
import org.junit.jupiter.api.Test;

import javax.annotation.Resource;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static cn.iocoder.dashboard.util.AssertUtils.assertPojoEquals;
import static cn.iocoder.dashboard.util.date.DateUtils.buildTime;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class SysOperateLogServiceImplTest extends BaseSpringBootUnitTest {

    @Resource
    private SysOperateLogService sysOperateLogServiceImpl;

    @Resource
    private SysOperateLogMapper sysOperateLogMapper;

    @Resource
    private SysUserMapper sysUserMapper;

    @Test
    public void testCreateOperateLogAsync() throws InterruptedException {

        String traceId = TracerUtils.getTraceId();
        SysOperateLogCreateReqVO reqVO = RandomUtils.randomPojo(SysOperateLogCreateReqVO.class, vo -> {
            vo.setTraceId(traceId);
            vo.setUserId(RandomUtil.randomLong(1, Long.MAX_VALUE));

            Map<String, Object> map = new HashMap<>();
            map.put("orderId", 1);
            vo.setExts(map);
        });

        // 执行service方法
        sysOperateLogServiceImpl.createOperateLogAsync(reqVO);

        // 等异步执行完
        Thread.sleep(2000);

        // 查询插入的数据
        SysOperateLogDO sysOperateLogDO = sysOperateLogMapper.selectOne("trace_id", traceId);

        // 断言
        assertNotNull(sysOperateLogDO);
        // 断言，忽略基本字段
        assertPojoEquals(
                SysOperateLogConvert.INSTANCE.convert(reqVO),
                sysOperateLogDO,
                getBaseDOFields()
        );
    }


    @Test
    public void testPageOperateLog() {

        // 构造测试数据

        // 先构造用户
        SysUserDO user = RandomUtils.randomPojo(SysUserDO.class, sysUserDO -> {
            sysUserDO.setNickname("wangkai");
            sysUserDO.setSex(SysSexEnum.MALE.getSEX());
            sysUserDO.setStatus(CommonStatusEnum.ENABLE.getStatus());
        });
        sysUserMapper.insert(user);
        Long userId = user.getId();


        // 构造操作日志
        SysOperateLogDO sysOperateLogDO = RandomUtils.randomPojo(SysOperateLogDO.class, entity -> {
            entity.setTraceId(TracerUtils.getTraceId());
            entity.setUserId(userId);
            entity.setModule("order");
            entity.setType(OperateTypeEnum.CREATE.getType());
            entity.setStartTime(buildTime(2021, 3, 6));
            entity.setResultCode(GlobalErrorCodeConstants.SUCCESS.getCode());

            Map<String, Object> map = new HashMap<>();
            map.put("orderId", 1);
            entity.setExts(map);
        });

        sysOperateLogMapper.insert(sysOperateLogDO);

        // 下面几个是不匹配的数据
        // 随机userId
        sysOperateLogMapper.insert(ObjectUtils.clone(sysOperateLogDO, logDO -> logDO.setUserId(userId + 1)));
        // module不同
        sysOperateLogMapper.insert(ObjectUtils.clone(sysOperateLogDO, logDO -> logDO.setModule("user")));
        // type不同
        sysOperateLogMapper.insert(ObjectUtils.clone(sysOperateLogDO, logDO -> logDO.setType(OperateTypeEnum.IMPORT.getType())));
        // createTime不同
        sysOperateLogMapper.insert(ObjectUtils.clone(sysOperateLogDO, logDO -> logDO.setStartTime(buildTime(2021, 2, 6))));
        // resultCode不同
        sysOperateLogMapper.insert(ObjectUtils.clone(sysOperateLogDO, logDO -> logDO.setResultCode(GlobalErrorCodeConstants.BAD_REQUEST.getCode())));

        // 构造调用参数
        SysOperateLogPageReqVO reqVO = new SysOperateLogPageReqVO();
        reqVO.setUserNickname("wangkai");
        reqVO.setModule("order");
        reqVO.setType(OperateTypeEnum.CREATE.getType());
        reqVO.setBeginTime(buildTime(2021, 3, 5));
        reqVO.setEndTime(buildTime(2021, 3, 7));
        reqVO.setSuccess(true);

        // 调用service方法
        PageResult<SysOperateLogDO> pageResult = sysOperateLogServiceImpl.pageOperateLog(reqVO);

        // 断言，只查到了一条符合条件的
        assertEquals(1, pageResult.getTotal());
        assertEquals(1, pageResult.getList().size());
        assertPojoEquals(sysOperateLogDO, pageResult.getList().get(0));
    }

    @Test
    public void testListOperateLogs() {

        // 构造测试数据

        // 先构造用户
        SysUserDO user = RandomUtils.randomPojo(SysUserDO.class, sysUserDO -> {
            sysUserDO.setNickname("wangkai");
            sysUserDO.setSex(SysSexEnum.MALE.getSEX());
            sysUserDO.setStatus(CommonStatusEnum.ENABLE.getStatus());
        });
        sysUserMapper.insert(user);
        Long userId = user.getId();


        // 构造操作日志
        SysOperateLogDO sysOperateLogDO = RandomUtils.randomPojo(SysOperateLogDO.class, entity -> {
            entity.setTraceId(TracerUtils.getTraceId());
            entity.setUserId(userId);
            entity.setModule("order");
            entity.setType(OperateTypeEnum.CREATE.getType());
            entity.setStartTime(buildTime(2021, 3, 6));
            entity.setResultCode(GlobalErrorCodeConstants.SUCCESS.getCode());

            Map<String, Object> map = new HashMap<>();
            map.put("orderId", 1);
            entity.setExts(map);
        });

        sysOperateLogMapper.insert(sysOperateLogDO);

        // 下面几个是不匹配的数据
        // 随机userId
        sysOperateLogMapper.insert(ObjectUtils.clone(sysOperateLogDO, logDO -> logDO.setUserId(userId + 1)));
        // module不同
        sysOperateLogMapper.insert(ObjectUtils.clone(sysOperateLogDO, logDO -> logDO.setModule("user")));
        // type不同
        sysOperateLogMapper.insert(ObjectUtils.clone(sysOperateLogDO, logDO -> logDO.setType(OperateTypeEnum.IMPORT.getType())));
        // createTime不同
        sysOperateLogMapper.insert(ObjectUtils.clone(sysOperateLogDO, logDO -> logDO.setStartTime(buildTime(2021, 2, 6))));
        // resultCode不同
        sysOperateLogMapper.insert(ObjectUtils.clone(sysOperateLogDO, logDO -> logDO.setResultCode(GlobalErrorCodeConstants.BAD_REQUEST.getCode())));

        // 构造调用参数
        SysOperateLogExportReqVO reqVO = new SysOperateLogExportReqVO();
        reqVO.setUserNickname("wangkai");
        reqVO.setModule("order");
        reqVO.setType(OperateTypeEnum.CREATE.getType());
        reqVO.setBeginTime(buildTime(2021, 3, 5));
        reqVO.setEndTime(buildTime(2021, 3, 7));
        reqVO.setSuccess(true);

        // 调用service方法
        List<SysOperateLogDO> list = sysOperateLogServiceImpl.listOperateLogs(reqVO);

        // 断言，只查到了一条符合条件的
        assertEquals(1, list.size());
        assertPojoEquals(sysOperateLogDO, list.get(0));
    }


    private static String[] getBaseDOFields() {
        Field[] fields = ReflectUtil.getFields(BaseDO.class);

        List<String> collect = Arrays.stream(fields)
                .map(Field::getName)
                .collect(Collectors.toList());
        collect.add("id");

        return ArrayUtil.toArray(collect, String.class);
    }

}
