import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { DescriptionItemSchema } from '#/components/description';

import { h } from 'vue';

import { DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';
import { formatDateTime } from '@vben/utils';

import dayjs from 'dayjs';

import { DictTag } from '#/components/dict-tag';

/** 列表的搜索表单 */
export function useGridFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'handlerName',
      label: '处理器的名字',
      component: 'Input',
      componentProps: {
        allowClear: true,
        placeholder: '请输入处理器的名字',
      },
    },
    {
      fieldName: 'beginTime',
      label: '开始执行时间',
      component: 'DatePicker',
      componentProps: {
        allowClear: true,
        placeholder: '选择开始执行时间',
        valueFormat: 'YYYY-MM-DD HH:mm:ss',
        showTime: {
          format: 'HH:mm:ss',
          defaultValue: dayjs('00:00:00', 'HH:mm:ss'),
        },
      },
    },
    {
      fieldName: 'endTime',
      label: '结束执行时间',
      component: 'DatePicker',
      componentProps: {
        allowClear: true,
        placeholder: '选择结束执行时间',
        valueFormat: 'YYYY-MM-DD HH:mm:ss',
        showTime: {
          format: 'HH:mm:ss',
          defaultValue: dayjs('23:59:59', 'HH:mm:ss'),
        },
      },
    },
    {
      fieldName: 'status',
      label: '任务状态',
      component: 'Select',
      componentProps: {
        options: getDictOptions(DICT_TYPE.INFRA_JOB_LOG_STATUS, 'number'),
        allowClear: true,
        placeholder: '请选择任务状态',
      },
    },
  ];
}

/** 表格列配置 */
export function useGridColumns(): VxeTableGridOptions['columns'] {
  return [
    {
      field: 'id',
      title: '日志编号',
      minWidth: 80,
    },
    {
      field: 'jobId',
      title: '任务编号',
      minWidth: 80,
    },
    {
      field: 'handlerName',
      title: '处理器的名字',
      minWidth: 180,
    },
    {
      field: 'handlerParam',
      title: '处理器的参数',
      minWidth: 140,
    },
    {
      field: 'executeIndex',
      title: '第几次执行',
      minWidth: 100,
    },
    {
      field: 'beginTime',
      title: '执行时间',
      minWidth: 280,
      formatter: ({ row }) => {
        return `${formatDateTime(row.beginTime)} ~ ${formatDateTime(row.endTime)}`;
      },
    },
    {
      field: 'duration',
      title: '执行时长',
      minWidth: 120,
      formatter: ({ row }) => {
        return `${row.duration} 毫秒`;
      },
    },
    {
      field: 'status',
      title: '任务状态',
      minWidth: 100,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.INFRA_JOB_LOG_STATUS },
      },
    },
    {
      title: '操作',
      width: 80,
      fixed: 'right',
      slots: { default: 'actions' },
    },
  ];
}

/** 详情页的字段 */
export function useDetailSchema(): DescriptionItemSchema[] {
  return [
    {
      field: 'id',
      label: '日志编号',
    },
    {
      field: 'jobId',
      label: '任务编号',
    },
    {
      field: 'handlerName',
      label: '处理器的名字',
    },
    {
      field: 'handlerParam',
      label: '处理器的参数',
    },
    {
      field: 'executeIndex',
      label: '第几次执行',
    },
    {
      field: 'beginTime',
      label: '执行时间',
      render: (val, data) => {
        if (val && data?.endTime) {
          return `${formatDateTime(val)} ~ ${formatDateTime(data.endTime)}`;
        }
        return '';
      },
    },
    {
      field: 'duration',
      label: '执行时长',
      render: (val) => {
        return val ? `${val} 毫秒` : '';
      },
    },
    {
      field: 'status',
      label: '任务状态',
      render: (val) => {
        return h(DictTag, {
          type: DICT_TYPE.INFRA_JOB_LOG_STATUS,
          value: val,
        });
      },
    },
    {
      field: 'result',
      label: '执行结果',
    },
  ];
}
