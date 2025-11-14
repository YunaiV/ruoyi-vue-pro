import type { Component } from 'vue';

interface AboutProps {
  description?: string;
  name?: string;
  title?: string;
}

interface DescriptionItem {
  content: Component | string;
  title: string;
}

export type { AboutProps, DescriptionItem };
