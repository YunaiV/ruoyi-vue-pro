package cn.iocoder.yudao.module.bpm.framework.flowable;

import cn.iocoder.yudao.module.bpm.service.definition.BpmProcessDefinitionServiceImpl;
import jakarta.annotation.Resource;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.repository.ProcessDefinition;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link BpmProcessDefinitionServiceImpl} 依赖 Flowable 引擎的集成测试
 *
 * <p>基类将 BpmProcessDefinitionService 声明为 Mock，这里直接构造真实实现，并注入真实的 {@link RepositoryService}。</p>
 *
 * @author HUIHUI
 */
public class BpmProcessDefinitionServiceFlowableTest extends BaseFlowableUnitTest {

    @Resource
    private RepositoryService repositoryService;

    private final BpmProcessDefinitionServiceImpl processDefinitionServiceImpl = new BpmProcessDefinitionServiceImpl();

    private String deploymentId;

    @BeforeEach
    public void setUp() {
        ReflectionTestUtils.setField(processDefinitionServiceImpl, "repositoryService", repositoryService);
        deploymentId = repositoryService.createDeployment()
                .addClasspathResource("bpmn/simple-approve.bpmn20.xml")
                .deploy().getId();
    }

    @AfterEach
    public void tearDown() {
        repositoryService.deleteDeployment(deploymentId, true);
    }

    @Test
    public void testGetProcessDefinition_success() {
        // 准备参数
        String id = repositoryService.createProcessDefinitionQuery().deploymentId(deploymentId).singleResult().getId();

        // 调用
        ProcessDefinition processDefinition = processDefinitionServiceImpl.getProcessDefinition(id);
        // 断言
        assertEquals("simpleApprove", processDefinition.getKey());
    }

    @Test
    public void testGetProcessDefinition_notExists() {
        // 调用，并断言：不存在时返回 null，而不是抛出 Flowable 异常，调用方据此提示“流程定义不存在”
        assertNull(processDefinitionServiceImpl.getProcessDefinition("not-exists"));
        assertNull(processDefinitionServiceImpl.getProcessDefinition(null));
    }

}
