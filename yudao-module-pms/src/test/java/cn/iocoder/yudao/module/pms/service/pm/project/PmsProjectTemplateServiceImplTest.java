package cn.iocoder.yudao.module.pms.service.pm.project;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.pms.controller.admin.pm.project.vo.template.PmsProjectTemplateSaveReqVO;
import cn.iocoder.yudao.module.pms.dal.dataobject.pm.project.PmsProjectTemplateDO;
import cn.iocoder.yudao.module.pms.dal.mysql.pm.project.PmsProjectTemplateMapper;
import cn.iocoder.yudao.module.pms.enums.pm.project.PmsProjectTypeEnum;
import cn.iocoder.yudao.module.pms.enums.pm.workitem.PmsWorkItemStatusTypeEnum;
import cn.iocoder.yudao.module.pms.enums.pm.workitem.PmsWorkItemTypeEnum;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import static cn.iocoder.yudao.framework.common.enums.CommonStatusEnum.ENABLE;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.module.pms.enums.ErrorCodeConstants.PROJECT_TEMPLATE_CONFIG_INVALID;
import static cn.iocoder.yudao.module.pms.enums.ErrorCodeConstants.PROJECT_TEMPLATE_NAME_DUPLICATE;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * {@link PmsProjectTemplateServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
@Import(PmsProjectTemplateServiceImpl.class)
public class PmsProjectTemplateServiceImplTest extends BaseDbUnitTest {

    @Resource
    private PmsProjectTemplateServiceImpl projectTemplateService;

    @Resource
    private PmsProjectTemplateMapper projectTemplateMapper;

    @Test
    public void testCreateProjectTemplate_success() {
        // 准备参数
        PmsProjectTemplateSaveReqVO reqVO = buildProjectTemplateSaveReqVO("通用项目模板-创建");

        // 调用
        Long templateId = projectTemplateService.createProjectTemplate(reqVO);

        // 断言
        PmsProjectTemplateDO template = projectTemplateMapper.selectById(templateId);
        assertNotNull(template);
        assertEquals("通用项目模板-创建", template.getName());
        assertEquals(singletonList(PmsWorkItemTypeEnum.TASK.getType()), template.getItemTypes());
        assertEquals("task_todo", template.getStatuses().get(0).getCode());
        assertEquals("todo", template.getBoards().get(0).getCode());
    }

    @Test
    public void testCreateProjectTemplate_defaultStatusMissing() {
        // 准备参数
        PmsProjectTemplateSaveReqVO reqVO = buildProjectTemplateSaveReqVO("通用项目模板-缺少初始状态");
        reqVO.getStatuses().get(0).setDefaultStatus(false);

        // 调用，并断言异常
        assertServiceException(() -> projectTemplateService.createProjectTemplate(reqVO),
                PROJECT_TEMPLATE_CONFIG_INVALID, "每种工作项必须且只能配置一个初始状态");
    }

    @Test
    public void testCreateProjectTemplate_nameDuplicate() {
        // mock 数据
        projectTemplateService.createProjectTemplate(buildProjectTemplateSaveReqVO("通用项目模板-重名"));

        // 准备参数
        PmsProjectTemplateSaveReqVO reqVO = buildProjectTemplateSaveReqVO("通用项目模板-重名");

        // 调用，并断言异常
        assertServiceException(() -> projectTemplateService.createProjectTemplate(reqVO),
                PROJECT_TEMPLATE_NAME_DUPLICATE);
    }

    @Test
    public void testUpdateProjectTemplate_success() {
        // mock 数据
        Long templateId = projectTemplateService.createProjectTemplate(
                buildProjectTemplateSaveReqVO("通用项目模板-修改前"));

        // 准备参数
        PmsProjectTemplateSaveReqVO reqVO = buildProjectTemplateSaveReqVO("通用项目模板-修改后")
                .setId(templateId).setDescription("更新后的通用协作模板");

        // 调用
        projectTemplateService.updateProjectTemplate(reqVO);

        // 断言
        PmsProjectTemplateDO template = projectTemplateMapper.selectById(templateId);
        assertNotNull(template);
        assertEquals("通用项目模板-修改后", template.getName());
        assertEquals("更新后的通用协作模板", template.getDescription());
    }

    @Test
    public void testDeleteProjectTemplate_success() {
        // mock 数据
        Long templateId = projectTemplateService.createProjectTemplate(
                buildProjectTemplateSaveReqVO("通用项目模板-删除"));

        // 调用
        projectTemplateService.deleteProjectTemplate(templateId);

        // 断言
        assertNull(projectTemplateMapper.selectById(templateId));
    }

    /**
     * 构建有效的通用项目模板保存参数
     *
     * @return 项目模板保存参数
     */
    private PmsProjectTemplateSaveReqVO buildProjectTemplateSaveReqVO(String name) {
        PmsProjectTemplateSaveReqVO.StatusTemplate todoStatus = new PmsProjectTemplateSaveReqVO.StatusTemplate()
                .setCode("task_todo").setName("待处理").setWorkItemType(PmsWorkItemTypeEnum.TASK.getType())
                .setStatusType(PmsWorkItemStatusTypeEnum.PENDING.getType()).setDefaultStatus(true)
                .setSort(10).setBoardCode("todo");
        PmsProjectTemplateSaveReqVO.StatusTemplate doneStatus = new PmsProjectTemplateSaveReqVO.StatusTemplate()
                .setCode("task_done").setName("已完成").setWorkItemType(PmsWorkItemTypeEnum.TASK.getType())
                .setStatusType(PmsWorkItemStatusTypeEnum.COMPLETED.getType()).setDefaultStatus(false)
                .setSort(20).setBoardCode("done");
        PmsProjectTemplateSaveReqVO.BoardTemplate todoBoard = new PmsProjectTemplateSaveReqVO.BoardTemplate()
                .setCode("todo").setName("待处理").setWorkItemType(PmsWorkItemTypeEnum.TASK.getType())
                .setSort(10).setStatusCodes(singletonList("task_todo"));
        PmsProjectTemplateSaveReqVO.BoardTemplate doneBoard = new PmsProjectTemplateSaveReqVO.BoardTemplate()
                .setCode("done").setName("已完成").setWorkItemType(PmsWorkItemTypeEnum.TASK.getType())
                .setSort(20).setStatusCodes(singletonList("task_done"));
        return new PmsProjectTemplateSaveReqVO().setName(name).setDescription("通用协作")
                .setProjectType(PmsProjectTypeEnum.GENERAL.getType()).setStatus(ENABLE.getStatus()).setSort(10)
                .setItemTypes(singletonList(PmsWorkItemTypeEnum.TASK.getType()))
                .setStatuses(asList(todoStatus, doneStatus)).setBoards(asList(todoBoard, doneBoard));
    }

}
