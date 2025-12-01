import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { DescriptionItemSchema } from '#/components/description';

import { h } from 'vue';

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
        allowClear: true,
        options: getDictOptions(DICT_TYPE.USER_TYPE, 'number'),
        placeholder: '请选择用户类型',
      },
    },
    {
      fieldName: 'templateCode',
      label: '模板编码',
      component: 'Input',
      componentProps: {
        allowClear: true,
        placeholder: '请输入模板编码',
      },
    },
    {
      fieldName: 'templateType',
      label: '模版类型',
      component: 'Select',
      componentProps: {
        options: getDictOptions(
          DICT_TYPE.SYSTEM_NOTIFY_TEMPLATE_TYPE,
          'number',
        ),
        allowClear: true,
        placeholder: '请选择模版类型',
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
      field: 'userType',
      title: '用户类型',
      minWidth: 120,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.USER_TYPE },
      },
    },
    {
      field: 'userId',
      title: '用户编号',
      minWidth: 100,
    },
    {
      field: 'templateCode',
      title: '模板编码',
      minWidth: 120,
    },
    {
      field: 'templateNickname',
      title: '发送人名称',
      minWidth: 180,
    },
    {
      field: 'templateContent',
      title: '模版内容',
      minWidth: 200,
    },
    {
      field: 'templateParams',
      title: '模版参数',
      minWidth: 180,
      formatter: ({ cellValue }) => {
        try {
          return JSON.stringify(cellValue);
        } catch {
          return '';
        }
      },
    },
    {
      field: 'templateType',
      title: '模版类型',
      minWidth: 120,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.SYSTEM_NOTIFY_TEMPLATE_TYPE },
      },
    },
    {
      field: 'readStatus',
      title: '是否已读',
      minWidth: 100,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.INFRA_BOOLEAN_STRING },
      },
    },
    {
      field: 'readTime',
      title: '阅读时间',
      minWidth: 180,
      formatter: 'formatDateTime',
    },
    {
      field: 'createTime',
      title: '创建时间',
      minWidth: 180,
      formatter: 'formatDateTime',
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
      field: 'userId',
      label: '用户编号',
    },
    {
      field: 'templateId',
      label: '模版编号',
    },
    {
      field: 'templateCode',
      label: '模板编码',
    },
    {
      field: 'templateNickname',
      label: '发送人名称',
    },
    {
      field: 'templateContent',
      label: '模版内容',
    },
    {
      field: 'templateParams',
      label: '模版参数',
      render: (val) => {
        try {
          return JSON.stringify(val);
        } catch {
          return '';
        }
      },
    },
    {
      field: 'templateType',
      label: '模版类型',
      render: (val) => {
        return h(DictTag, {
          type: DICT_TYPE.SYSTEM_NOTIFY_TEMPLATE_TYPE,
          value: val,
        });
      },
    },
    {
      field: 'readStatus',
      label: '是否已读',
      render: (val) => {
        return h(DictTag, {
          type: DICT_TYPE.INFRA_BOOLEAN_STRING,
          value: val,
        });
      },
    },
    {
      field: 'readTime',
      label: '阅读时间',
      render: (val) => formatDateTime(val) as string,
    },
    {
      field: 'createTime',
      label: '创建时间',
      render: (val) => formatDateTime(val) as string,
    },
  ];
}
