import type { VariantProps } from 'class-variance-authority';

import { cva } from 'class-variance-authority';

export const sheetVariants = cva(
  'bg-background shadow-lg transition ease-in-out data-[state=open]:animate-in data-[state=closed]:animate-out data-[state=closed]:duration-300 data-[state=open]:duration-500 border-border',
  {
    defaultVariants: {
      side: 'right',
    },
    variants: {
      side: {
        bottom:
          'inset-x-0 bottom-0 border-t border-border data-[state=closed]:slide-out-to-bottom data-[state=open]:slide-in-from-bottom',
        left: 'inset-y-0 left-0 h-full w-3/4 border-r data-[state=closed]:slide-out-to-left data-[state=open]:slide-in-from-left ',
        right:
          'inset-y-0 right-0 w-3/4 border-l  data-[state=closed]:slide-out-to-right data-[state=open]:slide-in-from-right',
        top: 'inset-x-0 top-0 border-b data-[state=closed]:slide-out-to-top data-[state=open]:slide-in-from-top',
      },
    },
  },
);

export type SheetVariants = VariantProps<typeof sheetVariants>;
