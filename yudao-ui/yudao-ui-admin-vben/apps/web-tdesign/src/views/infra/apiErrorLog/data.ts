import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { DescriptionItemSchema } from '#/components/description';

import { h } from 'vue';

import { JsonViewer } from '@vben/common-ui';
import { DICT_TYPE, InfraApiErrorLogProcessStatusEnum } from '@vben/constants';
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
      fieldName: 'exceptionTime',
      label: '异常时间',
      component: 'RangePicker',
      componentProps: {
        ...getRangePickerDefaultProps(),
        allowClear: true,
      },
    },
    {
      fieldName: 'processStatus',
      label: '处理状态',
      component: 'Select',
      componentProps: {
        options: getDictOptions(
          DICT_TYPE.INFRA_API_ERROR_LOG_PROCESS_STATUS,
          'number',
        ),
        allowClear: true,
        placeholder: '请选择处理状态',
      },
      defaultValue: InfraApiErrorLogProcessStatusEnum.INIT,
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
      minWidth: 200,
    },
    {
      field: 'exceptionTime',
      title: '异常发生时间',
      minWidth: 180,
      formatter: 'formatDateTime',
    },
    {
      field: 'exceptionName',
      title: '异常名',
      minWidth: 180,
    },
    {
      field: 'processStatus',
      title: '处理状态',
      minWidth: 120,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.INFRA_API_ERROR_LOG_PROCESS_STATUS },
      },
    },
    {
      title: '操作',
      minWidth: 220,
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
      field: 'exceptionTime',
      label: '异常时间',
      render: (val) => {
        return formatDateTime(val) as string;
      },
    },
    {
      field: 'exceptionName',
      label: '异常名',
    },
    {
      field: 'exceptionStackTrace',
      label: '异常堆栈',
      show: (val) => !val,
      render: (val) => {
        if (val) {
          return h('textarea', {
            value: val,
            style:
              'width: 100%; min-height: 200px; max-height: 400px; resize: vertical;',
            readonly: true,
          });
        }
        return '';
      },
    },
    {
      field: 'processStatus',
      label: '处理状态',
      render: (val) => {
        return h(DictTag, {
          type: DICT_TYPE.INFRA_API_ERROR_LOG_PROCESS_STATUS,
          value: val,
        });
      },
    },
    {
      field: 'processUserId',
      label: '处理人',
      show: (val) => !val,
    },
    {
      field: 'processTime',
      label: '处理时间',
      show: (val) => !val,
      render: (val) => {
        return formatDateTime(val) as string;
      },
    },
  ];
}
