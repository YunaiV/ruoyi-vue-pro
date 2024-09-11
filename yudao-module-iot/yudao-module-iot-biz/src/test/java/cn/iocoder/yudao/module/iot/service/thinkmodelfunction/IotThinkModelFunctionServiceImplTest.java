package cn.iocoder.yudao.module.iot.service.thinkmodelfunction;

import org.junit.jupiter.api.Test;

import jakarta.annotation.Resource;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;

import cn.iocoder.yudao.module.iot.controller.admin.thinkmodelfunction.vo.*;
import cn.iocoder.yudao.module.iot.dal.dataobject.thinkmodelfunction.IotThinkModelFunctionDO;
import cn.iocoder.yudao.module.iot.dal.mysql.thinkmodelfunction.IotThinkModelFunctionMapper;

import org.springframework.context.annotation.Import;

import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.*;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.*;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link IotThinkModelFunctionServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
@Import(IotThinkModelFunctionServiceImpl.class)
public class IotThinkModelFunctionServiceImplTest extends BaseDbUnitTest {

    @Resource
    private IotThinkModelFunctionServiceImpl thinkModelFunctionService;

    @Resource
    private IotThinkModelFunctionMapper thinkModelFunctionMapper;

    @Test
    public void testCreateThinkModelFunction_success() {
        // 准备参数
        IotThinkModelFunctionSaveReqVO createReqVO = randomPojo(IotThinkModelFunctionSaveReqVO.class);

        // 调用
        Long thinkModelFunctionId = thinkModelFunctionService.createThinkModelFunction(createReqVO);
        // 断言
        assertNotNull(thinkModelFunctionId);
        // 校验记录的属性是否正确
        IotThinkModelFunctionDO thinkModelFunction = thinkModelFunctionMapper.selectById(thinkModelFunctionId);
        assertPojoEquals(createReqVO, thinkModelFunction, "id");
    }

    @Test
    public void testDeleteThinkModelFunction_success() {
        // mock 数据
        IotThinkModelFunctionDO dbThinkModelFunction = randomPojo(IotThinkModelFunctionDO.class);
        thinkModelFunctionMapper.insert(dbThinkModelFunction);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbThinkModelFunction.getId();

        // 调用
        thinkModelFunctionService.deleteThinkModelFunction(id);
       // 校验数据不存在了
       assertNull(thinkModelFunctionMapper.selectById(id));
    }

    @Test
    public void testDeleteThinkModelFunction_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用, 并断言异常
        assertServiceException(() -> thinkModelFunctionService.deleteThinkModelFunction(id), THINK_MODEL_FUNCTION_NOT_EXISTS);
    }

}