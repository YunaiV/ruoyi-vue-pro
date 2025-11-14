<script setup lang="ts">
import type { Ref } from 'vue';

import type { IProducts } from './typing';

import { ref } from 'vue';

import { keepPreviousData, useQuery } from '@tanstack/vue-query';
import { Button } from 'ant-design-vue';

const LIMIT = 10;
const fetcher = async (page: Ref<number>): Promise<IProducts> => {
  const res = await fetch(
    `https://dummyjson.com/products?limit=${LIMIT}&skip=${(page.value - 1) * LIMIT}`,
  );
  return res.json();
};

const page = ref(1);
const { data, error, isError, isPending, isPlaceholderData } = useQuery({
  // The data from the last successful fetch is available while new data is being requested.
  placeholderData: keepPreviousData,
  queryFn: () => fetcher(page),
  queryKey: ['products', page],
});
const prevPage = () => {
  page.value = Math.max(page.value - 1, 1);
};
const nextPage = () => {
  if (!isPlaceholderData.value) {
    page.value = page.value + 1;
  }
};
</script>

<template>
  <div class="flex gap-4">
    <Button size="small" @click="prevPage">上一页</Button>
    <p>当前页: {{ page }}</p>
    <Button size="small" @click="nextPage">下一页</Button>
  </div>
  <div class="p-4">
    <div v-if="isPending">加载中...</div>
    <div v-else-if="isError">出错了: {{ error }}</div>
    <div v-else-if="data">
      <ul>
        <li v-for="item in data.products" :key="item.id">
          {{ item.title }}
        </li>
      </ul>
    </div>
  </div>
</template>
