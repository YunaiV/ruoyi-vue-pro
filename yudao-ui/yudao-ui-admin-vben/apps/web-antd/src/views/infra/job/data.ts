import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { DescriptionItemSchema } from '#/components/description';

import { h, markRaw } from 'vue';

import { DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';
import { formatDateTime } from '@vben/utils';

import { Timeline } from 'ant-design-vue';

import { CronTab } from '#/components/cron-tab';
import { DictTag } from '#/components/dict-tag';

/** 新增/修改的表单 */
export function useFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'id',
      component: 'Input',
      dependencies: {
        triggerFields: [''],
        show: () => false,
      },
    },
    {
      fieldName: 'name',
      label: '任务名称',
      component: 'Input',
      componentProps: {
        placeholder: '请输入任务名称',
      },
      rules: 'required',
    },
    {
      fieldName: 'handlerName',
      label: '处理器的名字',
      component: 'Input',
      componentProps: {
        placeholder: '请输入处理器的名字',
      },
      dependencies: {
        triggerFields: ['id'],
        disabled: (values) => !!values.id,
      },
      rules: 'required',
    },
    {
      fieldName: 'handlerParam',
      label: '处理器的参数',
      component: 'Input',
      componentProps: {
        placeholder: '请输入处理器的参数',
      },
    },
    {
      fieldName: 'cronExpression',
      label: 'CRON 表达式',
      component: markRaw(CronTab),
      componentProps: {
        placeholder: '请输入 CRON 表达式',
      },
      rules: 'required',
    },
    {
      fieldName: 'retryCount',
      label: '重试次数',
      component: 'InputNumber',
      componentProps: {
        placeholder: '请输入重试次数。设置为 0 时，不进行重试',
        min: 0,
      },
      rules: 'required',
    },
    {
      fieldName: 'retryInterval',
      label: '重试间隔',
      component: 'InputNumber',
      componentProps: {
        placeholder: '请输入重试间隔，单位：毫秒。设置为 0 时，无需间隔',
        min: 0,
      },
      rules: 'required',
    },
    {
      fieldName: 'monitorTimeout',
      label: '监控超时时间',
      component: 'InputNumber',
      componentProps: {
        placeholder: '请输入监控超时时间，单位：毫秒',
        min: 0,
      },
    },
  ];
}

/** 列表的搜索表单 */
export function useGridFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'name',
      label: '任务名称',
      component: 'Input',
      componentProps: {
        allowClear: true,
        placeholder: '请输入任务名称',
      },
    },
    {
      fieldName: 'status',
      label: '任务状态',
      component: 'Select',
      componentProps: {
        options: getDictOptions(DICT_TYPE.INFRA_JOB_STATUS, 'number'),
        allowClear: true,
        placeholder: '请选择任务状态',
      },
    },
    {
      fieldName: 'handlerName',
      label: '处理器的名字',
      component: 'Input',
      componentProps: {
        allowClear: true,
        placeholder: '请输入处理器的名字',
      },
    },
  ];
}

/** 表格列配置 */
export function useGridColumns(): VxeTableGridOptions['columns'] {
  return [
    { type: 'checkbox', width: 40 },
    {
      field: 'id',
      title: '任务编号',
      minWidth: 80,
    },
    {
      field: 'name',
      title: '任务名称',
      minWidth: 120,
    },
    {
      field: 'status',
      title: '任务状态',
      minWidth: 100,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.INFRA_JOB_STATUS },
      },
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
      field: 'cronExpression',
      title: 'CRON 表达式',
      minWidth: 120,
    },
    {
      title: '操作',
      width: 240,
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
      label: '任务编号',
    },
    {
      field: 'name',
      label: '任务名称',
    },
    {
      field: 'status',
      label: '任务状态',
      render: (val) => {
        return h(DictTag, {
          type: DICT_TYPE.INFRA_JOB_STATUS,
          value: val,
        });
      },
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
      field: 'cronExpression',
      label: 'Cron 表达式',
    },
    {
      field: 'retryCount',
      label: '重试次数',
    },
    {
      label: '重试间隔',
      field: 'retryInterval',
      render: (val) => {
        return val ? `${val} 毫秒` : '无间隔';
      },
    },
    {
      label: '监控超时时间',
      field: 'monitorTimeout',
      render: (val) => {
        return val && val > 0 ? `${val} 毫秒` : '未开启';
      },
    },
    {
      field: 'nextTimes',
      label: '后续执行时间',
      render: (val) => {
        if (!val || val.length === 0) {
          return '无后续执行时间';
        }
        return h(Timeline, {}, () =>
          val?.map((time: Date) =>
            h(Timeline.Item, {}, () => formatDateTime(time)),
          ),
        );
      },
    },
  ];
}
