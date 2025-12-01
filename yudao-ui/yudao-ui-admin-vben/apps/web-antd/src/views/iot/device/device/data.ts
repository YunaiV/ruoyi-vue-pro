import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';

import { DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';

import { z } from '#/adapter/form';
import { getSimpleDeviceList } from '#/api/iot/device/device';
import { getSimpleDeviceGroupList } from '#/api/iot/device/group';
import {
  DeviceTypeEnum,
  getSimpleProductList,
} from '#/api/iot/product/product';

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
      fieldName: 'productId',
      label: '产品',
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
      fieldName: 'deviceName',
      label: 'DeviceName',
      component: 'Input',
      componentProps: {
        placeholder: '请输入 DeviceName',
      },
      rules: z
        .string()
        .min(4, 'DeviceName 长度不能少于 4 个字符')
        .max(32, 'DeviceName 长度不能超过 32 个字符')
        .regex(
          /^[\w.\-:@]{4,32}$/,
          '支持英文字母、数字、下划线（_）、中划线（-）、点号（.）、半角冒号（:）和特殊字符@',
        ),
    },
    {
      fieldName: 'gatewayId',
      label: '网关设备',
      component: 'ApiSelect',
      componentProps: {
        api: () => getSimpleDeviceList(DeviceTypeEnum.GATEWAY),
        labelField: 'nickname',
        valueField: 'id',
        placeholder: '子设备可选择父设备',
      },
      dependencies: {
        triggerFields: ['deviceType'],
        show: (values) => values.deviceType === 1, // GATEWAY_SUB
      },
    },
    {
      fieldName: 'nickname',
      label: '备注名称',
      component: 'Input',
      componentProps: {
        placeholder: '请输入备注名称',
      },
      rules: z
        .string()
        .min(4, '备注名称长度限制为 4~64 个字符')
        .max(64, '备注名称长度限制为 4~64 个字符')
        .regex(
          /^[\u4E00-\u9FA5\u3040-\u30FF\w]+$/,
          '备注名称只能包含中文、英文字母、日文、数字和下划线（_）',
        )
        .optional()
        .or(z.literal('')),
    },
    {
      fieldName: 'groupIds',
      label: '设备分组',
      component: 'ApiSelect',
      componentProps: {
        api: getSimpleDeviceGroupList,
        labelField: 'name',
        valueField: 'id',
        mode: 'multiple',
        placeholder: '请选择设备分组',
      },
    },
    {
      fieldName: 'serialNumber',
      label: '设备序列号',
      component: 'Input',
      componentProps: {
        placeholder: '请输入设备序列号',
      },
      rules: z
        .string()
        .regex(/^[\w-]+$/, '序列号只能包含字母、数字、中划线和下划线')
        .optional()
        .or(z.literal('')),
    },
    {
      fieldName: 'locationType',
      label: '定位类型',
      component: 'RadioGroup',
      componentProps: {
        options: getDictOptions(DICT_TYPE.IOT_LOCATION_TYPE, 'number'),
        buttonStyle: 'solid',
        optionType: 'button',
      },
    },
    {
      fieldName: 'longitude',
      label: '设备经度',
      component: 'InputNumber',
      componentProps: {
        placeholder: '请输入设备经度',
        class: 'w-full',
      },
      dependencies: {
        triggerFields: ['locationType'],
        show: (values) => values.locationType === 3, // MANUAL
      },
    },
    {
      fieldName: 'latitude',
      label: '设备维度',
      component: 'InputNumber',
      componentProps: {
        placeholder: '请输入设备维度',
        class: 'w-full',
      },
      dependencies: {
        triggerFields: ['locationType'],
        show: (values) => values.locationType === 3, // MANUAL
      },
    },
  ];
}

/** 设备分组表单 */
export function useGroupFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'groupIds',
      label: '设备分组',
      component: 'ApiSelect',
      componentProps: {
        api: getSimpleDeviceGroupList,
        labelField: 'name',
        valueField: 'id',
        mode: 'multiple',
        placeholder: '请选择设备分组',
      },
      rules: 'required',
    },
  ];
}

/** 设备导入表单 */
export function useImportFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'file',
      label: '设备数据',
      component: 'Upload',
      rules: 'required',
      help: '仅允许导入 xls、xlsx 格式文件',
    },
    {
      fieldName: 'updateSupport',
      label: '是否覆盖',
      component: 'Switch',
      componentProps: {
        checkedChildren: '是',
        unCheckedChildren: '否',
      },
      rules: z.boolean().default(false),
      help: '是否更新已经存在的设备数据',
    },
  ];
}

/** 列表的搜索表单 */
export function useGridFormSchema(): VbenFormSchema[] {
  return [
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
      fieldName: 'deviceName',
      label: 'DeviceName',
      component: 'Input',
      componentProps: {
        placeholder: '请输入 DeviceName',
        allowClear: true,
      },
    },
    {
      fieldName: 'nickname',
      label: '备注名称',
      component: 'Input',
      componentProps: {
        placeholder: '请输入备注名称',
        allowClear: true,
      },
    },
    {
      fieldName: 'deviceType',
      label: '设备类型',
      component: 'Select',
      componentProps: {
        options: getDictOptions(DICT_TYPE.IOT_PRODUCT_DEVICE_TYPE, 'number'),
        placeholder: '请选择设备类型',
        allowClear: true,
      },
    },
    {
      fieldName: 'status',
      label: '设备状态',
      component: 'Select',
      componentProps: {
        options: getDictOptions(DICT_TYPE.IOT_DEVICE_STATE, 'number'),
        placeholder: '请选择设备状态',
        allowClear: true,
      },
    },
    {
      fieldName: 'groupId',
      label: '设备分组',
      component: 'ApiSelect',
      componentProps: {
        api: getSimpleDeviceGroupList,
        labelField: 'name',
        valueField: 'id',
        placeholder: '请选择设备分组',
        allowClear: true,
      },
    },
  ];
}

/** 列表的字段 */
export function useGridColumns(): VxeTableGridOptions['columns'] {
  return [
    { type: 'checkbox', width: 40 },
    {
      field: 'deviceName',
      title: 'DeviceName',
      minWidth: 150,
    },
    {
      field: 'nickname',
      title: '备注名称',
      minWidth: 120,
    },
    {
      field: 'productId',
      title: '所属产品',
      minWidth: 120,
      slots: { default: 'product' },
    },
    {
      field: 'deviceType',
      title: '设备类型',
      minWidth: 100,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.IOT_PRODUCT_DEVICE_TYPE },
      },
    },
    {
      field: 'groupIds',
      title: '所属分组',
      minWidth: 150,
      slots: { default: 'groups' },
    },
    {
      field: 'state',
      title: '设备状态',
      minWidth: 100,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.IOT_DEVICE_STATE },
      },
    },
    {
      field: 'onlineTime',
      title: '最后上线时间',
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
