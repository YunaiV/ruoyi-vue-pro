package cn.iocoder.yudao.module.bpm.service.definition;

import cn.hutool.core.util.RandomUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.form.BpmFormSaveReqVO;
import cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.form.BpmFormPageReqVO;
import cn.iocoder.yudao.module.bpm.dal.dataobject.definition.BpmFormDO;
import cn.iocoder.yudao.module.bpm.dal.mysql.definition.BpmFormMapper;
import cn.iocoder.yudao.module.bpm.service.definition.dto.BpmFormFieldRespDTO;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static cn.iocoder.yudao.framework.common.util.object.ObjectUtils.cloneIgnoreId;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertPojoEquals;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomLongId;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
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

    private List<String> randomFields() {
        int size = RandomUtil.randomInt(1, 3);
        return Stream.iterate(0, i -> i).limit(size)
                .map(i -> JsonUtils.toJsonString(randomPojo(BpmFormFieldRespDTO.class)))
                .collect(Collectors.toList());
    }

}
