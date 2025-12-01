<script setup lang="ts">
// TODO @芋艿
import type { StatsData } from './data';

import { onMounted, ref } from 'vue';

import { ComparisonCard, Page } from '@vben/common-ui';

import { Col, Row } from 'ant-design-vue';

import { getStatisticsSummary } from '#/api/iot/statistics';

import { defaultStatsData } from './data';
import DeviceCountCard from './modules/device-count-card.vue';
import DeviceStateCountCard from './modules/device-state-count-card.vue';
import MessageTrendCard from './modules/message-trend-card.vue';

defineOptions({ name: 'IoTHome' });

const loading = ref(true);
const statsData = ref<StatsData>(defaultStatsData);

/** 加载统计数据 */
async function loadStatisticsData(): Promise<StatsData> {
  try {
    return await getStatisticsSummary();
  } catch (error) {
    // TODO @haohao：后续记得删除下哈。catch 部分可以删除
    // 开发环境：记录错误信息，便于调试
    console.error('获取统计数据出错:', error);
    // 开发环境：提示使用 Mock 数据，提醒检查后端接口
    console.warn('使用 Mock 数据，请检查后端接口是否已实现');

    // TODO @haohao：后续记得删除下哈。
    // 开发调试：返回 Mock 数据，确保前端功能正常开发
    // 生产环境：建议移除 Mock 数据，直接抛出错误或返回空数据
    return {
      productCategoryCount: 12,
      productCount: 45,
      deviceCount: 328,
      deviceMessageCount: 15_678,
      productCategoryTodayCount: 2,
      productTodayCount: 5,
      deviceTodayCount: 23,
      deviceMessageTodayCount: 1234,
      deviceOnlineCount: 256,
      deviceOfflineCount: 48,
      deviceInactiveCount: 24,
      productCategoryDeviceCounts: {
        智能家居: 120,
        工业设备: 98,
        环境监测: 65,
        智能穿戴: 45,
      },
    };
  }
}

/** 加载数据 */
async function loadData() {
  loading.value = true;
  try {
    statsData.value = await loadStatisticsData();
  } catch (error) {
    // TODO @haohao：后续记得删除下哈。catch 部分可以删除
    console.error('获取统计数据出错:', error);
  } finally {
    loading.value = false;
  }
}

/** 组件挂载时加载数据 */
onMounted(() => {
  loadData();
});
</script>

<template>
  <Page>
    <!-- 第一行：统计卡片 -->
    <Row :gutter="16" class="mb-4">
      <Col :span="6">
        <ComparisonCard
          title="分类数量"
          :value="statsData.productCategoryCount"
          :today-count="statsData.productCategoryTodayCount"
          icon="menu"
          icon-color="text-blue-500"
          :loading="loading"
        />
      </Col>
      <Col :span="6">
        <ComparisonCard
          title="产品数量"
          :value="statsData.productCount"
          :today-count="statsData.productTodayCount"
          icon="box"
          icon-color="text-orange-500"
          :loading="loading"
        />
      </Col>
      <Col :span="6">
        <ComparisonCard
          title="设备数量"
          :value="statsData.deviceCount"
          :today-count="statsData.deviceTodayCount"
          icon="cpu"
          icon-color="text-purple-500"
          :loading="loading"
        />
      </Col>
      <Col :span="6">
        <ComparisonCard
          title="设备消息数"
          :value="statsData.deviceMessageCount"
          :today-count="statsData.deviceMessageTodayCount"
          icon="message"
          icon-color="text-teal-500"
          :loading="loading"
        />
      </Col>
    </Row>

    <!-- 第二行：图表 -->
    <Row :gutter="16" class="mb-4">
      <Col :span="12">
        <DeviceCountCard :stats-data="statsData" :loading="loading" />
      </Col>
      <Col :span="12">
        <DeviceStateCountCard :stats-data="statsData" :loading="loading" />
      </Col>
    </Row>

    <!-- 第三行：消息统计 -->
    <Row :gutter="16">
      <Col :span="24">
        <MessageTrendCard />
      </Col>
    </Row>
  </Page>
</template>

<style scoped>
:deep(.vben-page-content) {
  padding: 16px;
}
</style>
