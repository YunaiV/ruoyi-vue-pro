<script lang="ts" setup>
import { computed, nextTick } from 'vue';

import { VbenButton } from '@vben-core/shadcn-ui';

interface Props {
  /**
   * 类型
   */
  type?: 'icon' | 'normal';
}

defineOptions({
  name: 'ThemeToggleButton',
});

const props = withDefaults(defineProps<Props>(), {
  type: 'normal',
});

const isDark = defineModel<boolean>();

const theme = computed(() => {
  return isDark.value ? 'light' : 'dark';
});

const bindProps = computed(() => {
  const type = props.type;

  return type === 'normal'
    ? {
        variant: 'heavy' as const,
      }
    : {
        class: 'rounded-full',
        size: 'icon' as const,
        style: { padding: '7px' },
        variant: 'icon' as const,
      };
});

function toggleTheme(event: MouseEvent) {
  const isAppearanceTransition =
    // @ts-expect-error
    document.startViewTransition &&
    !window.matchMedia('(prefers-reduced-motion: reduce)').matches;
  if (!isAppearanceTransition || !event) {
    isDark.value = !isDark.value;
    return;
  }
  const x = event.clientX;
  const y = event.clientY;
  const endRadius = Math.hypot(
    Math.max(x, innerWidth - x),
    Math.max(y, innerHeight - y),
  );
  // @ts-ignore startViewTransition
  const transition = document.startViewTransition(async () => {
    isDark.value = !isDark.value;
    await nextTick();
  });
  transition.ready.then(() => {
    const clipPath = [
      `circle(0px at ${x}px ${y}px)`,
      `circle(${endRadius}px at ${x}px ${y}px)`,
    ];
    const animate = document.documentElement.animate(
      {
        clipPath: isDark.value ? [...clipPath].reverse() : clipPath,
      },
      {
        duration: 450,
        easing: 'ease-in',
        pseudoElement: isDark.value
          ? '::view-transition-old(root)'
          : '::view-transition-new(root)',
      },
    );
    animate.onfinish = () => {
      transition.skipTransition();
    };
  });
}
</script>

<template>
  <VbenButton
    :aria-label="theme"
    :class="[`is-${theme}`]"
    aria-live="polite"
    class="theme-toggle cursor-pointer border-none bg-none hover:animate-[shrink_0.3s_ease-in-out]"
    v-bind="bindProps"
    @click.stop="toggleTheme"
  >
    <svg aria-hidden="true" height="24" viewBox="0 0 24 24" width="24">
      <mask
        id="theme-toggle-moon"
        class="theme-toggle__moon"
        fill="hsl(var(--foreground)/80%)"
        stroke="none"
      >
        <rect fill="white" height="100%" width="100%" x="0" y="0" />
        <circle cx="40" cy="8" fill="black" r="11" />
      </mask>
      <circle
        id="sun"
        class="theme-toggle__sun"
        cx="12"
        cy="12"
        mask="url(#theme-toggle-moon)"
        r="11"
      />
      <g class="theme-toggle__sun-beams">
        <line x1="12" x2="12" y1="1" y2="3" />
        <line x1="12" x2="12" y1="21" y2="23" />
        <line x1="4.22" x2="5.64" y1="4.22" y2="5.64" />
        <line x1="18.36" x2="19.78" y1="18.36" y2="19.78" />
        <line x1="1" x2="3" y1="12" y2="12" />
        <line x1="21" x2="23" y1="12" y2="12" />
        <line x1="4.22" x2="5.64" y1="19.78" y2="18.36" />
        <line x1="18.36" x2="19.78" y1="5.64" y2="4.22" />
      </g>
    </svg>
  </VbenButton>
</template>

<style scoped>
.theme-toggle {
  &__moon {
    & > circle {
      transition: transform 0.5s cubic-bezier(0, 0, 0.3, 1);
    }
  }

  &__sun {
    @apply fill-foreground/90 stroke-none;

    transform-origin: center center;
    transition: transform 1.6s cubic-bezier(0.25, 0, 0.2, 1);

    &:hover > svg > & {
      @apply fill-foreground/90;
    }
  }

  &__sun-beams {
    @apply stroke-foreground/90 stroke-[2px];

    transform-origin: center center;
    transition:
      transform 1.6s cubic-bezier(0.5, 1.5, 0.75, 1.25),
      opacity 0.6s cubic-bezier(0.25, 0, 0.3, 1);

    &:hover > svg > & {
      @apply stroke-foreground;
    }
  }

  &.is-light {
    .theme-toggle__sun {
      @apply scale-50;
    }

    .theme-toggle__sun-beams {
      transform: rotateZ(0.25turn);
    }
  }

  &.is-dark {
    .theme-toggle__moon {
      & > circle {
        transform: translateX(-20px);
      }
    }

    .theme-toggle__sun-beams {
      @apply opacity-0;
    }
  }

  &:hover > svg {
    .theme-toggle__sun,
    .theme-toggle__moon {
      @apply fill-foreground;
    }
  }
}
</style>
