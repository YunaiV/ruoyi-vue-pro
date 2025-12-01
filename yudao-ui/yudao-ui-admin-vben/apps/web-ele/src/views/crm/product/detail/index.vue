<script setup lang="ts">
import type { CrmProductApi } from '#/api/crm/product';
import type { SystemOperateLogApi } from '#/api/system/operate-log';

import { onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';

import { Page } from '@vben/common-ui';
import { useTabs } from '@vben/hooks';

import { ElButton, ElCard, ElTabPane, ElTabs } from 'element-plus';

import { getOperateLogPage } from '#/api/crm/operateLog';
import { BizTypeEnum } from '#/api/crm/permission';
import { getProduct } from '#/api/crm/product';
import { useDescription } from '#/components/description';
import { OperateLog } from '#/components/operate-log';

import { useDetailSchema } from './data';
import Info from './modules/info.vue';

const route = useRoute();
const router = useRouter();
const tabs = useTabs();

const loading = ref(false); // 加载中
const productId = ref(0); // 产品编号
const product = ref<CrmProductApi.Product>({} as CrmProductApi.Product); // 产品详情
const logList = ref<SystemOperateLogApi.OperateLog[]>([]); // 操作日志
const activeTabName = ref('1'); // 选中 Tab 名

const [Descriptions] = useDescription({
  border: false,
  column: 4,
  schema: useDetailSchema(),
});

/** 加载详情 */
async function getProductDetail() {
  loading.value = true;
  try {
    product.value = await getProduct(productId.value);
    // 操作日志
    const res = await getOperateLogPage({
      bizType: BizTypeEnum.CRM_PRODUCT,
      bizId: productId.value,
    });
    logList.value = res.list;
  } finally {
    loading.value = false;
  }
  loading.value = false;
}

/** 返回列表页 */
function handleBack() {
  tabs.closeCurrentTab();
  router.push({ name: 'CrmProduct' });
}

/** 加载数据 */
onMounted(() => {
  productId.value = Number(route.params.id);
  getProductDetail();
});
</script>

<template>
  <Page auto-content-height :title="product?.name" :loading="loading">
    <template #extra>
      <div class="flex items-center gap-2">
        <ElButton @click="handleBack"> 返回 </ElButton>
      </div>
    </template>
    <ElCard class="min-h-[10%]">
      <Descriptions :data="product" />
    </ElCard>
    <ElCard class="mt-4 min-h-[60%]">
      <ElTabs v-model:model-value="activeTabName">
        <ElTabPane label="详细资料" name="1">
          <Info :product="product" />
        </ElTabPane>
        <ElTabPane label="操作日志" name="2">
          <OperateLog :log-list="logList" />
        </ElTabPane>
      </ElTabs>
    </ElCard>
  </Page>
</template>
