import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';

import { h } from 'vue';

import { Tag } from 'ant-design-vue';

import { getRangePickerDefaultProps } from '#/utils';

/** 列表的搜索表单 */
export function useGridFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'nickname',
      label: '签到用户',
      component: 'Input',
      componentProps: {
        placeholder: '请输入签到用户',
        allowClear: true,
      },
    },
    {
      fieldName: 'day',
      label: '签到天数',
      component: 'Input',
      componentProps: {
        placeholder: '请输入签到天数',
        allowClear: true,
      },
    },
    {
      fieldName: 'createTime',
      label: '签到时间',
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
      field: 'nickname',
      title: '签到用户',
      minWidth: 150,
    },
    {
      field: 'day',
      title: '签到天数',
      minWidth: 120,
      formatter: ({ cellValue }) => ['第', cellValue, '天'].join(' '),
    },
    {
      field: 'point',
      title: '获得积分',
      minWidth: 120,
      slots: {
        default: ({ row }) => {
          return h(
            Tag,
            {
              class: 'mr-5px',
              color: row.point > 0 ? 'blue' : 'red',
            },
            () => (row.point > 0 ? `+${row.point}` : row.point),
          );
        },
      },
    },
    {
      field: 'createTime',
      title: '签到时间',
      minWidth: 180,
      formatter: 'formatDateTime',
    },
  ];
}
