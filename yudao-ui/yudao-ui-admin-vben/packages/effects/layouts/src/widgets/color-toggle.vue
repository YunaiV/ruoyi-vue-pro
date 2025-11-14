<script setup lang="ts">
import type { BuiltinThemeType } from '@vben/types';

import { Palette } from '@vben/icons';
import {
  COLOR_PRESETS,
  preferences,
  updatePreferences,
} from '@vben/preferences';

import { VbenIconButton } from '@vben-core/shadcn-ui';

defineOptions({
  name: 'AuthenticationColorToggle',
});

function handleUpdate(colorPrimary: string, type: BuiltinThemeType) {
  updatePreferences({
    theme: {
      colorPrimary,
      builtinType: type,
    },
  });
}
</script>

<template>
  <div class="group relative flex items-center overflow-hidden">
    <div
      class="flex w-0 overflow-hidden transition-all duration-500 ease-out group-hover:w-60"
    >
      <template v-for="preset in COLOR_PRESETS" :key="preset.color">
        <VbenIconButton
          class="flex-center flex-shrink-0"
          @click="handleUpdate(preset.color, preset.type)"
        >
          <div
            :style="{ backgroundColor: preset.color }"
            class="flex-center relative size-5 rounded-full hover:scale-110"
          >
            <svg
              v-if="preferences.theme.builtinType === preset.type"
              class="h-3.5 w-3.5 text-white"
              height="1em"
              viewBox="0 0 15 15"
              width="1em"
            >
              <path
                clip-rule="evenodd"
                d="M11.467 3.727c.289.189.37.576.181.865l-4.25 6.5a.625.625 0 0 1-.944.12l-2.75-2.5a.625.625 0 0 1 .841-.925l2.208 2.007l3.849-5.886a.625.625 0 0 1 .865-.181"
                fill="currentColor"
                fill-rule="evenodd"
              />
            </svg>
          </div>
        </VbenIconButton>
      </template>
    </div>

    <VbenIconButton>
      <Palette class="text-primary size-4" />
    </VbenIconButton>
  </div>
</template>
