<script lang="ts" setup>
import type { WxLocationProps } from './types';

import { computed, onMounted, ref } from 'vue';

import { IconifyIcon } from '@vben/icons';

import { Col, message, Row } from 'ant-design-vue';

import { getTradeConfig } from '#/api/mall/trade/config';

/** 微信消息 - 定位 */
defineOptions({ name: 'WxLocation' });

const props = withDefaults(defineProps<WxLocationProps>(), {
  qqMapKey: '', // QQ 地图的密钥 https://lbs.qq.com/service/staticV2/staticGuide/staticDoc
});

const fetchedQqMapKey = ref('');
const resolvedQqMapKey = computed(
  () => props.qqMapKey || fetchedQqMapKey.value || '',
);

const mapUrl = computed(() => {
  return `https://map.qq.com/?type=marker&isopeninfowin=1&markertype=1&pointx=${props.locationY}&pointy=${props.locationX}&name=${props.label}&ref=yudao`;
});

const mapImageUrl = computed(() => {
  return `https://apis.map.qq.com/ws/staticmap/v2/?zoom=10&markers=color:blue|label:A|${props.locationX},${props.locationY}&key=${resolvedQqMapKey.value}&size=250*180`;
});

async function fetchQqMapKey() {
  try {
    const data = await getTradeConfig();
    fetchedQqMapKey.value = data.tencentLbsKey ?? '';
    if (!fetchedQqMapKey.value) {
      message.warning('请先配置腾讯位置服务密钥');
    }
  } catch {
    message.error('获取腾讯位置服务密钥失败');
  }
}

onMounted(async () => {
  if (!props.qqMapKey) {
    await fetchQqMapKey();
  }
});

defineExpose({
  locationX: props.locationX,
  locationY: props.locationY,
  label: props.label,
  qqMapKey: resolvedQqMapKey,
});
</script>

<template>
  <div>
    <a :href="mapUrl" target="_blank" class="text-primary">
      <Col>
        <Row>
          <img :src="mapImageUrl" alt="地图位置" />
        </Row>
        <Row class="mt-2">
          <IconifyIcon icon="lucide:map-pin" class="mr-1" />
          {{ label }}
        </Row>
      </Col>
    </a>
  </div>
</template>
