import type { DescriptionItemSchema } from '#/components/description';

import { h } from 'vue';

import { DICT_TYPE } from '@vben/constants';
import { erpPriceInputFormatter, formatDateTime } from '@vben/utils';

import { DictTag } from '#/components/dict-tag';

/** 详情页的字段 */
export function useDetailSchema(): DescriptionItemSchema[] {
  return [
    {
      field: 'customerName',
      label: '客户名称',
    },
    {
      field: 'contractNo',
      label: '合同编号',
    },
    {
      field: 'price',
      label: '计划回款金额',
      render: (val) => erpPriceInputFormatter(val),
    },
    {
      field: 'returnTime',
      label: '计划回款日期',
      render: (val) => formatDateTime(val) as string,
    },
    {
      field: 'receivable',
      label: '实际回款金额',
      render: (val) => erpPriceInputFormatter(val?.price ?? 0),
    },
  ];
}

/** 详情页的基础字段 */
export function useDetailBaseSchema(): DescriptionItemSchema[] {
  return [
    {
      field: 'period',
      label: '期数',
    },
    {
      field: 'customerName',
      label: '客户名称',
    },
    {
      field: 'contractNo',
      label: '合同编号',
    },
    {
      field: 'returnTime',
      label: '计划回款日期',
      render: (val) => formatDateTime(val) as string,
    },
    {
      field: 'price',
      label: '计划回款金额',
      render: (val) => erpPriceInputFormatter(val),
    },
    {
      field: 'returnType',
      label: '计划回款方式',
      render: (val) =>
        h(DictTag, {
          type: DICT_TYPE.CRM_RECEIVABLE_RETURN_TYPE,
          value: val,
        }),
    },
    {
      field: 'remindDays',
      label: '提前几天提醒',
    },
    {
      field: 'receivable',
      label: '实际回款金额',
      render: (val) => erpPriceInputFormatter(val ?? 0),
    },
    {
      field: 'receivableRemain',
      label: '未回款金额',
      render: (val, data) => {
        const paid = data?.receivable?.price ?? 0;
        return erpPriceInputFormatter(Math.max(val - paid, 0));
      },
    },
    {
      field: 'receivable.returnTime',
      label: '实际回款日期',
      render: (val) => formatDateTime(val) as string,
    },
    {
      field: 'remark',
      label: '备注',
    },
  ];
}

/** 系统信息字段 */
export function useDetailSystemSchema(): DescriptionItemSchema[] {
  return [
    {
      field: 'ownerUserName',
      label: '负责人',
    },
    {
      field: 'creatorName',
      label: '创建人',
    },
    {
      field: 'createTime',
      label: '创建时间',
      render: (val) => formatDateTime(val) as string,
    },
    {
      field: 'updateTime',
      label: '更新时间',
      render: (val) => formatDateTime(val) as string,
    },
  ];
}
