<!--
  BaseButton.vue — 全局按钮（pill形）
  Props:
    variant: 'primary' (teal) | 'ghost' (outline) | 'danger'
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
      'relative overflow-hidden',
      'w-full h-12 rounded-full font-semibold text-sm',
      'flex items-center justify-center gap-2',
      'transition-all duration-200',
      'disabled:opacity-40 disabled:cursor-not-allowed',
      variant === 'primary'
        ? 'bg-accent text-white shadow-glow-sm hover:shadow-glow hover:-translate-y-px active:scale-95 active:translate-y-0'
        : variant === 'danger'
          ? 'bg-danger text-white hover:opacity-90 active:scale-95'
          : 'bg-transparent text-text border border-border hover:border-accent/60 hover:text-accent hover:bg-accent/5 active:scale-95',
    ]"
  >
    <!-- Shimmer overlay for primary -->
    <span v-if="variant === 'primary'"
          class="pointer-events-none absolute inset-0
                 bg-gradient-to-b from-white/10 to-transparent" />
    <!-- Spinner -->
    <svg v-if="loading" class="animate-spin-ring h-4 w-4 shrink-0"
         viewBox="0 0 24 24" fill="none">
      <circle cx="12" cy="12" r="10" stroke="currentColor"
              stroke-opacity=".25" stroke-width="3"/>
      <path d="M22 12a10 10 0 0 0-10-10" stroke="currentColor"
            stroke-width="3" stroke-linecap="round"/>
    </svg>
    <slot />
  </button>
</template>
