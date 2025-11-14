<script setup lang="ts">
import { computed } from 'vue';

const props = withDefaults(defineProps<{ password?: string }>(), {
  password: '',
});

const strengthList: string[] = [
  '',
  '#e74242',
  '#ED6F6F',
  '#EFBD47',
  '#55D18780',
  '#55D187',
];

const currentStrength = computed(() => {
  return checkPasswordStrength(props.password);
});

const currentColor = computed(() => {
  return strengthList[currentStrength.value];
});

/**
 * Check the strength of a password
 */
function checkPasswordStrength(password: string) {
  let strength = 0;

  // Check length
  if (password.length >= 8) strength++;

  // Check for lowercase letters
  if (/[a-z]/.test(password)) strength++;

  // Check for uppercase letters
  if (/[A-Z]/.test(password)) strength++;

  // Check for numbers
  if (/\d/.test(password)) strength++;

  // Check for special characters
  if (/[^\da-z]/i.test(password)) strength++;

  return strength;
}
</script>

<template>
  <div class="relative mt-2 flex items-center justify-between">
    <template v-for="index in 5" :key="index">
      <div
        class="dark:bg-input-background bg-heavy relative mr-1 h-1.5 w-1/5 rounded-sm last:mr-0"
      >
        <span
          :style="{
            backgroundColor: currentColor,
            width: currentStrength >= index ? '100%' : '',
          }"
          class="absolute left-0 h-full w-0 rounded-sm transition-all duration-500"
        ></span>
      </div>
    </template>
  </div>
</template>
