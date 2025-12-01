import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';

import { DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';
import { useUserStore } from '@vben/stores';

import { getSimpleContactList } from '#/api/crm/contact';
import { getCustomerSimpleList } from '#/api/crm/customer';
import { getAreaTree } from '#/api/system/area';
import { getSimpleUserList } from '#/api/system/user';

/** 新增/修改的表单 */
export function useFormSchema(): VbenFormSchema[] {
  const userStore = useUserStore();
  return [
    {
      fieldName: 'id',
      component: 'Input',
      dependencies: {
        triggerFields: [''],
        show: () => false,
      },
    },
    {
      fieldName: 'name',
      label: '联系人姓名',
      component: 'Input',
      rules: 'required',
      componentProps: {
        placeholder: '请输入联系人姓名',
      },
    },
    {
      fieldName: 'ownerUserId',
      label: '负责人',
      component: 'ApiSelect',
      rules: 'required',
      dependencies: {
        triggerFields: ['id'],
        disabled: (values) => values.id,
      },
      componentProps: {
        api: getSimpleUserList,
        labelField: 'nickname',
        valueField: 'id',
        placeholder: '请选择负责人',
      },
      defaultValue: userStore.userInfo?.id,
    },
    {
      fieldName: 'customerId',
      label: '客户名称',
      component: 'ApiSelect',
      rules: 'required',
      componentProps: {
        api: getCustomerSimpleList,
        labelField: 'name',
        valueField: 'id',
        placeholder: '请选择客户',
      },
    },
    {
      fieldName: 'mobile',
      label: '手机',
      component: 'Input',
      componentProps: {
        placeholder: '请输入手机号',
      },
    },
    {
      fieldName: 'telephone',
      label: '电话',
      component: 'Input',
      componentProps: {
        placeholder: '请输入电话',
      },
    },
    {
      fieldName: 'email',
      label: '邮箱',
      component: 'Input',
      componentProps: {
        placeholder: '请输入邮箱',
      },
    },
    {
      fieldName: 'wechat',
      label: '微信',
      component: 'Input',
      componentProps: {
        placeholder: '请输入微信',
      },
    },
    {
      fieldName: 'qq',
      label: 'QQ',
      component: 'Input',
      componentProps: {
        placeholder: '请输入QQ',
      },
    },
    {
      fieldName: 'post',
      label: '职位',
      component: 'Input',
      componentProps: {
        placeholder: '请输入职位',
      },
    },
    {
      fieldName: 'master',
      label: '关键决策人',
      component: 'RadioGroup',
      componentProps: {
        options: getDictOptions(DICT_TYPE.INFRA_BOOLEAN_STRING, 'boolean'),
        placeholder: '请选择是否关键决策人',
        buttonStyle: 'solid',
        optionType: 'button',
      },
      defaultValue: false,
    },
    {
      fieldName: 'sex',
      label: '性别',
      component: 'Select',
      componentProps: {
        options: getDictOptions(DICT_TYPE.SYSTEM_USER_SEX, 'number'),
        placeholder: '请选择性别',
      },
    },
    {
      fieldName: 'parentId',
      label: '直属上级',
      component: 'ApiSelect',
      componentProps: {
        api: getSimpleContactList,
        labelField: 'name',
        valueField: 'id',
        placeholder: '请选择直属上级',
      },
    },
    {
      fieldName: 'areaId',
      label: '地址',
      component: 'ApiTreeSelect',
      componentProps: {
        api: getAreaTree,
        fieldNames: { label: 'name', value: 'id', children: 'children' },
        placeholder: '请选择地址',
      },
    },
    {
      fieldName: 'detailAddress',
      label: '详细地址',
      component: 'Input',
      componentProps: {
        placeholder: '请输入详细地址',
      },
    },
    {
      fieldName: 'contactNextTime',
      label: '下次联系时间',
      component: 'DatePicker',
      componentProps: {
        showTime: true,
        format: 'YYYY-MM-DD HH:mm:ss',
        valueFormat: 'x',
        placeholder: '请选择下次联系时间',
      },
    },
    {
      fieldName: 'remark',
      label: '备注',
      component: 'Textarea',
      componentProps: {
        placeholder: '请输入备注',
      },
    },
  ];
}

/** 列表的搜索表单 */
export function useGridFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'customerId',
      label: '客户',
      component: 'ApiSelect',
      componentProps: {
        api: getCustomerSimpleList,
        labelField: 'name',
        valueField: 'id',
        placeholder: '请选择客户',
      },
    },
    {
      fieldName: 'name',
      label: '姓名',
      component: 'Input',
      componentProps: {
        placeholder: '请输入联系人姓名',
        allowClear: true,
      },
    },
    {
      fieldName: 'mobile',
      label: '手机号',
      component: 'Input',
      componentProps: {
        placeholder: '请输入手机号',
        allowClear: true,
      },
    },
    {
      fieldName: 'telephone',
      label: '电话',
      component: 'Input',
      componentProps: {
        placeholder: '请输入电话',
        allowClear: true,
      },
    },
    {
      fieldName: 'wechat',
      label: '微信',
      component: 'Input',
      componentProps: {
        placeholder: '请输入微信',
        allowClear: true,
      },
    },
    {
      fieldName: 'email',
      label: '电子邮箱',
      component: 'Input',
      componentProps: {
        placeholder: '请输入电子邮箱',
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
      title: '联系人姓名',
      fixed: 'left',
      minWidth: 240,
      slots: { default: 'name' },
    },
    {
      field: 'customerName',
      title: '客户名称',
      fixed: 'left',
      minWidth: 240,
      slots: { default: 'customerName' },
    },
    {
      field: 'mobile',
      title: '手机',
      minWidth: 120,
    },
    {
      field: 'telephone',
      title: '电话',
      minWidth: 130,
    },
    {
      field: 'email',
      title: '邮箱',
      minWidth: 180,
    },
    {
      field: 'post',
      title: '职位',
      minWidth: 120,
    },
    {
      field: 'areaName',
      title: '地址',
      minWidth: 120,
    },
    {
      field: 'detailAddress',
      title: '详细地址',
      minWidth: 180,
    },
    {
      field: 'master',
      title: '关键决策人',
      minWidth: 120,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.INFRA_BOOLEAN_STRING },
      },
    },
    {
      field: 'parentId',
      title: '直属上级',
      minWidth: 120,
      slots: { default: 'parentId' },
    },
    {
      field: 'contactNextTime',
      title: '下次联系时间',
      formatter: 'formatDateTime',
      minWidth: 180,
    },
    {
      field: 'sex',
      title: '性别',
      minWidth: 120,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.SYSTEM_USER_SEX },
      },
    },
    {
      field: 'remark',
      title: '备注',
      minWidth: 200,
    },
    {
      field: 'contactLastTime',
      title: '最后跟进时间',
      formatter: 'formatDateTime',
      minWidth: 180,
    },
    {
      field: 'ownerUserName',
      title: '负责人',
      minWidth: 120,
    },
    {
      field: 'ownerUserDeptName',
      title: '所属部门',
      minWidth: 120,
    },
    {
      field: 'updateTime',
      title: '更新时间',
      formatter: 'formatDateTime',
      minWidth: 180,
    },
    {
      field: 'createTime',
      title: '创建时间',
      formatter: 'formatDateTime',
      minWidth: 180,
    },
    {
      field: 'creatorName',
      title: '创建人',
      minWidth: 120,
    },
    {
      title: '操作',
      width: 180,
      fixed: 'right',
      slots: { default: 'actions' },
    },
  ];
}
