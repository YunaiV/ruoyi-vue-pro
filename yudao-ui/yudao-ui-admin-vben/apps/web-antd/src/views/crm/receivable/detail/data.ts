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
      field: 'totalPrice',
      label: '合同金额（元）',
      content: (data) =>
        erpPriceInputFormatter(data?.contract?.totalPrice ?? data.totalPrice),
    },
    {
      field: 'returnTime',
      label: '回款日期',
      content: (data) => formatDateTime(data?.returnTime) as string,
    },
    {
      field: 'price',
      label: '回款金额（元）',
      content: (data) => erpPriceInputFormatter(data.price),
    },
    {
      field: 'ownerUserName',
      label: '负责人',
    },
  ];
}

/** 详情页的基础字段 */
export function useDetailBaseSchema(): DescriptionItemSchema[] {
  return [
    {
      field: 'no',
      label: '回款编号',
    },
    {
      field: 'customerName',
      label: '客户名称',
    },
    {
      field: 'contract',
      label: '合同编号',
      content: (data) => data?.contract?.no,
    },
    {
      field: 'returnTime',
      label: '回款日期',
      content: (data) => formatDateTime(data?.returnTime) as string,
    },
    {
      field: 'price',
      label: '回款金额',
      content: (data) => erpPriceInputFormatter(data.price),
    },
    {
      field: 'returnType',
      label: '回款方式',
      content: (data) =>
        h(DictTag, {
          type: DICT_TYPE.CRM_RECEIVABLE_RETURN_TYPE,
          value: data?.returnType,
        }),
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
      content: (data) => formatDateTime(data?.createTime) as string,
    },
    {
      field: 'updateTime',
      label: '更新时间',
      content: (data) => formatDateTime(data?.updateTime) as string,
    },
  ];
}
