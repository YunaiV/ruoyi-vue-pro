import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';

import { DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';
import { fenToYuan } from '@vben/utils';

import { getRangePickerDefaultProps } from '#/utils';

/** 列表的搜索表单 */
export function useGridFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'userId',
      label: '用户编号',
      component: 'Input',
      componentProps: {
        placeholder: '请输入用户编号',
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
        options: getDictOptions(DICT_TYPE.BROKERAGE_RECORD_BIZ_TYPE, 'number'),
      },
    },
    {
      fieldName: 'status',
      label: '状态',
      component: 'Select',
      componentProps: {
        placeholder: '请选择状态',
        clearable: true,
        options: getDictOptions(DICT_TYPE.BROKERAGE_RECORD_STATUS, 'number'),
      },
    },
    {
      fieldName: 'createTime',
      label: '创建时间',
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
      minWidth: 60,
    },
    {
      field: 'userId',
      title: '用户编号',
      minWidth: 80,
    },
    {
      field: 'userAvatar',
      title: '头像',
      minWidth: 70,
      cellRender: {
        name: 'CellImage',
        props: {
          height: 40,
          width: 40,
          shape: 'circle',
        },
      },
    },
    {
      field: 'userNickname',
      title: '昵称',
      minWidth: 80,
    },
    {
      field: 'bizType',
      title: '业务类型',
      minWidth: 85,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.BROKERAGE_RECORD_BIZ_TYPE },
      },
    },
    {
      field: 'bizId',
      title: '业务编号',
      minWidth: 80,
    },
    {
      field: 'title',
      title: '标题',
      minWidth: 110,
    },
    {
      field: 'price',
      title: '金额',
      minWidth: 60,
      formatter: ({ row }) => `￥${fenToYuan(row.price)}`,
    },
    {
      field: 'description',
      title: '说明',
      minWidth: 120,
    },
    {
      field: 'status',
      title: '状态',
      minWidth: 85,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.BROKERAGE_RECORD_STATUS },
      },
    },
    {
      field: 'unfreezeTime',
      title: '解冻时间',
      minWidth: 180,
      formatter: 'formatDateTime',
    },
    {
      field: 'createTime',
      title: '创建时间',
      width: 180,
      formatter: 'formatDateTime',
    },
  ];
}
