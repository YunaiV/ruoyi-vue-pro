import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { SystemSocialUserApi } from '#/api/system/social/user';
import type { DescriptionItemSchema } from '#/components/description';

import { h } from 'vue';

import { DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';

import { DictTag } from '#/components/dict-tag';
import { getRangePickerDefaultProps } from '#/utils';

/** 列表的搜索表单 */
export function useGridFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'type',
      label: '社交平台',
      component: 'Select',
      componentProps: {
        options: getDictOptions(DICT_TYPE.SYSTEM_SOCIAL_TYPE, 'number'),
        placeholder: '请选择社交平台',
        allowClear: true,
      },
    },
    {
      fieldName: 'nickname',
      label: '用户昵称',
      component: 'Input',
      componentProps: {
        placeholder: '请输入用户昵称',
        allowClear: true,
      },
    },
    {
      fieldName: 'openid',
      label: '社交 openid',
      component: 'Input',
      componentProps: {
        placeholder: '请输入社交 openid',
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
export function useGridColumns(): VxeTableGridOptions['columns'] {
  return [
    {
      field: 'type',
      title: '社交平台',
      minWidth: 100,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.SYSTEM_SOCIAL_TYPE },
      },
    },
    {
      field: 'openid',
      title: '社交 openid',
      minWidth: 180,
    },
    {
      field: 'nickname',
      title: '用户昵称',
      minWidth: 120,
    },
    {
      field: 'avatar',
      title: '用户头像',
      minWidth: 100,
      cellRender: {
        name: 'CellImage',
      },
    },
    {
      field: 'createTime',
      title: '创建时间',
      minWidth: 180,
      formatter: 'formatDateTime',
    },
    {
      field: 'updateTime',
      title: '更新时间',
      minWidth: 180,
      formatter: 'formatDateTime',
    },
    {
      title: '操作',
      width: 120,
      fixed: 'right',
      slots: { default: 'actions' },
    },
  ];
}

/** 详情页的字段 */
export function useDetailSchema(): DescriptionItemSchema[] {
  return [
    {
      field: 'type',
      label: '社交平台',
      content: (data: SystemSocialUserApi.SocialUser) => {
        return h(DictTag, {
          type: DICT_TYPE.SYSTEM_SOCIAL_TYPE,
          value: data?.type,
        });
      },
    },
    {
      field: 'nickname',
      label: '用户昵称',
    },
    {
      field: 'avatar',
      label: '用户头像',
      // TODO @芋艿：使用 antd 的 Image 组件
      content: (data: SystemSocialUserApi.SocialUser) => {
        if (data?.avatar) {
          return h('img', {
            src: data.avatar,
            style: 'width: 30px; height: 30px; cursor: pointer;',
            onClick: () => {
              // 可以添加图片预览功能
              window.open(data.avatar, '_blank');
            },
          });
        }
        return '无';
      },
    },
    {
      field: 'token',
      label: '社交 token',
    },
    {
      field: 'rawTokenInfo',
      label: '原始 Token 数据',
    },
    {
      field: 'rawUserInfo',
      label: '原始 User 数据',
    },
    {
      field: 'code',
      label: '最后一次的认证 code',
    },
    {
      field: 'state',
      label: '最后一次的认证 state',
    },
  ];
}
