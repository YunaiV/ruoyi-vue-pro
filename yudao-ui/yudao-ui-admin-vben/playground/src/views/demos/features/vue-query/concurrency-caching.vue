<script lang="ts" setup>
import type { Recordable } from '@vben/types';

import { useQuery } from '@tanstack/vue-query';

import { useVbenForm } from '#/adapter/form';
import { getMenuList } from '#/api';

const queryKey = ['demo', 'api', 'options'];
const count = 4;

const { dataUpdatedAt, promise: fetchDataFn } = useQuery({
  // 在组件渲染期间预取数据
  experimental_prefetchInRender: true,
  // 获取接口数据的函数
  queryFn: getMenuList,
  queryKey,
  // 每次组件挂载时都重新获取数据。如果不需要每次都重新获取就不要设置为always
  refetchOnMount: 'always',
  // 缓存时间
  staleTime: 1000 * 60 * 5,
});

async function fetchOptions() {
  return await fetchDataFn.value;
}

const schema = [];

for (let i = 0; i < count; i++) {
  schema.push({
    component: 'ApiSelect',
    componentProps: {
      api: fetchOptions,
      class: 'w-full',
      filterOption: (input: string, option: Recordable<any>) => {
        return option.label.toLowerCase().includes(input.toLowerCase());
      },
      labelField: 'name',
      showSearch: true,
      valueField: 'id',
    },
    fieldName: `field${i}`,
    label: `Select ${i}`,
  });
}

const [Form] = useVbenForm({
  schema,
  showDefaultActions: false,
});
</script>
<template>
  <div>
    <div class="mb-2 flex gap-2">
      <div>以下{{ count }}个组件共用一个数据源。</div>
      <div>缓存更新时间：{{ new Date(dataUpdatedAt).toLocaleString() }}</div>
    </div>
    <Form />
  </div>
</template>
