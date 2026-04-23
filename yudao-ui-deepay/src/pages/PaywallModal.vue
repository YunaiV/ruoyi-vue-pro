<!--
  PaywallModal.vue — 配额超限弹窗（转化核心组件）

  触发时机：
    1. generate 接口返回 code=NO_QUOTA / exceeded=true
    2. 用户连续点击生成 ≥2 次（由父组件控制）

  转化设计原则：
    - 不硬拦，而是展示价值："AI已分析爆款趋势" → 用户觉得值
    - 损失感文案："再生成1次就能找到爆款"
    - 默认高亮中间档 PACK_M（30次）
    - 限时提示制造紧迫感
-->
<!--
  PaywallModal.vue — 配额超限弹窗（底部上滑 bottom-sheet）
  Deblock级暗色 + 绿色CTA + 损失感文案
-->
<script setup>
import { ref, computed } from 'vue'
import { createPayment } from '@/api'

const props = defineProps({
  show:    { type: Boolean, default: false },
  userId:  { type: String,  default: null },
  message: { type: String,  default: '今日免费次数已用完' },
  plans:   { type: Array,   default: () => [] },
})
const emit = defineEmits(['close', 'paid'])

const buying       = ref(false)
const selectedPlan = ref('PACK_M')

const displayPlans = computed(() => {
  if (props.plans?.length) return props.plans
  return [
    { id: 'DAY_PASS', quota: 9999, priceEur: '2.99', desc: '今日不限次数', tag: '⚡ 限时' },
    { id: 'PACK_S',   quota: 10,   priceEur: '1.99', desc: '10次生成' },
    { id: 'PACK_M',   quota: 30,   priceEur: '4.99', desc: '30次畅享',     tag: '⭐ 推荐' },
    { id: 'PACK_L',   quota: 100,  priceEur: '12.99',desc: '100次专业版' },
  ]
})

async function buy() {
  if (buying.value) return
  buying.value = true
  try {
    const res = await createPayment(props.userId, selectedPlan.value)
    if (res?.payUrl) window.location.href = res.payUrl
  } catch (e) {
    console.error('支付创建失败', e)
  } finally {
    buying.value = false
  }
}
</script>

<template>
  <Teleport to="body">
    <Transition name="overlay">
      <div v-if="show"
           class="fixed inset-0 z-50 bg-black/70 flex items-end"
           @click.self="$emit('close')">

        <Transition name="sheet">
          <div v-if="show"
               class="w-full max-w-[480px] mx-auto
                      bg-[#111] rounded-t-3xl
                      border-t border-[#1A1A1A]
                      px-5 pt-4 pb-[calc(1.5rem+env(safe-area-inset-bottom))]">

            <!-- 把手 -->
            <div class="w-10 h-1 bg-[#2A2A2A] rounded-full mx-auto mb-6" />

            <!-- 标题 + 副标题 -->
            <h3 class="text-[1.2rem] font-semibold mb-1.5">解锁更多生成</h3>
            <p class="text-[#9CA3AF] text-sm leading-relaxed mb-6">
              {{ message }}
            </p>

            <!-- 套餐 -->
            <div class="flex flex-col gap-2.5 mb-6">
              <div
                v-for="plan in displayPlans" :key="plan.id"
                :class="[
                  'relative flex items-center gap-3 px-4 py-3.5 rounded-2xl',
                  'border transition-all duration-200 cursor-pointer',
                  selectedPlan === plan.id
                    ? 'border-accent bg-accent/5'
                    : 'border-[#1A1A1A] bg-[#1A1A1A]',
                ]"
                :style="selectedPlan === plan.id
                  ? 'box-shadow: 0 0 0 1px #00FF88' : ''"
                @click="selectedPlan = plan.id"
              >
                <!-- 标签 -->
                <span v-if="plan.tag"
                      class="absolute -top-2.5 right-3
                             bg-accent text-black text-[10px] font-bold
                             px-2 py-0.5 rounded-full">
                  {{ plan.tag }}
                </span>

                <span class="text-[17px] font-black min-w-[56px]">
                  {{ plan.quota >= 9999 ? '不限' : `${plan.quota}次` }}
                </span>
                <span class="text-[#9CA3AF] text-sm flex-1">{{ plan.desc }}</span>
                <span :class="['text-[17px] font-bold',
                               selectedPlan === plan.id ? 'text-accent' : 'text-white']">
                  €{{ plan.priceEur }}
                </span>
              </div>
            </div>

            <!-- CTA -->
            <button
              :disabled="buying"
              class="btn-primary font-medium text-sm mb-3"
              @click="buy"
            >
              <svg v-if="buying" class="animate-spin h-4 w-4 shrink-0"
                   viewBox="0 0 24 24" fill="none">
                <circle cx="12" cy="12" r="10" stroke="currentColor"
                        stroke-opacity=".3" stroke-width="3"/>
                <path d="M22 12a10 10 0 0 0-10-10" stroke="currentColor"
                      stroke-width="3" stroke-linecap="round"/>
              </svg>
              {{ buying ? '跳转支付中…' : '立即解锁' }}
            </button>

            <p class="text-center text-[#9CA3AF] text-xs cursor-pointer"
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
.overlay-enter-from,  .overlay-leave-to      { opacity: 0; }

.sheet-enter-active, .sheet-leave-active {
  transition: transform .32s cubic-bezier(.25,.46,.45,.94);
}
.sheet-enter-from, .sheet-leave-to { transform: translateY(100%); }
</style>
