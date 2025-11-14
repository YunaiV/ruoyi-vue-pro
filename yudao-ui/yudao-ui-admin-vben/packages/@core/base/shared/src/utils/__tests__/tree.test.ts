import { describe, expect, it } from 'vitest';

import { filterTree, mapTree, traverseTreeValues } from '../tree';

describe('traverseTreeValues', () => {
  interface Node {
    children?: Node[];
    name: string;
  }

  type NodeValue = string;

  const sampleTree: Node[] = [
    {
      name: 'A',
      children: [
        { name: 'B' },
        {
          name: 'C',
          children: [{ name: 'D' }, { name: 'E' }],
        },
      ],
    },
    {
      name: 'F',
      children: [
        { name: 'G' },
        {
          name: 'H',
          children: [{ name: 'I' }],
        },
      ],
    },
  ];

  it('traverses tree and returns all node values', () => {
    const values = traverseTreeValues<Node, NodeValue>(
      sampleTree,
      (node) => node.name,
      {
        childProps: 'children',
      },
    );
    expect(values).toEqual(['A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I']);
  });

  it('handles empty tree', () => {
    const values = traverseTreeValues<Node, NodeValue>([], (node) => node.name);
    expect(values).toEqual([]);
  });

  it('handles tree with only root node', () => {
    const rootNode = { name: 'A' };
    const values = traverseTreeValues<Node, NodeValue>(
      [rootNode],
      (node) => node.name,
    );
    expect(values).toEqual(['A']);
  });

  it('handles tree with only leaf nodes', () => {
    const leafNodes = [{ name: 'A' }, { name: 'B' }, { name: 'C' }];
    const values = traverseTreeValues<Node, NodeValue>(
      leafNodes,
      (node) => node.name,
    );
    expect(values).toEqual(['A', 'B', 'C']);
  });
});

describe('filterTree', () => {
  const tree = [
    {
      id: 1,
      children: [
        { id: 2 },
        { id: 3, children: [{ id: 4 }, { id: 5 }, { id: 6 }] },
        { id: 7 },
      ],
    },
    { id: 8, children: [{ id: 9 }, { id: 10 }] },
    { id: 11 },
  ];

  it('should return all nodes when condition is always true', () => {
    const result = filterTree(tree, () => true, { childProps: 'children' });
    expect(result).toEqual(tree);
  });

  it('should return only root nodes when condition is always false', () => {
    const result = filterTree(tree, () => false);
    expect(result).toEqual([]);
  });

  it('should return nodes with even id values', () => {
    const result = filterTree(tree, (node) => node.id % 2 === 0);
    expect(result).toEqual([{ id: 8, children: [{ id: 10 }] }]);
  });

  it('should return nodes with odd id values and their ancestors', () => {
    const result = filterTree(tree, (node) => node.id % 2 === 1);
    expect(result).toEqual([
      {
        id: 1,
        children: [{ id: 3, children: [{ id: 5 }] }, { id: 7 }],
      },
      { id: 11 },
    ]);
  });

  it('should return nodes with "leaf" in their name', () => {
    const tree = [
      {
        name: 'root',
        children: [
          { name: 'leaf 1' },
          {
            name: 'branch',
            children: [{ name: 'leaf 2' }, { name: 'leaf 3' }],
          },
          { name: 'leaf 4' },
        ],
      },
    ];
    const result = filterTree(
      tree,
      (node) => node.name.includes('leaf') || node.name === 'root',
    );
    expect(result).toEqual([
      {
        name: 'root',
        children: [{ name: 'leaf 1' }, { name: 'leaf 4' }],
      },
    ]);
  });
});

describe('mapTree', () => {
  it('map infinite depth tree using mapTree', () => {
    const tree = [
      {
        id: 1,
        name: 'node1',
        children: [
          { id: 2, name: 'node2' },
          { id: 3, name: 'node3' },
          {
            id: 4,
            name: 'node4',
            children: [
              {
                id: 5,
                name: 'node5',
                children: [
                  { id: 6, name: 'node6' },
                  { id: 7, name: 'node7' },
                ],
              },
              { id: 8, name: 'node8' },
            ],
          },
        ],
      },
    ];
    const newTree = mapTree(tree, (node) => ({
      ...node,
      name: `${node.name}-new`,
    }));

    expect(newTree).toEqual([
      {
        id: 1,
        name: 'node1-new',
        children: [
          { id: 2, name: 'node2-new' },
          { id: 3, name: 'node3-new' },
          {
            id: 4,
            name: 'node4-new',
            children: [
              {
                id: 5,
                name: 'node5-new',
                children: [
                  { id: 6, name: 'node6-new' },
                  { id: 7, name: 'node7-new' },
                ],
              },
              { id: 8, name: 'node8-new' },
            ],
          },
        ],
      },
    ]);
  });
});
