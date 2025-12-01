import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';

import { CommonStatusEnum, DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';

import { z } from '#/adapter/form';
import { getAreaTree } from '#/api/system/area';
import { getSimpleUserList } from '#/api/system/user';
import { getRangePickerDefaultProps } from '#/utils';

/** 新增/修改的表单 */
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
      component: 'Input',
      fieldName: 'name',
      label: '门店名称',
      rules: 'required',
      componentProps: {
        placeholder: '请输入门店名称',
      },
    },
    {
      component: 'Input',
      fieldName: 'phone',
      label: '门店手机',
      rules: 'mobileRequired',
      componentProps: {
        placeholder: '请输入门店手机',
      },
    },
    {
      component: 'ImageUpload',
      fieldName: 'logo',
      label: '门店 logo',
      rules: 'required',
      formItemClass: 'col-span-2',
      componentProps: {
        placeholder: '请上传门店 logo',
      },
      help: '推荐 180x180 图片分辨率',
    },
    {
      component: 'Textarea',
      fieldName: 'introduction',
      label: '门店简介',
      formItemClass: 'col-span-2',
      componentProps: {
        placeholder: '请输入门店简介',
        rows: 4,
      },
    },
    {
      fieldName: 'areaId',
      label: '门店所在地区',
      component: 'ApiTreeSelect',
      rules: 'required',
      componentProps: {
        api: getAreaTree,
        fieldNames: { label: 'name', value: 'id', children: 'children' },
        placeholder: '请选择省市区',
      },
    },
    {
      component: 'Input',
      fieldName: 'detailAddress',
      label: '门店详细地址',
      rules: 'required',
      componentProps: {
        placeholder: '请输入门店详细地址',
      },
    },
    {
      component: 'TimeRangePicker',
      fieldName: 'rangeTime',
      label: '营业时间',
      rules: 'required',
      componentProps: {
        format: 'HH:mm',
      },
    },
    {
      fieldName: 'status',
      label: '门店状态',
      component: 'RadioGroup',
      componentProps: {
        options: getDictOptions(DICT_TYPE.COMMON_STATUS, 'number'),
        buttonStyle: 'solid',
        optionType: 'button',
      },
      rules: z.number().default(CommonStatusEnum.ENABLE),
    },
    {
      component: 'Input',
      fieldName: 'longitude',
      label: '经度',
      rules: 'required',
      componentProps: {
        placeholder: '请输入门店经度',
      },
    },
    {
      component: 'Input',
      fieldName: 'latitude',
      label: '纬度',
      rules: 'required',
      componentProps: {
        placeholder: '请输入门店纬度',
      },
    },
  ];
}

/** 绑定店员的表单 */
export function useBindFormSchema(): VbenFormSchema[] {
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
      component: 'Input',
      fieldName: 'name',
      label: '门店名称',
      dependencies: {
        triggerFields: ['id'],
        disabled: true,
      },
    },
    {
      component: 'ApiSelect',
      fieldName: 'verifyUserIds',
      label: '门店店员',
      rules: 'required',
      componentProps: {
        api: getSimpleUserList,
        labelField: 'nickname',
        valueField: 'id',
        mode: 'tags',
        allowClear: true,
        placeholder: '请选择门店店员',
      },
    },
  ];
}

/** 列表的搜索表单 */
export function useGridFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'phone',
      label: '门店手机',
      component: 'Input',
      componentProps: {
        placeholder: '请输入门店手机',
        allowClear: true,
      },
    },
    {
      fieldName: 'name',
      label: '门店名称',
      component: 'Input',
      componentProps: {
        placeholder: '请输入门店名称',
        allowClear: true,
      },
    },
    {
      fieldName: 'status',
      label: '门店状态',
      component: 'Select',
      componentProps: {
        allowClear: true,
        options: getDictOptions(DICT_TYPE.COMMON_STATUS, 'number'),
        placeholder: '请选择门店状态',
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
      minWidth: 80,
    },
    {
      field: 'logo',
      title: '门店 logo',
      minWidth: 100,
      cellRender: {
        name: 'CellImage',
      },
    },
    {
      field: 'name',
      title: '门店名称',
      minWidth: 150,
    },
    {
      field: 'phone',
      title: '门店手机',
      minWidth: 120,
    },
    {
      field: 'detailAddress',
      title: '地址',
      minWidth: 200,
    },
    {
      field: 'openingTime',
      title: '营业时间',
      minWidth: 160,
      formatter: ({ row }) => {
        return `${row.openingTime} ~ ${row.closingTime}`;
      },
    },
    {
      field: 'status',
      title: '开启状态',
      minWidth: 100,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.COMMON_STATUS },
      },
    },
    {
      field: 'createTime',
      title: '创建时间',
      minWidth: 160,
      formatter: 'formatDateTime',
    },
    {
      title: '操作',
      width: 220,
      fixed: 'right',
      slots: { default: 'actions' },
    },
  ];
}
