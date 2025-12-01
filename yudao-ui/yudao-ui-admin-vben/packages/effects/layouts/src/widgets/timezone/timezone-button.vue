<script setup lang="ts">
import { ref, unref } from 'vue';

import { createIconifyIcon } from '@vben/icons';
import { $t } from '@vben/locales';
import { useTimezoneStore } from '@vben/stores';

import { useVbenModal } from '@vben-core/popup-ui';
import {
  RadioGroup,
  RadioGroupItem,
  VbenIconButton,
} from '@vben-core/shadcn-ui';

const TimezoneIcon = createIconifyIcon('fluent-mdl2:world-clock');

const timezoneStore = useTimezoneStore();

const timezoneRef = ref<string | undefined>();

const timezoneOptionsRef = ref<
  {
    label: string;
    value: string;
  }[]
>([]);

const [Modal, modalApi] = useVbenModal({
  fullscreenButton: false,
  onConfirm: async () => {
    try {
      modalApi.setState({ confirmLoading: true });
      const timezone = unref(timezoneRef);
      if (timezone) {
        await timezoneStore.setTimezone(timezone);
      }
      modalApi.close();
    } finally {
      modalApi.setState({ confirmLoading: false });
    }
  },
  async onOpenChange(isOpen) {
    if (isOpen) {
      timezoneRef.value = unref(timezoneStore.timezone);
      timezoneOptionsRef.value = await timezoneStore.getTimezoneOptions();
    }
  },
});

const handleClick = () => {
  modalApi.open();
};
</script>

<template>
  <div>
    <VbenIconButton
      :tooltip="$t('ui.widgets.timezone.setTimezone')"
      class="hover:animate-[shrink_0.3s_ease-in-out]"
      @click="handleClick"
    >
      <TimezoneIcon class="text-foreground size-4" />
    </VbenIconButton>
    <Modal :title="$t('ui.widgets.timezone.setTimezone')">
      <div class="timezone-container">
        <RadioGroup v-model="timezoneRef" class="flex flex-col gap-2">
          <div
            class="flex cursor-pointer items-center gap-2"
            v-for="item in timezoneOptionsRef"
            :key="`container${item.value}`"
          >
            <RadioGroupItem :id="item.value" :value="item.value" />
            <label :for="item.value" class="cursor-pointer">{{
              item.label
            }}</label>
          </div>
        </RadioGroup>
      </div>
    </Modal>
  </div>
</template>

<style scoped>
.timezone-container {
  padding-left: 20px;
}
</style>
