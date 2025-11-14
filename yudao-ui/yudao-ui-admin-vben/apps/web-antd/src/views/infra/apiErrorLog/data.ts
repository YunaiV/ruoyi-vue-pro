import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { InfraApiErrorLogApi } from '#/api/infra/api-error-log';
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
      content: (data: InfraApiErrorLogApi.ApiErrorLog) => {
        return h(DictTag, {
          type: DICT_TYPE.USER_TYPE,
          value: data.userType,
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
      content: (data: InfraApiErrorLogApi.ApiErrorLog) => {
        if (data?.requestMethod && data?.requestUrl) {
          return `${data.requestMethod} ${data.requestUrl}`;
        }
        return '';
      },
    },
    {
      field: 'requestParams',
      label: '请求参数',
      content: (data: InfraApiErrorLogApi.ApiErrorLog) => {
        if (data.requestParams) {
          return h(JsonViewer, {
            value: JSON.parse(data.requestParams),
            previewMode: true,
          });
        }
        return '';
      },
    },
    {
      field: 'exceptionTime',
      label: '异常时间',
      content: (data: InfraApiErrorLogApi.ApiErrorLog) => {
        return formatDateTime(data?.exceptionTime || '') as string;
      },
    },
    {
      field: 'exceptionName',
      label: '异常名',
    },
    {
      field: 'exceptionStackTrace',
      label: '异常堆栈',
      hidden: (data: InfraApiErrorLogApi.ApiErrorLog) =>
        !data?.exceptionStackTrace,
      content: (data: InfraApiErrorLogApi.ApiErrorLog) => {
        if (data?.exceptionStackTrace) {
          return h('textarea', {
            value: data.exceptionStackTrace,
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
      content: (data: InfraApiErrorLogApi.ApiErrorLog) => {
        return h(DictTag, {
          type: DICT_TYPE.INFRA_API_ERROR_LOG_PROCESS_STATUS,
          value: data?.processStatus,
        });
      },
    },
    {
      field: 'processUserId',
      label: '处理人',
      hidden: (data: InfraApiErrorLogApi.ApiErrorLog) => !data?.processUserId,
    },
    {
      field: 'processTime',
      label: '处理时间',
      hidden: (data: InfraApiErrorLogApi.ApiErrorLog) => !data?.processTime,
      content: (data: InfraApiErrorLogApi.ApiErrorLog) => {
        return formatDateTime(data?.processTime || '') as string;
      },
    },
  ];
}
