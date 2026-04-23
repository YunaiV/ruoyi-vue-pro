<!--
  NotFound.vue — 404 页面
  路径：/:pathMatch(.*)*
-->
<script setup>
import { useRouter } from 'vue-router'
const router = useRouter()
</script>

<template>
  <div class="min-h-screen bg-bg text-white flex flex-col items-center
              justify-center px-6 text-center">
    <p class="text-6xl mb-5">🔍</p>
    <h2 class="text-xl font-bold mb-2">页面不存在</h2>
    <p class="text-muted text-sm mb-8">你访问的页面已移走或不存在</p>
    <button
      class="bg-accent text-black font-bold h-12 w-full max-w-[280px]
             rounded-full active:scale-95 transition-transform duration-100 text-sm"
      @click="router.push('/')"
    >
      返回首页
    </button>
  </div>
</template>
