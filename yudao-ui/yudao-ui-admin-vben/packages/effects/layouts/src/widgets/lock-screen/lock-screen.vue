<script setup lang="ts">
import { computed, reactive, ref } from 'vue';

import { LockKeyhole } from '@vben/icons';
import { $t, useI18n } from '@vben/locales';
import { storeToRefs, useAccessStore } from '@vben/stores';

import { useScrollLock } from '@vben-core/composables';
import { useVbenForm, z } from '@vben-core/form-ui';
import { VbenAvatar, VbenButton } from '@vben-core/shadcn-ui';

import { useDateFormat, useNow } from '@vueuse/core';

interface Props {
  avatar?: string;
}

defineOptions({
  name: 'LockScreen',
});

withDefaults(defineProps<Props>(), {
  avatar: '',
});

defineEmits<{ toLogin: [] }>();

const { locale } = useI18n();
const accessStore = useAccessStore();

const now = useNow();
const meridiem = useDateFormat(now, 'A');
const hour = useDateFormat(now, 'HH');
const minute = useDateFormat(now, 'mm');
const date = useDateFormat(now, 'YYYY-MM-DD dddd', { locales: locale.value });

const showUnlockForm = ref(false);
const { lockScreenPassword } = storeToRefs(accessStore);

const [Form, { form, validate, getFieldComponentRef }] = useVbenForm(
  reactive({
    commonConfig: {
      hideLabel: true,
      hideRequiredMark: true,
    },
    schema: computed(() => [
      {
        component: 'VbenInputPassword' as const,
        componentProps: {
          placeholder: $t('ui.widgets.lockScreen.placeholder'),
        },
        fieldName: 'password',
        label: $t('authentication.password'),
        rules: z.string().min(1, { message: $t('authentication.passwordTip') }),
      },
    ]),
    showDefaultActions: false,
  }),
);

const validPass = computed(
  () => lockScreenPassword?.value === form?.values?.password,
);

async function handleSubmit() {
  const { valid } = await validate();
  if (valid) {
    if (validPass.value) {
      accessStore.unlockScreen();
    } else {
      form.setFieldError('password', $t('authentication.passwordErrorTip'));
    }
  }
}

function toggleUnlockForm() {
  showUnlockForm.value = !showUnlockForm.value;
  if (showUnlockForm.value) {
    requestAnimationFrame(() => {
      getFieldComponentRef('password')
        ?.$el?.querySelector('[name="password"]')
        ?.focus();
    });
  }
}

useScrollLock();
</script>

<template>
  <div class="bg-background fixed z-[2000] size-full">
    <transition name="slide-left">
      <div v-show="!showUnlockForm" class="size-full">
        <div
          class="flex-col-center text-foreground/80 hover:text-foreground group fixed left-1/2 top-6 z-[2001] -translate-x-1/2 cursor-pointer text-xl font-semibold"
          @click="toggleUnlockForm"
        >
          <LockKeyhole
            class="size-5 transition-all duration-300 group-hover:scale-125"
          />
          <span>{{ $t('ui.widgets.lockScreen.unlock') }}</span>
        </div>
        <div class="flex h-full w-full items-center justify-center">
          <div class="flex w-full justify-center gap-4 px-4 sm:gap-6 md:gap-8">
            <div
              class="bg-accent relative flex h-[140px] w-[140px] items-center justify-center rounded-xl text-[36px] sm:h-[160px] sm:w-[160px] sm:text-[42px] md:h-[200px] md:w-[200px] md:text-[72px]"
            >
              <span
                class="absolute left-3 top-3 text-xs font-semibold sm:text-sm md:text-xl"
              >
                {{ meridiem }}
              </span>
              {{ hour }}
            </div>
            <div
              class="bg-accent flex h-[140px] w-[140px] items-center justify-center rounded-xl text-[36px] sm:h-[160px] sm:w-[160px] sm:text-[42px] md:h-[200px] md:w-[200px] md:text-[72px]"
            >
              {{ minute }}
            </div>
          </div>
        </div>
      </div>
    </transition>

    <transition name="slide-right">
      <div
        v-if="showUnlockForm"
        class="flex-center size-full"
        @keydown.enter.prevent="handleSubmit"
      >
        <div class="flex-col-center mb-10 w-[90%] max-w-[300px] px-4">
          <VbenAvatar :src="avatar" class="enter-x mb-6 size-20" />
          <div class="enter-x mb-2 w-full items-center">
            <Form />
          </div>
          <VbenButton class="enter-x w-full" @click="handleSubmit">
            {{ $t('ui.widgets.lockScreen.entry') }}
          </VbenButton>
          <VbenButton
            class="enter-x my-2 w-full"
            variant="ghost"
            @click="$emit('toLogin')"
          >
            {{ $t('ui.widgets.lockScreen.backToLogin') }}
          </VbenButton>
          <VbenButton
            class="enter-x mr-2 w-full"
            variant="ghost"
            @click="toggleUnlockForm"
          >
            {{ $t('common.back') }}
          </VbenButton>
        </div>
      </div>
    </transition>

    <div
      class="enter-y absolute bottom-5 w-full text-center text-xl md:text-2xl xl:text-xl 2xl:text-3xl"
    >
      <div v-if="showUnlockForm" class="enter-x mb-2 text-2xl md:text-3xl">
        {{ hour }}:{{ minute }}
        <span class="text-base md:text-lg">{{ meridiem }}</span>
      </div>
      <div class="text-xl md:text-3xl">{{ date }}</div>
    </div>
  </div>
</template>
