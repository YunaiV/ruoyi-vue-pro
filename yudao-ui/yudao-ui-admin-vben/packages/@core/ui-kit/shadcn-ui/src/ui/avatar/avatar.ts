import type { VariantProps } from 'class-variance-authority';

import { cva } from 'class-variance-authority';

export const avatarVariant = cva(
  'inline-flex items-center justify-center font-normal text-foreground select-none shrink-0 bg-secondary overflow-hidden',
  {
    variants: {
      shape: {
        circle: 'rounded-full',
        square: 'rounded-md',
      },
      size: {
        base: 'h-16 w-16 text-2xl',
        lg: 'h-32 w-32 text-5xl',
        sm: 'h-10 w-10 text-xs',
      },
    },
  },
);

export type AvatarVariants = VariantProps<typeof avatarVariant>;
