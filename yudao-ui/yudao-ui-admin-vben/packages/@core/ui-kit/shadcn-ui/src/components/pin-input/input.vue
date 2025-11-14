<script setup lang="ts">
import type { PinInputProps } from './types';

import { computed, onBeforeUnmount, ref, useId, watch } from 'vue';

import { PinInput, PinInputGroup, PinInputInput } from '../../ui';
import { VbenButton } from '../button';

defineOptions({
  inheritAttrs: false,
});

const {
  codeLength = 6,
  createText = async () => {},
  disabled = false,
  handleSendCode = async () => {},
  loading = false,
  maxTime = 60,
} = defineProps<PinInputProps>();

const emit = defineEmits<{
  complete: [];
  sendError: [error: any];
}>();

const timer = ref<ReturnType<typeof setTimeout>>();

const modelValue = defineModel<string>();

const inputValue = ref<string[]>([]);
const countdown = ref(0);

const btnText = computed(() => {
  const countdownValue = countdown.value;
  return createText?.(countdownValue);
});

const btnLoading = computed(() => {
  return loading || countdown.value > 0;
});

watch(
  () => modelValue.value,
  () => {
    inputValue.value = modelValue.value?.split('') ?? [];
  },
);

watch(inputValue, (val) => {
  modelValue.value = val.join('');
});

function handleComplete(e: string[]) {
  modelValue.value = e.join('');
  emit('complete');
}

async function handleSend(e: Event) {
  try {
    e?.preventDefault();
    countdown.value = maxTime;
    startCountdown();
    await handleSendCode();
  } catch (error) {
    console.error('Failed to send code:', error);
    // Consider emitting an error event or showing a notification
    emit('sendError', error);
  }
}

function startCountdown() {
  if (countdown.value > 0) {
    timer.value = setTimeout(() => {
      countdown.value--;
      startCountdown();
    }, 1000);
  }
}

onBeforeUnmount(() => {
  countdown.value = 0;
  clearTimeout(timer.value);
});

const id = useId();
</script>

<template>
  <PinInput
    :id="id"
    v-model="inputValue"
    :disabled="disabled"
    class="flex w-full justify-between"
    otp
    placeholder="○"
    type="number"
    @complete="handleComplete"
  >
    <div class="relative flex w-full">
      <PinInputGroup class="mr-2">
        <PinInputInput
          v-for="(item, index) in codeLength"
          :key="item"
          :index="index"
        />
      </PinInputGroup>
      <VbenButton
        :disabled="disabled"
        :loading="btnLoading"
        class="flex-grow"
        size="lg"
        variant="outline"
        @click="handleSend"
      >
        {{ btnText }}
      </VbenButton>
    </div>
  </PinInput>
</template>
