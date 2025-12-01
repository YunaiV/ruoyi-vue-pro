import type { DescriptionItemSchema } from '#/components/description';

import { h } from 'vue';

import { DICT_TYPE } from '@vben/constants';
import { erpPriceInputFormatter } from '@vben/utils';

import { DictTag } from '#/components/dict-tag';

/** 详情页的字段 */
export function useDetailSchema(): DescriptionItemSchema[] {
  return [
    {
      field: 'categoryName',
      label: '产品类别',
    },
    {
      field: 'unit',
      label: '产品单位',
      render: (val) =>
        h(DictTag, { type: DICT_TYPE.CRM_PRODUCT_UNIT, value: val }),
    },
    {
      field: 'price',
      label: '产品价格（元）',
      render: (val) => erpPriceInputFormatter(val),
    },
    {
      field: 'no',
      label: '产品编码',
    },
  ];
}

/** 详情页的基础字段 */
export function useDetailBaseSchema(): DescriptionItemSchema[] {
  return [
    {
      field: 'name',
      label: '产品名称',
    },
    {
      field: 'no',
      label: '产品编码',
    },
    {
      field: 'price',
      label: '价格（元）',
      render: (val) => erpPriceInputFormatter(val),
    },
    {
      field: 'description',
      label: '产品描述',
    },
    {
      field: 'categoryName',
      label: '产品类型',
    },
    {
      field: 'status',
      label: '是否上下架',
      render: (val) =>
        h(DictTag, { type: DICT_TYPE.CRM_PRODUCT_STATUS, value: val }),
    },
    {
      field: 'unit',
      label: '产品单位',
      render: (val) =>
        h(DictTag, { type: DICT_TYPE.CRM_PRODUCT_UNIT, value: val }),
    },
  ];
}
