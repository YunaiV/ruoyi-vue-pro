import type { Rule } from '@form-create/ant-design-vue';

import type { Ref } from 'vue';

import type { Menu } from '#/components/form-create/typing';

import { isRef, nextTick, onMounted } from 'vue';

import formCreate from '@form-create/ant-design-vue';

import { apiSelectRule } from '#/components/form-create/rules/data';

import {
  useDictSelectRule,
  useEditorRule,
  useSelectRule,
  useUploadFileRule,
  useUploadImageRule,
  useUploadImagesRule,
} from './rules';

/** 编码表单 Conf */
export function encodeConf(designerRef: any) {
  // 关联案例：https://gitee.com/yudaocode/yudao-ui-admin-vue3/pulls/834/
  return formCreate.toJson(designerRef.value.getOption());
}

/** 解码表单 Conf */
export function decodeConf(conf: string) {
  return formCreate.parseJson(conf);
}

/** 编码表单 Fields */
export function encodeFields(designerRef: any) {
  const rule = designerRef.value.getRule();
  const fields: string[] = [];
  rule.forEach((item: any) => {
    fields.push(formCreate.toJson(item));
  });
  return fields;
}

/** 解码表单 Fields */
export function decodeFields(fields: string[]) {
  const rule: Rule[] = [];
  fields.forEach((item) => {
    rule.push(formCreate.parseJson(item));
  });
  return rule;
}

/** 设置表单的 Conf 和 Fields，适用 FcDesigner 场景 */
export function setConfAndFields(
  designerRef: any,
  conf: string,
  fields: string | string[],
) {
  designerRef.value.setOption(decodeConf(conf));
  // 处理 fields 参数类型，确保传入 decodeFields 的是 string[] 类型
  const fieldsArray = Array.isArray(fields) ? fields : [fields];
  designerRef.value.setRule(decodeFields(fieldsArray));
}

/** 设置表单的 Conf 和 Fields，适用 form-create 场景 */
export function setConfAndFields2(
  detailPreview: any,
  conf: string,
  fields: string[],
  value?: any,
) {
  if (isRef(detailPreview)) {
    detailPreview = detailPreview.value;
  }
  detailPreview.option = decodeConf(conf);
  detailPreview.rule = decodeFields(fields);
  if (value) {
    detailPreview.value = value;
  }
}

export function makeRequiredRule() {
  return {
    type: 'Required',
    field: 'formCreate$required',
    title: '是否必填',
  };
}

export function localeProps(
  t: (msg: string) => any,
  prefix: string,
  rules: any[],
) {
  return rules.map((rule: { field: string; title: any }) => {
    if (rule.field === 'formCreate$required') {
      rule.title = t('props.required') || rule.title;
    } else if (rule.field && rule.field !== '_optionType') {
      rule.title = t(`components.${prefix}.${rule.field}`) || rule.title;
    }
    return rule;
  });
}

/**
 * 解析表单组件的  field, title 等字段（递归，如果组件包含子组件）
 *
 * @param rule  组件的生成规则 https://www.form-create.com/v3/guide/rule
 * @param fields 解析后表单组件字段
 * @param parentTitle  如果是子表单，子表单的标题，默认为空
 */
export function parseFormFields(
  rule: Record<string, any>,
  fields: Array<Record<string, any>> = [],
  parentTitle: string = '',
) {
  const { type, field, $required, title: tempTitle, children } = rule;
  if (field && tempTitle) {
    let title = tempTitle;
    if (parentTitle) {
      title = `${parentTitle}.${tempTitle}`;
    }
    let required = false;
    if ($required) {
      required = true;
    }
    fields.push({
      field,
      title,
      type,
      required,
    });
    // TODO 子表单 需要处理子表单字段
    // if (type === 'group' && rule.props?.rule && Array.isArray(rule.props.rule)) {
    //   // 解析子表单的字段
    //   rule.props.rule.forEach((item) => {
    //     parseFields(item, fieldsPermission, title)
    //   })
    // }
  }
  if (children && Array.isArray(children)) {
    children.forEach((rule) => {
      parseFormFields(rule, fields);
    });
  }
}

/**
 * 表单设计器增强 hook
 * 新增
 * - 文件上传
 * - 单图上传
 * - 多图上传
 * - 字典选择器
 * - 用户选择器
 * - 部门选择器
 * - 富文本
 */
export async function useFormCreateDesigner(designer: Ref) {
  const editorRule = useEditorRule();
  const uploadFileRule = useUploadFileRule();
  const uploadImageRule = useUploadImageRule();
  const uploadImagesRule = useUploadImagesRule();

  /** 构建表单组件 */
  function buildFormComponents() {
    // 移除自带的上传组件规则，使用 uploadFileRule、uploadImgRule、uploadImgsRule 替代
    designer.value?.removeMenuItem('upload');
    // 移除自带的富文本组件规则，使用 editorRule 替代
    designer.value?.removeMenuItem('fc-editor');
    const components = [
      editorRule,
      uploadFileRule,
      uploadImageRule,
      uploadImagesRule,
    ];
    components.forEach((component) => {
      // 插入组件规则
      designer.value?.addComponent(component);
      // 插入拖拽按钮到 `main` 分类下
      designer.value?.appendMenuItem('main', {
        icon: component.icon,
        name: component.name,
        label: component.label,
      });
    });
  }

  const userSelectRule = useSelectRule({
    name: 'UserSelect',
    label: '用户选择器',
    icon: 'icon-eye',
  });
  const deptSelectRule = useSelectRule({
    name: 'DeptSelect',
    label: '部门选择器',
    icon: 'icon-tree',
    props: [
      {
        type: 'select',
        field: 'returnType',
        title: '返回值类型',
        value: 'id',
        options: [
          { label: '部门编号', value: 'id' },
          { label: '部门名称', value: 'name' },
        ],
      },
    ],
  });
  const dictSelectRule = useDictSelectRule();
  const apiSelectRule0 = useSelectRule({
    name: 'ApiSelect',
    label: '接口选择器',
    icon: 'icon-json',
    props: [...apiSelectRule],
    event: ['click', 'change', 'visibleChange', 'clear', 'blur', 'focus'],
  });

  /** 构建系统字段菜单 */
  function buildSystemMenu() {
    // 移除自带的下拉选择器组件，使用 currencySelectRule 替代
    // designer.value?.removeMenuItem('select')
    // designer.value?.removeMenuItem('radio')
    // designer.value?.removeMenuItem('checkbox')
    const components = [
      userSelectRule,
      deptSelectRule,
      dictSelectRule,
      apiSelectRule0,
    ];
    const menu: Menu = {
      name: 'system',
      title: '系统字段',
      list: components.map((component) => {
        // 插入组件规则
        designer.value?.addComponent(component);
        // 插入拖拽按钮到 `system` 分类下
        return {
          icon: component.icon,
          name: component.name,
          label: component.label,
        };
      }),
    };
    designer.value?.addMenu(menu);
  }

  onMounted(async () => {
    await nextTick();
    buildFormComponents();
    buildSystemMenu();
  });
}
