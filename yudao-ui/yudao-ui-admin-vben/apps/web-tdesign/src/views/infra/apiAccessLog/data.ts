import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { DescriptionItemSchema } from '#/components/description';

import { h } from 'vue';

import { JsonViewer } from '@vben/common-ui';
import { DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';
import { formatDateTime } from '@vben/utils';

import { DictTag } from '#/components/dict-tag';
import { getRangePickerDefaultProps } from '#/utils';

/** 列表的搜索表单 */
export function useGridFormSchema(): VbenFormSchema[] {
  return [
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
      fieldName: 'applicationName',
      label: '应用名',
      component: 'Input',
      componentProps: {
        allowClear: true,
        placeholder: '请输入应用名',
      },
    },
    {
      fieldName: 'beginTime',
      label: '请求时间',
      component: 'RangePicker',
      componentProps: {
        ...getRangePickerDefaultProps(),
        allowClear: true,
      },
    },
    {
      fieldName: 'duration',
      label: '执行时长',
      component: 'Input',
      componentProps: {
        allowClear: true,
        placeholder: '请输入执行时长',
      },
    },
    {
      fieldName: 'resultCode',
      label: '结果码',
      component: 'Input',
      componentProps: {
        allowClear: true,
        placeholder: '请输入结果码',
      },
    },
  ];
}

/** 列表的字段 */
export function useGridColumns(): VxeTableGridOptions['columns'] {
  return [
    {
      field: 'id',
      title: '日志编号',
      minWidth: 100,
    },
    {
      field: 'userId',
      title: '用户编号',
      minWidth: 100,
    },
    {
      field: 'userType',
      title: '用户类型',
      minWidth: 120,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.USER_TYPE },
      },
    },
    {
      field: 'applicationName',
      title: '应用名',
      minWidth: 150,
    },
    {
      field: 'requestMethod',
      title: '请求方法',
      minWidth: 80,
    },
    {
      field: 'requestUrl',
      title: '请求地址',
      minWidth: 300,
    },
    {
      field: 'beginTime',
      title: '请求时间',
      minWidth: 180,
      formatter: 'formatDateTime',
    },
    {
      field: 'duration',
      title: '执行时长',
      minWidth: 120,
      formatter: ({ cellValue }) => `${cellValue} ms`,
    },
    {
      field: 'resultCode',
      title: '操作结果',
      minWidth: 150,
      formatter: ({ row }) => {
        return row.resultCode === 0 ? '成功' : `失败(${row.resultMsg})`;
      },
    },
    {
      field: 'operateModule',
      title: '操作模块',
      minWidth: 150,
    },
    {
      field: 'operateName',
      title: '操作名',
      minWidth: 220,
    },
    {
      field: 'operateType',
      title: '操作类型',
      minWidth: 120,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.INFRA_OPERATE_TYPE },
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
      field: 'traceId',
      label: '链路追踪',
    },
    {
      field: 'applicationName',
      label: '应用名',
    },
    {
      field: 'userId',
      label: '用户Id',
    },
    {
      field: 'userType',
      label: '用户类型',
      render: (val) => {
        return h(DictTag, {
          type: DICT_TYPE.USER_TYPE,
          value: val,
        });
      },
    },
    {
      field: 'userIp',
      label: '用户 IP',
    },
    {
      field: 'userAgent',
      label: '用户 UA',
    },
    {
      field: 'requestMethod',
      label: '请求信息',
      render: (val, data) => {
        if (val && data?.requestUrl) {
          return `${val} ${data.requestUrl}`;
        }
        return '';
      },
    },
    {
      field: 'requestParams',
      label: '请求参数',
      render: (val) => {
        if (val) {
          return h(JsonViewer, {
            value: JSON.parse(val),
            previewMode: true,
          });
        }
        return '';
      },
    },
    {
      field: 'responseBody',
      label: '请求结果',
    },
    {
      label: '请求时间',
      field: 'beginTime',
      render: (val, data) => {
        if (val && data?.endTime) {
          return `${formatDateTime(val)} ~ ${formatDateTime(data.endTime)}`;
        }
        return '';
      },
    },
    {
      label: '请求耗时',
      field: 'duration',
      render: (val) => {
        return val ? `${val} ms` : '';
      },
    },
    {
      label: '操作结果',
      field: 'resultCode',
      render: (val, data) => {
        if (val === 0) {
          return '正常';
        } else if (val > 0 && data?.resultMsg) {
          return `失败 | ${val} | ${data.resultMsg}`;
        }
        return '';
      },
    },
    {
      field: 'operateModule',
      label: '操作模块',
    },
    {
      field: 'operateName',
      label: '操作名',
    },
    {
      field: 'operateType',
      label: '操作类型',
      render: (val) => {
        return h(DictTag, {
          type: DICT_TYPE.INFRA_OPERATE_TYPE,
          value: val,
        });
      },
    },
  ];
}
