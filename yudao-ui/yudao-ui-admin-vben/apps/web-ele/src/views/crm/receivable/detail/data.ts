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
      render: (val, data) =>
        erpPriceInputFormatter(val ?? data?.contract?.totalPrice ?? 0),
    },
    {
      field: 'returnTime',
      label: '回款日期',
      render: (val) => formatDateTime(val) as string,
    },
    {
      field: 'price',
      label: '回款金额（元）',
      render: (val) => erpPriceInputFormatter(val),
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
      render: (val, data) =>
        val && data?.contract?.no ? data?.contract?.no : '',
    },
    {
      field: 'returnTime',
      label: '回款日期',
      render: (val) => formatDateTime(val) as string,
    },
    {
      field: 'price',
      label: '回款金额',
      render: (val) => erpPriceInputFormatter(val),
    },
    {
      field: 'returnType',
      label: '回款方式',
      render: (val) =>
        h(DictTag, {
          type: DICT_TYPE.CRM_RECEIVABLE_RETURN_TYPE,
          value: val,
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
      render: (val) => formatDateTime(val) as string,
    },
    {
      field: 'updateTime',
      label: '更新时间',
      render: (val) => formatDateTime(val) as string,
    },
  ];
}
