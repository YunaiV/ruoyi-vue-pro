package cn.iocoder.yudao.adminserver.modules.bpm.service.definition;

import cn.iocoder.yudao.adminserver.BaseDbUnitTest;
import cn.iocoder.yudao.adminserver.modules.bpm.controller.definition.vo.form.BpmFormCreateReqVO;
import cn.iocoder.yudao.adminserver.modules.bpm.controller.definition.vo.form.BpmFormPageReqVO;
import cn.iocoder.yudao.adminserver.modules.bpm.controller.definition.vo.form.BpmFormUpdateReqVO;
import cn.iocoder.yudao.adminserver.modules.bpm.dal.dataobject.definition.BpmFormDO;
import cn.iocoder.yudao.adminserver.modules.bpm.dal.mysql.definition.BpmFormMapper;
import cn.iocoder.yudao.adminserver.modules.bpm.service.definition.impl.BpmFormServiceImpl;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;

import static cn.iocoder.yudao.adminserver.modules.bpm.enums.BpmErrorCodeConstants.FORM_NOT_EXISTS;
import static cn.iocoder.yudao.framework.common.util.object.ObjectUtils.cloneIgnoreId;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertPojoEquals;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomLongId;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link BpmFormServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
@Import(BpmFormServiceImpl.class)
public class BpmFormServiceTest extends BaseDbUnitTest {

    @Resource
    private BpmFormServiceImpl formService;

    @Resource
    private BpmFormMapper formMapper;

    @Test
    public void testCreateForm_success() {
        // 准备参数
        BpmFormCreateReqVO reqVO = randomPojo(BpmFormCreateReqVO.class);

        // 调用
        Long formId = formService.createForm(reqVO);
        // 断言
        assertNotNull(formId);
        // 校验记录的属性是否正确
        BpmFormDO form = formMapper.selectById(formId);
        assertPojoEquals(reqVO, form);
    }

    @Test
    public void testUpdateForm_success() {
        // mock 数据
        BpmFormDO dbForm = randomPojo(BpmFormDO.class);
        formMapper.insert(dbForm);// @Sql: 先插入出一条存在的数据
        // 准备参数
        BpmFormUpdateReqVO reqVO = randomPojo(BpmFormUpdateReqVO.class, o -> {
            o.setId(dbForm.getId()); // 设置更新的 ID
        });

        // 调用
        formService.updateForm(reqVO);
        // 校验是否更新正确
        BpmFormDO form = formMapper.selectById(reqVO.getId()); // 获取最新的
        assertPojoEquals(reqVO, form);
    }

    @Test
    public void testUpdateForm_notExists() {
        // 准备参数
        BpmFormUpdateReqVO reqVO = randomPojo(BpmFormUpdateReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> formService.updateForm(reqVO), FORM_NOT_EXISTS);
    }

    @Test
    public void testDeleteForm_success() {
        // mock 数据
        BpmFormDO dbForm = randomPojo(BpmFormDO.class);
        formMapper.insert(dbForm);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbForm.getId();

        // 调用
        formService.deleteForm(id);
        // 校验数据不存在了
        assertNull(formMapper.selectById(id));
    }

    @Test
    public void testDeleteForm_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用, 并断言异常
        assertServiceException(() -> formService.deleteForm(id), FORM_NOT_EXISTS);
    }

    @Test
    public void testGetFormPage() {
        // mock 数据
        BpmFormDO dbForm = randomPojo(BpmFormDO.class, o -> { // 等会查询到
            o.setName("芋道源码");
        });
        formMapper.insert(dbForm);
        // 测试 name 不匹配
        formMapper.insert(cloneIgnoreId(dbForm, o -> o.setName("源码")));
        // 准备参数
        BpmFormPageReqVO reqVO = new BpmFormPageReqVO();
        reqVO.setName("芋道");

        // 调用
        PageResult<BpmFormDO> pageResult = formService.getFormPage(reqVO);
        // 断言
        assertEquals(1, pageResult.getTotal());
        assertEquals(1, pageResult.getList().size());
        assertPojoEquals(dbForm, pageResult.getList().get(0));
    }

}
