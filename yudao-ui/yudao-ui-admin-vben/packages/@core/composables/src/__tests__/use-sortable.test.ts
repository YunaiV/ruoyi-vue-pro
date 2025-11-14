import type { SortableOptions } from 'sortablejs';

import { beforeEach, describe, expect, it, vi } from 'vitest';

import { useSortable } from '../use-sortable';

describe('useSortable', () => {
  beforeEach(() => {
    vi.mock('sortablejs/modular/sortable.complete.esm.js', () => ({
      default: {
        create: vi.fn(),
      },
    }));
  });
  it('should call Sortable.create with the correct options', async () => {
    // Create a mock element
    const mockElement = document.createElement('div') as HTMLDivElement;

    // Define custom options
    const customOptions: SortableOptions = {
      group: 'test-group',
      sort: false,
    };

    // Use the useSortable function
    const { initializeSortable } = useSortable(mockElement, customOptions);

    // Initialize sortable
    await initializeSortable();

    // Import sortablejs to access the mocked create function
    const Sortable = await import(
      'sortablejs/modular/sortable.complete.esm.js'
    );

    // Verify that Sortable.create was called with the correct parameters
    expect(Sortable.default.create).toHaveBeenCalledTimes(1);
    expect(Sortable.default.create).toHaveBeenCalledWith(
      mockElement,
      expect.objectContaining({
        animation: 300,
        delay: 400,
        delayOnTouchOnly: true,
        ...customOptions,
      }),
    );
  });
});
