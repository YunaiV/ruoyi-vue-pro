<!--
  ShopEdit.vue — 店铺商品管理
  路径：/shop/:id/edit

  功能：
  ✔ 编辑店铺名称
  ✔ 每个商品：编辑名称、价格、划线价、标签、简介、图片链接
  ✔ 新增商品
  ✔ 删除商品（需确认）
  ✔ 保存（本地 + 静默同步后端）
  ✔ 预览 / 分享店铺链接
-->
<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getShop, updateShop } from '@/api/shop'
import { buildShareLink, shareOrCopy, initUserId } from '@/utils/user'

const route  = useRoute()
const router = useRouter()
const shopId = route.params.id
const userId = initUserId()

const shop      = ref(null)
const loading   = ref(true)
const saving    = ref(false)
const saved     = ref(false)
const saveError = ref('')

// 每个商品展开编辑状态
const expanded  = ref({})

// 新增商品表单
const showAddForm = ref(false)
const newProduct  = ref({ name: '', price: '', originalPrice: '', desc: '', badge: '', img: '' })
const addError    = ref('')

const BADGE_OPTIONS = [
  { value: '',     label: '无标签' },
  { value: 'NEW',  label: 'NEW  · 新品' },
  { value: 'HOT',  label: 'HOT  · 热销' },
  { value: 'SALE', label: 'SALE · 折扣' },
]

onMounted(async () => {
  try {
    const data = await getShop(shopId)
    // 深拷贝，避免直接修改 localStorage 缓存引用
    shop.value = JSON.parse(JSON.stringify(data))
  } catch (_) {
    saveError.value = '店铺不存在或加载失败'
  } finally {
    loading.value = false
  }
})

function toggleExpand(index) {
  expanded.value = { ...expanded.value, [index]: !expanded.value[index] }
}

async function save() {
  if (!shop.value || saving.value) return
  saving.value  = true
  saved.value   = false
  saveError.value = ''
  try {
    await updateShop(shopId, {
      name:     shop.value.name,
      products: shop.value.products,
    })
    saved.value = true
    setTimeout(() => { saved.value = false }, 2000)
  } catch (_) {
    saveError.value = '保存失败，请重试'
  } finally {
    saving.value = false
  }
}

function confirmRemove(index) {
  if (!confirm(`确认删除「${shop.value.products[index]?.name || '商品'}」？`)) return
  shop.value.products.splice(index, 1)
  // 更新展开状态索引
  const next = {}
  Object.keys(expanded.value).forEach(k => {
    const n = Number(k)
    if (n < index) next[n] = expanded.value[k]
    else if (n > index) next[n - 1] = expanded.value[k]
  })
  expanded.value = next
  save()
}

function submitAdd() {
  addError.value = ''
  const name  = newProduct.value.name.trim()
  const price = Number(newProduct.value.price)
  if (!name)  { addError.value = '请填写商品名称'; return }
  if (!price) { addError.value = '请填写有效价格'; return }

  const product = {
    name,
    title:         name,
    price,
    originalPrice: newProduct.value.originalPrice ? Number(newProduct.value.originalPrice) : undefined,
    desc:          newProduct.value.desc.trim()  || undefined,
    badge:         newProduct.value.badge        || null,
    img:           newProduct.value.img.trim()   || undefined,
    gradient:      'linear-gradient(135deg,#111 0%,#1e2a3a 100%)',
  }
  shop.value.products = [...(shop.value.products || []), product]
  showAddForm.value   = false
  newProduct.value    = { name: '', price: '', originalPrice: '', desc: '', badge: '', img: '' }
  save()
}

function previewShop() {
  router.push(`/shop/${shopId}`)
}

function shareShop() {
  shareOrCopy(buildShareLink(shopId, userId), shop.value?.name || '我的店铺')
}

const fmt = v => v !== undefined && v !== null ? Number(v).toFixed(2) : ''
</script>

<template>
  <div class="min-h-screen bg-bg text-white">

    <!-- 顶部导航 -->
    <header class="sticky top-0 z-10 bg-bg/90 backdrop-blur-md
                   border-b border-border px-4 py-3
                   flex items-center gap-3">
      <button class="text-muted active:text-white transition-colors"
              @click="router.back()">
        <svg class="h-5 w-5" fill="none" viewBox="0 0 24 24"
             stroke="currentColor" stroke-width="2.5">
          <path stroke-linecap="round" stroke-linejoin="round" d="M15 19l-7-7 7-7"/>
        </svg>
      </button>
      <span class="font-semibold text-sm flex-1">编辑店铺</span>

      <!-- 保存按钮 -->
      <button
        :disabled="saving"
        class="text-sm font-bold px-4 py-1.5 rounded-full
               active:scale-95 transition-transform duration-100
               disabled:opacity-50 disabled:cursor-not-allowed"
        style="background:#1abc9c;color:#fff"
        @click="save"
      >
        {{ saving ? '保存中…' : saved ? '✔ 已保存' : '保存' }}
      </button>
    </header>

    <!-- 加载中 -->
    <div v-if="loading" class="flex items-center justify-center min-h-[60vh]">
      <div class="w-8 h-8 border-2 border-accent border-t-transparent
                  rounded-full animate-spin" />
    </div>

    <!-- 出错 -->
    <div v-else-if="saveError && !shop"
         class="flex flex-col items-center justify-center min-h-[60vh] gap-4 px-6 text-center">
      <p class="text-4xl">😕</p>
      <p class="text-muted text-sm">{{ saveError }}</p>
      <button class="btn-primary max-w-[200px]" @click="router.push('/me')">返回</button>
    </div>

    <template v-else-if="shop">
      <div class="max-w-[480px] mx-auto px-4 pt-5 pb-32 space-y-6">

        <!-- ── 店铺名 ──────────────────────────────────────── -->
        <section>
          <label class="text-xs text-muted uppercase tracking-widest block mb-2">店铺名称</label>
          <input
            v-model="shop.name"
            type="text"
            maxlength="30"
            placeholder="给你的店铺起个名字"
            class="w-full h-11 rounded-xl px-3 text-sm bg-surface
                   border border-border text-white placeholder:text-muted
                   focus:outline-none focus:border-accent transition-colors"
          />
        </section>

        <!-- ── 商品列表 ────────────────────────────────────── -->
        <section>
          <div class="flex items-center justify-between mb-3">
            <h2 class="font-semibold text-sm">商品管理</h2>
            <span class="text-muted text-xs">{{ shop.products?.length ?? 0 }} 件</span>
          </div>

          <div class="space-y-3">
            <div
              v-for="(product, i) in shop.products"
              :key="i"
              class="card rounded-2xl overflow-hidden"
            >
              <!-- 商品摘要行（点击展开编辑）-->
              <button
                class="w-full flex items-center gap-3 p-3 text-left
                       active:opacity-80 transition-opacity"
                @click="toggleExpand(i)"
              >
                <!-- 商品图预览 / 占位色块 -->
                <div
                  class="w-12 h-12 rounded-xl shrink-0 overflow-hidden"
                  :style="{ background: product.gradient || '#1A1A1A' }"
                >
                  <img v-if="product.img" :src="product.img"
                       class="w-full h-full object-cover"
                       :alt="product.name" />
                </div>

                <div class="flex-1 min-w-0">
                  <p class="font-semibold text-sm truncate">{{ product.name || '未命名商品' }}</p>
                  <div class="flex items-center gap-2 mt-0.5">
                    <span class="text-accent text-xs font-bold">€{{ fmt(product.price) }}</span>
                    <span v-if="product.originalPrice"
                          class="text-muted text-xs line-through">
                      €{{ fmt(product.originalPrice) }}
                    </span>
                    <span v-if="product.badge"
                          class="text-[10px] font-bold px-1.5 py-0.5 rounded-full"
                          :style="{
                            background: product.badge === 'NEW'  ? 'rgba(26,188,156,0.12)' :
                                        product.badge === 'HOT'  ? '#FF6B6B20' : '#8B5CF620',
                            color:      product.badge === 'NEW'  ? '#1abc9c' :
                                        product.badge === 'HOT'  ? '#FF6B6B' : '#A78BFA',
                          }">
                      {{ product.badge }}
                    </span>
                  </div>
                </div>

                <!-- 展开箭头 -->
                <svg
                  class="h-4 w-4 text-muted shrink-0 transition-transform duration-200"
                  :class="{ 'rotate-90': expanded[i] }"
                  fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"
                >
                  <path stroke-linecap="round" stroke-linejoin="round" d="M9 5l7 7-7 7"/>
                </svg>
              </button>

              <!-- 展开：编辑表单 -->
              <div v-if="expanded[i]"
                   class="border-t border-border px-3 pb-4 pt-3 space-y-3">

                <div class="grid grid-cols-2 gap-3">
                  <!-- 商品名 -->
                  <div class="col-span-2">
                    <label class="text-[10px] text-muted block mb-1">商品名称</label>
                    <input
                      v-model="product.name"
                      @input="product.title = product.name"
                      type="text" maxlength="40"
                      placeholder="商品名称"
                      class="w-full h-9 rounded-lg px-3 text-sm bg-surface2
                             border border-border text-white placeholder:text-muted
                             focus:outline-none focus:border-accent"
                    />
                  </div>

                  <!-- 售价 -->
                  <div>
                    <label class="text-[10px] text-muted block mb-1">售价（€）</label>
                    <input
                      v-model="product.price"
                      type="number" min="0" step="0.01" placeholder="0.00"
                      class="w-full h-9 rounded-lg px-3 text-sm bg-surface2
                             border border-border text-white placeholder:text-muted
                             focus:outline-none focus:border-accent"
                    />
                  </div>

                  <!-- 划线价 -->
                  <div>
                    <label class="text-[10px] text-muted block mb-1">划线价（€，可选）</label>
                    <input
                      v-model="product.originalPrice"
                      type="number" min="0" step="0.01" placeholder="原价"
                      class="w-full h-9 rounded-lg px-3 text-sm bg-surface2
                             border border-border text-white placeholder:text-muted
                             focus:outline-none focus:border-accent"
                    />
                  </div>

                  <!-- 标签 -->
                  <div>
                    <label class="text-[10px] text-muted block mb-1">商品标签</label>
                    <select
                      v-model="product.badge"
                      class="w-full h-9 rounded-lg px-3 text-sm bg-surface2
                             border border-border text-white
                             focus:outline-none focus:border-accent"
                    >
                      <option v-for="opt in BADGE_OPTIONS" :key="opt.value"
                              :value="opt.value || null">{{ opt.label }}</option>
                    </select>
                  </div>

                  <!-- 图片链接 -->
                  <div>
                    <label class="text-[10px] text-muted block mb-1">图片链接（可选）</label>
                    <input
                      v-model="product.img"
                      type="url" placeholder="https://..."
                      class="w-full h-9 rounded-lg px-3 text-sm bg-surface2
                             border border-border text-white placeholder:text-muted
                             focus:outline-none focus:border-accent"
                    />
                  </div>

                  <!-- 简介 -->
                  <div class="col-span-2">
                    <label class="text-[10px] text-muted block mb-1">商品简介（可选）</label>
                    <input
                      v-model="product.desc"
                      type="text" maxlength="60" placeholder="一句话描述…"
                      class="w-full h-9 rounded-lg px-3 text-sm bg-surface2
                             border border-border text-white placeholder:text-muted
                             focus:outline-none focus:border-accent"
                    />
                  </div>
                </div>

                <!-- 删除按钮 -->
                <button
                  class="text-xs font-semibold px-3 py-1.5 rounded-full
                         bg-danger/10 text-danger border border-danger/20
                         active:scale-95 transition-transform duration-100"
                  @click="confirmRemove(i)"
                >
                  删除此商品
                </button>
              </div>
            </div>

            <!-- 无商品提示 -->
            <div v-if="!shop.products?.length"
                 class="card p-6 text-center rounded-2xl">
              <p class="text-muted text-sm">还没有商品，点击下方添加</p>
            </div>
          </div>
        </section>

        <!-- ── 新增商品 ────────────────────────────────────── -->
        <section>
          <button
            v-if="!showAddForm"
            class="w-full h-11 rounded-full text-sm font-semibold
                   flex items-center justify-center gap-2
                   active:scale-95 transition-transform duration-100"
            style="border:1px dashed #333;color:#9CA3AF"
            @click="showAddForm = true"
          >
            <svg class="h-4 w-4" fill="none" viewBox="0 0 24 24"
                 stroke="currentColor" stroke-width="2">
              <path stroke-linecap="round" stroke-linejoin="round" d="M12 4v16m8-8H4"/>
            </svg>
            添加商品
          </button>

          <div v-else class="card p-4 rounded-2xl space-y-3">
            <p class="font-semibold text-sm">新增商品</p>

            <div class="grid grid-cols-2 gap-3">
              <div class="col-span-2">
                <label class="text-[10px] text-muted block mb-1">商品名称 *</label>
                <input
                  v-model="newProduct.name"
                  type="text" maxlength="40" placeholder="例：经典白T恤"
                  class="w-full h-9 rounded-lg px-3 text-sm bg-surface2
                         border border-border text-white placeholder:text-muted
                         focus:outline-none focus:border-accent"
                />
              </div>
              <div>
                <label class="text-[10px] text-muted block mb-1">售价（€）*</label>
                <input
                  v-model="newProduct.price"
                  type="number" min="0" step="0.01" placeholder="0.00"
                  class="w-full h-9 rounded-lg px-3 text-sm bg-surface2
                         border border-border text-white placeholder:text-muted
                         focus:outline-none focus:border-accent"
                />
              </div>
              <div>
                <label class="text-[10px] text-muted block mb-1">划线价（€，可选）</label>
                <input
                  v-model="newProduct.originalPrice"
                  type="number" min="0" step="0.01" placeholder="原价"
                  class="w-full h-9 rounded-lg px-3 text-sm bg-surface2
                         border border-border text-white placeholder:text-muted
                         focus:outline-none focus:border-accent"
                />
              </div>
              <div>
                <label class="text-[10px] text-muted block mb-1">标签</label>
                <select
                  v-model="newProduct.badge"
                  class="w-full h-9 rounded-lg px-3 text-sm bg-surface2
                         border border-border text-white
                         focus:outline-none focus:border-accent"
                >
                  <option v-for="opt in BADGE_OPTIONS" :key="opt.value"
                          :value="opt.value">{{ opt.label }}</option>
                </select>
              </div>
              <div>
                <label class="text-[10px] text-muted block mb-1">图片链接（可选）</label>
                <input
                  v-model="newProduct.img"
                  type="url" placeholder="https://..."
                  class="w-full h-9 rounded-lg px-3 text-sm bg-surface2
                         border border-border text-white placeholder:text-muted
                         focus:outline-none focus:border-accent"
                />
              </div>
              <div class="col-span-2">
                <label class="text-[10px] text-muted block mb-1">简介（可选）</label>
                <input
                  v-model="newProduct.desc"
                  type="text" maxlength="60" placeholder="一句话描述…"
                  class="w-full h-9 rounded-lg px-3 text-sm bg-surface2
                         border border-border text-white placeholder:text-muted
                         focus:outline-none focus:border-accent"
                />
              </div>
            </div>

            <p v-if="addError" class="text-xs text-danger">{{ addError }}</p>

            <div class="flex gap-2">
              <button
                class="flex-1 h-10 rounded-full text-sm font-bold
                       active:scale-95 transition-transform duration-100"
                style="background:#1abc9c;color:#fff"
                @click="submitAdd"
              >
                添加
              </button>
              <button
                class="flex-1 h-10 rounded-full text-sm font-semibold
                       border border-border text-muted
                       active:scale-95 transition-transform duration-100"
                @click="showAddForm = false; addError = ''"
              >
                取消
              </button>
            </div>
          </div>
        </section>

        <!-- ── 保存错误提示 ───────────────────────────────── -->
        <p v-if="saveError && shop" class="text-xs text-danger text-center">{{ saveError }}</p>

      </div><!-- /max-w -->

      <!-- 底部操作栏：预览 + 分享 -->
      <div class="fixed bottom-0 left-0 right-0 z-20
                  bg-bg/95 backdrop-blur-md border-t border-border
                  px-4 pt-3 pb-[calc(.75rem+env(safe-area-inset-bottom))]
                  flex gap-3">
        <button
          class="flex-1 h-12 rounded-full text-sm font-semibold
                 border border-border text-white
                 flex items-center justify-center gap-2
                 active:scale-95 transition-transform duration-100"
          @click="previewShop"
        >
          <svg class="h-4 w-4" fill="none" viewBox="0 0 24 24"
               stroke="currentColor" stroke-width="2">
            <path stroke-linecap="round" stroke-linejoin="round"
                  d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"/>
            <path stroke-linecap="round" stroke-linejoin="round"
                  d="M2.458 12C3.732 7.943 7.523 5 12 5c4.477 0 8.268 2.943 9.542 7-1.274 4.057-5.065 7-9.542 7-4.477 0-8.268-2.943-9.542-7z"/>
          </svg>
          预览店铺
        </button>
        <button
          class="flex-1 h-12 rounded-full text-sm font-bold
                 flex items-center justify-center gap-2
                 active:scale-95 transition-transform duration-100"
          style="background:#1abc9c;color:#fff"
          @click="shareShop"
        >
          <svg class="h-4 w-4" fill="none" viewBox="0 0 24 24"
               stroke="currentColor" stroke-width="2">
            <path stroke-linecap="round" stroke-linejoin="round"
                  d="M8.684 13.342C8.886 12.938 9 12.482 9 12c0-.482-.114-.938-.316-1.342m0 2.684a3 3 0 1 1 0-2.684m0 2.684l6.632 3.316m-6.632-6l6.632-3.316m0 0a3 3 0 1 0 5.367-2.684 3 3 0 0 0-5.367 2.684zm0 9.316a3 3 0 1 0 5.368 2.684 3 3 0 0 0-5.368-2.684z"/>
          </svg>
          分享赚钱
        </button>
      </div>
    </template>

  </div>
</template>
