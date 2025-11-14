<script lang="ts" setup>
import type { RouteLocationNormalized } from 'vue-router';

import { computed, ref } from 'vue';
import { useRoute } from 'vue-router';

import { preferences } from '@vben/preferences';
import { useTabbarStore } from '@vben/stores';

import { VbenSpinner } from '@vben-core/shadcn-ui';

defineOptions({ name: 'IFrameRouterView' });

const spinningList = ref<boolean[]>([]);
const tabbarStore = useTabbarStore();
const route = useRoute();

const enableTabbar = computed(() => preferences.tabbar.enable);

const iframeRoutes = computed(() => {
  if (!enableTabbar.value) {
    return route.meta.iframeSrc ? [route] : [];
  }
  return tabbarStore.getTabs.filter((tab) => !!tab.meta?.iframeSrc);
});

const tabNames = computed(
  () => new Set(iframeRoutes.value.map((item) => item.name as string)),
);

const showIframe = computed(() => iframeRoutes.value.length > 0);

function routeShow(tabItem: RouteLocationNormalized) {
  return tabItem.name === route.name;
}

function canRender(tabItem: RouteLocationNormalized) {
  const { meta, name } = tabItem;

  if (!name || !tabbarStore.renderRouteView) {
    return false;
  }

  if (!enableTabbar.value) {
    return routeShow(tabItem);
  }

  // 跟随 keepAlive 状态,与其他tab页保持一致
  if (
    !meta?.keepAlive &&
    tabNames.value.has(name as string) &&
    name !== route.name
  ) {
    return false;
  }
  return tabbarStore.getTabs.some((tab) => tab.name === name);
}

function hideLoading(index: number) {
  spinningList.value[index] = false;
}

function showSpinning(index: number) {
  const curSpinning = spinningList.value[index];
  // 首次加载时显示loading
  return curSpinning === undefined ? true : curSpinning;
}
</script>
<template>
  <template v-if="showIframe">
    <template v-for="(item, index) in iframeRoutes" :key="item.fullPath">
      <div
        v-if="canRender(item)"
        v-show="routeShow(item)"
        class="relative size-full"
      >
        <VbenSpinner :spinning="showSpinning(index)" />
        <iframe
          :src="item.meta.iframeSrc as string"
          class="size-full"
          @load="hideLoading(index)"
        ></iframe>
      </div>
    </template>
  </template>
</template>
