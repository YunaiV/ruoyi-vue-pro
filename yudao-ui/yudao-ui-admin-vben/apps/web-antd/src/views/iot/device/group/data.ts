import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';

import { DICT_TYPE } from '@vben/constants';

import { z } from '#/adapter/form';
import { getSimpleDeviceGroupList } from '#/api/iot/device/group';

/** 新增/修改设备分组的表单 */
export function useFormSchema(): VbenFormSchema[] {
  return [
    {
      component: 'Input',
      fieldName: 'id',
      dependencies: {
        triggerFields: [''],
        show: () => false,
      },
    },
    {
      fieldName: 'name',
      label: '分组名称',
      component: 'Input',
      componentProps: {
        placeholder: '请输入分组名称',
      },
      rules: z
        .string()
        .min(1, '分组名称不能为空')
        .max(64, '分组名称长度不能超过 64 个字符'),
    },
    {
      fieldName: 'parentId',
      label: '父级分组',
      component: 'ApiTreeSelect',
      componentProps: {
        api: getSimpleDeviceGroupList,
        fieldNames: {
          label: 'name',
          value: 'id',
        },
        placeholder: '请选择父级分组',
        allowClear: true,
      },
    },
    {
      fieldName: 'description',
      label: '分组描述',
      component: 'Textarea',
      componentProps: {
        placeholder: '请输入分组描述',
        rows: 3,
      },
    },
  ];
}

/** 列表的搜索表单 */
export function useGridFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'name',
      label: '分组名称',
      component: 'Input',
      componentProps: {
        placeholder: '请输入分组名称',
        allowClear: true,
      },
    },
  ];
}

/** 列表的字段 */
export function useGridColumns(): VxeTableGridOptions['columns'] {
  return [
    {
      field: 'name',
      title: '分组名称',
      minWidth: 200,
      treeNode: true,
    },
    {
      field: 'description',
      title: '分组描述',
      minWidth: 200,
    },
    {
      field: 'status',
      title: '状态',
      width: 100,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.COMMON_STATUS },
      },
    },
    {
      field: 'deviceCount',
      title: '设备数量',
      minWidth: 100,
    },
    {
      field: 'createTime',
      title: '创建时间',
      minWidth: 180,
      formatter: 'formatDateTime',
    },
    {
      title: '操作',
      width: 200,
      fixed: 'right',
      slots: { default: 'actions' },
    },
  ];
}
