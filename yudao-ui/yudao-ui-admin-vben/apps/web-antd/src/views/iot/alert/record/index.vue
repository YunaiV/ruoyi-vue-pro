<script setup lang="ts">
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { AlertRecord } from '#/api/iot/alert/record';

import { h, onMounted, ref } from 'vue';

import { Page } from '@vben/common-ui';
import { IconifyIcon } from '@vben/icons';

import { Button, message, Modal, Popover, Tag } from 'ant-design-vue';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import { getAlertRecordPage, processAlertRecord } from '#/api/iot/alert/record';
import { getSimpleDeviceList } from '#/api/iot/device/device';
import { getSimpleProductList } from '#/api/iot/product/product';

import { useGridColumns, useGridFormSchema } from './data';

defineOptions({ name: 'IoTAlertRecord' });

const productList = ref<any[]>([]);
const deviceList = ref<any[]>([]);

/** 刷新表格 */
function handleRefresh() {
  gridApi.query();
}

// 加载产品和设备列表
async function loadData() {
  productList.value = await getSimpleProductList();
  deviceList.value = await getSimpleDeviceList();
}

// 获取告警级别文本
function getLevelText(level?: number) {
  const levelMap: Record<number, string> = {
    1: '提示',
    2: '一般',
    3: '警告',
    4: '严重',
    5: '紧急',
  };
  return level ? levelMap[level] || `级别${level}` : '-';
}

// 获取告警级别颜色
function getLevelColor(level?: number) {
  const colorMap: Record<number, string> = {
    1: 'blue',
    2: 'green',
    3: 'orange',
    4: 'red',
    5: 'purple',
  };
  return level ? colorMap[level] || 'default' : 'default';
}

// 获取产品名称
function getProductName(productId?: number) {
  if (!productId) return '-';
  const product = productList.value.find((p: any) => p.id === productId);
  return product?.name || '加载中...';
}

// 获取设备名称
function getDeviceName(deviceId?: number) {
  if (!deviceId) return '-';
  const device = deviceList.value.find((d: any) => d.id === deviceId);
  return device?.deviceName || '加载中...';
}

// 处理告警记录
async function handleProcess(row: AlertRecord) {
  Modal.confirm({
    title: '处理告警记录',
    content: h('div', [
      h('p', '请输入处理原因：'),
      h('textarea', {
        id: 'processRemark',
        class: 'ant-input',
        rows: 3,
        placeholder: '请输入处理原因',
      }),
    ]),
    async onOk() {
      const textarea = document.querySelector(
        '#processRemark',
      ) as HTMLTextAreaElement;
      const processRemark = textarea?.value || '';

      if (!processRemark) {
        message.warning('请输入处理原因');
        throw new Error('请输入处理原因');
      }

      const hideLoading = message.loading({
        content: '正在处理...',
        duration: 0,
      });
      try {
        await processAlertRecord(row.id as number, processRemark);
        message.success('处理成功');
        handleRefresh();
      } catch (error) {
        console.error('处理失败:', error);
        throw error;
      } finally {
        hideLoading();
      }
    },
  });
}

// 查看告警记录详情
function handleView(row: AlertRecord) {
  Modal.info({
    title: '告警记录详情',
    width: 600,
    content: h('div', { class: 'space-y-2' }, [
      h('div', [
        h('span', { class: 'font-semibold' }, '告警名称：'),
        h('span', row.configName || '-'),
      ]),
      h('div', [
        h('span', { class: 'font-semibold' }, '告警级别：'),
        h('span', getLevelText(row.configLevel)),
      ]),
      h('div', [
        h('span', { class: 'font-semibold' }, '设备消息：'),
        h(
          'pre',
          { class: 'mt-1 text-xs bg-gray-50 p-2 rounded' },
          row.deviceMessage || '-',
        ),
      ]),
      h('div', [
        h('span', { class: 'font-semibold' }, '处理结果：'),
        h('span', row.processRemark || '-'),
      ]),
      h('div', [
        h('span', { class: 'font-semibold' }, '处理时间：'),
        h(
          'span',
          row.processTime
            ? new Date(row.processTime).toLocaleString('zh-CN')
            : '-',
        ),
      ]),
    ]),
  });
}

const [Grid, gridApi] = useVbenVxeGrid({
  formOptions: {
    schema: useGridFormSchema(),
  },
  gridOptions: {
    columns: useGridColumns(),
    height: 'auto',
    keepSource: true,
    proxyConfig: {
      ajax: {
        query: async ({ page }, formValues) => {
          return await getAlertRecordPage({
            pageNo: page.currentPage,
            pageSize: page.pageSize,
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
  } as VxeTableGridOptions<AlertRecord>,
});

onMounted(() => {
  loadData();
});
</script>

<template>
  <Page auto-content-height>
    <Grid table-title="告警记录列表">
      <!-- 告警级别列 -->
      <template #configLevel="{ row }">
        <Tag :color="getLevelColor(row.configLevel)">
          {{ getLevelText(row.configLevel) }}
        </Tag>
      </template>

      <!-- 产品名称列 -->
      <template #product="{ row }">
        <span>{{ getProductName(row.productId) }}</span>
      </template>

      <!-- 设备名称列 -->
      <template #device="{ row }">
        <span>{{ getDeviceName(row.deviceId) }}</span>
      </template>

      <!-- 设备消息列 -->
      <template #deviceMessage="{ row }">
        <Popover
          v-if="row.deviceMessage"
          placement="topLeft"
          trigger="hover"
          :overlay-style="{ maxWidth: '600px' }"
        >
          <template #content>
            <pre class="text-xs">{{ row.deviceMessage }}</pre>
          </template>
          <Button size="small" type="link">
            <IconifyIcon icon="ant-design:eye-outlined" class="mr-1" />
            查看消息
          </Button>
        </Popover>
        <span v-else class="text-gray-400">-</span>
      </template>

      <!-- 操作列 -->
      <template #actions="{ row }">
        <TableAction
          :actions="[
            {
              label: '处理',
              type: 'link',
              icon: ACTION_ICON.EDIT,
              onClick: handleProcess.bind(null, row),
              ifShow: !row.processStatus,
            },
            {
              label: '查看',
              type: 'link',
              icon: ACTION_ICON.VIEW,
              onClick: handleView.bind(null, row),
              ifShow: row.processStatus,
            },
          ]"
        />
      </template>
    </Grid>
  </Page>
</template>
