import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { DescriptionItemSchema } from '#/components/description';

import { h } from 'vue';

import { DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';
import { formatDateTime } from '@vben/utils';

import { getSimpleMailAccountList } from '#/api/system/mail/account';
import { DictTag } from '#/components/dict-tag';
import { getRangePickerDefaultProps } from '#/utils';

/** 列表的搜索表单 */
export function useGridFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'sendTime',
      label: '发送时间',
      component: 'RangePicker',
      componentProps: {
        ...getRangePickerDefaultProps(),
        allowClear: true,
      },
    },
    {
      fieldName: 'userId',
      label: '用户编号',
      component: 'Input',
      componentProps: {
        allowClear: true,
        placeholder: '请输入用户编号',
      },
    },
    {
      fieldName: 'userType',
      label: '用户类型',
      component: 'Select',
      componentProps: {
        options: getDictOptions(DICT_TYPE.USER_TYPE, 'number'),
        allowClear: true,
        placeholder: '请选择用户类型',
      },
    },
    {
      fieldName: 'sendStatus',
      label: '发送状态',
      component: 'Select',
      componentProps: {
        options: getDictOptions(DICT_TYPE.SYSTEM_MAIL_SEND_STATUS, 'number'),
        allowClear: true,
        placeholder: '请选择发送状态',
      },
    },
    {
      fieldName: 'accountId',
      label: '邮箱账号',
      component: 'ApiSelect',
      componentProps: {
        api: getSimpleMailAccountList,
        labelField: 'mail',
        valueField: 'id',
        allowClear: true,
        placeholder: '请选择邮箱账号',
      },
    },
    {
      fieldName: 'templateId',
      label: '模板编号',
      component: 'Input',
      componentProps: {
        allowClear: true,
        placeholder: '请输入模板编号',
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
      field: 'sendTime',
      title: '发送时间',
      minWidth: 180,
      formatter: 'formatDateTime',
    },
    {
      field: 'userType',
      title: '接收用户',
      minWidth: 150,
      slots: { default: 'userInfo' },
    },
    {
      field: 'toMails',
      title: '接收信息',
      minWidth: 300,
      formatter: ({ row }) => {
        const lines: string[] = [];
        if (row.toMails && row.toMails.length > 0) {
          lines.push(`收件：${row.toMails.join('、')}`);
        }
        if (row.ccMails && row.ccMails.length > 0) {
          lines.push(`抄送：${row.ccMails.join('、')}`);
        }
        if (row.bccMails && row.bccMails.length > 0) {
          lines.push(`密送：${row.bccMails.join('、')}`);
        }
        return lines.join('\n');
      },
    },
    {
      field: 'templateTitle',
      title: '邮件标题',
      minWidth: 120,
    },
    {
      field: 'templateContent',
      title: '邮件内容',
      minWidth: 300,
    },
    {
      field: 'fromMail',
      title: '发送邮箱',
      minWidth: 120,
    },
    {
      field: 'sendStatus',
      title: '发送状态',
      minWidth: 120,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.SYSTEM_MAIL_SEND_STATUS },
      },
    },
    {
      field: 'templateCode',
      title: '模板编码',
      minWidth: 120,
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
      label: '编号',
    },
    {
      field: 'createTime',
      label: '创建时间',
      render: (val) => {
        return formatDateTime(val) as string;
      },
    },
    {
      field: 'fromMail',
      label: '发送邮箱',
    },
    {
      field: 'userId',
      label: '接收用户',
      render: (val, data) => {
        if (data?.userType && val) {
          return h('div', [
            h(DictTag, {
              type: DICT_TYPE.USER_TYPE,
              value: data.userType,
            }),
            ` (${val})`,
          ]);
        }
        return '无';
      },
    },
    {
      field: 'toMails',
      label: '接收信息',
      render: (val, data) => {
        const lines: string[] = [];
        if (val && val.length > 0) {
          lines.push(`收件：${val.join('、')}`);
        }
        if (data?.ccMails && data.ccMails.length > 0) {
          lines.push(`抄送：${data.ccMails.join('、')}`);
        }
        if (data?.bccMails && data.bccMails.length > 0) {
          lines.push(`密送：${data.bccMails.join('、')}`);
        }
        return h(
          'div',
          {
            style: { whiteSpace: 'pre-line' },
          },
          lines.join('\n'),
        );
      },
    },
    {
      field: 'templateId',
      label: '模板编号',
    },
    {
      field: 'templateCode',
      label: '模板编码',
    },
    {
      field: 'templateTitle',
      label: '邮件标题',
    },
    {
      field: 'templateContent',
      label: '邮件内容',
      span: 2,
      render: (val) => {
        return h('div', {
          innerHTML: val || '',
        });
      },
    },
    {
      field: 'sendStatus',
      label: '发送状态',
      render: (val) => {
        return h(DictTag, {
          type: DICT_TYPE.SYSTEM_MAIL_SEND_STATUS,
          value: val,
        });
      },
    },
    {
      field: 'sendTime',
      label: '发送时间',
      render: (val) => formatDateTime(val) as string,
    },
    {
      field: 'sendMessageId',
      label: '发送消息编号',
    },
    {
      field: 'sendException',
      label: '发送异常',
    },
  ];
}
