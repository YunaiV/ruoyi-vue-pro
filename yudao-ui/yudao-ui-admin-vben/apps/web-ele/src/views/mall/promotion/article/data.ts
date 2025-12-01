import type { VbenFormSchema } from '#/adapter/form';
import type { VxeGridPropTypes } from '#/adapter/vxe-table';
import type { MallArticleCategoryApi } from '#/api/mall/promotion/article/category';

import { CommonStatusEnum, DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';

import { z } from '#/adapter/form';
import { getSimpleArticleCategoryList } from '#/api/mall/promotion/article/category';
import { getRangePickerDefaultProps } from '#/utils';

/** 关联数据 */
let categoryList: MallArticleCategoryApi.ArticleCategory[] = [];
getSimpleArticleCategoryList().then((data) => (categoryList = data));

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
      fieldName: 'title',
      label: '文章标题',
      component: 'Input',
      componentProps: {
        placeholder: '请输入文章标题',
      },
      rules: 'required',
    },
    {
      fieldName: 'categoryId',
      label: '文章分类',
      component: 'ApiSelect',
      componentProps: {
        api: getSimpleArticleCategoryList,
        labelField: 'name',
        valueField: 'id',
        placeholder: '请选择文章分类',
      },
      rules: 'required',
    },
    {
      fieldName: 'author',
      label: '文章作者',
      component: 'Input',
      componentProps: {
        placeholder: '请输入文章作者',
      },
    },
    {
      fieldName: 'introduction',
      label: '文章简介',
      component: 'Input',
      componentProps: {
        placeholder: '请输入文章简介',
      },
    },
    {
      fieldName: 'picUrl',
      label: '文章封面',
      component: 'ImageUpload',
      formItemClass: 'col-span-2',
      componentProps: {
        placeholder: '请上传文章封面',
      },
      rules: 'required',
    },
    {
      fieldName: 'recommendHot',
      label: '是否热门',
      component: 'RadioGroup',
      componentProps: {
        options: getDictOptions(DICT_TYPE.INFRA_BOOLEAN_STRING, 'boolean'),
      },
      rules: 'required',
      defaultValue: true,
    },
    {
      fieldName: 'recommendBanner',
      label: '是否轮播图',
      component: 'RadioGroup',
      componentProps: {
        options: getDictOptions(DICT_TYPE.INFRA_BOOLEAN_STRING, 'boolean'),
      },
      rules: 'required',
      defaultValue: true,
    },
    {
      // TODO: @puhui999：商品关联
      fieldName: 'spuId',
      label: '商品关联',
      component: 'Input',
      formItemClass: 'col-span-2',
      componentProps: {
        placeholder: '请输入商品 SPU 编号',
      },
    },
    {
      fieldName: 'sort',
      label: '排序',
      component: 'InputNumber',
      componentProps: {
        min: 0,
        placeholder: '请输入排序',
        controlsPosition: 'right',
        class: '!w-full',
      },
      rules: 'required',
    },
    {
      fieldName: 'status',
      label: '状态',
      component: 'RadioGroup',
      componentProps: {
        options: getDictOptions(DICT_TYPE.COMMON_STATUS, 'number'),
      },
      rules: z.number().default(CommonStatusEnum.ENABLE),
    },
    {
      fieldName: 'content',
      label: '文章内容',
      component: 'RichTextarea',
      rules: 'required',
      formItemClass: 'col-span-2',
    },
  ];
}

/** 列表的搜索表单 */
export function useGridFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'categoryId',
      label: '文章分类',
      component: 'ApiSelect',
      componentProps: {
        api: getSimpleArticleCategoryList,
        labelField: 'name',
        valueField: 'id',
        placeholder: '请选择文章分类',
        allowClear: true,
      },
    },
    {
      fieldName: 'title',
      label: '文章标题',
      component: 'Input',
      componentProps: {
        placeholder: '请输入文章标题',
        allowClear: true,
      },
    },
    {
      fieldName: 'status',
      label: '状态',
      component: 'Select',
      componentProps: {
        options: getDictOptions(DICT_TYPE.COMMON_STATUS, 'number'),
        placeholder: '请选择状态',
        allowClear: true,
      },
    },
    {
      fieldName: 'createTime',
      label: '创建时间',
      component: 'RangePicker',
      componentProps: {
        ...getRangePickerDefaultProps(),
        allowClear: true,
      },
    },
  ];
}

/** 列表的字段 */
export function useGridColumns(): VxeGridPropTypes.Columns {
  return [
    {
      field: 'id',
      title: '编号',
      minWidth: 100,
    },
    {
      field: 'title',
      title: '标题',
      minWidth: 200,
    },
    {
      field: 'picUrl',
      title: '封面',
      minWidth: 120,
      cellRender: {
        name: 'CellImage',
      },
    },
    {
      field: 'categoryId',
      title: '分类',
      minWidth: 100,
      formatter: ({ cellValue }) =>
        categoryList.find((item) => item.id === cellValue)?.name || '-',
    },
    {
      field: 'browseCount',
      title: '浏览量',
      minWidth: 100,
    },
    {
      field: 'author',
      title: '作者',
      minWidth: 120,
    },
    {
      field: 'introduction',
      title: '文章简介',
      minWidth: 250,
    },
    {
      field: 'sort',
      title: '排序',
      minWidth: 80,
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
      width: 180,
      fixed: 'right',
      slots: { default: 'actions' },
    },
  ];
}
