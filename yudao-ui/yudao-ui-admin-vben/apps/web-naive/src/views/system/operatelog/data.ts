import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { DescriptionItemSchema } from '#/components/description';

import { formatDateTime } from '@vben/utils';

import { getSimpleUserList } from '#/api/system/user';
import { getRangePickerDefaultProps } from '#/utils';

/** 列表的搜索表单 */
export function useGridFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'userId',
      label: '操作人',
      component: 'ApiSelect',
      componentProps: {
        api: getSimpleUserList,
        labelField: 'nickname',
        valueField: 'id',
        clearable: true,
        placeholder: '请选择操作人员',
      },
    },
    {
      fieldName: 'type',
      label: '操作模块',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入操作模块',
      },
    },
    {
      fieldName: 'subType',
      label: '操作名',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入操作名',
      },
    },
    {
      fieldName: 'action',
      label: '操作内容',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入操作内容',
      },
    },
    {
      fieldName: 'createTime',
      label: '操作时间',
      component: 'DatePicker',
      componentProps: {
        ...getRangePickerDefaultProps(),
        clearable: true,
      },
    },
    {
      fieldName: 'bizId',
      label: '业务编号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入业务编号',
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
      field: 'userName',
      title: '操作人',
      minWidth: 120,
    },
    {
      field: 'type',
      title: '操作模块',
      minWidth: 120,
    },
    {
      field: 'subType',
      title: '操作名',
      minWidth: 160,
    },
    {
      field: 'action',
      title: '操作内容',
      minWidth: 200,
    },
    {
      field: 'createTime',
      title: '操作时间',
      minWidth: 180,
      formatter: 'formatDateTime',
    },
    {
      field: 'bizId',
      title: '业务编号',
      minWidth: 120,
    },
    {
      field: 'userIp',
      title: '操作 IP',
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
      label: '日志编号',
    },
    {
      field: 'traceId',
      label: '链路追踪',
      show: (val) => !val,
    },
    {
      field: 'userId',
      label: '操作人编号',
    },
    {
      field: 'userName',
      label: '操作人名字',
    },
    {
      field: 'userIp',
      label: '操作人 IP',
    },
    {
      field: 'userAgent',
      label: '操作人 UA',
    },
    {
      field: 'type',
      label: '操作模块',
    },
    {
      field: 'subType',
      label: '操作名',
    },
    {
      field: 'action',
      label: '操作内容',
    },
    {
      field: 'extra',
      label: '操作拓展参数',
      show: (val) => !val,
    },
    {
      field: 'requestUrl',
      label: '请求 URL',
      render: (val, data) => {
        if (data?.requestMethod && val) {
          return `${data.requestMethod} ${val}`;
        }
        return '';
      },
    },
    {
      field: 'createTime',
      label: '操作时间',
      render: (val) => formatDateTime(val) as string,
    },
    {
      field: 'bizId',
      label: '业务编号',
    },
  ];
}
