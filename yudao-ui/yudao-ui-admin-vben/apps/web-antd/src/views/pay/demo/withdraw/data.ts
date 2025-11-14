import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';

import { DICT_TYPE } from '@vben/constants';

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
      fieldName: 'subject',
      label: '提现标题',
      component: 'Input',
      rules: 'required',
      componentProps: {
        placeholder: '请输入提现标题',
      },
    },
    {
      fieldName: 'price',
      label: '提现金额',
      component: 'InputNumber',
      rules: 'required',
      componentProps: {
        min: 1,
        precision: 2,
        step: 0.01,
        placeholder: '请输入提现金额',
      },
    },
    {
      fieldName: 'type',
      label: '提现类型',
      component: 'Select',
      rules: 'required',
      componentProps: {
        options: [
          { label: '支付宝', value: 1 },
          { label: '微信余额', value: 2 },
          { label: '钱包余额', value: 3 },
        ],
        placeholder: '请选择提现类型',
      },
    },
    {
      fieldName: 'userAccount',
      label: '收款人账号',
      component: 'Input',
      rules: 'required',
      dependencies: {
        triggerFields: ['type'],
        componentProps: (values) => {
          const type = values.type;
          let placeholder = '请输入收款人账号';
          switch (type) {
            case 1: {
              placeholder = '请输入支付宝账号';
              break;
            }
            case 2: {
              placeholder = '请输入微信 openid';
              break;
            }
            case 3: {
              placeholder = '请输入钱包编号';
              break;
            }
          }
          return {
            placeholder,
          };
        },
      },
    },
    {
      fieldName: 'userName',
      label: '收款人姓名',
      component: 'Input',
      componentProps: {
        placeholder: '请输入收款人姓名',
      },
    },
  ];
}

/** 列表的字段 */
export function useGridColumns(): VxeTableGridOptions['columns'] {
  return [
    {
      field: 'id',
      title: '提现单编号',
      minWidth: 100,
    },
    {
      field: 'subject',
      title: '提现标题',
      minWidth: 150,
    },
    {
      field: 'type',
      title: '提现类型',
      minWidth: 100,
      slots: { default: 'type' },
    },
    {
      field: 'price',
      title: '提现金额',
      minWidth: 100,
      formatter: 'formatAmount2',
    },
    {
      field: 'userName',
      title: '收款人姓名',
      minWidth: 120,
    },
    {
      field: 'userAccount',
      title: '收款人账号',
      minWidth: 150,
    },
    {
      field: 'status',
      title: '提现状态',
      minWidth: 100,
      slots: { default: 'status' },
    },
    {
      field: 'payTransferId',
      title: '转账单号',
      minWidth: 120,
    },
    {
      field: 'transferChannelCode',
      title: '转账渠道',
      minWidth: 130,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.PAY_CHANNEL_CODE },
      },
    },
    {
      field: 'transferTime',
      title: '转账时间',
      minWidth: 180,
      formatter: 'formatDateTime',
    },
    {
      field: 'transferErrorMsg',
      title: '转账失败原因',
      minWidth: 150,
    },
    {
      title: '操作',
      width: 220,
      fixed: 'right',
      slots: { default: 'actions' },
    },
  ];
}
