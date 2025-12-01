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
      render: (val) => formatDateTime(val) as string,
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
      render: (val, data) => {
        const areaName = val ?? '';
        const detailAddress = data?.detailAddress ?? '';
        return [areaName, detailAddress].filter((item) => !!item).join(' ');
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
      render: (val) =>
        h(DictTag, {
          type: DICT_TYPE.INFRA_BOOLEAN_STRING,
          value: val,
        }),
    },
    {
      field: 'sex',
      label: '性别',
      render: (val) =>
        h(DictTag, { type: DICT_TYPE.SYSTEM_USER_SEX, value: val }),
    },
    {
      field: 'contactNextTime',
      label: '下次联系时间',
      render: (val) => formatDateTime(val) as string,
    },
    {
      field: 'remark',
      label: '备注',
    },
  ];
}
