import type { VbenFormSchema } from '#/adapter/form';
import type {
  VxeGridPropTypes,
  VxeTableGridOptions,
} from '#/adapter/vxe-table';
import type { BpmProcessInstanceApi } from '#/api/bpm/processInstance';

import { DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';

import { getRangePickerDefaultProps } from '#/utils';

/** 搜索的表单 */
export function useGridFormSchema(
  userList: any[] = [],
  formFields: any[] = [],
): VbenFormSchema[] {
  // 基础搜索字段
  const baseFormSchema = [
    {
      fieldName: 'startUserId',
      label: '发起人',
      component: 'Select',
      componentProps: {
        placeholder: '请选择发起人',
        allowClear: true,
        options: userList.map((user) => ({
          label: user.nickname,
          value: user.id,
        })),
      },
    },
    {
      fieldName: 'name',
      label: '流程名称',
      component: 'Input',
      componentProps: {
        placeholder: '请输入流程名称',
        allowClear: true,
      },
    },
    {
      fieldName: 'status',
      label: '流程状态',
      component: 'Select',
      componentProps: {
        placeholder: '请选择流程状态',
        allowClear: true,
        options: getDictOptions(
          DICT_TYPE.BPM_PROCESS_INSTANCE_STATUS,
          'number',
        ),
      },
    },
    {
      fieldName: 'createTime',
      label: '发起时间',
      component: 'RangePicker',
      componentProps: {
        ...getRangePickerDefaultProps(),
        allowClear: true,
      },
    },
    {
      fieldName: 'endTime',
      label: '结束时间',
      component: 'RangePicker',
      componentProps: {
        ...getRangePickerDefaultProps(),
        allowClear: true,
      },
    },
  ];

  // 动态表单字段 暂时只支持 input 和 textarea,  TODO 其他类型的支持
  const dynamicFormSchema = formFields
    .filter((item) => item.type === 'input' || item.type === 'textarea')
    // 根据类型选择合适的表单组件
    .map((item) => {
      return {
        fieldName: `formFieldsParams.${item.field}`,
        label: item.title,
        component: 'Input',
        componentProps: {
          placeholder: `请输入${item.title}`,
          allowClear: true,
        },
      };
    });

  return [...baseFormSchema, ...dynamicFormSchema];
}

/** 列表的字段 */
export function useGridColumns(
  formFields: any[] = [],
): VxeTableGridOptions<BpmProcessInstanceApi.ProcessInstance>['columns'] {
  const baseColumns: VxeGridPropTypes.Columns<BpmProcessInstanceApi.ProcessInstance> =
    [
      {
        field: 'name',
        title: '流程名称',
        minWidth: 250,
        fixed: 'left',
      },
      {
        field: 'startUser.nickname',
        title: '流程发起人',
        minWidth: 200,
      },
      {
        field: 'status',
        title: '流程状态',
        minWidth: 120,
        cellRender: {
          name: 'CellDict',
          props: { type: DICT_TYPE.BPM_PROCESS_INSTANCE_STATUS },
        },
      },
      {
        field: 'startTime',
        title: '发起时间',
        minWidth: 180,
        formatter: 'formatDateTime',
      },
      {
        field: 'endTime',
        title: '结束时间',
        minWidth: 180,
        formatter: 'formatDateTime',
      },
    ];

  // 添加动态表单字段列，暂时全部以字符串，TODO 展示优化, 按 type 展示控制
  const formFieldColumns = (formFields || []).map((item) => ({
    field: `formVariables.${item.field}`,
    title: item.title,
    minWidth: 120,
    formatter: ({ row }: any) => {
      return row.formVariables?.[item.field] ?? '';
    },
  }));

  return [
    ...baseColumns,
    ...formFieldColumns,
    {
      title: '操作',
      width: 180,
      fixed: 'right',
      slots: { default: 'actions' },
    },
  ];
}
