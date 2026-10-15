package cn.iocoder.yudao.module.bpm.service.definition;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.framework.test.core.util.AssertUtils;
import cn.iocoder.yudao.framework.test.core.util.RandomUtils;
import cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.expression.BpmProcessExpressionPageReqVO;
import cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.expression.BpmProcessExpressionSaveReqVO;
import cn.iocoder.yudao.module.bpm.dal.dataobject.definition.BpmProcessExpressionDO;
import cn.iocoder.yudao.module.bpm.dal.mysql.definition.BpmProcessExpressionMapper;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import static cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils.buildBetweenTime;
import static cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils.buildTime;
import static cn.iocoder.yudao.framework.common.util.object.ObjectUtils.cloneIgnoreId;
import static cn.iocoder.yudao.module.bpm.enums.ErrorCodeConstants.PROCESS_EXPRESSION_NOT_EXISTS;

/**
 * {@link BpmProcessExpressionServiceImpl} 的单元测试类
 *
 * @author HUIHUI
 */
@Import(BpmProcessExpressionServiceImpl.class)
public class BpmProcessExpressionServiceImplTest extends BaseDbUnitTest {

    @Resource
    private BpmProcessExpressionServiceImpl processExpressionService;
    @Resource
    private BpmProcessExpressionMapper processExpressionMapper;

    @Test
    public void testCreateProcessExpression_success() {
        // 准备参数
        BpmProcessExpressionSaveReqVO reqVO = RandomUtils.randomPojo(BpmProcessExpressionSaveReqVO.class)
                .setId(null);

        // 调用
        Long expressionId = processExpressionService.createProcessExpression(reqVO);
        // 断言
        Assertions.assertNotNull(expressionId);
        AssertUtils.assertPojoEquals(reqVO, processExpressionMapper.selectById(expressionId), "id");
    }

    @Test
    public void testUpdateProcessExpression_success() {
        // 准备参数
        BpmProcessExpressionDO dbExpression = RandomUtils.randomPojo(BpmProcessExpressionDO.class);
        processExpressionMapper.insert(dbExpression);
        BpmProcessExpressionSaveReqVO reqVO = RandomUtils.randomPojo(BpmProcessExpressionSaveReqVO.class)
                .setId(dbExpression.getId());

        // 调用
        processExpressionService.updateProcessExpression(reqVO);
        // 断言
        AssertUtils.assertPojoEquals(reqVO, processExpressionMapper.selectById(dbExpression.getId()));
    }

    @Test
    public void testUpdateProcessExpression_notExists() {
        // 准备参数
        BpmProcessExpressionSaveReqVO reqVO = RandomUtils.randomPojo(BpmProcessExpressionSaveReqVO.class);

        // 调用，并断言异常
        AssertUtils.assertServiceException(() -> processExpressionService.updateProcessExpression(reqVO),
                PROCESS_EXPRESSION_NOT_EXISTS);
    }

    @Test
    public void testDeleteProcessExpression_success() {
        // 准备参数
        BpmProcessExpressionDO dbExpression = RandomUtils.randomPojo(BpmProcessExpressionDO.class);
        processExpressionMapper.insert(dbExpression);

        // 调用
        processExpressionService.deleteProcessExpression(dbExpression.getId());
        // 断言
        Assertions.assertNull(processExpressionMapper.selectById(dbExpression.getId()));
    }

    @Test
    public void testDeleteProcessExpression_notExists() {
        // 准备参数
        Long id = RandomUtils.randomLongId();

        // 调用，并断言异常
        AssertUtils.assertServiceException(() -> processExpressionService.deleteProcessExpression(id),
                PROCESS_EXPRESSION_NOT_EXISTS);
    }

    @Test
    public void testGetProcessExpressionPage() {
        // 准备参数
        BpmProcessExpressionDO dbExpression = RandomUtils.randomPojo(BpmProcessExpressionDO.class, o -> {
            o.setName("请假审批");
            o.setStatus(CommonStatusEnum.ENABLE.getStatus());
            o.setCreateTime(buildTime(2023, 2, 2));
        });
        processExpressionMapper.insert(dbExpression);
        processExpressionMapper.insert(cloneIgnoreId(dbExpression, o -> o.setName("报销审批")));
        processExpressionMapper.insert(cloneIgnoreId(dbExpression, o -> o.setStatus(CommonStatusEnum.DISABLE.getStatus())));
        processExpressionMapper.insert(cloneIgnoreId(dbExpression, o -> o.setCreateTime(buildTime(2024, 2, 2))));
        BpmProcessExpressionPageReqVO reqVO = new BpmProcessExpressionPageReqVO();
        reqVO.setName("请假");
        reqVO.setStatus(CommonStatusEnum.ENABLE.getStatus());
        reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));

        // 调用
        PageResult<BpmProcessExpressionDO> pageResult = processExpressionService.getProcessExpressionPage(reqVO);
        // 断言
        Assertions.assertEquals(1, pageResult.getTotal());
        AssertUtils.assertPojoEquals(dbExpression, pageResult.getList().get(0));
    }

}
