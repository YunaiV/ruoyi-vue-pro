import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';

import { DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';
import { useUserStore } from '@vben/stores';
import { erpPriceInputFormatter } from '@vben/utils';

import { getContractSimpleList } from '#/api/crm/contract';
import { getCustomerSimpleList } from '#/api/crm/customer';
import { getSimpleUserList } from '#/api/system/user';

/** 新增/修改的表单 */
export function useFormSchema(): VbenFormSchema[] {
  const userStore = useUserStore();
  return [
    {
      fieldName: 'period',
      label: '期数',
      component: 'Input',
      componentProps: {
        placeholder: '保存时自动生成',
        disabled: true,
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
      label: '客户',
      component: 'ApiSelect',
      rules: 'required',
      componentProps: {
        api: getCustomerSimpleList,
        labelField: 'name',
        valueField: 'id',
        placeholder: '请选择客户',
        allowClear: true,
      },
    },
    {
      fieldName: 'contractId',
      label: '合同',
      component: 'Select',
      rules: 'required',
      componentProps: {
        options: [],
        placeholder: '请选择合同',
        allowClear: true,
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
          const res = await getContractSimpleList(values.customerId);
          return {
            options: res.map((item) => ({
              label: item.name,
              value: item.id,
            })),
            placeholder: '请选择合同',
            onChange: (value: number) => {
              const contract = res.find((item) => item.id === value);
              if (contract) {
                values.price =
                  contract.totalPrice - contract.totalReceivablePrice;
              }
            },
          };
        },
      },
    },
    {
      fieldName: 'price',
      label: '计划回款金额',
      component: 'InputNumber',
      rules: 'required',
      componentProps: {
        placeholder: '请输入计划回款金额',
        min: 0,
        precision: 2,
        controlsPosition: 'right',
        class: '!w-full',
      },
    },
    {
      fieldName: 'returnTime',
      label: '计划回款日期',
      component: 'DatePicker',
      rules: 'required',
      componentProps: {
        placeholder: '请选择计划回款日期',
        valueFormat: 'x',
        format: 'YYYY-MM-DD',
        class: '!w-full',
      },
      defaultValue: new Date(),
    },
    {
      fieldName: 'remindDays',
      label: '提前几天提醒',
      component: 'InputNumber',
      componentProps: {
        placeholder: '请输入提前几天提醒',
        min: 0,
        controlsPosition: 'right',
        class: '!w-full',
      },
    },
    {
      fieldName: 'returnType',
      label: '回款方式',
      component: 'Select',
      componentProps: {
        options: getDictOptions(DICT_TYPE.CRM_RECEIVABLE_RETURN_TYPE, 'number'),
        placeholder: '请选择回款方式',
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
      formItemClass: 'md:col-span-2',
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
        allowClear: true,
      },
    },
    {
      fieldName: 'contractNo',
      label: '合同编号',
      component: 'Input',
      componentProps: {
        placeholder: '请输入合同编号',
        allowClear: true,
      },
    },
  ];
}

export function useGridColumns(): VxeTableGridOptions['columns'] {
  return [
    {
      title: '客户名称',
      field: 'customerName',
      minWidth: 150,
      fixed: 'left',
      slots: { default: 'customerName' },
    },
    {
      title: '合同编号',
      field: 'contractNo',
      minWidth: 200,
    },
    {
      title: '期数',
      field: 'period',
      minWidth: 150,
      slots: { default: 'period' },
    },
    {
      title: '计划回款金额（元）',
      field: 'price',
      minWidth: 160,
      formatter: 'formatAmount2',
    },
    {
      title: '计划回款日期',
      field: 'returnTime',
      minWidth: 180,
      formatter: 'formatDateTime',
    },
    {
      title: '提前几天提醒',
      field: 'remindDays',
      minWidth: 150,
    },
    {
      title: '提醒日期',
      field: 'remindTime',
      minWidth: 180,
      formatter: 'formatDateTime',
    },
    {
      title: '回款方式',
      field: 'returnType',
      minWidth: 130,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.CRM_RECEIVABLE_RETURN_TYPE },
      },
    },
    {
      title: '备注',
      field: 'remark',
      minWidth: 120,
    },
    {
      title: '负责人',
      field: 'ownerUserName',
      minWidth: 120,
    },
    {
      title: '实际回款金额（元）',
      field: 'receivable.price',
      minWidth: 160,
      formatter: 'formatAmount2',
    },
    {
      title: '实际回款日期',
      field: 'receivable.returnTime',
      minWidth: 180,
      formatter: 'formatDateTime',
    },
    {
      title: '未回款金额（元）',
      field: 'unpaidPrice',
      minWidth: 160,
      formatter: ({ row }) => {
        if (row.receivable) {
          return erpPriceInputFormatter(row.price - row.receivable.price);
        }
        return erpPriceInputFormatter(row.price);
      },
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
      minWidth: 100,
    },
    {
      title: '操作',
      field: 'actions',
      width: 220,
      fixed: 'right',
      slots: { default: 'actions' },
    },
  ];
}
