import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';

import { h, ref } from 'vue';

import { DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';

import { Button } from 'ant-design-vue';

import { z } from '#/adapter/form';
import { getSimpleProductCategoryList } from '#/api/iot/product/category';
import { getProductPage } from '#/api/iot/product/product';

/** 新增/修改产品的表单 */
export function useFormSchema(formApi?: any): VbenFormSchema[] {
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
      fieldName: 'productKey',
      label: 'ProductKey',
      component: 'Input',
      componentProps: {
        placeholder: '请输入 ProductKey',
      },
      dependencies: {
        triggerFields: ['id'],
        if(values) {
          return !values.id;
        },
      },
      rules: z
        .string()
        .min(1, 'ProductKey 不能为空')
        .max(32, 'ProductKey 长度不能超过 32 个字符'),
      suffix: () => {
        // 创建时的 ProductKey 字段（带生成按钮）
        return h(
          Button,
          {
            type: 'default',
            onClick: () => {
              formApi?.setFieldValue('productKey', generateProductKey());
            },
          },
          { default: () => '重新生成' },
        );
      },
    },
    {
      fieldName: 'productKey',
      label: 'ProductKey',
      component: 'Input',
      componentProps: {
        placeholder: '请输入 ProductKey',
        disabled: true, // 编辑时的 ProductKey 字段（禁用，无按钮）
      },
      dependencies: {
        triggerFields: ['id'],
        if(values) {
          return !!values.id;
        },
      },
      rules: z
        .string()
        .min(1, 'ProductKey 不能为空')
        .max(32, 'ProductKey 长度不能超过 32 个字符'),
    },
    {
      fieldName: 'name',
      label: '产品名称',
      component: 'Input',
      componentProps: {
        placeholder: '请输入产品名称',
      },
      rules: z
        .string()
        .min(1, '产品名称不能为空')
        .max(64, '产品名称长度不能超过 64 个字符'),
    },
    {
      fieldName: 'categoryId',
      label: '产品分类',
      component: 'ApiSelect',
      componentProps: {
        api: getSimpleProductCategoryList,
        labelField: 'name',
        valueField: 'id',
        placeholder: '请选择产品分类',
      },
      rules: 'required',
    },
    {
      fieldName: 'deviceType',
      label: '设备类型',
      component: 'RadioGroup',
      componentProps: {
        options: getDictOptions(DICT_TYPE.IOT_PRODUCT_DEVICE_TYPE, 'number'),
        buttonStyle: 'solid',
        optionType: 'button',
      },
      rules: 'required',
    },
    {
      fieldName: 'netType',
      label: '联网方式',
      component: 'Select',
      componentProps: {
        options: getDictOptions(DICT_TYPE.IOT_NET_TYPE, 'number'),
        placeholder: '请选择联网方式',
      },
      rules: 'required',
    },
    {
      fieldName: 'codecType',
      label: '数据格式',
      component: 'RadioGroup',
      componentProps: {
        options: getDictOptions(DICT_TYPE.IOT_CODEC_TYPE, 'string'),
        buttonStyle: 'solid',
        optionType: 'button',
      },
      rules: 'required',
    },
    {
      fieldName: 'status',
      label: '产品状态',
      component: 'RadioGroup',
      componentProps: {
        options: getDictOptions(DICT_TYPE.COMMON_STATUS, 'number'),
        buttonStyle: 'solid',
        optionType: 'button',
      },
      rules: 'required',
    },
    {
      fieldName: 'icon',
      label: '产品图标',
      component: 'ImageUpload',
    },
    {
      fieldName: 'picUrl',
      label: '产品图片',
      component: 'ImageUpload',
    },
    {
      fieldName: 'description',
      label: '产品描述',
      component: 'Textarea',
      componentProps: {
        placeholder: '请输入产品描述',
        rows: 3,
      },
    },
  ];
}

/** 基础表单字段（不含图标、图片、描述） */
export function useBasicFormSchema(formApi?: any): VbenFormSchema[] {
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
      fieldName: 'productKey',
      label: 'ProductKey',
      component: 'Input',
      componentProps: {
        placeholder: '请输入 ProductKey',
      },
      dependencies: {
        triggerFields: ['id'],
        if(values) {
          return !values.id;
        },
      },
      rules: z
        .string()
        .min(1, 'ProductKey 不能为空')
        .max(32, 'ProductKey 长度不能超过 32 个字符'),
      suffix: () => {
        return h(
          Button,
          {
            type: 'default',
            onClick: () => {
              formApi?.setFieldValue('productKey', generateProductKey());
            },
          },
          { default: () => '重新生成' },
        );
      },
    },
    {
      fieldName: 'productKey',
      label: 'ProductKey',
      component: 'Input',
      componentProps: {
        placeholder: '请输入 ProductKey',
        disabled: true,
      },
      dependencies: {
        triggerFields: ['id'],
        if(values) {
          return !!values.id;
        },
      },
      rules: z
        .string()
        .min(1, 'ProductKey 不能为空')
        .max(32, 'ProductKey 长度不能超过 32 个字符'),
    },
    {
      fieldName: 'name',
      label: '产品名称',
      component: 'Input',
      componentProps: {
        placeholder: '请输入产品名称',
      },
      rules: z
        .string()
        .min(1, '产品名称不能为空')
        .max(64, '产品名称长度不能超过 64 个字符'),
    },
    {
      fieldName: 'categoryId',
      label: '产品分类',
      component: 'ApiSelect',
      componentProps: {
        api: getSimpleProductCategoryList,
        labelField: 'name',
        valueField: 'id',
        placeholder: '请选择产品分类',
      },
      rules: 'required',
    },
    {
      fieldName: 'deviceType',
      label: '设备类型',
      component: 'RadioGroup',
      componentProps: {
        options: getDictOptions(DICT_TYPE.IOT_PRODUCT_DEVICE_TYPE, 'number'),
        buttonStyle: 'solid',
        optionType: 'button',
      },
      rules: 'required',
    },
    {
      fieldName: 'netType',
      label: '联网方式',
      component: 'Select',
      componentProps: {
        options: getDictOptions(DICT_TYPE.IOT_NET_TYPE, 'number'),
        placeholder: '请选择联网方式',
      },
      rules: 'required',
    },
    {
      fieldName: 'codecType',
      label: '数据格式',
      component: 'RadioGroup',
      componentProps: {
        options: getDictOptions(DICT_TYPE.IOT_CODEC_TYPE, 'string'),
        buttonStyle: 'solid',
        optionType: 'button',
      },
      rules: 'required',
    },
    {
      fieldName: 'status',
      label: '产品状态',
      component: 'RadioGroup',
      componentProps: {
        options: getDictOptions(DICT_TYPE.COMMON_STATUS, 'number'),
        buttonStyle: 'solid',
        optionType: 'button',
      },
      rules: 'required',
    },
    {
      fieldName: 'locationType',
      label: '定位类型',
      component: 'Select',
      componentProps: {
        options: getDictOptions(DICT_TYPE.IOT_LOCATION_TYPE, 'number'),
        placeholder: '请选择定位类型',
      },
      rules: 'required',
    },
  ];
}

/** 高级设置表单字段（图标、图片、产品描述） */
export function useAdvancedFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'icon',
      label: '产品图标',
      component: 'IconPicker', // 用这个组件 产品卡片列表 可以根据这个显示 否则就显示默认的
      componentProps: {
        placeholder: '请选择产品图标',
        prefix: 'carbon',
        autoFetchApi: false,
      },
    },
    {
      fieldName: 'picUrl',
      label: '产品图片',
      component: 'ImageUpload',
    },
    {
      fieldName: 'description',
      label: '产品描述',
      component: 'Textarea',
      componentProps: {
        placeholder: '请输入产品描述',
        rows: 3,
      },
      formItemClass: 'col-span-2', // 让描述占满两列
    },
  ];
}

/** 列表的搜索表单 */
// TODO @haohao：貌似用不上？
export function useGridFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'name',
      label: '产品名称',
      component: 'Input',
      componentProps: {
        placeholder: '请输入产品名称',
        allowClear: true,
      },
    },
    {
      fieldName: 'productKey',
      label: 'ProductKey',
      component: 'Input',
      componentProps: {
        placeholder: '请输入产品标识',
        allowClear: true,
      },
    },
  ];
}

/** 列表的字段 */
export function useGridColumns(): VxeTableGridOptions['columns'] {
  return [
    {
      field: 'id',
      title: 'ID',
      width: 80,
    },
    {
      field: 'productKey',
      title: 'ProductKey',
      minWidth: 150,
    },
    {
      field: 'categoryId',
      title: '品类',
      minWidth: 120,
      slots: { default: 'category' },
    },
    {
      field: 'deviceType',
      title: '设备类型',
      minWidth: 120,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.IOT_PRODUCT_DEVICE_TYPE },
      },
    },
    {
      field: 'icon',
      title: '产品图标',
      width: 100,
      slots: { default: 'icon' },
    },
    {
      field: 'picUrl',
      title: '产品图片',
      width: 100,
      slots: { default: 'picUrl' },
    },
    {
      field: 'createTime',
      title: '创建时间',
      minWidth: 180,
      formatter: 'formatDateTime',
    },
    {
      title: '操作',
      width: 220,
      fixed: 'right',
      slots: { default: 'actions' },
    },
  ];
}

/** 查询产品列表 */
// TODO @haohao：貌似可以删除？
export async function queryProductList({ page }: any, searchParams: any) {
  return await getProductPage({
    pageNo: page.currentPage,
    pageSize: page.pageSize,
    ...searchParams,
  });
}

/** 创建图片预览状态 */
// TODO @haohao：可能不一定用的上；
export function useImagePreview() {
  const previewVisible = ref(false);
  const previewImage = ref('');

  function handlePreviewImage(url: string) {
    previewImage.value = url;
    previewVisible.value = true;
  }

  return {
    previewVisible,
    previewImage,
    handlePreviewImage,
  };
}

// TODO @haohao：放到对应的 form 里
/** 生成 ProductKey（包含大小写字母和数字） */
export function generateProductKey(): string {
  const chars =
    'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';
  let result = '';
  for (let i = 0; i < 16; i++) {
    result += chars.charAt(Math.floor(Math.random() * chars.length));
  }
  return result;
}
