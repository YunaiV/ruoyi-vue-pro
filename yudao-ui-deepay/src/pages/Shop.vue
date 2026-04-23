<!--
  Shop.vue — 店铺总入口（路由层）
  路径：/shop/:id?ref=userId

  逻辑：
  1. 先检查 localStorage shop_{id}（模板店 / AI店）
  2. 否则调 API 获取商品数据
  3. 根据 shop.id / shop.type 切换模板组件
  4. buy 事件 → createOrder(shopId, amount, currency, refUser) → Jeepay payUrl
  5. share 事件 → 生成带 ?ref=myUserId 的分享链接
  6. ?ref= 首次写入 localStorage（不覆盖已有值，防多次覆盖）
-->
<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getShop } from '@/api/shop'
import { createOrder } from '@/api'
import { initUserId, captureRef, getRefUser, buildShareLink, shareOrCopy } from '@/utils/user'

import TemplateMinimal from '@/components/templates/TemplateMinimal.vue'
import TemplateStreet  from '@/components/templates/TemplateStreet.vue'
import TemplateLuxury  from '@/components/templates/TemplateLuxury.vue'
import TemplateGrid    from '@/components/templates/TemplateGrid.vue'
import TemplateSingle  from '@/components/templates/TemplateSingle.vue'
import TemplateAI      from '@/components/templates/TemplateAI.vue'

// template-id → component（模板名 + 布局类型 双映射）
const TEMPLATE_MAP = {
  // 按模板 id
  minimal:    TemplateMinimal,
  street:     TemplateStreet,
  luxury:     TemplateLuxury,
  ecommerce:  TemplateGrid,
  spotlight:  TemplateSingle,
  // 按布局 type（AI商品 / 旧数据 fallback）
  single:     TemplateSingle,
  grid:       TemplateGrid,
  ai:         TemplateAI,
}

const route  = useRoute()
const router = useRouter()

const shop     = ref(null)
const loading  = ref(true)
const error    = ref('')
const ordering = ref(false)

// 当前用户 id（用于生成自己的分享链接）
const MY_USER_ID = initUserId()

// 选出模板组件（id 优先，type 兜底）
const CurrentTemplate = computed(() =>
  shop.value
    ? (TEMPLATE_MAP[shop.value.id] || TEMPLATE_MAP[shop.value.type] || TemplateMinimal)
    : null
)

onMounted(async () => {
  // ── 裂变：首次写入推荐人，不覆盖已有值（防刷）──────────────────────
  captureRef(route.query.ref, MY_USER_ID)

  // ── 加载店铺数据 ──────────────────────────────────────────────────
  const shopId = route.params.id
  try {
    const data = await getShop(shopId)
    // Normalize API response into unified shop structure when it doesn't
    // already contain a theme (i.e. it came from the backend, not localStorage)
    if (data && !data.theme) {
      shop.value = {
        id:       data.type || 'minimal',
        type:     data.type || 'minimal',
        name:     data.title,
        theme: {
          bg:      '#0B0B0B',
          card:    '#111111',
          border:  '#1A1A1A',
          text:    '#FFFFFF',
          subText: '#9CA3AF',
          primary: '#00FF88',
        },
        gradient: '#111',
        products: [{
          img:   data.image,
          title: data.title,
          price: data.price,
        }],
        _raw: data,
      }
    } else {
      shop.value = data
    }
  } catch {
    error.value = '商品加载失败'
  } finally {
    loading.value = false
  }
})

// ── 购买 → 统一走 Jeepay 支付 ─────────────────────────────────────────
async function onBuy() {
  if (ordering.value || !shop.value) return

  const product = shop.value.products?.[0] || {}
  const amount  = Number(product.price) || 0

  // 未设价格时退化为分享（纯展示模板）
  if (!amount) {
    onShare()
    return
  }

  ordering.value = true
  error.value    = ''
  try {
    const refUser = getRefUser()
    const order   = await createOrder(route.params.id, amount, 'EUR', refUser)
    if (order?.payUrl) {
      window.location.href = order.payUrl
    } else {
      error.value = '支付链接获取失败，请重试'
    }
  } catch {
    error.value = '下单失败，请检查网络后重试'
  } finally {
    ordering.value = false
  }
}

// ── 分享（带 ?ref=myUserId 裂变链接）─────────────────────────────────
function onShare() {
  const link = buildShareLink(route.params.id, MY_USER_ID)
  shareOrCopy(link, shop.value?.name || 'Deepay')
}
</script>

<template>
  <div>

    <!-- Loading -->
    <div v-if="loading"
         class="min-h-screen bg-bg flex items-center justify-center">
      <div class="text-center">
        <div class="w-8 h-8 border-2 border-accent border-t-transparent
                    rounded-full animate-spin mx-auto mb-3" />
        <p class="text-muted text-sm">加载中…</p>
      </div>
    </div>

    <!-- 错误 -->
    <div v-else-if="error || !shop"
         class="min-h-screen bg-bg text-white
                flex flex-col items-center justify-center gap-4 px-6 text-center">
      <p class="text-4xl">😕</p>
      <p class="text-muted text-sm">{{ error || '店铺不存在' }}</p>
      <button class="btn-primary max-w-[200px]"
              @click="router.push('/')">返回首页</button>
    </div>

    <!-- 模板组件（动态挂载）-->
    <component
      v-else
      :is="CurrentTemplate"
      :shop="shop"
      @buy="onBuy"
      @share="onShare"
    />

  </div>
</template>
