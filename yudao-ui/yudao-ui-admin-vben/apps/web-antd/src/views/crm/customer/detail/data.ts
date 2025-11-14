import type { VbenFormSchema } from '#/adapter/form';
import type { DescriptionItemSchema } from '#/components/description';

import { h } from 'vue';

import { DICT_TYPE } from '@vben/constants';
import { useUserStore } from '@vben/stores';
import { formatDateTime } from '@vben/utils';

import { getSimpleUserList } from '#/api/system/user';
import { DictTag } from '#/components/dict-tag';

/** 分配客户表单 */
export function useDistributeFormSchema(): VbenFormSchema[] {
  const userStore = useUserStore();
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
      fieldName: 'ownerUserId',
      label: '负责人',
      component: 'ApiSelect',
      componentProps: {
        api: () => getSimpleUserList(),
        fieldNames: {
          label: 'nickname',
          value: 'id',
        },
      },
      defaultValue: userStore.userInfo?.id,
      rules: 'required',
    },
  ];
}

/** 详情页的字段 */
export function useDetailSchema(): DescriptionItemSchema[] {
  return [
    {
      field: 'level',
      label: '客户级别',
      content: (data) =>
        h(DictTag, { type: DICT_TYPE.CRM_CUSTOMER_LEVEL, value: data?.level }),
    },
    {
      field: 'dealStatus',
      label: '成交状态',
      content: (data) => (data.dealStatus ? '已成交' : '未成交'),
    },
    {
      field: 'ownerUserName',
      label: '负责人',
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
      label: '客户名称',
    },
    {
      field: 'source',
      label: '客户来源',
      content: (data) =>
        h(DictTag, {
          type: DICT_TYPE.CRM_CUSTOMER_SOURCE,
          value: data?.source,
        }),
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
      field: 'areaName',
      label: '地址',
      content: (data) => {
        const areaName = data?.areaName ?? '';
        const detailAddress = data?.detailAddress ?? '';
        return [areaName, detailAddress].filter(Boolean).join(' ');
      },
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
      field: 'industryId',
      label: '客户行业',
      content: (data) =>
        h(DictTag, {
          type: DICT_TYPE.CRM_CUSTOMER_INDUSTRY,
          value: data?.industryId,
        }),
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
