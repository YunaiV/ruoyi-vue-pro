<!--
  ImageCard.vue — 可选中的图片卡片
  Props:
    src:      String  — image URL
    selected: Boolean — green border + checkmark badge
    label:    String  — optional bottom label
    index:    Number  — staggered fade-in delay
-->
<script setup>
defineProps({
  src:      { type: String,  required: true },
  selected: { type: Boolean, default: false },
  label:    { type: String,  default: '' },
  index:    { type: Number,  default: 0 },
})
defineEmits(['select'])
</script>

<template>
  <div
    :class="[
      'relative aspect-[3/4] rounded-2xl overflow-hidden cursor-pointer',
      'transition-all duration-150 active:scale-95',
      'border-2',
      selected ? 'border-accent' : 'border-border',
    ]"
    :style="{ animationDelay: `${index * 0.06}s` }"
    class="img-fade"
    @click="$emit('select')"
  >
    <img :src="src" loading="lazy"
         class="w-full h-full object-cover" />

    <!-- Selected badge -->
    <div v-if="selected"
         class="absolute bottom-2.5 left-1/2 -translate-x-1/2
                bg-accent text-black text-xs font-bold
                px-3 py-1 rounded-full whitespace-nowrap">
      ✓ 已选
    </div>

    <!-- Optional label -->
    <div v-if="label && !selected"
         class="absolute bottom-0 left-0 right-0 p-2.5
                bg-gradient-to-t from-black/70 to-transparent">
      <p class="text-xs font-medium">{{ label }}</p>
    </div>
  </div>
</template>

<style scoped>
.img-fade {
  opacity: 0;
  animation: img-appear .5s ease forwards;
}
@keyframes img-appear {
  from { opacity: 0; transform: scale(.96); }
  to   { opacity: 1; transform: scale(1);   }
}
</style>
