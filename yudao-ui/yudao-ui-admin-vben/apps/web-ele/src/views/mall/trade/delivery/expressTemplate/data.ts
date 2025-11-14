import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';

import { DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';

import { z } from '#/adapter/form';

/** 计费方式列标题映射 */
export const CHARGE_MODE_TITLE_MAP: Record<
  number,
  {
    extraCountTitle: string;
    startCountTitle: string;
  }
> = {
  1: { startCountTitle: '首件', extraCountTitle: '续件' },
  2: { startCountTitle: '首件重量(kg)', extraCountTitle: '续件重量(kg)' },
  3: { startCountTitle: '首件体积(m³)', extraCountTitle: '续件体积(m³)' },
};

/** 包邮方式列标题映射 */
export const FREE_MODE_TITLE_MAP: Record<number, { freeCountTitle: string }> = {
  1: { freeCountTitle: '包邮件数' },
  2: { freeCountTitle: '包邮重量(kg)' },
  3: { freeCountTitle: '包邮体积(m³)' },
};

/** 运费设置表格列 */
export function useChargesColumns(
  chargeMode = 1,
): VxeTableGridOptions['columns'] {
  const chargeTitleMap = CHARGE_MODE_TITLE_MAP[chargeMode];
  return [
    {
      field: 'areaIds',
      title: '区域',
      minWidth: 300,
      slots: { default: 'areaIds' },
    },
    {
      field: 'startCount',
      title: chargeTitleMap?.startCountTitle,
      width: 120,
      slots: { default: 'startCount' },
    },
    {
      field: 'startPrice',
      title: '运费(元)',
      width: 120,
      slots: { default: 'startPrice' },
    },
    {
      field: 'extraCount',
      title: chargeTitleMap?.extraCountTitle,
      width: 120,
      slots: { default: 'extraCount' },
    },
    {
      field: 'extraPrice',
      title: '续费(元)',
      width: 120,
      slots: { default: 'extraPrice' },
    },
    {
      title: '操作',
      width: 80,
      fixed: 'right',
      slots: { default: 'actions' },
    },
  ];
}

/** 包邮设置表格列 */
export function useFreesColumns(
  chargeMode = 1,
): VxeTableGridOptions['columns'] {
  const freeTitleMap = FREE_MODE_TITLE_MAP[chargeMode];
  return [
    {
      field: 'areaIds',
      title: '区域',
      minWidth: 300,
      slots: { default: 'areaIds' },
    },
    {
      field: 'freeCount',
      title: freeTitleMap?.freeCountTitle,
      width: 120,
      slots: { default: 'freeCount' },
    },
    {
      field: 'freePrice',
      title: '包邮金额(元)',
      width: 120,
      slots: { default: 'freePrice' },
    },
    {
      title: '操作',
      width: 80,
      fixed: 'right',
      slots: { default: 'actions' },
    },
  ];
}

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
      component: 'Input',
      fieldName: 'name',
      label: '模板名称',
      componentProps: {
        placeholder: '请输入模板名称',
      },
      rules: 'required',
    },
    {
      fieldName: 'chargeMode',
      label: '计费方式',
      component: 'RadioGroup',
      componentProps: {
        options: getDictOptions(DICT_TYPE.EXPRESS_CHARGE_MODE, 'number'),
      },
      rules: z.number().default(1),
    },
    {
      fieldName: 'sort',
      label: '显示顺序',
      component: 'InputNumber',
      componentProps: {
        placeholder: '请输入显示顺序',
        min: 0,
        controlsPosition: 'right',
        class: '!w-full',
      },
      rules: 'required',
    },
    {
      fieldName: 'charges',
      label: '运费设置',
      component: 'Input',
      formItemClass: 'col-span-3',
    },
    {
      fieldName: 'frees',
      label: '包邮设置',
      component: 'Input',
      formItemClass: 'col-span-3',
    },
  ];
}

/** 列表的搜索表单 */
export function useGridFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'name',
      label: '模板名称',
      component: 'Input',
      componentProps: {
        placeholder: '请输入模板名称',
        clearable: true,
      },
    },
    {
      fieldName: 'chargeMode',
      label: '计费方式',
      component: 'Select',
      componentProps: {
        placeholder: '请选择计费方式',
        clearable: true,
        options: getDictOptions(DICT_TYPE.EXPRESS_CHARGE_MODE, 'number'),
      },
    },
  ];
}

/** 列表的字段 */
export function useGridColumns(): VxeTableGridOptions['columns'] {
  return [
    {
      field: 'id',
      title: '编号',
      minWidth: 100,
    },
    {
      field: 'name',
      title: '模板名称',
      minWidth: 200,
    },
    {
      field: 'chargeMode',
      title: '计费方式',
      minWidth: 100,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.EXPRESS_CHARGE_MODE },
      },
    },
    {
      field: 'sort',
      title: '显示顺序',
      minWidth: 100,
    },
    {
      field: 'createTime',
      title: '创建时间',
      minWidth: 180,
      formatter: 'formatDateTime',
    },
    {
      title: '操作',
      width: 130,
      fixed: 'right',
      slots: { default: 'actions' },
    },
  ];
}
