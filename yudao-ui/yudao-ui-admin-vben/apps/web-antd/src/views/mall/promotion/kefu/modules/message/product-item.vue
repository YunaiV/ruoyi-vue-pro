<script lang="ts" setup>
import { useRouter } from 'vue-router';

import { fenToYuan } from '@vben/utils';

import { Button, Image } from 'ant-design-vue';

defineProps({
  spuId: {
    type: Number,
    default: 0,
  },
  picUrl: {
    type: String,
    default: 'https://img1.baidu.com/it/u=1601695551,235775011&fm=26&fmt=auto',
  },
  title: {
    type: String,
    default: '',
  },
  price: {
    type: [String, Number],
    default: '',
  },
  salesCount: {
    type: [String, Number],
    default: '',
  },
  stock: {
    type: [String, Number],
    default: '',
  },
});

const { push } = useRouter();

/** 查看商品详情 */
function openDetail(spuId: number) {
  push({ name: 'ProductSpuDetail', params: { id: spuId } });
}
</script>

<template>
  <div
    class="mb-2.5 flex w-full cursor-pointer items-center rounded-lg bg-gray-500/30 p-2.5"
    @click.stop="openDetail(spuId)"
  >
    <!-- 左侧商品图片-->
    <div class="mr-2 w-16">
      <Image
        :initial-index="0"
        :preview-src-list="[picUrl]"
        :src="picUrl"
        class="h-full w-full rounded-lg"
        fit="contain"
        preview-teleported
        @click.stop
      />
    </div>
    <!-- 右侧商品信息 -->
    <div class="flex-1">
      <div class="line-clamp-1 w-full text-base font-bold">{{ title }}</div>
      <div class="my-1">
        <span class="mr-5">库存: {{ stock || 0 }}</span>
        <span>销量: {{ salesCount || 0 }}</span>
      </div>
      <div class="flex items-center justify-between">
        <span class="text-red-500">￥{{ fenToYuan(price) }}</span>
        <Button size="small" text type="primary">详情</Button>
      </div>
    </div>
  </div>
</template>
