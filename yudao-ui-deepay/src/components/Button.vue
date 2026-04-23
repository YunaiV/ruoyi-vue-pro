<!--
  Button.vue — reusable branded button
  Props:
    variant: 'primary' (neon green) | 'ghost' (dark outline)
    size:    'md' (default) | 'sm' | 'lg'
    loading: show spinner + disable
-->
<script setup>
defineProps({
  variant: { type: String, default: 'primary' },
  size:    { type: String, default: 'md' },
  loading: { type: Boolean, default: false },
  disabled:{ type: Boolean, default: false },
})
</script>

<template>
  <button
    v-bind="$attrs"
    :disabled="loading || disabled"
    :class="[
      'flex items-center justify-center gap-2 font-bold rounded-2xl w-full',
      'transition-transform duration-100 active:scale-95',
      'disabled:opacity-40 disabled:cursor-not-allowed',
      variant === 'primary'
        ? 'bg-accent text-black'
        : 'bg-surface2 text-white border border-border',
      size === 'lg' ? 'h-14 text-base'
        : size === 'sm' ? 'h-9 text-sm'
        : 'h-12 text-sm',
    ]"
  >
    <!-- Spinner -->
    <svg v-if="loading" class="animate-spin shrink-0"
         :class="size === 'sm' ? 'h-3.5 w-3.5' : 'h-4 w-4'"
         viewBox="0 0 24 24" fill="none">
      <circle cx="12" cy="12" r="10" stroke="currentColor"
              stroke-opacity=".3" stroke-width="3"/>
      <path d="M22 12a10 10 0 0 0-10-10" stroke="currentColor"
            stroke-width="3" stroke-linecap="round"/>
    </svg>
    <slot />
  </button>
</template>
