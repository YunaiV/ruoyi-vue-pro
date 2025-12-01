import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';

import { AiModelTypeEnum, CommonStatusEnum, DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';

import { z } from '#/adapter/form';
import { getModelSimpleList } from '#/api/ai/model/model';
import { getRangePickerDefaultProps } from '#/utils';

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
      fieldName: 'name',
      label: '知识库名称',
      componentProps: {
        placeholder: '请输入知识库名称',
      },
      rules: 'required',
    },
    {
      fieldName: 'description',
      label: '知识库描述',
      component: 'Textarea',
      componentProps: {
        rows: 3,
        placeholder: '请输入知识库描述',
      },
    },
    {
      component: 'ApiSelect',
      fieldName: 'embeddingModelId',
      label: '向量模型',
      componentProps: {
        api: () => getModelSimpleList(AiModelTypeEnum.EMBEDDING),
        labelField: 'name',
        valueField: 'id',
        allowClear: true,
        placeholder: '请选择向量模型',
      },
      rules: 'required',
    },
    {
      fieldName: 'topK',
      label: '检索 topK',
      component: 'InputNumber',
      componentProps: {
        placeholder: '请输入检索 topK',
        min: 0,
        max: 10,
      },
      rules: 'required',
    },
    {
      fieldName: 'similarityThreshold',
      label: '检索相似度阈值',
      component: 'InputNumber',
      componentProps: {
        placeholder: '请输入检索相似度阈值',
        min: 0,
        max: 1,
        step: 0.01,
        precision: 2,
      },
      rules: 'required',
    },
    {
      fieldName: 'status',
      label: '是否启用',
      component: 'RadioGroup',
      componentProps: {
        options: getDictOptions(DICT_TYPE.COMMON_STATUS, 'number'),
        buttonStyle: 'solid',
        optionType: 'button',
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
      label: '知识库名称',
      component: 'Input',
      componentProps: {
        placeholder: '请输入知识库名称',
        allowClear: true,
      },
    },
    {
      fieldName: 'status',
      label: '是否启用',
      component: 'Select',
      componentProps: {
        options: getDictOptions(DICT_TYPE.COMMON_STATUS, 'number'),
        placeholder: '请选择是否启用',
        allowClear: true,
      },
    },
    {
      fieldName: 'createTime',
      label: '创建时间',
      component: 'RangePicker',
      componentProps: {
        ...getRangePickerDefaultProps(),
        allowClear: true,
      },
    },
  ];
}

/** 列表的字段 */
export function useGridColumns(): VxeTableGridOptions['columns'] {
  return [
    {
      field: 'id',
      title: '编号',
      minWidth: 100,
    },
    {
      field: 'name',
      title: '知识库名称',
      minWidth: 150,
    },
    {
      field: 'description',
      title: '知识库描述',
      minWidth: 200,
    },
    {
      field: 'embeddingModel',
      title: '向量化模型',
      minWidth: 150,
    },
    {
      field: 'status',
      title: '是否启用',
      minWidth: 100,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.COMMON_STATUS },
      },
    },
    {
      field: 'createTime',
      title: '创建时间',
      minWidth: 180,
      formatter: 'formatDateTime',
    },
    {
      title: '操作',
      width: 280,
      fixed: 'right',
      slots: { default: 'actions' },
    },
  ];
}
