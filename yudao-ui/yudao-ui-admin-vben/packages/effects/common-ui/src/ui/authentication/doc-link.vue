<script setup lang="ts">
defineOptions({
  name: 'DocLink',
});
</script>

<template>
  <div class="w-full sm:mx-auto md:max-w-md">
    <div class="mt-4 flex items-center justify-between">
      <span class="border-input w-[35%] border-b dark:border-gray-600"></span>
      <span class="text-muted-foreground text-center text-xs uppercase">
        萌新必读
      </span>
      <span class="border-input w-[35%] border-b dark:border-gray-600"></span>
    </div>

    <div class="mt-4 flex w-full justify-between">
      <a
        href="https://doc.iocoder.cn/"
        target="_blank"
        class="text-primary hover:text-primary/80 text-sm"
      >
        📚 开发指南
      </a>
      <a
        href="https://doc.iocoder.cn/video/"
        target="_blank"
        class="text-primary hover:text-primary/80 text-sm"
      >
        🔥 视频教程
      </a>
      <a
        href="https://www.iocoder.cn/Interview/good-collection/"
        target="_blank"
        class="text-primary hover:text-primary/80 text-sm"
      >
        ⚡ 面试手册
      </a>
      <a
        href="http://static.yudao.iocoder.cn/mp/xinyu370.jpeg"
        target="_blank"
        class="text-primary hover:text-primary/80 text-sm"
      >
        🤝 外包咨询
      </a>
    </div>
  </div>
</template>
