<script setup lang="ts">
import { Page } from '@vben/common-ui';

import { Col, Row } from 'ant-design-vue';

// 导入业务逻辑
import { useIotHome } from './data';
// 导入组件
import ComparisonCard from './modules/ComparisonCard.vue';
import DeviceCountCard from './modules/DeviceCountCard.vue';
import DeviceStateCountCard from './modules/DeviceStateCountCard.vue';
import MessageTrendCard from './modules/MessageTrendCard.vue';

defineOptions({ name: 'IoTHome' });

// 使用业务逻辑 Hook
const { loading, statsData } = useIotHome();
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
