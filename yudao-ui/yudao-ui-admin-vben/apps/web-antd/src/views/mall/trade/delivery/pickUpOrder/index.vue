<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { MallOrderApi } from '#/api/mall/trade/order';

import { h, ref } from 'vue';

import { Page, prompt, SummaryCard } from '@vben/common-ui';
import { DeliveryTypeEnum } from '@vben/constants';
import { $t } from '@vben/locales';
import { fenToYuan } from '@vben/utils';

import { Card, Image, Input, message, Tag } from 'ant-design-vue';

import { TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import {
  getOrderPage,
  getOrderSummary,
  pickUpOrderByVerifyCode,
} from '#/api/mall/trade/order';

import { useGridColumns, useGridFormSchema } from './data';

const summary = ref<MallOrderApi.OrderSummaryRespVO>();

/** 刷新表格 */
function handleRefresh() {
  gridApi.query();
}

/** 获取订单统计数据 */
async function getOrderSum() {
  const query = await gridApi.formApi.getValues();
  query.deliveryType = DeliveryTypeEnum.PICK_UP.type;
  summary.value = await getOrderSummary(query);
}

/** 核销订单 */
async function handlePickup(pickUpVerifyCode?: string) {
  // 如果没有传核销码，则弹窗输入
  if (!pickUpVerifyCode) {
    await prompt({
      component: () => {
        return h(Input, {});
      },
      content: '请输入核销码',
      title: '核销订单',
      modelPropName: 'value',
    }).then(async (val) => {
      if (val) {
        pickUpVerifyCode = val;
      }
    });
  }
  if (!pickUpVerifyCode) {
    return;
  }

  // 执行核销
  const hideLoading = message.loading({
    content: '订单核销中 ...',
    duration: 0,
  });
  try {
    await pickUpOrderByVerifyCode(pickUpVerifyCode);
    message.success($t('ui.actionMessage.operationSuccess'));
    handleRefresh();
  } finally {
    hideLoading();
  }
}

const port = ref('');
const ports = ref([]);
const reader = ref('');
const serialPort = ref(false); // 是否连接扫码枪

/** 连接扫码枪 */
async function connectToSerialPort() {
  try {
    // 判断浏览器支持串口通信
    if (
      'serial' in navigator &&
      navigator.serial !== null &&
      typeof navigator.serial === 'object' &&
      'requestPort' in navigator.serial
    ) {
      // 提示用户选择一个串口
      port.value = await (navigator.serial as any).requestPort();
    } else {
      message.error('浏览器不支持扫码枪连接，请更换浏览器重试');
      return;
    }

    // 获取用户之前授予该网站访问权限的所有串口
    ports.value = await (navigator.serial as any).getPorts();

    // 等待串口打开
    await (port.value as any).open({
      baudRate: 9600,
      dataBits: 8,
      stopBits: 2,
    });

    message.success('成功连接扫码枪');
    serialPort.value = true;
    await readData();
  } catch (error) {
    // 处理连接串口出错的情况
    console.error('Error connecting to serial port:', error);
  }
}

/** 监听扫码枪输入 */
async function readData() {
  reader.value = (port.value as any).readable.getReader();
  let data = ''; // 扫码数据
  // 监听来自串口的数据
  while (true) {
    const { value, done } = await (reader.value as any).read();
    if (done) {
      // 允许稍后关闭串口
      (reader.value as any).releaseLock();
      break;
    }
    // 获取发送的数据
    const serialData = new TextDecoder().decode(value);
    data = `${data}${serialData}`;
    if (serialData.includes('\r')) {
      // 读取结束
      const codeData = data.replace('\r', '');
      data = ''; // 清空下次读取不会叠加
      console.warn(`二维码数据:${codeData}`);
      // 处理拿到数据逻辑
      await handlePickup(codeData);
    }
  }
}

/** 断开扫码枪连接 */
async function cutPort() {
  if (port.value === '') {
    message.warning('请先连接或打开扫码枪');
  } else {
    await (reader.value as any).cancel();
    await (port.value as any).close();
    port.value = '';
    console.warn('断开扫码枪连接');
    message.success('已成功断开扫码枪连接');
    serialPort.value = false;
  }
}

const [Grid, gridApi] = useVbenVxeGrid({
  formOptions: {
    schema: useGridFormSchema(),
  },
  gridOptions: {
    cellConfig: {
      height: 80,
    },
    columns: useGridColumns(),
    height: 'auto',
    keepSource: true,
    proxyConfig: {
      ajax: {
        query: async ({ page }, formValues) => {
          await getOrderSum();
          return await getOrderPage({
            pageNo: page.currentPage,
            pageSize: page.pageSize,
            deliveryType: DeliveryTypeEnum.PICK_UP.type,
            ...formValues,
          });
        },
      },
    },
    rowConfig: {
      keyField: 'id',
      isHover: true,
    },
    toolbarConfig: {
      refresh: true,
      search: true,
    },
  } as VxeTableGridOptions<MallOrderApi.Order>,
});
</script>

<template>
  <Page auto-content-height>
    <Card class="mb-2">
      <div class="flex flex-row gap-4">
        <SummaryCard
          class="flex flex-1"
          title="订单数量"
          icon="icon-park-outline:transaction-order"
          icon-color="bg-blue-100"
          icon-bg-color="text-blue-500"
          :value="summary?.orderCount || 0"
        />
        <SummaryCard
          class="flex flex-1"
          title="订单金额"
          icon="streamline:money-cash-file-dollar-common-money-currency-cash-file"
          icon-color="bg-purple-100"
          icon-bg-color="text-purple-500"
          prefix="￥"
          :decimals="2"
          :value="Number(fenToYuan(summary?.orderPayPrice || 0))"
        />
        <SummaryCard
          class="flex flex-1"
          title="退款单数"
          icon="heroicons:receipt-refundAfterSale"
          icon-color="bg-yellow-100"
          icon-bg-color="text-yellow-500"
          :value="summary?.afterSaleCount || 0"
        />
        <SummaryCard
          class="flex flex-1"
          title="退款金额"
          icon="ri:refundAfterSale-2-line"
          icon-color="bg-green-100"
          icon-bg-color="text-green-500"
          prefix="￥"
          :decimals="2"
          :value="Number(fenToYuan(summary?.afterSalePrice || 0))"
        />
      </div>
    </Card>

    <Grid class="h-4/5" table-title="核销订单">
      <template #spuName="{ row }">
        <div class="flex flex-col gap-2">
          <div
            v-for="item in row.items"
            :key="item.id!"
            class="flex items-start gap-2 text-left"
          >
            <Image
              :src="item.picUrl"
              :alt="item.spuName"
              :width="60"
              :height="60"
              :preview="{
                src: item.picUrl,
              }"
            />
            <div class="flex flex-1 flex-col gap-1">
              <span class="text-sm">{{ item.spuName }}</span>
              <div class="flex flex-wrap gap-1">
                <Tag
                  v-for="property in item.properties"
                  :key="property.propertyId!"
                  size="small"
                >
                  {{ property.propertyName }}: {{ property.valueName }}
                </Tag>
              </div>
              <span class="text-xs text-gray-500">
                {{ fenToYuan(item.price!) }} 元 x {{ item.count }}
              </span>
            </div>
          </div>
        </div>
      </template>
      <template #toolbar-tools>
        <TableAction
          :actions="[
            {
              label: '核销',
              type: 'primary',
              icon: 'lucide:circle-check-big',
              auth: ['trade:order:pick-up'],
              onClick: handlePickup.bind(null, undefined),
            },
            {
              label: serialPort ? '断开扫描枪' : '连接扫描枪',
              type: 'primary',
              icon: serialPort ? 'lucide:circle-x' : 'lucide:circle-play',
              danger: serialPort,
              onClick: serialPort ? cutPort : connectToSerialPort,
            },
          ]"
        />
      </template>
    </Grid>
  </Page>
</template>
