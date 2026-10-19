package cn.iocoder.yudao.module.pms.service.pm.workitem;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.pms.controller.admin.pm.workitem.vo.label.PmsWorkItemLabelSaveReqVO;
import cn.iocoder.yudao.module.pms.dal.dataobject.pm.workitem.PmsWorkItemLabelDO;
import cn.iocoder.yudao.module.pms.dal.mysql.pm.workitem.PmsWorkItemLabelMapper;
import javax.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import java.util.Arrays;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomLongId;
import static cn.iocoder.yudao.module.pms.enums.ErrorCodeConstants.WORK_ITEM_LABELS_INVALID;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@link PmsWorkItemLabelServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
@Import(PmsWorkItemLabelServiceImpl.class)
public class PmsWorkItemLabelServiceImplTest extends BaseDbUnitTest {

    @Resource
    private PmsWorkItemLabelServiceImpl labelService;

    @Resource
    private PmsWorkItemLabelMapper labelMapper;

    @Test
    public void testCreateWorkItemLabel_success() {
        // 准备参数
        PmsWorkItemLabelSaveReqVO createReqVO = new PmsWorkItemLabelSaveReqVO()
                .setName("  紧急  ").setColor(" #F56C6C ");

        // 调用
        Long labelId = labelService.createWorkItemLabel(createReqVO);

        // 断言
        PmsWorkItemLabelDO label = labelMapper.selectById(labelId);
        assertEquals("紧急", label.getName());
        assertEquals("#F56C6C", label.getColor());
    }

    @Test
    public void testUpdateWorkItemLabel_success() {
        // mock 数据
        PmsWorkItemLabelDO label = new PmsWorkItemLabelDO().setName("紧急").setColor("#F56C6C");
        labelMapper.insert(label);
        // 准备参数
        PmsWorkItemLabelSaveReqVO reqVO = new PmsWorkItemLabelSaveReqVO().setId(label.getId())
                .setName("普通").setColor("#409EFF");

        // 调用
        labelService.updateWorkItemLabel(reqVO);

        // 断言
        assertEquals("普通", labelMapper.selectById(label.getId()).getName());
    }

    @Test
    public void testValidateLabelIds_notExists() {
        // 调用，并断言异常
        assertServiceException(() -> labelService.validateWorkItemLabelIds(
                Arrays.asList(randomLongId(), randomLongId())), WORK_ITEM_LABELS_INVALID);
    }

}
