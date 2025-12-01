import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';

import { AiModelTypeEnum, CommonStatusEnum, DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';

import { z } from '#/adapter/form';
import { getSimpleKnowledgeList } from '#/api/ai/knowledge/knowledge';
import { getModelSimpleList } from '#/api/ai/model/model';
import { getToolSimpleList } from '#/api/ai/model/tool';

/** 新增/修改的表单 */
export function useFormSchema(): VbenFormSchema[] {
  return [
    {
      component: 'Input',
      fieldName: 'id',
      dependencies: {
        triggerFields: [''],
        show: () => false,
      },
    },
    {
      component: 'Input',
      fieldName: 'formType',
      dependencies: {
        triggerFields: [''],
        show: () => false,
      },
    },
    {
      component: 'Input',
      fieldName: 'name',
      label: '角色名称',
      rules: 'required',
      componentProps: {
        placeholder: '请输入角色名称',
      },
    },
    {
      component: 'ImageUpload',
      fieldName: 'avatar',
      label: '角色头像',
      rules: 'required',
    },
    {
      fieldName: 'modelId',
      label: '绑定模型',
      component: 'ApiSelect',
      componentProps: {
        placeholder: '请选择绑定模型',
        api: () => getModelSimpleList(AiModelTypeEnum.CHAT),
        labelField: 'name',
        valueField: 'id',
        allowClear: true,
      },
      dependencies: {
        triggerFields: ['formType'],
        show: (values) => {
          return values.formType === 'create' || values.formType === 'update';
        },
      },
    },
    {
      component: 'Input',
      fieldName: 'category',
      label: '角色类别',
      rules: 'required',
      componentProps: {
        placeholder: '请输入角色类别',
      },
      dependencies: {
        triggerFields: ['formType'],
        show: (values) => {
          return values.formType === 'create' || values.formType === 'update';
        },
      },
    },
    {
      component: 'Textarea',
      fieldName: 'description',
      label: '角色描述',
      componentProps: {
        placeholder: '请输入角色描述',
      },
      rules: 'required',
    },
    {
      fieldName: 'systemMessage',
      label: '角色设定',
      component: 'Textarea',
      componentProps: {
        placeholder: '请输入角色设定',
      },
      rules: 'required',
    },
    {
      fieldName: 'knowledgeIds',
      label: '引用知识库',
      component: 'ApiSelect',
      componentProps: {
        placeholder: '请选择引用知识库',
        api: getSimpleKnowledgeList,
        labelField: 'name',
        mode: 'multiple',
        valueField: 'id',
        allowClear: true,
      },
    },
    {
      fieldName: 'toolIds',
      label: '引用工具',
      component: 'ApiSelect',
      componentProps: {
        placeholder: '请选择引用工具',
        api: getToolSimpleList,
        mode: 'multiple',
        labelField: 'name',
        valueField: 'id',
        allowClear: true,
      },
    },
    {
      fieldName: 'mcpClientNames',
      label: '引用 MCP',
      component: 'Select',
      componentProps: {
        placeholder: '请选择 MCP',
        options: getDictOptions(DICT_TYPE.AI_MCP_CLIENT_NAME, 'string'),
        mode: 'multiple',
        allowClear: true,
      },
    },
    {
      fieldName: 'publicStatus',
      label: '是否公开',
      component: 'RadioGroup',
      componentProps: {
        options: getDictOptions(DICT_TYPE.INFRA_BOOLEAN_STRING, 'boolean'),
      },
      defaultValue: true,
      dependencies: {
        triggerFields: ['formType'],
        show: (values) => {
          return values.formType === 'create' || values.formType === 'update';
        },
      },
      rules: 'required',
    },
    {
      fieldName: 'sort',
      label: '角色排序',
      component: 'InputNumber',
      componentProps: {
        placeholder: '请输入角色排序',
        controlsPosition: 'right',
        class: '!w-full',
      },
      dependencies: {
        triggerFields: ['formType'],
        show: (values) => {
          return values.formType === 'create' || values.formType === 'update';
        },
      },
      rules: 'required',
    },
    {
      fieldName: 'status',
      label: '开启状态',
      component: 'RadioGroup',
      componentProps: {
        options: getDictOptions(DICT_TYPE.COMMON_STATUS, 'number'),
      },
      dependencies: {
        triggerFields: ['formType'],
        show: (values) => {
          return values.formType === 'create' || values.formType === 'update';
        },
      },
      rules: z.number().default(CommonStatusEnum.ENABLE),
    },
  ];
}

/** 列表的搜索表单 */
export function useGridFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'name',
      label: '角色名称',
      component: 'Input',
      componentProps: {
        placeholder: '请输入角色名称',
      },
    },
    {
      fieldName: 'category',
      label: '角色类别',
      component: 'Input',
      componentProps: {
        placeholder: '请输入角色类别',
      },
    },
    {
      fieldName: 'publicStatus',
      label: '是否公开',
      component: 'Select',
      componentProps: {
        placeholder: '请选择是否公开',
        options: getDictOptions(DICT_TYPE.INFRA_BOOLEAN_STRING, 'boolean'),
        allowClear: true,
      },
      defaultValue: true,
    },
  ];
}

/** 列表的字段 */
export function useGridColumns(): VxeTableGridOptions['columns'] {
  return [
    {
      field: 'name',
      title: '角色名称',
      minWidth: 100,
    },
    {
      title: '绑定模型',
      field: 'modelName',
      minWidth: 100,
    },
    {
      title: '角色头像',
      field: 'avatar',
      minWidth: 140,
      cellRender: {
        name: 'CellImage',
        props: {
          width: 40,
          height: 40,
        },
      },
    },
    {
      title: '角色类别',
      field: 'category',
      minWidth: 100,
    },
    {
      title: '角色描述',
      field: 'description',
      minWidth: 100,
    },
    {
      title: '角色设定',
      field: 'systemMessage',
      minWidth: 100,
    },
    {
      title: '知识库',
      field: 'knowledgeIds',
      minWidth: 100,
      formatter: ({ cellValue }) => {
        return !cellValue || cellValue.length === 0
          ? '-'
          : `引用${cellValue.length}个`;
      },
    },
    {
      title: '工具',
      field: 'toolIds',
      minWidth: 100,
      formatter: ({ cellValue }) => {
        return !cellValue || cellValue.length === 0
          ? '-'
          : `引用${cellValue.length}个`;
      },
    },
    {
      title: 'MCP',
      field: 'mcpClientNames',
      minWidth: 100,
      formatter: ({ cellValue }) => {
        return !cellValue || cellValue.length === 0
          ? '-'
          : `引用${cellValue.length}个`;
      },
    },
    {
      field: 'publicStatus',
      title: '是否公开',
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.INFRA_BOOLEAN_STRING },
      },
      minWidth: 80,
    },
    {
      field: 'status',
      title: '状态',
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.COMMON_STATUS },
      },
      minWidth: 80,
    },
    {
      title: '角色排序',
      field: 'sort',
      minWidth: 80,
    },
    {
      title: '操作',
      width: 130,
      fixed: 'right',
      slots: { default: 'actions' },
    },
  ];
}
