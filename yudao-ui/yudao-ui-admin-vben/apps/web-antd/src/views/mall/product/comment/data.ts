import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { MallCommentApi } from '#/api/mall/product/comment';

import { z } from '#/adapter/form';
import { getRangePickerDefaultProps } from '#/utils';

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
      fieldName: 'spuId',
      label: '商品',
      component: 'Input',
      componentProps: {
        placeholder: '请选择商品',
      },
      rules: 'required',
    },
    {
      fieldName: 'skuId',
      label: '商品规格',
      component: 'Input',
      componentProps: {
        placeholder: '请选择商品规格',
      },
      dependencies: {
        triggerFields: ['spuId'],
        show: (values) => !!values.spuId,
      },
      rules: 'required',
    },
    {
      fieldName: 'userAvatar',
      label: '用户头像',
      component: 'ImageUpload',
      componentProps: {
        placeholder: '请上传用户头像',
      },
      rules: 'required',
    },
    {
      fieldName: 'userNickname',
      label: '用户名称',
      component: 'Input',
      componentProps: {
        placeholder: '请输入用户名称',
      },
      rules: 'required',
    },
    {
      fieldName: 'content',
      label: '评论内容',
      component: 'Textarea',
      componentProps: {
        placeholder: '请输入评论内容',
      },
      rules: 'required',
    },
    {
      fieldName: 'descriptionScores',
      label: '描述星级',
      component: 'Rate',
      rules: z.number().min(1).max(5).default(5),
    },
    {
      fieldName: 'benefitScores',
      label: '服务星级',
      component: 'Rate',
      rules: z.number().min(1).max(5).default(5),
    },
    {
      fieldName: 'picUrls',
      label: '评论图片',
      component: 'ImageUpload',
      componentProps: {
        maxNumber: 9,
        placeholder: '请上传评论图片',
      },
      rules: 'required',
    },
  ];
}

/** 列表的搜索表单 */
export function useGridFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'replyStatus',
      label: '回复状态',
      component: 'RadioGroup',
      componentProps: {
        options: [
          { label: '已回复', value: true },
          { label: '未回复', value: false },
        ],
        placeholder: '请选择回复状态',
        allowClear: true,
      },
    },
    {
      fieldName: 'spuName',
      label: '商品名称',
      component: 'Input',
      componentProps: {
        placeholder: '请输入商品名称',
        allowClear: true,
      },
    },
    {
      fieldName: 'userNickname',
      label: '用户名称',
      component: 'Input',
      componentProps: {
        placeholder: '请输入用户名称',
        allowClear: true,
      },
    },
    {
      fieldName: 'orderId',
      label: '订单编号',
      component: 'Input',
      componentProps: {
        placeholder: '请输入订单编号',
        allowClear: true,
      },
    },
    {
      fieldName: 'createTime',
      label: '评论时间',
      component: 'RangePicker',
      componentProps: {
        ...getRangePickerDefaultProps(),
        allowClear: true,
      },
    },
  ];
}

/** 列表的字段 */
export function useGridColumns(
  onStatusChange?: (
    newStatus: boolean,
    row: MallCommentApi.Comment,
  ) => PromiseLike<boolean | undefined>,
): VxeTableGridOptions['columns'] {
  return [
    {
      field: 'id',
      title: '评论编号',
      fixed: 'left',
      minWidth: 80,
    },
    {
      field: 'skuPicUrl',
      title: '商品图片',
      minWidth: 100,
      cellRender: {
        name: 'CellImage',
      },
    },
    {
      field: 'spuName',
      title: '商品名称',
      minWidth: 250,
    },
    {
      field: 'skuProperties',
      title: '商品属性',
      minWidth: 200,
      formatter: ({ cellValue }) => {
        return cellValue && cellValue.length > 0
          ? cellValue
              .map((item: any) => `${item.propertyName} : ${item.valueName}`)
              .join('\n')
          : '-';
      },
    },
    {
      field: 'userNickname',
      title: '用户名称',
      minWidth: 100,
    },
    {
      field: 'descriptionScores',
      title: '商品评分',
      minWidth: 150,
      slots: {
        default: 'descriptionScores',
      },
    },
    {
      field: 'benefitScores',
      title: '服务评分',
      minWidth: 150,
      slots: {
        default: 'benefitScores',
      },
    },
    {
      field: 'content',
      title: '评论内容',
      minWidth: 210,
    },
    {
      field: 'picUrls',
      title: '评论图片',
      minWidth: 120,
      cellRender: {
        name: 'CellImages',
      },
    },
    {
      field: 'replyContent',
      title: '回复内容',
      minWidth: 250,
    },
    {
      field: 'createTime',
      title: '评论时间',
      minWidth: 180,
      formatter: 'formatDateTime',
    },
    {
      field: 'visible',
      title: '是否展示',
      minWidth: 110,
      align: 'center',
      cellRender: {
        attrs: { beforeChange: onStatusChange },
        name: 'CellSwitch',
        props: {
          checkedValue: true,
          unCheckedValue: false,
        },
      },
    },
    {
      title: '操作',
      width: 80,
      fixed: 'right',
      slots: { default: 'actions' },
    },
  ];
}
