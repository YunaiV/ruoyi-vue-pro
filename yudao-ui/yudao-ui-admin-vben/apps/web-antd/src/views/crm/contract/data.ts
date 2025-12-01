import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';

import { DICT_TYPE } from '@vben/constants';
import { useUserStore } from '@vben/stores';
import { erpPriceInputFormatter, erpPriceMultiply } from '@vben/utils';

import { z } from '#/adapter/form';
import { getSimpleBusinessList } from '#/api/crm/business';
import { getSimpleContactList } from '#/api/crm/contact';
import { getCustomerSimpleList } from '#/api/crm/customer';
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
      fieldName: 'no',
      label: '合同编号',
      component: 'Input',
      componentProps: {
        placeholder: '保存时自动生成',
        disabled: true,
      },
    },
    {
      fieldName: 'name',
      label: '合同名称',
      component: 'Input',
      rules: 'required',
      componentProps: {
        placeholder: '请输入合同名称',
      },
    },
    {
      fieldName: 'ownerUserId',
      label: '负责人',
      component: 'ApiSelect',
      componentProps: {
        api: getSimpleUserList,
        labelField: 'nickname',
        valueField: 'id',
      },
      dependencies: {
        triggerFields: ['id'],
        disabled: (values) => values.id,
      },
      defaultValue: userStore.userInfo?.id,
      rules: 'required',
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
      fieldName: 'businessId',
      label: '商机名称',
      component: 'Select',
      componentProps: {
        options: [],
        placeholder: '请选择商机',
      },
      dependencies: {
        triggerFields: ['customerId'],
        disabled: (values) => !values.customerId,
        async componentProps(values) {
          if (!values.customerId) {
            return {
              options: [],
              placeholder: '请选择客户',
            };
          }
          const res = await getSimpleBusinessList();
          const list = res.filter(
            (item) => item.customerId === values.customerId,
          );
          return {
            options: list.map((item) => ({
              label: item.name,
              value: item.id,
            })),
            placeholder: '请选择商机',
          };
        },
      },
    },
    {
      fieldName: 'orderDate',
      label: '下单日期',
      component: 'DatePicker',
      rules: 'required',
      componentProps: {
        showTime: false,
        format: 'YYYY-MM-DD',
        valueFormat: 'x',
        placeholder: '请选择下单日期',
      },
    },
    {
      fieldName: 'startTime',
      label: '合同开始时间',
      component: 'DatePicker',
      componentProps: {
        showTime: false,
        format: 'YYYY-MM-DD',
        valueFormat: 'x',
        placeholder: '请选择合同开始时间',
      },
    },
    {
      fieldName: 'endTime',
      label: '合同结束时间',
      component: 'DatePicker',
      componentProps: {
        showTime: false,
        format: 'YYYY-MM-DD',
        valueFormat: 'x',
        placeholder: '请选择合同结束时间',
      },
    },
    {
      fieldName: 'signUserId',
      label: '公司签约人',
      component: 'ApiSelect',
      componentProps: {
        api: getSimpleUserList,
        labelField: 'nickname',
        valueField: 'id',
      },
      defaultValue: userStore.userInfo?.id,
    },
    {
      fieldName: 'signContactId',
      label: '客户签约人',
      component: 'Select',
      componentProps: {
        options: [],
        placeholder: '请选择客户签约人',
      },
      dependencies: {
        triggerFields: ['customerId'],
        disabled: (values) => !values.customerId,
        async componentProps(values) {
          if (!values.customerId) {
            return {
              options: [],
              placeholder: '请选择客户',
            };
          }
          const res = await getSimpleContactList();
          const list = res.filter(
            (item) => item.customerId === values.customerId,
          );
          return {
            options: list.map((item) => ({
              label: item.name,
              value: item.id,
            })),
            placeholder: '请选择客户签约人',
          };
        },
      },
    },
    {
      fieldName: 'remark',
      label: '备注',
      component: 'Textarea',
      componentProps: {
        placeholder: '请输入备注',
        rows: 4,
      },
    },
    {
      fieldName: 'product',
      label: '产品清单',
      component: 'Input',
      formItemClass: 'col-span-3',
    },
    {
      fieldName: 'totalProductPrice',
      label: '产品总金额',
      component: 'InputNumber',
      componentProps: {
        min: 0,
        precision: 2,
        placeholder: '请输入产品总金额',
      },
      rules: z.number().min(0).optional().default(0),
    },
    {
      fieldName: 'discountPercent',
      label: '整单折扣（%）',
      component: 'InputNumber',
      componentProps: {
        min: 0,
        precision: 2,
        placeholder: '请输入整单折扣',
      },
      rules: z.number().min(0).max(100).optional().default(0),
    },
    {
      fieldName: 'totalPrice',
      label: '折扣后金额',
      component: 'InputNumber',
      componentProps: {
        min: 0,
        precision: 2,
        disabled: true,
      },
      dependencies: {
        triggerFields: ['totalProductPrice', 'discountPercent'],
        trigger(values, form) {
          const discountPrice =
            erpPriceMultiply(
              values.totalProductPrice,
              values.discountPercent / 100,
            ) ?? 0;
          form.setFieldValue(
            'totalPrice',
            values.totalProductPrice - discountPrice,
          );
        },
      },
    },
  ];
}

/** 列表的搜索表单 */
export function useGridFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'no',
      label: '合同编号',
      component: 'Input',
      componentProps: {
        placeholder: '请输入合同编号',
        allowClear: true,
      },
    },
    {
      fieldName: 'name',
      label: '合同名称',
      component: 'Input',
      componentProps: {
        placeholder: '请输入合同名称',
        allowClear: true,
      },
    },
    {
      fieldName: 'customerId',
      label: '客户',
      component: 'ApiSelect',
      componentProps: {
        api: getCustomerSimpleList,
        labelField: 'name',
        valueField: 'id',
        placeholder: '请选择客户',
        allowClear: true,
      },
    },
  ];
}

export function useGridColumns(): VxeTableGridOptions['columns'] {
  return [
    {
      title: '合同编号',
      field: 'no',
      minWidth: 180,
      fixed: 'left',
    },
    {
      title: '合同名称',
      field: 'name',
      minWidth: 160,
      fixed: 'left',
      slots: { default: 'name' },
    },
    {
      title: '客户名称',
      field: 'customerName',
      minWidth: 120,
      slots: { default: 'customerName' },
    },
    {
      title: '商机名称',
      field: 'businessName',
      minWidth: 130,
      slots: { default: 'businessName' },
    },
    {
      title: '合同金额（元）',
      field: 'totalPrice',
      minWidth: 140,
      formatter: 'formatAmount2',
    },
    {
      title: '下单时间',
      field: 'orderDate',
      minWidth: 120,
      formatter: 'formatDateTime',
    },
    {
      title: '合同开始时间',
      field: 'startTime',
      minWidth: 120,
      formatter: 'formatDateTime',
    },
    {
      title: '合同结束时间',
      field: 'endTime',
      minWidth: 120,
      formatter: 'formatDateTime',
    },
    {
      title: '客户签约人',
      field: 'signContactName',
      minWidth: 130,
      slots: { default: 'signContactName' },
    },
    {
      title: '公司签约人',
      field: 'signUserName',
      minWidth: 130,
    },
    {
      title: '备注',
      field: 'remark',
      minWidth: 200,
    },
    {
      title: '已回款金额（元）',
      field: 'totalReceivablePrice',
      minWidth: 140,
      formatter: 'formatAmount2',
    },
    {
      title: '未回款金额（元）',
      field: 'unReceivablePrice',
      minWidth: 140,
      formatter: ({ row }) => {
        return erpPriceInputFormatter(
          row.totalPrice - row.totalReceivablePrice,
        );
      },
    },
    {
      title: '最后跟进时间',
      field: 'contactLastTime',
      minWidth: 180,
      formatter: 'formatDateTime',
    },
    {
      title: '负责人',
      field: 'ownerUserName',
      minWidth: 120,
    },
    {
      title: '所属部门',
      field: 'ownerUserDeptName',
      minWidth: 100,
    },
    {
      title: '更新时间',
      field: 'updateTime',
      minWidth: 180,
      formatter: 'formatDateTime',
    },
    {
      title: '创建时间',
      field: 'createTime',
      minWidth: 180,
      formatter: 'formatDateTime',
    },
    {
      title: '创建人',
      field: 'creatorName',
      minWidth: 120,
    },
    {
      title: '合同状态',
      field: 'auditStatus',
      fixed: 'right',
      minWidth: 120,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.CRM_AUDIT_STATUS },
      },
    },
    {
      title: '操作',
      field: 'actions',
      fixed: 'right',
      minWidth: 130,
      slots: { default: 'actions' },
    },
  ];
}
