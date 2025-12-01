import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { MallBrokerageUserApi } from '#/api/mall/trade/brokerage/user';

import { DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';
import { fenToYuan } from '@vben/utils';

import { getRangePickerDefaultProps } from '#/utils';

/** 列表的搜索表单 */
export function useGridFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'bindUserId',
      label: '推广员编号',
      component: 'Input',
      componentProps: {
        placeholder: '请输入推广员编号',
        allowClear: true,
      },
    },
    {
      fieldName: 'brokerageEnabled',
      label: '推广资格',
      component: 'RadioGroup',
      componentProps: {
        placeholder: '请选择推广资格',
        allowClear: true,
        options: [
          { label: '有', value: true },
          { label: '无', value: false },
        ],
      },
      defaultValue: true,
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
export function useGridColumns(
  onBrokerageEnabledChange?: (
    newEnabled: boolean,
    row: MallBrokerageUserApi.BrokerageUser,
  ) => PromiseLike<boolean | undefined>,
): VxeTableGridOptions['columns'] {
  return [
    {
      field: 'id',
      title: '用户编号',
      minWidth: 80,
    },
    {
      field: 'avatar',
      title: '头像',
      minWidth: 70,
      cellRender: {
        name: 'CellImage',
      },
    },
    {
      field: 'nickname',
      title: '昵称',
      minWidth: 80,
    },
    {
      field: 'brokerageUserCount',
      title: '推广人数',
      minWidth: 80,
    },
    {
      field: 'brokerageOrderCount',
      title: '推广订单数量',
      minWidth: 110,
    },
    {
      field: 'brokerageOrderPrice',
      title: '推广订单金额',
      minWidth: 110,
      formatter: ({ row }) => `￥${fenToYuan(row.brokerageOrderPrice)}`,
    },
    {
      field: 'withdrawPrice',
      title: '已提现金额',
      minWidth: 100,
      formatter: ({ row }) => `￥${fenToYuan(row.withdrawPrice)}`,
    },
    {
      field: 'withdrawCount',
      title: '已提现次数',
      minWidth: 100,
    },
    {
      field: 'price',
      title: '未提现金额',
      minWidth: 100,
      formatter: ({ row }) => `￥${fenToYuan(row.price)}`,
    },
    {
      field: 'frozenPrice',
      title: '冻结中佣金',
      minWidth: 100,
      formatter: ({ row }) => `￥${fenToYuan(row.frozenPrice)}`,
    },
    {
      field: 'brokerageEnabled',
      title: '推广资格',
      minWidth: 80,
      align: 'center',
      cellRender: {
        attrs: { beforeChange: onBrokerageEnabledChange },
        name: 'CellSwitch',
        props: {
          checkedValue: true,
          uncheckedValue: false,
          checkedChildren: '有',
          unCheckedChildren: '无',
        },
      },
    },
    {
      field: 'brokerageTime',
      title: '成为推广员时间',
      minWidth: 180,
      formatter: 'formatDateTime',
    },
    {
      field: 'bindUserId',
      title: '上级推广员编号',
      minWidth: 150,
    },
    {
      field: 'bindUserTime',
      title: '推广员绑定时间',
      minWidth: 180,
      formatter: 'formatDateTime',
    },
    {
      title: '操作',
      width: 150,
      fixed: 'right',
      slots: { default: 'actions' },
    },
  ];
}

/** 创建分销员表单配置 */
export function useCreateFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'userId',
      label: '分销员编号',
      component: 'InputSearch',
      componentProps: {
        placeholder: '请输入分销员编号',
      },
      rules: 'required',
    },
    {
      fieldName: 'bindUserId',
      label: '上级推广员编号',
      component: 'InputSearch',
      componentProps: {
        placeholder: '请输入上级推广员编号',
      },
      rules: 'required',
    },
  ];
}

/** 修改分销用户表单配置 */
export function useUpdateFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'bindUserId',
      label: '上级推广员编号',
      component: 'InputSearch',
      componentProps: {
        placeholder: '请输入上级推广员编号',
      },
      rules: 'required',
    },
  ];
}

/** 用户列表弹窗搜索表单配置 */
export function useUserListFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'level',
      label: '用户类型',
      component: 'Select',
      componentProps: {
        options: [
          { label: '全部', value: undefined },
          { label: '一级推广人', value: '1' },
          { label: '二级推广人', value: '2' },
        ],
        allowClear: true,
      },
    },
    {
      fieldName: 'bindUserTime',
      label: '绑定时间',
      component: 'RangePicker',
      componentProps: {
        ...getRangePickerDefaultProps(),
        allowClear: true,
      },
    },
  ];
}

/** 用户列表弹窗表格列配置 */
export function useUserListColumns(): VxeTableGridOptions['columns'] {
  return [
    {
      field: 'id',
      title: '用户编号',
      minWidth: 80,
    },
    {
      field: 'avatar',
      title: '头像',
      minWidth: 70,
      cellRender: {
        name: 'CellImage',
        props: {
          width: 24,
          height: 24,
          shape: 'circle',
        },
      },
    },
    {
      field: 'nickname',
      title: '昵称',
      minWidth: 80,
    },
    {
      field: 'brokerageUserCount',
      title: '推广人数',
      minWidth: 80,
    },
    {
      field: 'brokerageOrderCount',
      title: '推广订单数量',
      minWidth: 110,
    },
    {
      field: 'brokerageEnabled',
      title: '推广资格',
      minWidth: 80,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.INFRA_BOOLEAN_STRING },
      },
    },
    {
      field: 'bindUserTime',
      title: '绑定时间',
      width: 180,
      formatter: 'formatDateTime',
    },
  ];
}

/** 推广订单列表弹窗搜索表单配置 */
export function useOrderListFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'sourceUserLevel',
      label: '用户类型',
      component: 'Select',
      componentProps: {
        options: [
          { label: '全部', value: 0 },
          { label: '一级推广人', value: 1 },
          { label: '二级推广人', value: 2 },
        ],
      },
      defaultValue: 0,
    },
    {
      fieldName: 'status',
      label: '状态',
      component: 'Select',
      componentProps: {
        placeholder: '请选择状态',
        allowClear: true,
        options: getDictOptions(DICT_TYPE.BROKERAGE_RECORD_STATUS, 'number'),
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

/** 推广订单列表弹窗表格列配置 */
export function useOrderListColumns(): VxeTableGridOptions['columns'] {
  return [
    {
      field: 'bizId',
      title: '订单编号',
      minWidth: 80,
    },
    {
      field: 'sourceUserId',
      title: '用户编号',
      minWidth: 80,
    },
    {
      field: 'sourceUserAvatar',
      title: '头像',
      minWidth: 70,
      cellRender: {
        name: 'CellImage',
        props: {
          width: 24,
          height: 24,
        },
      },
    },
    {
      field: 'sourceUserNickname',
      title: '昵称',
      minWidth: 80,
    },
    {
      field: 'price',
      title: '佣金',
      minWidth: 100,
      formatter: ({ row }) => `￥${fenToYuan(row.price)}`,
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
      field: 'createTime',
      title: '创建时间',
      width: 180,
      formatter: 'formatDateTime',
    },
  ];
}
