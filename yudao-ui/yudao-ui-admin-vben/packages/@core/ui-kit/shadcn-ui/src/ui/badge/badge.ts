import type { VariantProps } from 'class-variance-authority';

import { cva } from 'class-variance-authority';

export const badgeVariants = cva(
  'inline-flex items-center rounded-md border border-border px-2.5 py-0.5 text-xs font-semibold transition-colors focus:outline-none focus:ring-2 focus:ring-ring focus:ring-offset-2',
  {
    defaultVariants: {
      variant: 'default',
    },
    variants: {
      variant: {
        default:
          'border-transparent bg-accent hover:bg-accent text-primary-foreground shadow',
        destructive:
          'border-transparent bg-destructive text-destructive-foreground shadow hover:bg-destructive-hover',
        outline: 'text-foreground',
        secondary:
          'border-transparent bg-secondary text-secondary-foreground hover:bg-secondary/80',
      },
    },
  },
);

export type BadgeVariants = VariantProps<typeof badgeVariants>;
