package cn.iocoder.yudao.module.system.service.logger;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.framework.test.core.util.RandomUtils;
import cn.iocoder.yudao.module.system.api.logger.dto.OperateLogCreateReqDTO;
import cn.iocoder.yudao.module.system.api.logger.dto.OperateLogPageReqDTO;
import cn.iocoder.yudao.module.system.controller.admin.logger.vo.operatelog.OperateLogPageReqVO;
import cn.iocoder.yudao.module.system.dal.dataobject.logger.OperateLogDO;
import cn.iocoder.yudao.module.system.dal.mysql.logger.OperateLogMapper;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import static cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils.buildBetweenTime;
import static cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils.buildTime;
import static cn.iocoder.yudao.framework.common.util.object.ObjectUtils.cloneIgnoreId;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertPojoEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Import({OperateLogServiceImpl.class})
public class OperateLogServiceImplTest extends BaseDbUnitTest {

    @Resource
    private OperateLogService operateLogServiceImpl;

    @Resource
    private OperateLogMapper operateLogMapper;

    @Test
    public void testCreateOperateLog() {
        OperateLogCreateReqDTO reqVO = RandomUtils.randomPojo(OperateLogCreateReqDTO.class);

        // 调研
        operateLogServiceImpl.createOperateLog(reqVO);
        // 断言
        OperateLogDO operateLogDO = operateLogMapper.selectOne(null);
        assertPojoEquals(reqVO, operateLogDO);
    }

    @Test
    public void testGetOperateLogPage_vo() {
        // 构造操作日志
        OperateLogDO operateLogDO = RandomUtils.randomPojo(OperateLogDO.class, o -> {
            o.setUserId(2048L);
            o.setBizId(999L);
            o.setType("订单");
            o.setSubType("创建订单");
            o.setAction("修改编号为 1 的用户信息");
            o.setCreateTime(buildTime(2021, 3, 6));
        });
        operateLogMapper.insert(operateLogDO);
        // 测试 userId 不匹配
        operateLogMapper.insert(cloneIgnoreId(operateLogDO, o -> o.setUserId(1024L)));
        // 测试 bizId 不匹配
        operateLogMapper.insert(cloneIgnoreId(operateLogDO, o -> o.setBizId(888L)));
        // 测试 type 不匹配
        operateLogMapper.insert(cloneIgnoreId(operateLogDO, o -> o.setType("退款")));
        // 测试 subType 不匹配
        operateLogMapper.insert(cloneIgnoreId(operateLogDO, o -> o.setSubType("创建退款")));
        // 测试 action 不匹配
        operateLogMapper.insert(cloneIgnoreId(operateLogDO, o -> o.setAction("修改编号为 1 退款信息")));
        // 测试 createTime 不匹配
        operateLogMapper.insert(cloneIgnoreId(operateLogDO, o -> o.setCreateTime(buildTime(2021, 2, 6))));

        // 构造调用参数
        OperateLogPageReqVO reqVO = new OperateLogPageReqVO();
        reqVO.setUserId(2048L);
        reqVO.setBizId(999L);
        reqVO.setType("订");
        reqVO.setSubType("订单");
        reqVO.setAction("用户信息");
        reqVO.setCreateTime(buildBetweenTime(2021, 3, 5, 2021, 3, 7));

        // 调用
        PageResult<OperateLogDO> pageResult = operateLogServiceImpl.getOperateLogPage(reqVO);
        // 断言，只查到了一条符合条件的
        assertEquals(1, pageResult.getTotal());
        assertEquals(1, pageResult.getList().size());
        assertPojoEquals(operateLogDO, pageResult.getList().get(0));
    }

    @Test
    public void testGetOperateLogPage_dto() {
        // 构造操作日志
        OperateLogDO operateLogDO = RandomUtils.randomPojo(OperateLogDO.class, o -> {
            o.setUserId(2048L);
            o.setBizId(999L);
            o.setType("订单");
        });
        operateLogMapper.insert(operateLogDO);
        // 测试 userId 不匹配
        operateLogMapper.insert(cloneIgnoreId(operateLogDO, o -> o.setUserId(1024L)));
        // 测试 bizId 不匹配
        operateLogMapper.insert(cloneIgnoreId(operateLogDO, o -> o.setBizId(888L)));
        // 测试 type 不匹配
        operateLogMapper.insert(cloneIgnoreId(operateLogDO, o -> o.setType("退款")));

        // 构造调用参数
        OperateLogPageReqDTO reqDTO = new OperateLogPageReqDTO();
        reqDTO.setUserId(2048L);
        reqDTO.setBizId(999L);
        reqDTO.setType("订单");

        // 调用
        PageResult<OperateLogDO> pageResult = operateLogServiceImpl.getOperateLogPage(reqDTO);
        // 断言，只查到了一条符合条件的
        assertEquals(1, pageResult.getTotal());
        assertEquals(1, pageResult.getList().size());
        assertPojoEquals(operateLogDO, pageResult.getList().get(0));
    }

}
