import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { AiModelApiKeyApi } from '#/api/ai/model/apiKey';

import { AiModelTypeEnum, CommonStatusEnum, DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';

import { z } from '#/adapter/form';
import { getApiKeySimpleList } from '#/api/ai/model/apiKey';

/** 关联数据 */
let apiKeyList: AiModelApiKeyApi.ApiKey[] = [];
getApiKeySimpleList().then((data) => (apiKeyList = data));

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
      fieldName: 'platform',
      label: '所属平台',
      component: 'Select',
      componentProps: {
        placeholder: '请选择所属平台',
        options: getDictOptions(DICT_TYPE.AI_PLATFORM, 'string'),
        allowClear: true,
      },
      rules: 'required',
    },
    {
      fieldName: 'type',
      label: '模型类型',
      component: 'Select',
      componentProps: (values) => {
        return {
          placeholder: '请输入模型类型',
          disabled: !!values.id,
          options: getDictOptions(DICT_TYPE.AI_MODEL_TYPE, 'number'),
          allowClear: true,
        };
      },
      rules: 'required',
    },
    {
      fieldName: 'keyId',
      label: 'API 秘钥',
      component: 'ApiSelect',
      componentProps: {
        placeholder: '请选择 API 秘钥',
        api: getApiKeySimpleList,
        labelField: 'name',
        valueField: 'id',
        allowClear: true,
      },
      rules: 'required',
    },
    {
      component: 'Input',
      fieldName: 'name',
      label: '模型名字',
      rules: 'required',
      componentProps: {
        placeholder: '请输入模型名字',
      },
    },
    {
      component: 'Input',
      fieldName: 'model',
      label: '模型标识',
      rules: 'required',
      componentProps: {
        placeholder: '请输入模型标识',
      },
    },
    {
      fieldName: 'sort',
      label: '模型排序',
      component: 'InputNumber',
      componentProps: {
        placeholder: '请输入模型排序',
      },
      rules: 'required',
    },
    {
      fieldName: 'status',
      label: '开启状态',
      component: 'RadioGroup',
      componentProps: {
        options: getDictOptions(DICT_TYPE.COMMON_STATUS, 'number'),
        buttonStyle: 'solid',
        optionType: 'button',
      },
      rules: z.number().default(CommonStatusEnum.ENABLE),
    },
    {
      fieldName: 'temperature',
      label: '温度参数',
      component: 'InputNumber',
      componentProps: {
        placeholder: '请输入温度参数',
        min: 0,
        max: 2,
      },
      dependencies: {
        triggerFields: ['type'],
        show: (values) => {
          return [AiModelTypeEnum.CHAT].includes(values.type);
        },
      },
      rules: 'required',
    },
    {
      fieldName: 'maxTokens',
      label: '回复数 Token 数',
      component: 'InputNumber',
      componentProps: {
        min: 0,
        max: 8192,
        placeholder: '请输入回复数 Token 数',
      },
      dependencies: {
        triggerFields: ['type'],
        show: (values) => {
          return [AiModelTypeEnum.CHAT].includes(values.type);
        },
      },
      rules: 'required',
    },
    {
      fieldName: 'maxContexts',
      label: '上下文数量',
      component: 'InputNumber',
      componentProps: {
        min: 0,
        max: 20,
        placeholder: '请输入上下文数量',
      },
      dependencies: {
        triggerFields: ['type'],
        show: (values) => {
          return [AiModelTypeEnum.CHAT].includes(values.type);
        },
      },
      rules: 'required',
    },
  ];
}

/** 列表的搜索表单 */
export function useGridFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'name',
      label: '模型名字',
      component: 'Input',
      componentProps: {
        placeholder: '请输入模型名字',
        allowClear: true,
      },
    },
    {
      fieldName: 'model',
      label: '模型标识',
      component: 'Input',
      componentProps: {
        placeholder: '请输入模型标识',
        allowClear: true,
      },
    },
    {
      fieldName: 'platform',
      label: '模型平台',
      component: 'Input',
      componentProps: {
        placeholder: '请输入模型平台',
        allowClear: true,
      },
    },
  ];
}

/** 列表的字段 */
export function useGridColumns(): VxeTableGridOptions['columns'] {
  return [
    {
      field: 'platform',
      title: '所属平台',
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.AI_PLATFORM },
      },
      minWidth: 100,
    },
    {
      field: 'type',
      title: '模型类型',
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.AI_MODEL_TYPE },
      },
      minWidth: 100,
    },
    {
      field: 'name',
      title: '模型名字',
      minWidth: 180,
    },
    {
      title: '模型标识',
      field: 'model',
      minWidth: 180,
    },
    {
      title: 'API 秘钥',
      field: 'keyId',
      formatter: ({ cellValue }) => {
        return (
          apiKeyList.find((apiKey) => apiKey.id === cellValue)?.name || '-'
        );
      },
      minWidth: 140,
    },
    {
      title: '排序',
      field: 'sort',
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
      field: 'temperature',
      title: '温度参数',
      minWidth: 100,
    },
    {
      title: '回复数 Token 数',
      field: 'maxTokens',
      minWidth: 140,
    },
    {
      title: '上下文数量',
      field: 'maxContexts',
      minWidth: 120,
    },
    {
      title: '操作',
      width: 130,
      fixed: 'right',
      slots: { default: 'actions' },
    },
  ];
}
