import type { DescriptionItemSchema } from '#/components/description';

import { h } from 'vue';

import { DICT_TYPE } from '@vben/constants';
import { formatDateTime } from '@vben/utils';

import { DictTag } from '#/components/dict-tag';

/** 详情页的基础字段 */
export function useDetailSchema(): DescriptionItemSchema[] {
  return [
    {
      field: 'customerName',
      label: '客户名称',
    },
    {
      field: 'post',
      label: '职务',
    },
    {
      field: 'mobile',
      label: '手机',
    },
    {
      field: 'createTime',
      label: '创建时间',
      content: (data) => formatDateTime(data?.createTime) as string,
    },
  ];
}

/** 详情页的基础字段 */
export function useDetailBaseSchema(): DescriptionItemSchema[] {
  return [
    {
      field: 'name',
      label: '姓名',
    },
    {
      field: 'customerName',
      label: '客户名称',
    },
    {
      field: 'mobile',
      label: '手机',
    },
    {
      field: 'telephone',
      label: '电话',
    },
    {
      field: 'email',
      label: '邮箱',
    },
    {
      field: 'qq',
      label: 'QQ',
    },
    {
      field: 'wechat',
      label: '微信',
    },
    {
      field: 'areaName',
      label: '地址',
      content: (data) => {
        const areaName = data?.areaName ?? '';
        const detailAddress = data?.detailAddress ?? '';
        return [areaName, detailAddress].filter(Boolean).join(' ');
      },
    },
    {
      field: 'post',
      label: '职务',
    },
    {
      field: 'parentName',
      label: '直属上级',
    },
    {
      field: 'master',
      label: '关键决策人',
      content: (data) =>
        h(DictTag, {
          type: DICT_TYPE.INFRA_BOOLEAN_STRING,
          value: data?.master,
        }),
    },
    {
      field: 'sex',
      label: '性别',
      content: (data) =>
        h(DictTag, { type: DICT_TYPE.SYSTEM_USER_SEX, value: data?.sex }),
    },
    {
      field: 'contactNextTime',
      label: '下次联系时间',
      content: (data) => formatDateTime(data?.contactNextTime) as string,
    },
    {
      field: 'remark',
      label: '备注',
    },
  ];
}
