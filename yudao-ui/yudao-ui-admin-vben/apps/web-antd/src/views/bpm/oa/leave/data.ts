import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { DescriptionItemSchema } from '#/components/description';

import { h } from 'vue';

import { DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';
import { formatDate } from '@vben/utils';

import { DictTag } from '#/components/dict-tag';
import { getRangePickerDefaultProps } from '#/utils';

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
      fieldName: 'type',
      label: '请假类型',
      component: 'Select',
      componentProps: {
        placeholder: '请选择请假类型',
        options: getDictOptions(DICT_TYPE.BPM_OA_LEAVE_TYPE, 'number'),
        allowClear: true,
      },
      rules: 'required',
    },

    {
      fieldName: 'startTime',
      label: '开始时间',
      component: 'DatePicker',
      componentProps: {
        placeholder: '请选择开始时间',
        showTime: true,
        valueFormat: 'x',
        format: 'YYYY-MM-DD HH:mm:ss',
      },
      rules: 'required',
    },
    {
      fieldName: 'endTime',
      label: '结束时间',
      component: 'DatePicker',
      componentProps: {
        placeholder: '请选择结束时间',
        showTime: true,
        valueFormat: 'x',
        format: 'YYYY-MM-DD HH:mm:ss',
      },
      rules: 'required',
    },
    {
      fieldName: 'reason',
      label: '原因',
      component: 'Textarea',
      componentProps: {
        placeholder: '请输入原因',
      },
      rules: 'required',
    },
  ];
}

/** 列表的搜索表单 */
export function useGridFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'type',
      label: '请假类型',
      component: 'Select',
      componentProps: {
        placeholder: '请选择请假类型',
        options: getDictOptions(DICT_TYPE.BPM_OA_LEAVE_TYPE, 'number'),
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
    {
      fieldName: 'status',
      label: '审批结果',
      component: 'Select',
      componentProps: {
        placeholder: '请选择审批结果',
        allowClear: true,
        options: getDictOptions(
          DICT_TYPE.BPM_PROCESS_INSTANCE_STATUS,
          'number',
        ),
      },
    },
    {
      fieldName: 'reason',
      label: '原因',
      component: 'Input',
      componentProps: {
        placeholder: '请输入原因',
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
      title: '申请编号',
      minWidth: 100,
    },
    {
      field: 'status',
      title: '状态',
      minWidth: 100,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.BPM_PROCESS_INSTANCE_STATUS },
      },
    },
    {
      field: 'startTime',
      title: '开始时间',
      minWidth: 180,
      formatter: 'formatDate',
    },
    {
      field: 'endTime',
      title: '结束时间',
      minWidth: 180,
      formatter: 'formatDate',
    },
    {
      field: 'type',
      title: '请假类型',
      minWidth: 100,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.BPM_OA_LEAVE_TYPE },
      },
    },
    {
      field: 'reason',
      title: '原因',
      minWidth: 150,
    },
    {
      field: 'createTime',
      title: '申请时间',
      minWidth: 180,
      formatter: 'formatDateTime',
    },
    {
      title: '操作',
      width: 220,
      fixed: 'right',
      slots: { default: 'actions' },
    },
  ];
}

/** 详情 */
export function useDetailFormSchema(): DescriptionItemSchema[] {
  return [
    {
      label: '请假类型',
      field: 'type',
      render: (val) =>
        h(DictTag, {
          type: DICT_TYPE.BPM_OA_LEAVE_TYPE,
          value: val,
        }),
    },
    {
      label: '开始时间',
      field: 'startTime',
      render: (val) => formatDate(val) as string,
    },
    {
      label: '结束时间',
      field: 'endTime',
      render: (val) => formatDate(val) as string,
    },
    {
      label: '原因',
      field: 'reason',
    },
  ];
}
