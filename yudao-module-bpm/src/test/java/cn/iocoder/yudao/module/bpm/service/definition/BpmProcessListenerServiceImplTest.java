package cn.iocoder.yudao.module.bpm.service.definition;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.framework.test.core.util.AssertUtils;
import cn.iocoder.yudao.framework.test.core.util.RandomUtils;
import cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.listener.BpmProcessListenerSaveReqVO;
import cn.iocoder.yudao.module.bpm.dal.dataobject.definition.BpmProcessListenerDO;
import cn.iocoder.yudao.module.bpm.dal.mysql.definition.BpmProcessListenerMapper;
import cn.iocoder.yudao.module.bpm.enums.definition.BpmProcessListenerTypeEnum;
import cn.iocoder.yudao.module.bpm.enums.definition.BpmProcessListenerValueTypeEnum;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import static cn.iocoder.yudao.module.bpm.enums.ErrorCodeConstants.PROCESS_LISTENER_CLASS_IMPLEMENTS_ERROR;
import static cn.iocoder.yudao.module.bpm.enums.ErrorCodeConstants.PROCESS_LISTENER_CLASS_NOT_FOUND;
import static cn.iocoder.yudao.module.bpm.enums.ErrorCodeConstants.PROCESS_LISTENER_EXPRESSION_INVALID;
import static cn.iocoder.yudao.module.bpm.enums.ErrorCodeConstants.PROCESS_LISTENER_NOT_EXISTS;

/**
 * {@link BpmProcessListenerServiceImpl} 的单元测试类
 *
 * @author HUIHUI
 */
@Import(BpmProcessListenerServiceImpl.class)
public class BpmProcessListenerServiceImplTest extends BaseDbUnitTest {

    @Resource
    private BpmProcessListenerServiceImpl processListenerService;
    @Resource
    private BpmProcessListenerMapper processListenerMapper;

    @Test
    public void testCreateProcessListener_class_success() {
        // 准备参数
        BpmProcessListenerSaveReqVO reqVO = randomRequest()
                .setType(BpmProcessListenerTypeEnum.EXECUTION.getType())
                .setValueType(BpmProcessListenerValueTypeEnum.CLASS.getType())
                .setValue("cn.iocoder.yudao.module.bpm.framework.flowable.core.listener.demo.exection.DemoDelegateClassExecutionListener");

        // 调用
        Long listenerId = processListenerService.createProcessListener(reqVO);
        // 断言
        Assertions.assertNotNull(listenerId);
        AssertUtils.assertPojoEquals(reqVO, processListenerMapper.selectById(listenerId));
    }

    @Test
    public void testCreateProcessListener_expression_success() {
        // 准备参数
        BpmProcessListenerSaveReqVO reqVO = randomRequest()
                .setValueType(BpmProcessListenerValueTypeEnum.EXPRESSION.getType()).setValue("${listener.handle}");

        // 调用
        Long listenerId = processListenerService.createProcessListener(reqVO);
        // 断言
        Assertions.assertNotNull(listenerId);
    }

    @Test
    public void testCreateProcessListener_classNotFound() {
        // 准备参数
        BpmProcessListenerSaveReqVO reqVO = randomRequest().setValueType(BpmProcessListenerValueTypeEnum.CLASS.getType())
                .setValue("cn.iocoder.yudao.module.bpm.NotExistsListener");

        // 调用，并断言异常
        AssertUtils.assertServiceException(() -> processListenerService.createProcessListener(reqVO),
                PROCESS_LISTENER_CLASS_NOT_FOUND, reqVO.getValue());
    }

    @Test
    public void testCreateProcessListener_classNotImplements() {
        // 准备参数
        BpmProcessListenerSaveReqVO reqVO = randomRequest().setValueType(BpmProcessListenerValueTypeEnum.CLASS.getType())
                .setValue(String.class.getName());

        // 调用，并断言异常
        AssertUtils.assertServiceException(() -> processListenerService.createProcessListener(reqVO),
                PROCESS_LISTENER_CLASS_IMPLEMENTS_ERROR, reqVO.getValue(),
                org.flowable.engine.delegate.TaskListener.class.getName());
    }

    @Test
    public void testCreateProcessListener_expressionInvalid() {
        // 准备参数
        BpmProcessListenerSaveReqVO reqVO = randomRequest().setValueType(BpmProcessListenerValueTypeEnum.EXPRESSION.getType())
                .setValue("listener.handle");

        // 调用，并断言异常
        AssertUtils.assertServiceException(() -> processListenerService.createProcessListener(reqVO),
                PROCESS_LISTENER_EXPRESSION_INVALID, reqVO.getValue());
    }

    @Test
    public void testDeleteProcessListener_notExists() {
        // 准备参数
        Long id = RandomUtils.randomLongId();

        // 调用，并断言异常
        AssertUtils.assertServiceException(() -> processListenerService.deleteProcessListener(id),
                PROCESS_LISTENER_NOT_EXISTS);
    }

    private static BpmProcessListenerSaveReqVO randomRequest() {
        return RandomUtils.randomPojo(BpmProcessListenerSaveReqVO.class, o -> {
            o.setStatus(CommonStatusEnum.ENABLE.getStatus());
            o.setType(BpmProcessListenerTypeEnum.TASK.getType());
            o.setEvent("create");
        });
    }

}
