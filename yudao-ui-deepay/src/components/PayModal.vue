<!--
  PayModal.vue — 付费弹窗（底部上滑 bottom-sheet）
  Props:
    show:    Boolean
    message: String  — 顶部文案（损失感）
    plans:   Array   — [{id, quota, priceEur, desc}]
  Emits:
    close
    buy(planId)
-->
<script setup>
import { ref, computed } from 'vue'

const props = defineProps({
  show:    { type: Boolean, default: false },
  message: { type: String,  default: '今日免费次数已用完' },
  plans:   { type: Array,   default: () => [] },
})
const emit = defineEmits(['close', 'buy'])

const selected = ref('PACK_M')

const displayPlans = computed(() => {
  if (props.plans?.length) return props.plans
  return [
    { id: 'DAY_PASS', quota: 9999, priceEur: '2.99', desc: '今日不限次数', tag: '⚡ 限时' },
    { id: 'PACK_S',   quota: 10,   priceEur: '1.99', desc: '10次生成' },
    { id: 'PACK_M',   quota: 30,   priceEur: '4.99', desc: '30次畅享',     tag: '⭐ 推荐' },
    { id: 'PACK_L',   quota: 100,  priceEur: '12.99',desc: '100次专业版' },
  ]
})
</script>

<template>
  <Teleport to="body">
    <Transition name="overlay">
      <div v-if="show"
           class="fixed inset-0 z-50 bg-black/70 flex items-end"
           @click.self="$emit('close')">

        <Transition name="sheet">
          <div v-if="show"
               class="w-full bg-surface rounded-t-3xl px-5 pt-5 pb-[calc(1.5rem+env(safe-area-inset-bottom))]
                      max-w-[480px] mx-auto">

            <!-- 顶部把手 -->
            <div class="w-10 h-1 bg-border rounded-full mx-auto mb-5" />

            <!-- 标题文案 -->
            <h3 class="text-lg font-bold mb-1">解锁更多生成</h3>
            <p class="text-muted text-sm mb-5">{{ message }}</p>

            <!-- 套餐列表 -->
            <div class="flex flex-col gap-2.5 mb-5">
              <div
                v-for="plan in displayPlans" :key="plan.id"
                :class="[
                  'relative flex items-center gap-3 p-4 rounded-2xl border-2 cursor-pointer',
                  'transition-colors duration-150',
                  selected === plan.id ? 'border-accent bg-accent/5' : 'border-border bg-surface2',
                ]"
                @click="selected = plan.id"
              >
                <!-- 推荐标签 -->
                <span v-if="plan.tag"
                      class="absolute -top-2.5 right-3 bg-accent text-black
                             text-[10px] font-bold px-2 py-0.5 rounded-full">
                  {{ plan.tag }}
                </span>

                <!-- 次数 -->
                <span class="text-lg font-black min-w-[56px]">
                  {{ plan.quota >= 9999 ? '不限' : `${plan.quota}次` }}
                </span>

                <!-- 描述 -->
                <span class="text-muted text-sm flex-1">{{ plan.desc }}</span>

                <!-- 价格 -->
                <span :class="['text-lg font-bold', selected === plan.id ? 'text-accent' : 'text-white']">
                  €{{ plan.priceEur }}
                </span>
              </div>
            </div>

            <!-- CTA -->
            <button
              class="bg-accent text-black font-bold h-12 w-full rounded-full
                     active:scale-95 transition-transform duration-100 text-sm"
              @click="$emit('buy', selected)"
            >
              立即解锁
            </button>

            <p class="text-center text-muted text-xs mt-3 cursor-pointer"
               @click="$emit('close')">
              先看看当前结果
            </p>

          </div>
        </Transition>
      </div>
    </Transition>
  </Teleport>
</template>

<style scoped>
.overlay-enter-active, .overlay-leave-active { transition: opacity .25s ease; }
.overlay-enter-from, .overlay-leave-to       { opacity: 0; }

.sheet-enter-active, .sheet-leave-active { transition: transform .3s cubic-bezier(.32,0,.67,0); }
.sheet-enter-from, .sheet-leave-to       { transform: translateY(100%); }
</style>
