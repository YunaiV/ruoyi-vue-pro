import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';

import {
  DICT_TYPE,
  PromotionConditionTypeEnum,
  PromotionProductScopeEnum,
} from '@vben/constants';
import { getDictOptions } from '@vben/hooks';
import { $t } from '@vben/locales';

import { z } from '#/adapter/form';
import { getRangePickerDefaultProps } from '#/utils';

/** 列表的搜索表单 */
export function useGridFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'name',
      label: '活动名称',
      component: 'Input',
      componentProps: {
        placeholder: '请输入活动名称',
        allowClear: true,
      },
    },
    {
      fieldName: 'status',
      label: '活动状态',
      component: 'Select',
      componentProps: {
        options: getDictOptions(DICT_TYPE.COMMON_STATUS, 'number'),
        placeholder: '请选择活动状态',
        allowClear: true,
      },
    },
    {
      fieldName: 'createTime',
      label: '活动时间',
      component: 'RangePicker',
      componentProps: {
        ...getRangePickerDefaultProps(),
        allowClear: true,
      },
    },
  ];
}

/** 列表的表格列 */
export function useGridColumns(): VxeTableGridOptions['columns'] {
  return [
    {
      field: 'name',
      title: '活动名称',
      minWidth: 200,
    },
    {
      field: 'productScope',
      title: '活动范围',
      minWidth: 120,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.PROMOTION_PRODUCT_SCOPE },
      },
    },
    {
      field: 'startTime',
      title: '活动开始时间',
      minWidth: 180,
      formatter: 'formatDateTime',
    },
    {
      field: 'endTime',
      title: '活动结束时间',
      minWidth: 180,
      formatter: 'formatDateTime',
    },
    {
      field: 'status',
      title: '状态',
      minWidth: 100,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.COMMON_STATUS },
      },
    },
    {
      field: 'createTime',
      title: '创建时间',
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

/** 新增/修改的表单 */
export function useFormSchema(): VbenFormSchema[] {
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
      label: '活动名称',
      component: 'Input',
      rules: 'required',
      componentProps: {
        placeholder: '请输入活动名称',
        allowClear: true,
      },
    },
    {
      fieldName: 'remark',
      label: '备注',
      component: 'Textarea',
      componentProps: {
        placeholder: '请输入备注',
        rows: 4,
        allowClear: true,
      },
    },
    {
      fieldName: 'startAndEndTime',
      label: '活动时间',
      component: 'RangePicker',
      rules: 'required',
      componentProps: {
        showTime: true,
        format: 'YYYY-MM-DD HH:mm:ss',
        placeholder: [
          $t('utils.rangePicker.beginTime'),
          $t('utils.rangePicker.endTime'),
        ],
      },
    },
    {
      fieldName: 'conditionType',
      label: '条件类型',
      component: 'RadioGroup',
      componentProps: {
        options: getDictOptions(DICT_TYPE.PROMOTION_CONDITION_TYPE, 'number'),
        buttonStyle: 'solid',
        optionType: 'button',
      },
      rules: z.number().default(PromotionConditionTypeEnum.PRICE.type),
    },
    {
      fieldName: 'productScope',
      label: '活动范围',
      component: 'RadioGroup',
      componentProps: {
        options: getDictOptions(DICT_TYPE.PROMOTION_PRODUCT_SCOPE, 'number'),
        buttonStyle: 'solid',
        optionType: 'button',
      },
      rules: z.number().default(PromotionProductScopeEnum.ALL.scope),
    },
    {
      fieldName: 'productSpuIds',
      label: '选择商品',
      component: 'Input',
      dependencies: {
        triggerFields: ['productScope', 'productScopeValues'],
        show: (values) => {
          return values.productScope === PromotionProductScopeEnum.SPU.scope;
        },
        trigger(values, form) {
          // 当加载已有数据时，根据 productScopeValues 设置 productSpuIds
          if (
            values.productScope === PromotionProductScopeEnum.SPU.scope &&
            values.productScopeValues
          ) {
            form.setFieldValue('productSpuIds', values.productScopeValues);
          }
        },
      },
      rules: 'required',
    },
    {
      fieldName: 'productCategoryIds',
      label: '选择分类',
      component: 'Input',
      dependencies: {
        triggerFields: ['productScope', 'productScopeValues'],
        show: (values) => {
          return (
            values.productScope === PromotionProductScopeEnum.CATEGORY.scope
          );
        },
        trigger(values, form) {
          // 当加载已有数据时，根据 productScopeValues 设置 productCategoryIds
          if (
            values.productScope === PromotionProductScopeEnum.CATEGORY.scope &&
            values.productScopeValues
          ) {
            const categoryIds = values.productScopeValues;
            // 单选时使用数组不能反显，取第一个元素
            form.setFieldValue(
              'productCategoryIds',
              Array.isArray(categoryIds) && categoryIds.length > 0
                ? categoryIds[0]
                : categoryIds,
            );
          }
        },
      },
      rules: 'required',
    },
    {
      fieldName: 'rules',
      label: '优惠设置',
      component: 'Input',
      formItemClass: 'items-start',
      rules: 'required',
    },
    {
      fieldName: 'productScopeValues', // 隐藏字段：用于自动同步 productScopeValues
      component: 'Input',
      dependencies: {
        triggerFields: ['productScope', 'productSpuIds', 'productCategoryIds'],
        show: () => false,
        trigger(values, form) {
          switch (values.productScope) {
            case PromotionProductScopeEnum.CATEGORY.scope: {
              const categoryIds = Array.isArray(values.productCategoryIds)
                ? values.productCategoryIds
                : [values.productCategoryIds];
              form.setFieldValue('productScopeValues', categoryIds);
              break;
            }
            case PromotionProductScopeEnum.SPU.scope: {
              form.setFieldValue('productScopeValues', values.productSpuIds);
              break;
            }
          }
        },
      },
    },
  ];
}
