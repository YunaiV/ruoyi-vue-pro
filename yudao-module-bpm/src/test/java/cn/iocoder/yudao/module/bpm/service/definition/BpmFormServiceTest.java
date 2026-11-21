package cn.iocoder.yudao.module.bpm.service.definition;

import cn.hutool.core.util.RandomUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.form.BpmFormSaveReqVO;
import cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.form.BpmFormPageReqVO;
import cn.iocoder.yudao.module.bpm.dal.dataobject.definition.BpmFormDO;
import cn.iocoder.yudao.module.bpm.dal.mysql.definition.BpmFormMapper;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static cn.iocoder.yudao.framework.common.util.object.ObjectUtils.cloneIgnoreId;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertPojoEquals;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomLongId;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static cn.iocoder.yudao.module.bpm.enums.ErrorCodeConstants.FORM_FIELD_REPEAT;
import static cn.iocoder.yudao.module.bpm.enums.ErrorCodeConstants.FORM_NOT_EXISTS;
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
        BpmFormSaveReqVO reqVO = randomPojo(BpmFormSaveReqVO.class, o -> {
            o.setConf("{}");
            o.setFields(randomFields());
        });

        // 调用
        Long formId = formService.createForm(reqVO);
        // 断言
        assertNotNull(formId);
        // 校验记录的属性是否正确
        BpmFormDO form = formMapper.selectById(formId);
        assertPojoEquals(reqVO, form);
    }

    @Test
    public void testCreateForm_fieldRepeat() {
        // 调用，并断言异常：顶层字段重复
        assertServiceException(() -> formService.createForm(buildFormSaveReqVO(
                        "{\"type\":\"input\",\"field\":\"amount\",\"title\":\"金额\"}",
                        "{\"type\":\"inputNumber\",\"field\":\"amount\",\"title\":\"数量\"}")),
                FORM_FIELD_REPEAT, "金额", "数量", "amount");
        // 调用，并断言异常：布局组件 children 中的字段，与顶层字段重复
        assertServiceException(() -> formService.createForm(buildFormSaveReqVO(
                        "{\"type\":\"input\",\"field\":\"amount\",\"title\":\"金额\"}",
                        "{\"type\":\"fcRow\",\"children\":[{\"type\":\"col\",\"children\":"
                                + "[{\"type\":\"input\",\"field\":\"amount\",\"title\":\"行内金额\"}]}]}")),
                FORM_FIELD_REPEAT, "金额", "行内金额", "amount");
        // 调用，并断言异常：同一个布局组件 children 中的字段重复
        assertServiceException(() -> formService.createForm(buildFormSaveReqVO(
                        "{\"type\":\"fcRow\",\"children\":[{\"type\":\"input\",\"field\":\"remark\",\"title\":\"备注 1\"},"
                                + "{\"type\":\"input\",\"field\":\"remark\",\"title\":\"备注 2\"}]}")),
                FORM_FIELD_REPEAT, "备注 1", "备注 2", "remark");
        assertEquals(0L, formMapper.selectCount());
    }

    @Test
    public void testCreateForm_fieldScope() {
        // 准备参数：布局组件没有 field
        BpmFormSaveReqVO layoutReqVO = buildFormSaveReqVO("{\"field\":\"a\"}",
                "{\"type\":\"fcRow\",\"children\":[{\"field\":\"b\"}]}");
        // 准备参数：表格表单、子表单的子字段与顶层字段同名，属于不同的作用域
        BpmFormSaveReqVO subFormReqVO = buildFormSaveReqVO("{\"field\":\"a\"}",
                "{\"type\":\"tableForm\",\"field\":\"t\",\"props\":{\"columns\":[{\"rule\":[{\"field\":\"a\"}]}]}}",
                "{\"type\":\"group\",\"field\":\"g\",\"props\":{\"rule\":[{\"field\":\"a\"}]}}");

        // 调用
        Long layoutFormId = formService.createForm(layoutReqVO);
        Long subFormId = formService.createForm(subFormReqVO);
        // 断言
        assertPojoEquals(layoutReqVO, formMapper.selectById(layoutFormId));
        assertPojoEquals(subFormReqVO, formMapper.selectById(subFormId));
    }

    @Test
    public void testCreateForm_controlFieldRepeat() {
        // 调用，并断言异常：联动规则新增的字段，与顶层字段重复
        assertServiceException(() -> formService.createForm(buildFormSaveReqVO(
                        "{\"type\":\"input\",\"field\":\"amount\",\"title\":\"金额\"}",
                        "{\"type\":\"switch\",\"field\":\"enable\",\"title\":\"启用\",\"control\":[{\"value\":true,\"rule\":"
                                + "[{\"type\":\"input\",\"field\":\"amount\",\"title\":\"联动金额\"}]}]}")),
                FORM_FIELD_REPEAT, "金额", "联动金额", "amount");
        // 调用，并断言异常：联动规则新增的布局组件（插入到 children 中），其中的字段与顶层字段重复
        assertServiceException(() -> formService.createForm(buildFormSaveReqVO(
                        "{\"type\":\"input\",\"field\":\"amount\",\"title\":\"金额\"}",
                        "{\"type\":\"switch\",\"field\":\"enable\",\"title\":\"启用\",\"control\":[{\"value\":true,\"child\":true,\"rule\":"
                                + "[{\"type\":\"fcRow\",\"children\":[{\"type\":\"input\",\"field\":\"amount\",\"title\":\"行内金额\"}]}]}]}")),
                FORM_FIELD_REPEAT, "金额", "行内金额", "amount");
        assertEquals(0L, formMapper.selectCount());
    }

    @Test
    public void testCreateForm_controlFieldScope() {
        // 准备参数：联动规则为字符串时，是已有字段的引用
        BpmFormSaveReqVO refReqVO = buildFormSaveReqVO("{\"field\":\"a\"}",
                "{\"field\":\"s\",\"control\":[{\"value\":1,\"rule\":[\"a\"]}]}");
        // 准备参数：联动规则新增的字段，与表格表单的子字段同名，属于不同的作用域
        BpmFormSaveReqVO subFormReqVO = buildFormSaveReqVO(
                "{\"field\":\"s\",\"control\":[{\"value\":1,\"rule\":[{\"field\":\"b\"}]}]}",
                "{\"type\":\"tableForm\",\"field\":\"t\",\"props\":{\"columns\":[{\"rule\":[{\"field\":\"b\"}]}]}}");

        // 调用
        Long refFormId = formService.createForm(refReqVO);
        Long subFormId = formService.createForm(subFormReqVO);
        // 断言
        assertPojoEquals(refReqVO, formMapper.selectById(refFormId));
        assertPojoEquals(subFormReqVO, formMapper.selectById(subFormId));
    }

    @Test
    public void testUpdateForm_success() {
        // mock 数据
        BpmFormDO dbForm = randomPojo(BpmFormDO.class, o -> {
            o.setConf("{}");
            o.setFields(randomFields());
        });
        formMapper.insert(dbForm);// @Sql: 先插入出一条存在的数据
        // 准备参数
        BpmFormSaveReqVO reqVO = randomPojo(BpmFormSaveReqVO.class, o -> {
            o.setId(dbForm.getId()); // 设置更新的 ID
            o.setConf("{'yudao': 'yuanma'}");
            o.setFields(randomFields());
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
        BpmFormSaveReqVO reqVO = randomPojo(BpmFormSaveReqVO.class, o -> {
            o.setConf("{'yudao': 'yuanma'}");
            o.setFields(randomFields());
        });

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

    /**
     * 随机 form-create 的表单项，field 不重复
     */
    private List<String> randomFields() {
        int size = RandomUtil.randomInt(1, 3);
        return Stream.iterate(0, i -> i + 1).limit(size)
                .map(i -> JsonUtils.toJsonString(Map.of("type", "input", "field", "field" + i,
                        "title", RandomUtil.randomString(4))))
                .collect(Collectors.toList());
    }

    private static BpmFormSaveReqVO buildFormSaveReqVO(String... fields) {
        BpmFormSaveReqVO reqVO = randomPojo(BpmFormSaveReqVO.class, o -> o.setConf("{}"));
        reqVO.setFields(List.of(fields));
        return reqVO;
    }

}
