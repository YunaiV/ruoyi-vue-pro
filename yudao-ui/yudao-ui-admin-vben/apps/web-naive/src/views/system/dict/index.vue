<script lang="ts" setup>
import { ref } from 'vue';

import { DocAlert, Page } from '@vben/common-ui';

import DataGrid from './modules/data-grid.vue';
import TypeGrid from './modules/type-grid.vue';

const searchDictType = ref<string>(); // 搜索的字典类型

function handleDictTypeSelect(dictType: string) {
  searchDictType.value = dictType;
}
</script>

<template>
  <Page auto-content-height>
    <template #doc>
      <DocAlert title="字典管理" url="https://doc.iocoder.cn/system-dict/" />
    </template>

    <div class="flex h-full">
      <!-- 左侧字典类型列表 -->
      <div class="w-1/2 pr-3">
        <TypeGrid @select="handleDictTypeSelect" />
      </div>
      <!-- 右侧字典数据列表 -->
      <div class="w-1/2">
        <DataGrid :dict-type="searchDictType" />
      </div>
    </div>
  </Page>
</template>
