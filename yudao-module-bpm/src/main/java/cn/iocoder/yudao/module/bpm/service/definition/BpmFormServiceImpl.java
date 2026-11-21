package cn.iocoder.yudao.module.bpm.service.definition;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.form.BpmFormPageReqVO;
import cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.form.BpmFormSaveReqVO;
import cn.iocoder.yudao.module.bpm.dal.dataobject.definition.BpmFormDO;
import cn.iocoder.yudao.module.bpm.dal.mysql.definition.BpmFormMapper;
import cn.iocoder.yudao.module.bpm.enums.ErrorCodeConstants;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.util.FlowableUtils;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.*;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;

/**
 * 动态表单 Service 实现类
 *
 * @author 风里雾里
 */
@Service
@Validated
public class BpmFormServiceImpl implements BpmFormService {

    @Resource
    private BpmFormMapper formMapper;

    @Override
    public Long createForm(BpmFormSaveReqVO createReqVO) {
        this.validateFields(createReqVO.getFields());
        // 插入
        BpmFormDO form = BeanUtils.toBean(createReqVO, BpmFormDO.class);
        formMapper.insert(form);
        // 返回
        return form.getId();
    }

    @Override
    public void updateForm(BpmFormSaveReqVO updateReqVO) {
        validateFields(updateReqVO.getFields());
        // 校验存在
        validateFormExists(updateReqVO.getId());
        // 更新
        BpmFormDO updateObj = BeanUtils.toBean(updateReqVO, BpmFormDO.class);
        formMapper.updateById(updateObj);
    }

    @Override
    public void deleteForm(Long id) {
        // 校验存在
        this.validateFormExists(id);
        // 删除
        formMapper.deleteById(id);
    }

    private void validateFormExists(Long id) {
        if (formMapper.selectById(id) == null) {
            throw exception(ErrorCodeConstants.FORM_NOT_EXISTS);
        }
    }

    @Override
    public BpmFormDO getForm(Long id) {
        return formMapper.selectById(id);
    }

    @Override
    public List<BpmFormDO> getFormList() {
        return formMapper.selectList();
    }

    @Override
    public List<BpmFormDO> getFormList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return formMapper.selectByIds(ids);
    }

    @Override
    public PageResult<BpmFormDO> getFormPage(BpmFormPageReqVO pageReqVO) {
        return formMapper.selectPage(pageReqVO);
    }

    /**
     * 校验 Field，避免 field 重复
     * <p>
     * 表单项是 form-create 的组件规则（JSON）：字段名为 field，标题为 title，布局组件的字段在 children 中，
     * 解析方式与审批时一致，见 {@link FlowableUtils#parseFormFieldRules(JsonNode, java.util.function.BiConsumer)}；
     * 另外，联动规则新增的字段也参与校验，见 {@link #validateFieldRules(JsonNode, Map)}。
     * 子表单（表格表单、子表单）的子字段属于子表单的值，与顶层字段的作用域不同，所以不参与校验
     *
     * @param fields field 数组
     */
    private void validateFields(List<String> fields) {
        if (CollUtil.isEmpty(fields)) {
            return;
        }
        // key 是 field，value 是组件规则。已经存在时，说明 field 重复
        Map<String, JsonNode> fieldRules = new HashMap<>();
        for (String field : fields) {
            JsonNode rule = JsonUtils.parseTree(field);
            Assert.notNull(rule);
            validateFieldRules(rule, fieldRules);
        }
    }

    /**
     * 校验组件规则中的字段，与已有的字段不重复
     * <p>
     * 联动规则 control[].rule 中的组件，由 form-create 插入到当前组件之后（默认）、指定组件的前后（prepend、append）或 children 中（child），
     * 按与当前组件属于同一个作用域校验；第一个元素为字符串时，整组为已有组件的引用（只控制显示、禁用等），不需要校验
     *
     * @param rule       组件规则
     * @param fieldRules 已有的字段规则，key 是 field
     */
    private static void validateFieldRules(JsonNode rule, Map<String, JsonNode> fieldRules) {
        FlowableUtils.parseFormFieldRules(rule, (fieldName, fieldRule) -> {
            JsonNode oldRule = fieldRules.putIfAbsent(fieldName, fieldRule);
            if (oldRule != null) {
                throw exception(ErrorCodeConstants.FORM_FIELD_REPEAT, JsonUtils.getText(oldRule, "title"),
                        JsonUtils.getText(fieldRule, "title"), fieldName);
            }
            // 联动规则：只有字段组件，才能按值联动
            JsonNode controls = fieldRule.get("control");
            if (controls == null || !controls.isArray()) {
                return;
            }
            controls.forEach(control -> {
                JsonNode controlRules = control.get("rule");
                if (controlRules != null && controlRules.isArray() && !controlRules.path(0).isTextual()) {
                    controlRules.forEach(controlRule -> validateFieldRules(controlRule, fieldRules));
                }
            });
        });
    }

}
