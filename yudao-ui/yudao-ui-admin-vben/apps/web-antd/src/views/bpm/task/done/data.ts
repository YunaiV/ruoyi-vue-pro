import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';

import { DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';

import { getCategorySimpleList } from '#/api/bpm/category';
import { getRangePickerDefaultProps } from '#/utils';

/** 列表的搜索表单 */
export function useGridFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'name',
      label: '任务名称',
      component: 'Input',
      componentProps: {
        placeholder: '请输入任务名称',
        allowClear: true,
      },
    },
    {
      fieldName: 'processDefinitionId',
      label: '所属流程',
      component: 'Input',
      componentProps: {
        placeholder: '请输入流程定义的编号',
        allowClear: true,
      },
    },
    {
      fieldName: 'category',
      label: '流程分类',
      component: 'ApiSelect',
      componentProps: {
        placeholder: '请输入流程分类',
        allowClear: true,
        api: getCategorySimpleList,
        labelField: 'name',
        valueField: 'code',
      },
    },
    {
      fieldName: 'status',
      label: '审批状态',
      component: 'Select',
      componentProps: {
        options: getDictOptions(DICT_TYPE.BPM_TASK_STATUS, 'number'),
        placeholder: '请选择审批状态',
        allowClear: true,
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
  ];
}

/** 列表的字段 */
export function useGridColumns(): VxeTableGridOptions['columns'] {
  return [
    {
      field: 'processInstance.name',
      title: '流程',
      minWidth: 200,
      fixed: 'left',
    },
    {
      field: 'processInstance.summary',
      title: '摘要',
      minWidth: 200,
      formatter: ({ cellValue }) => {
        return cellValue && cellValue.length > 0
          ? cellValue
              .map((item: any) => `${item.key} : ${item.value}`)
              .join('\n')
          : '-';
      },
    },
    {
      field: 'processInstance.startUser.nickname',
      title: '发起人',
      minWidth: 120,
    },
    {
      field: 'name',
      title: '当前任务',
      minWidth: 180,
    },
    {
      field: 'createTime',
      title: '任务开始时间',
      minWidth: 180,
      formatter: 'formatDateTime',
    },
    {
      field: 'endTime',
      title: '任务结束时间',
      minWidth: 180,
      formatter: 'formatDateTime',
    },
    {
      field: 'status',
      title: '审批状态',
      minWidth: 180,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.BPM_TASK_STATUS },
      },
    },
    {
      field: 'reason',
      title: '审批建议',
      minWidth: 180,
    },
    {
      field: 'durationInMillis',
      title: '耗时',
      minWidth: 180,
      formatter: 'formatPast2',
    },
    {
      field: 'processInstanceId',
      title: '流程编号',
      minWidth: 280,
    },
    {
      field: 'id',
      title: '任务编号',
      minWidth: 280,
    },
    {
      title: '操作',
      width: 120,
      fixed: 'right',
      slots: { default: 'actions' },
    },
  ];
}
