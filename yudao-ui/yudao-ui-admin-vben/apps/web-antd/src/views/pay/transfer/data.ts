import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { DescriptionItemSchema } from '#/components/description';

import { h } from 'vue';

import { DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';
import { erpPriceInputFormatter, formatDateTime } from '@vben/utils';

import { Tag } from 'ant-design-vue';

import { DictTag } from '#/components/dict-tag';
import { getRangePickerDefaultProps } from '#/utils';

/** 列表的搜索表单 */
export function useGridFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'no',
      label: '转账单号',
      component: 'Input',
      componentProps: {
        allowClear: true,
        placeholder: '请输入转账单号',
      },
    },
    {
      fieldName: 'channelCode',
      label: '转账渠道',
      component: 'Select',
      componentProps: {
        options: getDictOptions(DICT_TYPE.PAY_CHANNEL_CODE),
        allowClear: true,
        placeholder: '请选择支付渠道',
      },
    },
    {
      fieldName: 'merchantTransferId',
      label: '商户单号',
      component: 'Input',
      componentProps: {
        allowClear: true,
        placeholder: '请输入商户单号',
      },
    },
    {
      fieldName: 'type',
      label: '类型',
      component: 'Select',
      componentProps: {
        options: getDictOptions(DICT_TYPE.PAY_TRANSFER_TYPE),
        allowClear: true,
        placeholder: '请选择类型',
      },
    },
    {
      fieldName: 'status',
      label: '转账状态',
      component: 'Select',
      componentProps: {
        options: getDictOptions(DICT_TYPE.PAY_TRANSFER_STATUS),
        allowClear: true,
        placeholder: '请选择转账状态',
      },
    },
    {
      fieldName: 'userName',
      label: '收款人姓名',
      component: 'Input',
      componentProps: {
        allowClear: true,
        placeholder: '请输入收款人姓名',
      },
    },
    {
      fieldName: 'userAccount',
      label: '收款人账号',
      component: 'Input',
      componentProps: {
        allowClear: true,
        placeholder: '请输入收款人账号',
      },
    },
    {
      fieldName: 'channelTransferNo',
      label: '渠道单号',
      component: 'Input',
      componentProps: {
        allowClear: true,
        placeholder: '请输入渠道单号',
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

/** 列表的字段 */
export function useGridColumns(): VxeTableGridOptions['columns'] {
  return [
    {
      field: 'id',
      title: '编号',
      minWidth: 100,
    },
    {
      field: 'price',
      title: '转账金额',
      minWidth: 120,
      formatter: 'formatAmount2',
    },
    {
      field: 'merchantTransferId',
      title: '转账单号',
      minWidth: 350,
      slots: {
        default: 'no',
      },
    },
    {
      field: 'status',
      title: '转账状态',
      minWidth: 120,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.PAY_TRANSFER_STATUS },
      },
    },
    {
      field: 'channelCode',
      title: '转账渠道',
      minWidth: 140,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.PAY_CHANNEL_CODE },
      },
    },
    {
      field: 'createTime',
      title: '创建时间',
      minWidth: 180,
      formatter: 'formatDateTime',
    },
    {
      field: 'successTime',
      title: '转账时间',
      minWidth: 180,
      formatter: 'formatDateTime',
    },
    {
      field: 'subject',
      title: '转账标题',
      minWidth: 150,
    },
    {
      field: 'appName',
      title: '支付应用',
      minWidth: 150,
    },
    {
      field: 'userName',
      title: '收款人姓名',
      minWidth: 150,
    },
    {
      field: 'userAccount',
      title: '收款账号',
      minWidth: 200,
    },
    {
      title: '操作',
      width: 80,
      fixed: 'right',
      slots: { default: 'actions' },
    },
  ];
}

/** 详情的字段 */
export function useDetailSchema(): DescriptionItemSchema[] {
  return [
    {
      field: 'merchantTransferId',
      label: '商户单号',
      render: (val) => h(Tag, {}, () => val),
    },
    {
      field: 'no',
      label: '转账单号',
      render: (val) => h(Tag, { color: 'orange' }, () => val),
    },
    {
      field: 'appId',
      label: '应用编号',
    },
    {
      field: 'status',
      label: '转账状态',
      render: (val) =>
        h(DictTag, {
          type: DICT_TYPE.PAY_TRANSFER_STATUS,
          value: val,
        }),
    },
    {
      field: 'price',
      label: '转账金额',
      render: (val) =>
        h(
          Tag,
          { color: 'success' },
          () => `￥${erpPriceInputFormatter(val || 0)}`,
        ),
    },
    {
      field: 'successTime',
      label: '转账时间',
      render: (val) => formatDateTime(val) as string,
    },
    {
      field: 'createTime',
      label: '创建时间',
      render: (val) => formatDateTime(val) as string,
    },
    {
      field: 'userName',
      label: '收款人姓名',
    },
    {
      field: 'userAccount',
      label: '收款人账号',
    },
    {
      field: 'channelCode',
      label: '支付渠道',
      render: (val) =>
        h(DictTag, {
          type: DICT_TYPE.PAY_CHANNEL_CODE,
          value: val,
        }),
    },
    {
      field: 'userIp',
      label: '支付 IP',
    },
    {
      field: 'channelTransferNo',
      label: '渠道单号',
      render: (val) => (val ? h(Tag, { color: 'success' }, () => val) : ''),
    },
    {
      field: 'notifyUrl',
      label: '通知 URL',
    },
    {
      field: 'channelNotifyData',
      label: '转账渠道通知内容',
    },
  ];
}
