<template>
  <div class="page">
    <div class="page-header">
      <h2>商品管理</h2>
      <p class="page-subtitle">AI 帮你查询、定价、管理商品</p>
    </div>

    <!-- 商品卡片列表（预留，实际数据通过 API 获取） -->
    <div class="empty-tip" v-if="!products.length">
      💬 在右下角 AI 助手问我：「外套定什么价合适？」或「最近有哪些商品在卖？」
    </div>

    <div v-else class="product-grid">
      <div v-for="p in products" :key="p.id" class="product-card">
        <img :src="p.image" class="product-card__img" />
        <div class="product-card__info">
          <div class="product-card__title">{{ p.title }}</div>
          <div class="product-card__price">¥{{ p.price }}</div>
          <div class="product-card__stock">库存 {{ p.stock }} 件</div>
        </div>
      </div>
    </div>

    <!-- AI 对话助手 -->
    <AiChatDrawer
      module="product"
      :customer-id="customerId"
      greeting="你好！问我关于商品的任何问题，例如：「外套应该定多少钱？」或「帮我查一下最近上架的商品」"
    />
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import AiChatDrawer from '@/components/AiChat/AiChatDrawer.vue'

const customerId = ref<number | undefined>(undefined)

interface Product {
  id: string
  title: string
  price: number
  stock: number
  image: string
}
const products = ref<Product[]>([])
</script>

<style scoped>
.page { padding: 24px; }
.page-header { margin-bottom: 24px; }
.page-header h2 { font-size: 22px; font-weight: 700; color: #1f2937; margin: 0 0 4px; }
.page-subtitle  { color: #6b7280; font-size: 14px; margin: 0; }
.empty-tip {
  text-align: center;
  color: #9ca3af;
  font-size: 15px;
  padding: 60px 20px;
  line-height: 1.8;
}
.product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: 16px;
}
.product-card { border-radius: 12px; overflow: hidden; border: 1px solid #e5e7eb; }
.product-card__img   { width: 100%; height: 200px; object-fit: cover; }
.product-card__info  { padding: 12px; }
.product-card__title { font-weight: 600; font-size: 14px; color: #111827; margin-bottom: 4px; }
.product-card__price { color: #6366f1; font-size: 15px; font-weight: 700; }
.product-card__stock { color: #9ca3af; font-size: 12px; margin-top: 2px; }
</style>
