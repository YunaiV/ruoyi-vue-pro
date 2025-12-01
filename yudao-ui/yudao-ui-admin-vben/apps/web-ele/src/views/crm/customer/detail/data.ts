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
        api: getSimpleUserList,
        labelField: 'nickname',
        valueField: 'id',
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
      render: (val) =>
        h(DictTag, { type: DICT_TYPE.CRM_CUSTOMER_LEVEL, value: val }),
    },
    {
      field: 'dealStatus',
      label: '成交状态',
      render: (val) => (val ? '已成交' : '未成交'),
    },
    {
      field: 'ownerUserName',
      label: '负责人',
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
      label: '客户名称',
    },
    {
      field: 'source',
      label: '客户来源',
      render: (val) =>
        h(DictTag, {
          type: DICT_TYPE.CRM_CUSTOMER_SOURCE,
          value: val,
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
      render: (val, data) => {
        const areaName = val ?? '';
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
      render: (val) =>
        h(DictTag, {
          type: DICT_TYPE.CRM_CUSTOMER_INDUSTRY,
          value: val,
        }),
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
