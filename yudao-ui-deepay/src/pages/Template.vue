<!--
  Template.vue — 模板列表页
  路径：/template
-->
<script setup>
import { useRouter } from 'vue-router'
import { templates } from '@/data/templates'

const router = useRouter()
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
      <span class="font-semibold text-sm">选择模板</span>
    </header>

    <div class="max-w-[480px] mx-auto px-4 pt-5 pb-10">

      <p class="text-muted text-sm mb-5">选择一个模板，一键生成你的专属店铺</p>

      <!-- 模板网格 -->
      <div class="grid grid-cols-2 gap-3">
        <div
          v-for="tpl in templates"
          :key="tpl.id"
          class="card cursor-pointer active:scale-[.97] transition-transform duration-100"
          @click="router.push(`/template/${tpl.id}`)"
        >
          <!-- 封面 -->
          <div
            class="h-36 w-full rounded-t-2xl flex flex-col justify-between p-3"
            :style="{ background: tpl.gradient }"
          >
            <span class="self-end text-[10px] bg-black/50 backdrop-blur-sm
                         px-2 py-0.5 rounded-full text-white/90">
              {{ tpl.tag }}
            </span>
            <!-- 模拟商品预览点 -->
            <div class="flex gap-1.5">
              <div
                v-for="(p, i) in tpl.products.slice(0, 2)"
                :key="i"
                class="h-10 flex-1 rounded-lg opacity-60"
                :style="{ background: p.gradient }"
              />
            </div>
          </div>

          <!-- 信息 -->
          <div class="p-3">
            <p class="font-semibold text-sm">{{ tpl.name }}</p>
            <p class="text-muted text-xs mt-1">
              {{ tpl.type === 'single' ? '单品展示' : '多品网格' }}
              · {{ tpl.products.length }} 款
            </p>
          </div>
        </div>
      </div>

    </div>
  </div>
</template>
