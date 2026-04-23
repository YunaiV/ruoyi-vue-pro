/**
 * templates.js — 模板数据（每个模板绑定完整设计系统）
 *
 * theme  → UI颜色规则（固定，不被AI改变）
 * style  → AI生成 prompt 规则（内容可调，风格锁定）
 * type   → 布局规则：single | grid
 */
export const templates = [
  {
    id: 'minimal',
    name: '极简黑',
    tag: '极简',
    type: 'single',
    theme: {
      bg:      '#0B0B0B',
      card:    '#111111',
      border:  '#1A1A1A',
      text:    '#FFFFFF',
      subText: '#9CA3AF',
      primary: '#00FF88',
    },
    style: {
      prompt:    'minimalist fashion, clean lines, premium quality, studio lighting',
      colorRule: 'black base, monochrome, no bright colors, subtle texture',
    },
    gradient: 'linear-gradient(150deg, #0B0B0B 0%, #1c2b3a 100%)',
    products: [
      {
        name:     '极简连帽衫',
        price:    29.99,
        gradient: 'linear-gradient(150deg, #111 0%, #1e2a3a 100%)',
      },
    ],
  },
  {
    id: 'street',
    name: '街头风',
    tag: '街头',
    type: 'grid',
    theme: {
      bg:      '#000000',
      card:    '#0D0D0D',
      border:  '#222222',
      text:    '#FFFFFF',
      subText: '#888888',
      primary: '#FF3B3B',
    },
    style: {
      prompt:    'streetwear fashion, bold graphic, urban style, high contrast',
      colorRule: 'black base, red accent, large graphics, oversized silhouette',
    },
    gradient: 'linear-gradient(150deg, #0d0000 0%, #1a0505 100%)',
    products: [
      {
        name:     '工装外套',
        price:    49.99,
        gradient: 'linear-gradient(150deg, #10172a 0%, #1e3050 100%)',
      },
      {
        name:     '宽松卫裤',
        price:    35.99,
        gradient: 'linear-gradient(150deg, #0d1420 0%, #182535 100%)',
      },
    ],
  },
  {
    id: 'luxury',
    name: '高奢风',
    tag: '高端',
    type: 'single',
    theme: {
      bg:      '#0A0A0A',
      card:    '#111111',
      border:  '#2A2A2A',
      text:    '#F5F0E8',
      subText: '#8A8070',
      primary: '#D4AF37',
    },
    style: {
      prompt:    'luxury fashion, high-end editorial, elegant, minimalist couture',
      colorRule: 'deep black base, gold accent, premium fabric texture, clean composition',
    },
    gradient: 'linear-gradient(150deg, #0A0A0A 0%, #1c1505 100%)',
    products: [
      {
        name:     '奢华风衣',
        price:    89.99,
        gradient: 'linear-gradient(150deg, #1a1505 0%, #2a2008 100%)',
      },
    ],
  },
  {
    id: 'ecommerce',
    name: '电商爆款',
    tag: '爆款',
    type: 'grid',
    theme: {
      bg:      '#FFFFFF',
      card:    '#F8F8F8',
      border:  '#E5E5E5',
      text:    '#111111',
      subText: '#666666',
      primary: '#000000',
    },
    style: {
      prompt:    'product photography, ecommerce style, clean white background, commercial',
      colorRule: 'white background, black typography, clean product shot, no distractions',
    },
    gradient: 'linear-gradient(150deg, #f0f0f0 0%, #e0e0e0 100%)',
    products: [
      {
        name:     '热卖T恤',
        price:    19.99,
        gradient: 'linear-gradient(150deg, #e8e8e8 0%, #d5d5d5 100%)',
      },
      {
        name:     '百搭裤',
        price:    24.99,
        gradient: 'linear-gradient(150deg, #ebebeb 0%, #d8d8d8 100%)',
      },
    ],
  },
  {
    id: 'spotlight',
    name: '单品爆款',
    tag: '聚焦',
    type: 'single',
    theme: {
      bg:      '#111111',
      card:    '#181818',
      border:  '#252525',
      text:    '#FFFFFF',
      subText: '#9CA3AF',
      primary: '#00FF88',
    },
    style: {
      prompt:    'hero product shot, spotlight lighting, dramatic, single item focus',
      colorRule: 'dark background, neon green accent, centered composition, high impact',
    },
    gradient: 'linear-gradient(150deg, #111111 0%, #1a2a1a 100%)',
    products: [
      {
        name:     '爆款主打款',
        price:    39.99,
        gradient: 'linear-gradient(150deg, #111 0%, #1a3a1a 100%)',
      },
    ],
  },
]
