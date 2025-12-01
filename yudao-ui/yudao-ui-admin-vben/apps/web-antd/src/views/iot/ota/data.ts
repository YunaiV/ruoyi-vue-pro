import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';

import { getSimpleProductList } from '#/api/iot/product/product';
import { getRangePickerDefaultProps } from '#/utils';

/** 新增/修改固件的表单 */
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
      label: '固件名称',
      component: 'Input',
      componentProps: {
        placeholder: '请输入固件名称',
      },
      rules: 'required',
    },
    {
      fieldName: 'productId',
      label: '所属产品',
      component: 'ApiSelect',
      componentProps: {
        api: getSimpleProductList,
        labelField: 'name',
        valueField: 'id',
        placeholder: '请选择产品',
      },
      rules: 'required',
    },
    {
      fieldName: 'version',
      label: '版本号',
      component: 'Input',
      componentProps: {
        placeholder: '请输入版本号',
      },
      rules: 'required',
    },
    {
      fieldName: 'description',
      label: '固件描述',
      component: 'Textarea',
      componentProps: {
        placeholder: '请输入固件描述',
        rows: 3,
      },
    },
    {
      fieldName: 'fileUrl',
      label: '固件文件',
      component: 'Upload',
      componentProps: {
        maxCount: 1,
        accept: '.bin,.hex,.zip',
      },
      rules: 'required',
      help: '支持上传 .bin、.hex、.zip 格式的固件文件',
    },
  ];
}

/** 列表的搜索表单 */
export function useGridFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'name',
      label: '固件名称',
      component: 'Input',
      componentProps: {
        placeholder: '请输入固件名称',
        allowClear: true,
      },
    },
    {
      fieldName: 'productId',
      label: '产品',
      component: 'ApiSelect',
      componentProps: {
        api: getSimpleProductList,
        labelField: 'name',
        valueField: 'id',
        placeholder: '请选择产品',
        allowClear: true,
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
      type: 'checkbox',
      width: 50,
      fixed: 'left',
    },
    {
      field: 'id',
      title: '固件编号',
      width: 100,
    },
    {
      field: 'name',
      title: '固件名称',
      minWidth: 150,
    },
    {
      field: 'version',
      title: '版本号',
      width: 120,
    },
    {
      field: 'productName',
      title: '所属产品',
      minWidth: 150,
    },
    {
      field: 'description',
      title: '固件描述',
      minWidth: 200,
      showOverflow: 'tooltip',
    },
    {
      field: 'fileSize',
      title: '文件大小',
      width: 120,
      formatter: ({ cellValue }) => {
        if (!cellValue) return '-';
        const kb = cellValue / 1024;
        if (kb < 1024) return `${kb.toFixed(2)} KB`;
        return `${(kb / 1024).toFixed(2)} MB`;
      },
    },
    {
      field: 'status',
      title: '状态',
      width: 100,
      formatter: ({ cellValue }) => {
        return cellValue === 1 ? '启用' : '禁用';
      },
    },
    {
      field: 'createTime',
      title: '创建时间',
      width: 180,
      formatter: 'formatDateTime',
    },
    {
      title: '操作',
      width: 160,
      fixed: 'right',
      slots: { default: 'actions' },
    },
  ];
}
