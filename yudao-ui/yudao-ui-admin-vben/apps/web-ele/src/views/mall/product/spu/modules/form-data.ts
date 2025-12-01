import type { VbenFormSchema } from '#/adapter/form';

import { DeliveryTypeEnum, DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';
import { handleTree } from '@vben/utils';

import { z } from '#/adapter/form';
import { getSimpleBrandList } from '#/api/mall/product/brand';
import { getCategoryList } from '#/api/mall/product/category';
import { getSimpleTemplateList } from '#/api/mall/trade/delivery/expressTemplate';

/** 基础设置的表单 */
export function useInfoFormSchema(): VbenFormSchema[] {
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
      label: '商品名称',
      component: 'Input',
      componentProps: {
        allowClear: true,
        placeholder: '请输入商品名称',
      },
      rules: 'required',
    },
    {
      fieldName: 'categoryId',
      label: '分类名称',
      // component: 'ApiCascader',
      component: 'ApiTreeSelect',
      componentProps: {
        api: async () => {
          const data = await getCategoryList({});
          return handleTree(data);
        },
        fieldNames: { label: 'name', value: 'id', children: 'children' },
        placeholder: '请选择商品分类',
      },
      rules: 'required',
    },
    {
      fieldName: 'brandId',
      label: '商品品牌',
      component: 'ApiSelect',
      componentProps: {
        api: getSimpleBrandList,
        labelField: 'name',
        valueField: 'id',
        allowClear: true,
        placeholder: '请选择商品品牌',
      },
      rules: 'required',
    },
    {
      fieldName: 'keyword',
      label: '商品关键字',
      component: 'Input',
      componentProps: {
        placeholder: '请输入商品关键字',
      },
      rules: 'required',
    },
    {
      fieldName: 'introduction',
      label: '商品简介',
      component: 'Textarea',
      componentProps: {
        placeholder: '请输入商品简介',
        autoSize: { minRows: 2, maxRows: 2 },
        showCount: true,
        maxlength: 128,
        allowClear: true,
      },
      rules: 'required',
    },
    {
      fieldName: 'picUrl',
      label: '商品封面图',
      component: 'ImageUpload',
      componentProps: {
        maxSize: 30,
      },
      rules: 'required',
    },
    {
      fieldName: 'sliderPicUrls',
      label: '商品轮播图',
      component: 'ImageUpload',
      componentProps: {
        maxNumber: 10,
        multiple: true,
        maxSize: 30,
      },
      rules: 'required',
    },
  ];
}

/** 价格库存的表单 */
export function useSkuFormSchema(): VbenFormSchema[] {
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
      fieldName: 'subCommissionType',
      label: '分销类型',
      component: 'RadioGroup',
      componentProps: {
        allowClear: true,
        options: [
          {
            label: '默认设置',
            value: false,
          },
          {
            label: '单独设置',
            value: true,
          },
        ],
      },
      rules: 'required',
    },
    {
      fieldName: 'specType',
      label: '商品规格',
      component: 'RadioGroup',
      componentProps: {
        allowClear: true,
        options: [
          {
            label: '单规格',
            value: false,
          },
          {
            label: '多规格',
            value: true,
          },
        ],
      },
      rules: 'required',
    },
    // TODO @xingyu：待补充商品属性
  ];
}

/** 物流设置的表单 */
export function useDeliveryFormSchema(): VbenFormSchema[] {
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
      fieldName: 'deliveryTypes',
      label: '配送方式',
      component: 'CheckboxGroup',
      componentProps: {
        options: getDictOptions(DICT_TYPE.TRADE_DELIVERY_TYPE, 'number'),
      },
      rules: 'required',
    },
    {
      fieldName: 'deliveryTemplateId',
      label: '运费模板',
      component: 'ApiSelect',
      componentProps: {
        api: getSimpleTemplateList,
        labelField: 'name',
        valueField: 'id',
      },
      dependencies: {
        triggerFields: ['deliveryTypes'],
        show: (values) =>
          !!values.deliveryTypes &&
          values.deliveryTypes.includes(DeliveryTypeEnum.EXPRESS.type),
      },
      rules: 'required',
    },
  ];
}

/** 商品详情的表单 */
export function useDescriptionFormSchema(): VbenFormSchema[] {
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
      fieldName: 'description',
      label: '商品详情',
      component: 'RichTextarea',
      componentProps: {
        placeholder: '请输入商品详情',
        height: 1000,
      },
      rules: 'required',
    },
  ];
}

/** 其它设置的表单 */
export function useOtherFormSchema(): VbenFormSchema[] {
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
      fieldName: 'sort',
      label: '商品排序',
      component: 'InputNumber',
      componentProps: {
        min: 0,
        controlsPosition: 'right',
        class: '!w-full',
      },
      rules: z.number().min(0).optional().default(0),
    },
    {
      fieldName: 'giveIntegral',
      label: '赠送积分',
      component: 'InputNumber',
      componentProps: {
        min: 0,
        controlsPosition: 'right',
        class: '!w-full',
      },
      rules: z.number().min(0).optional().default(0),
    },
    {
      fieldName: 'virtualSalesCount',
      label: '虚拟销量',
      component: 'InputNumber',
      componentProps: {
        min: 0,
        controlsPosition: 'right',
        class: '!w-full',
      },
      rules: z.number().min(0).optional().default(0),
    },
  ];
}
