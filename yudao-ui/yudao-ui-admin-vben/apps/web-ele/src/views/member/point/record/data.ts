import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';

import { h } from 'vue';

import { DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';

import { ElTag } from 'element-plus';

import { getRangePickerDefaultProps } from '#/utils';

/** 列表的搜索表单 */
export function useGridFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'nickname',
      label: '用户',
      component: 'Input',
      componentProps: {
        placeholder: '请输入用户昵称',
        clearable: true,
      },
    },
    {
      fieldName: 'bizType',
      label: '业务类型',
      component: 'Select',
      componentProps: {
        placeholder: '请选择业务类型',
        clearable: true,
        options: getDictOptions(DICT_TYPE.MEMBER_POINT_BIZ_TYPE, 'number'),
      },
    },
    {
      fieldName: 'title',
      label: '积分标题',
      component: 'Input',
      componentProps: {
        placeholder: '请输入积分标题',
        clearable: true,
      },
    },
    {
      fieldName: 'createDate',
      label: '获得时间',
      component: 'RangePicker',
      componentProps: {
        ...getRangePickerDefaultProps(),
        clearable: true,
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
      field: 'createTime',
      title: '获得时间',
      minWidth: 180,
      formatter: 'formatDateTime',
    },
    {
      field: 'nickname',
      title: '用户',
      minWidth: 150,
    },
    {
      field: 'point',
      title: '获得积分',
      minWidth: 120,
      slots: {
        default: ({ row }) => {
          return h(
            ElTag,
            {
              type: row.point > 0 ? 'primary' : 'danger',
            },
            () => (row.point > 0 ? `+${row.point}` : row.point),
          );
        },
      },
    },
    {
      field: 'totalPoint',
      title: '总积分',
      minWidth: 100,
    },
    {
      field: 'title',
      title: '标题',
      minWidth: 200,
    },
    {
      field: 'description',
      title: '描述',
      minWidth: 200,
    },
    {
      field: 'bizId',
      title: '业务编码',
      minWidth: 120,
    },
    {
      field: 'bizType',
      title: '业务类型',
      minWidth: 120,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.MEMBER_POINT_BIZ_TYPE },
      },
    },
  ];
}
