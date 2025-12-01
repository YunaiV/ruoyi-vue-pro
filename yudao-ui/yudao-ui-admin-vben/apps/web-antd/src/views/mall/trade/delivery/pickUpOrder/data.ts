import type { VbenFormSchema } from '#/adapter/form';
import type { VxeGridPropTypes } from '#/adapter/vxe-table';
import type { MallDeliveryPickUpStoreApi } from '#/api/mall/trade/delivery/pickUpStore';

import { ref } from 'vue';

import { DICT_TYPE } from '@vben/constants';
import { useUserStore } from '@vben/stores';

import { getSimpleDeliveryPickUpStoreList } from '#/api/mall/trade/delivery/pickUpStore';
import { getRangePickerDefaultProps } from '#/utils';

/** 关联数据 */
const userStore = useUserStore();
const pickUpStoreList = ref<MallDeliveryPickUpStoreApi.DeliveryPickUpStore[]>(
  [],
);
getSimpleDeliveryPickUpStoreList().then((res) => {
  pickUpStoreList.value = res;
  // 移除自己无法核销的门店
  const userId = userStore?.userInfo?.id;
  pickUpStoreList.value = pickUpStoreList.value.filter((item) =>
    item.verifyUserIds?.includes(userId),
  );
});

/** 列表的搜索表单 */
export function useGridFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'createTime',
      label: '创建时间',
      component: 'RangePicker',
      componentProps: {
        ...getRangePickerDefaultProps(),
        allowClear: true,
      },
    },
    {
      fieldName: 'pickUpStoreIds',
      label: '自提门店',
      component: 'Select',
      componentProps: {
        options: pickUpStoreList,
        labelField: 'name',
        valueField: 'id',
        placeholder: '请选择自提门店',
      },
      defaultValue: pickUpStoreList.value[0]?.id,
    },
    {
      fieldName: 'no',
      label: '订单号',
      component: 'Input',
      componentProps: {
        placeholder: '请输入订单号',
        allowClear: true,
      },
    },
    {
      fieldName: 'userId',
      label: '用户 UID',
      component: 'Input',
      componentProps: {
        placeholder: '请输入用户 UID',
        allowClear: true,
      },
    },
    {
      fieldName: 'userNickname',
      label: '用户昵称',
      component: 'Input',
      componentProps: {
        placeholder: '请输入用户昵称',
        allowClear: true,
      },
    },
    {
      fieldName: 'userMobile',
      label: '用户电话',
      component: 'Input',
      componentProps: {
        placeholder: '请输入用户电话',
        allowClear: true,
      },
    },
  ];
}

/** 表格列配置 */
export function useGridColumns(): VxeGridPropTypes.Columns {
  return [
    {
      field: 'no',
      title: '订单号',
      fixed: 'left',
      minWidth: 180,
    },
    {
      field: 'user.nickname',
      title: '用户信息',
      minWidth: 100,
    },
    {
      field: 'brokerageUser.nickname',
      title: '推荐人信息',
      minWidth: 100,
    },
    {
      field: 'spuName',
      title: '商品信息',
      minWidth: 300,
      slots: { default: 'spuName' },
    },
    {
      field: 'payPrice',
      title: '实付金额(元)',
      formatter: 'formatAmount2',
      minWidth: 180,
    },
    {
      field: 'storeStaffName',
      title: '核销员',
      minWidth: 160,
    },
    {
      field: 'pickUpStoreId',
      title: '核销门店',
      minWidth: 160,
      formatter: ({ row }) => {
        return (
          pickUpStoreList.value.find((item) => item.id === row.pickUpStoreId)
            ?.name || ''
        );
      },
    },
    {
      field: 'payStatus',
      title: '支付状态',
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.INFRA_BOOLEAN_STRING },
      },
      minWidth: 80,
    },
    {
      field: 'status',
      title: '订单状态',
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.TRADE_ORDER_STATUS },
      },
      minWidth: 80,
    },
    {
      field: 'createTime',
      title: '下单时间',
      formatter: 'formatDateTime',
      minWidth: 160,
    },
  ];
}
