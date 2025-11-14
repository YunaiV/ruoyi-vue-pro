import { describe, expect, it } from 'vitest';

import { uniqueByField } from '../unique';

describe('uniqueByField', () => {
  it('should return an array with unique items based on id field', () => {
    const items = [
      { id: 1, name: 'Item 1' },
      { id: 2, name: 'Item 2' },
      { id: 3, name: 'Item 3' },
      { id: 1, name: 'Duplicate Item' },
    ];

    const uniqueItems = uniqueByField(items, 'id');

    expect(uniqueItems).toHaveLength(3);
    expect(uniqueItems).toEqual([
      { id: 1, name: 'Item 1' },
      { id: 2, name: 'Item 2' },
      { id: 3, name: 'Item 3' },
    ]);
  });

  it('should return an empty array when input array is empty', () => {
    const items: any[] = []; // Empty array

    const uniqueItems = uniqueByField(items, 'id');

    // Assert expected results
    expect(uniqueItems).toEqual([]);
  });

  it('should handle arrays with only one item correctly', () => {
    const items = [{ id: 1, name: 'Item 1' }];

    const uniqueItems = uniqueByField(items, 'id');

    // Assert expected results
    expect(uniqueItems).toHaveLength(1);
    expect(uniqueItems).toEqual([{ id: 1, name: 'Item 1' }]);
  });

  it('should preserve the order of the first occurrence of each item', () => {
    const items = [
      { id: 2, name: 'Item 2' },
      { id: 1, name: 'Item 1' },
      { id: 3, name: 'Item 3' },
      { id: 1, name: 'Duplicate Item' },
    ];

    const uniqueItems = uniqueByField(items, 'id');

    // Assert expected results (order of first occurrences preserved)
    expect(uniqueItems).toEqual([
      { id: 2, name: 'Item 2' },
      { id: 1, name: 'Item 1' },
      { id: 3, name: 'Item 3' },
    ]);
  });
});
