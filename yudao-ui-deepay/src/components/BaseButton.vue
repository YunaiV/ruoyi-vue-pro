<!--
  BaseButton.vue — 极简全局按钮（pill形）
  Props:
    variant: 'primary' (neon-green) | 'ghost' (dark outline)
    loading: Boolean
    disabled: Boolean
-->
<script setup>
defineProps({
  variant:  { type: String,  default: 'primary' },
  loading:  { type: Boolean, default: false },
  disabled: { type: Boolean, default: false },
})
defineEmits(['click'])
</script>

<template>
  <button
    v-bind="$attrs"
    :disabled="loading || disabled"
    :class="[
      'w-full h-12 rounded-full font-medium text-sm',
      'flex items-center justify-center gap-2',
      'active:scale-95 transition-transform duration-100',
      'disabled:opacity-40 disabled:cursor-not-allowed',
      variant === 'primary'
        ? 'bg-accent text-black'
        : 'bg-surface2 text-white border border-border',
    ]"
  >
    <svg v-if="loading" class="animate-spin h-4 w-4 shrink-0"
         viewBox="0 0 24 24" fill="none">
      <circle cx="12" cy="12" r="10" stroke="currentColor"
              stroke-opacity=".3" stroke-width="3"/>
      <path d="M22 12a10 10 0 0 0-10-10" stroke="currentColor"
            stroke-width="3" stroke-linecap="round"/>
    </svg>
    <slot />
  </button>
</template>
