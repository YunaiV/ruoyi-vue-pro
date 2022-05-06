package cn.iocoder.yudao.module.bpm;

import org.flowable.engine.*;
import org.flowable.engine.impl.ProcessEngineImpl;
import org.flowable.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.flowable.engine.impl.test.TestHelper;
import org.flowable.engine.test.FlowableRule;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;

/**
 * 抽象测试基类
 *
 * @author henryyan cuicui
 */
public abstract class AbstractOATest {

    protected String configurationResource = "flowable.cfg.xml";

    /**
     * 专门用于测试套件
     */
    @Rule
    public FlowableRule activitiRule = new FlowableRule("flowable.cfg.xml");

    protected ProcessEngineConfigurationImpl processEngineConfiguration;

    protected ProcessEngine processEngine;
    protected RepositoryService repositoryService;
    protected RuntimeService runtimeService;
    protected TaskService taskService;
    protected HistoryService historyService;
    protected IdentityService identityService;
    protected ManagementService managementService;
    protected FormService formService;

    /**
     * 开始测试
     */
    @BeforeClass
    public static void setUpForClass() throws Exception {
        System.out.println("++++++++ 开始测试 ++++++++");
    }

    /**
     * 结束测试
     */
    @AfterClass
    public static void testOverForClass() throws Exception {
        System.out.println("-------- 结束测试 --------");
    }

    protected void initializeProcessEngine() {
        processEngine = TestHelper.getProcessEngine(configurationResource);
    }

    /**
     * 初始化变量
     */
    @Before
    public void setUp() throws Exception {
        if (processEngine == null) {
            initializeProcessEngine();
        }

        processEngineConfiguration = ((ProcessEngineImpl) processEngine).getProcessEngineConfiguration();
        repositoryService = processEngine.getRepositoryService();
        runtimeService = processEngine.getRuntimeService();
        taskService = processEngine.getTaskService();
        historyService = processEngine.getHistoryService();
        identityService = processEngine.getIdentityService();
        managementService = processEngine.getManagementService();
        formService = processEngine.getFormService();
    }

}
