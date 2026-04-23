<!--
  Landing.vue — 完整产品官网（8模块精细版）
  Hero → Product → Visual → Feature → UseCase → Pricing → FAQ → CTA/Footer
-->
<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

// ── Scroll-reveal ─────────────────────────────────────
let io = null
onMounted(() => {
  io = new IntersectionObserver(entries => {
    entries.forEach(e => {
      if (e.isIntersecting) {
        e.target.classList.add('is-visible')
        io.unobserve(e.target)
      }
    })
  }, { threshold: 0.08 })
  document.querySelectorAll('.reveal').forEach(el => io.observe(el))
})
onUnmounted(() => io?.disconnect())

// ── FAQ ───────────────────────────────────────────────
const openFaq = ref(null)
const faqs = [
  { q: '生成的设计可以商用吗？',   a: '可以。Deepay生成的设计版权归你所有，可自由用于商业场景。' },
  { q: '每天可以免费生成多少次？', a: '每日免费3次，购买套餐可获得更多（最高不限次）。' },
  { q: '生成的图片质量如何？',     a: 'AI基于1688/TikTok/Shein实时爆款数据生成，分辨率适合电商上架。' },
  { q: '是否支持定制风格？',       a: '支持6大品类 × 6种风格组合，后续将开放更多维度。' },
  { q: '购买后如何使用？',         a: '支付成功后次数立即到账，刷新生成页即可使用，无需登录。' },
]

// ── Features（横向滚动）──────────────────────────────
const features = [
  { icon: '📊', title: 'AI趋势分析',   desc: '实时抓取1688/TikTok/Shein热榜' },
  { icon: '🎨', title: '自动设计生成', desc: '一键输出10款热卖风格' },
  { icon: '🔁', title: '多变体输出',   desc: '可反复生成，不重复' },
  { icon: '🛒', title: '一键商品化',   desc: '选图直接生成商品页' },
]

// ── Use cases ─────────────────────────────────────────
const cases = [
  { icon: '🏪', who: '电商卖家',   pain: '不知道什么款好卖',   after: 'AI爆款自动推荐' },
  { icon: '👗', who: '服装设计师', pain: '花大量时间找参考图',  after: '10秒生成10款趋势' },
  { icon: '💼', who: '品牌创业者', pain: '设计成本高、周期长',  after: '极低成本快速出图' },
]

// ── Pricing ───────────────────────────────────────────
const plans = [
  { id: 'FREE',     label: '免费',     price: '€0',     quota: '每日3次',    accent: false, tag: '' },
  { id: 'DAY_PASS', label: '日通票',   price: '€2.99',  quota: '今日不限次', accent: false, tag: '⚡ 限时' },
  { id: 'PACK_S',   label: '入门套餐', price: '€1.99',  quota: '10次生成',   accent: true,  tag: '👍 推荐' },
  { id: 'PACK_M',   label: '标准套餐', price: '€4.99',  quota: '30次生成',   accent: false, tag: '' },
  { id: 'PACK_L',   label: '专业套餐', price: '€12.99', quota: '100次生成',  accent: false, tag: '' },
]

// ── Visual preview tiles ──────────────────────────────
const previews = [
  { g: 'linear-gradient(150deg,#0d1117,#1c2b3a)', label: '欧美外套',  tag: '🔥 热卖' },
  { g: 'linear-gradient(150deg,#10172a,#1e3050)', label: '韩系连衣',  tag: '✨ 新款' },
  { g: 'linear-gradient(150deg,#111,#1c1c1c)',    label: '极简上衣',  tag: '💎 精选' },
  { g: 'linear-gradient(150deg,#0d1420,#182535)', label: '工装裤',    tag: '📈 爆款' },
]
</script>

<template>
  <div class="bg-bg text-white font-sans overflow-x-hidden">

    <!-- ── 顶部 Nav ──────────────────────────────────── -->
    <nav class="sticky top-0 z-20 bg-bg/85 backdrop-blur-md
                border-b border-[#1A1A1A] px-5 py-3.5
                flex items-center justify-between">
      <div class="flex items-center gap-2">
        <span class="w-7 h-7 rounded-lg bg-accent flex items-center justify-center
                     text-black font-black text-sm select-none">D</span>
        <span class="font-medium text-sm">Deepay</span>
      </div>
      <button class="bg-accent text-black font-medium text-xs px-4 h-8 rounded-full
                     active:scale-95 transition-transform duration-100"
              @click="router.push('/generate')">
        开始生成
      </button>
    </nav>

    <!-- ── ① HERO ─────────────────────────────────────── -->
    <section class="min-h-[92vh] flex flex-col justify-center items-center
                    text-center px-7 relative">

      <h1 class="fade-up text-[2.75rem] font-semibold leading-[1.1]
                 tracking-tight mb-5"
          style="animation-delay:.1s">
        AI驱动的<br>
        <span class="text-accent">爆款发现</span>
      </h1>

      <p class="fade-up sub text-[15px] mb-10" style="animation-delay:.24s">
        让设计更快成为销量
      </p>

      <div class="fade-up w-full max-w-[260px]" style="animation-delay:.38s">
        <button class="btn-primary text-sm" @click="router.push('/generate')">
          免费开始生成
        </button>
      </div>

      <p class="fade-up text-[#9CA3AF]/40 text-xs mt-4" style="animation-delay:.5s">
        免费体验 · 无需注册
      </p>

      <div class="absolute bottom-8 opacity-25 animate-bounce">
        <svg class="h-4 w-4 text-[#9CA3AF]" fill="none" viewBox="0 0 24 24"
             stroke="currentColor" stroke-width="2">
          <path stroke-linecap="round" stroke-linejoin="round" d="M19 9l-7 7-7-7"/>
        </svg>
      </div>
    </section>

    <!-- ── ② PRODUCT ──────────────────────────────────── -->
    <section class="section container">
      <div class="reveal mb-10">
        <h2 class="title">从趋势到设计，只需一步</h2>
      </div>
      <div class="space-y-4">
        <div v-for="(item, i) in [
          { n:'01', title:'趋势分析',   sub:'实时抓取市场爆款方向' },
          { n:'02', title:'AI生成设计', sub:'自动输出多套风格方案' },
          { n:'03', title:'快速选款',   sub:'直接进入商品化流程' },
        ]" :key="i"
             class="reveal card p-5 flex items-start gap-4"
             :style="`transition-delay:${i*.1}s`">
          <span class="text-accent font-black text-lg shrink-0 w-8">{{ item.n }}</span>
          <div>
            <p class="text-lg mb-1 font-medium">{{ item.title }}</p>
            <p class="sub">{{ item.sub }}</p>
          </div>
        </div>
      </div>
    </section>

    <!-- ── ③ VISUAL（错位瀑布）──────────────────────── -->
    <section class="section container">
      <div class="reveal mb-8">
        <h2 class="title">生成效果预览</h2>
      </div>
      <div class="grid grid-cols-2 gap-4">
        <div
          v-for="(item, i) in previews" :key="i"
          class="reveal card img-in cursor-pointer
                 active:scale-[.97] transition-all duration-300
                 opacity-70 hover:opacity-100"
          :class="i % 2 === 1 ? 'mt-8' : ''"
          :style="`background:${item.g};
                   aspect-ratio:3/4;
                   transition-delay:${i*.07}s`"
          @click="router.push('/generate')"
        >
          <div class="relative h-full">
            <span class="absolute top-3 left-3 text-[11px] font-medium
                         bg-black/40 backdrop-blur-sm text-white/80
                         px-2.5 py-1 rounded-full">
              {{ item.tag }}
            </span>
            <div class="absolute bottom-0 left-0 right-0 p-3.5
                        bg-gradient-to-t from-black/75 to-transparent">
              <p class="text-sm font-medium">{{ item.label }}</p>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- ── ④ FEATURE（横向滚动）─────────────────────── -->
    <section class="section">
      <div class="container reveal mb-8">
        <h2 class="title">核心能力</h2>
      </div>
      <!-- 横向滚动不裁剪左右padding -->
      <div class="flex gap-4 overflow-x-auto scrollbar-hide
                  px-4 pb-2">
        <div v-for="(f, i) in features" :key="i"
             class="reveal card min-w-[190px] p-5 shrink-0"
             :style="`transition-delay:${i*.07}s`">
          <span class="text-2xl mb-3 block">{{ f.icon }}</span>
          <p class="font-medium mb-1.5">{{ f.title }}</p>
          <p class="sub text-[13px] leading-relaxed">{{ f.desc }}</p>
        </div>
      </div>
    </section>

    <!-- ── ⑤ USE CASE ─────────────────────────────────── -->
    <section class="section container">
      <div class="reveal mb-8">
        <h2 class="title">适用人群</h2>
      </div>
      <div class="space-y-4">
        <div v-for="(c, i) in cases" :key="i"
             class="reveal card p-5 flex items-start gap-4"
             :style="`transition-delay:${i*.09}s`">
          <span class="text-3xl shrink-0">{{ c.icon }}</span>
          <div>
            <p class="font-semibold mb-2">{{ c.who }}</p>
            <p class="sub text-[13px] mb-1">痛点：{{ c.pain }}</p>
            <p class="text-accent text-[13px]">解决：{{ c.after }}</p>
          </div>
        </div>
      </div>
    </section>

    <!-- ── ⑥ PRICING ──────────────────────────────────── -->
    <section class="section container">
      <div class="reveal mb-2">
        <h2 class="title mb-2">价格</h2>
        <p class="sub">按需购买，无订阅</p>
      </div>
      <div class="space-y-3 mt-8">
        <div
          v-for="(p, i) in plans" :key="p.id"
          class="reveal card relative p-5 flex items-center gap-4
                 cursor-pointer active:scale-[.98] transition-all duration-200"
          :style="`transition-delay:${i*.06}s;
                   ${p.accent ? 'border-color:#00FF88;box-shadow:0 0 0 1px #00FF88,0 0 20px rgba(0,255,136,.12)' : ''}`"
          @click="router.push(p.id === 'FREE' ? '/generate' : `/generate?plan=${p.id}`)"
        >
          <span v-if="p.tag"
                class="absolute -top-2.5 right-4 bg-accent text-black
                       text-[10px] font-bold px-2 py-0.5 rounded-full">
            {{ p.tag }}
          </span>
          <div class="flex-1">
            <p class="font-medium mb-0.5">{{ p.label }}</p>
            <p class="sub text-[13px]">{{ p.quota }}</p>
          </div>
          <p :class="['text-xl font-black', p.accent ? 'text-accent' : '']">
            {{ p.price }}
          </p>
        </div>
      </div>
    </section>

    <!-- ── ⑦ FAQ ──────────────────────────────────────── -->
    <section class="section container">
      <div class="reveal mb-8">
        <h2 class="title">常见问题</h2>
      </div>
      <div class="space-y-2">
        <div v-for="(faq, i) in faqs" :key="i"
             class="reveal card overflow-hidden"
             :style="`transition-delay:${i*.06}s`">
          <button class="w-full flex items-center justify-between
                         px-5 py-4 text-left text-sm font-medium"
                  @click="openFaq = openFaq === i ? null : i">
            {{ faq.q }}
            <svg class="h-4 w-4 sub shrink-0 transition-transform duration-200"
                 :class="openFaq === i ? 'rotate-180' : ''"
                 fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
              <path stroke-linecap="round" stroke-linejoin="round" d="M19 9l-7 7-7-7"/>
            </svg>
          </button>
          <Transition name="faq">
            <div v-if="openFaq === i"
                 class="px-5 pb-5 sub text-[13px] leading-relaxed
                        border-t border-[#1A1A1A] pt-4">
              {{ faq.a }}
            </div>
          </Transition>
        </div>
      </div>
    </section>

    <!-- ── ⑧ CTA ───────────────────────────────────────── -->
    <section class="section container text-center">
      <div class="reveal mb-6">
        <h2 class="title mb-3">开始你的爆款设计</h2>
        <p class="sub">免费体验 3 次 · 无需注册</p>
      </div>
      <div class="reveal">
        <button class="btn-primary text-sm" @click="router.push('/generate')">
          立即生成
        </button>
      </div>
    </section>

    <footer class="text-center sub text-xs py-8 border-t border-[#1A1A1A] opacity-50">
      © 2025 Deepay AI · 让设计更快成为销量
    </footer>

  </div>
</template>

<style scoped>
.reveal {
  opacity: 0;
  transform: translateY(18px);
  transition: opacity 0.65s ease, transform 0.65s ease;
}
.reveal.is-visible {
  opacity: 1;
  transform: translateY(0);
}
.faq-enter-active, .faq-leave-active {
  transition: max-height .25s ease, opacity .2s ease;
  overflow: hidden;
  max-height: 200px;
}
.faq-enter-from, .faq-leave-to { max-height: 0; opacity: 0; }
</style>
