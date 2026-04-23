/**
 * inspiration.js — 时装灵感库种子数据
 */

/** @type {import('./inspiration').InspirationItem[]} */
export const inspirationItems = [

  // ── 时装周 · Paris ──────────────────────────────────────────────────
  {
    id: 'fw_paris_001',
    image: 'https://images.unsplash.com/photo-1558618666-fcd25c85cd64?w=600&q=80&fit=crop',
    source: 'fashion_week',
    brand:  'Paris Fashion Week',
    style:  'minimal',
    type:   'coat',
    season: 'AW 2024',
    desc:   '极简黑白大衣，结构感强',
    score: 88, usable: true, layer: 'design', tags: ['clean','minimal'],
  },
  {
    id: 'fw_paris_002',
    image: 'https://images.unsplash.com/photo-1509631179647-0177331693ae?w=600&q=80&fit=crop',
    source: 'fashion_week',
    brand:  'Paris Fashion Week',
    style:  'avant-garde',
    type:   'dress',
    season: 'SS 2024',
    desc:   '廓形连衣裙，强调剪裁',
    score: 85, usable: true, layer: 'design', tags: ['avant','sculptural'],
  },
  {
    id: 'fw_paris_003',
    image: 'https://images.unsplash.com/photo-1515886657613-9f3515b0c78f?w=600&q=80&fit=crop',
    source: 'fashion_week',
    brand:  'Paris Fashion Week',
    style:  'luxury',
    type:   'suit',
    season: 'AW 2024',
    desc:   '高端西装套装，精致细节',
    score: 91, usable: true, layer: 'design', tags: ['luxury','premium'],
  },
  {
    id: 'fw_paris_004',
    image: 'https://images.unsplash.com/photo-1539109136881-3be0616acf4b?w=600&q=80&fit=crop',
    source: 'fashion_week',
    brand:  'Paris Fashion Week',
    style:  'minimal',
    type:   'trench',
    season: 'SS 2025',
    desc:   '风衣廓形，米色经典',
    score: 82, usable: true, layer: 'design', tags: ['clean','minimal'],
  },

  // ── 时装周 · Milan ──────────────────────────────────────────────────
  {
    id: 'fw_milan_001',
    image: 'https://images.unsplash.com/photo-1469334031218-e382a71b716b?w=600&q=80&fit=crop',
    source: 'fashion_week',
    brand:  'Milan Fashion Week',
    style:  'luxury',
    type:   'gown',
    season: 'AW 2024',
    desc:   '米兰秀台礼服，奢华面料',
    score: 90, usable: true, layer: 'design', tags: ['luxury','premium'],
  },
  {
    id: 'fw_milan_002',
    image: 'https://images.unsplash.com/photo-1558618047-3c8c76ca7d13?w=600&q=80&fit=crop',
    source: 'fashion_week',
    brand:  'Milan Fashion Week',
    style:  'streetwear',
    type:   'jacket',
    season: 'SS 2024',
    desc:   '运动夹克，街头奢华融合',
    score: 84, usable: true, layer: 'design', tags: ['trendy','bold'],
  },
  {
    id: 'fw_milan_003',
    image: 'https://images.unsplash.com/photo-1490481651871-ab68de25d43d?w=600&q=80&fit=crop',
    source: 'fashion_week',
    brand:  'Milan Fashion Week',
    style:  'elegant',
    type:   'blouse',
    season: 'SS 2025',
    desc:   '精致衬衫，意式优雅',
    score: 87, usable: true, layer: 'design', tags: ['elegant','soft'],
  },

  // ── 时装周 · New York ───────────────────────────────────────────────
  {
    id: 'fw_ny_001',
    image: 'https://images.unsplash.com/photo-1496747611176-843222e1e57c?w=600&q=80&fit=crop',
    source: 'fashion_week',
    brand:  'New York Fashion Week',
    style:  'minimal',
    type:   'coat',
    season: 'AW 2024',
    desc:   '纽约极简主义大衣',
    score: 83, usable: true, layer: 'design', tags: ['clean','minimal'],
  },
  {
    id: 'fw_ny_002',
    image: 'https://images.unsplash.com/photo-1518611012118-696072aa579a?w=600&q=80&fit=crop',
    source: 'fashion_week',
    brand:  'New York Fashion Week',
    style:  'streetwear',
    type:   'set',
    season: 'SS 2024',
    desc:   '运动时尚套装，都市风',
    score: 86, usable: true, layer: 'design', tags: ['trendy','bold'],
  },
  {
    id: 'fw_ny_003',
    image: 'https://images.unsplash.com/photo-1485968579580-b6d095142e6e?w=600&q=80&fit=crop',
    source: 'fashion_week',
    brand:  'New York Fashion Week',
    style:  'trendy',
    type:   'dress',
    season: 'SS 2025',
    desc:   '大胆印花连衣裙',
    score: 89, usable: true, layer: 'design', tags: ['trendy','bold'],
  },

  // ── 品牌 · COS ──────────────────────────────────────────────────────
  {
    id: 'brand_cos_001',
    image: 'https://images.unsplash.com/photo-1525507119028-ed4c629a60a3?w=600&q=80&fit=crop',
    source: 'brand_lookbook',
    brand:  'COS',
    style:  'minimal',
    type:   'coat',
    season: 'AW 2024',
    desc:   'COS 极简羊毛大衣',
    score: 80, usable: true, layer: 'commercial', tags: ['clean','minimal'],
  },
  {
    id: 'brand_cos_002',
    image: 'https://images.unsplash.com/photo-1434389677669-e08b4cac3105?w=600&q=80&fit=crop',
    source: 'brand_lookbook',
    brand:  'COS',
    style:  'minimal',
    type:   'dress',
    season: 'SS 2024',
    desc:   'COS 结构感连衣裙',
    score: 77, usable: true, layer: 'commercial', tags: ['clean','minimal'],
  },
  {
    id: 'brand_cos_003',
    image: 'https://images.unsplash.com/photo-1483985988355-763728e1935b?w=600&q=80&fit=crop',
    source: 'brand_lookbook',
    brand:  'COS',
    style:  'minimal',
    type:   'trousers',
    season: 'SS 2025',
    desc:   'COS 宽腿剪裁长裤',
    score: 75, usable: true, layer: 'commercial', tags: ['clean','minimal'],
  },

  // ── 品牌 · ARKET ─────────────────────────────────────────────────────
  {
    id: 'brand_arket_001',
    image: 'https://images.unsplash.com/photo-1584370848010-d7fe6bc767ec?w=600&q=80&fit=crop',
    source: 'brand_lookbook',
    brand:  'ARKET',
    style:  'minimal',
    type:   'shirt',
    season: 'SS 2024',
    desc:   'ARKET 纯棉衬衫，极简主义',
    score: 82, usable: true, layer: 'commercial', tags: ['clean','minimal'],
  },
  {
    id: 'brand_arket_002',
    image: 'https://images.unsplash.com/photo-1619603364853-a8ff3db2b12e?w=600&q=80&fit=crop',
    source: 'brand_lookbook',
    brand:  'ARKET',
    style:  'minimal',
    type:   'knitwear',
    season: 'AW 2024',
    desc:   'ARKET 精制针织，北欧风格',
    score: 78, usable: true, layer: 'commercial', tags: ['clean','minimal'],
  },

  // ── 品牌 · Acne Studios ─────────────────────────────────────────────
  {
    id: 'brand_acne_001',
    image: 'https://images.unsplash.com/photo-1551232864-3f0890e580d9?w=600&q=80&fit=crop',
    source: 'brand_lookbook',
    brand:  'Acne Studios',
    style:  'minimal',
    type:   'coat',
    season: 'AW 2024',
    desc:   'Acne Studios 标志性廓形大衣',
    score: 88, usable: true, layer: 'commercial', tags: ['clean','minimal'],
  },
  {
    id: 'brand_acne_002',
    image: 'https://images.unsplash.com/photo-1548036328-c9fa89d128fa?w=600&q=80&fit=crop',
    source: 'brand_lookbook',
    brand:  'Acne Studios',
    style:  'trendy',
    type:   'jacket',
    season: 'SS 2025',
    desc:   'Acne Studios 前卫夹克',
    score: 85, usable: true, layer: 'commercial', tags: ['trendy','bold'],
  },

  // ── 品牌 · ZARA ──────────────────────────────────────────────────────
  {
    id: 'brand_zara_001',
    image: 'https://images.unsplash.com/photo-1591047139829-d91aecb6caea?w=600&q=80&fit=crop',
    source: 'brand_lookbook',
    brand:  'ZARA',
    style:  'trendy',
    type:   'coat',
    season: 'AW 2024',
    desc:   'ZARA 快时尚廓形大衣',
    score: 76, usable: true, layer: 'commercial', tags: ['trendy','bold'],
  },
  {
    id: 'brand_zara_002',
    image: 'https://images.unsplash.com/photo-1568252542512-9fe8fe9c87bb?w=600&q=80&fit=crop',
    source: 'brand_lookbook',
    brand:  'ZARA',
    style:  'elegant',
    type:   'dress',
    season: 'SS 2024',
    desc:   'ZARA 优雅连衣裙，可落地',
    score: 79, usable: true, layer: 'commercial', tags: ['elegant','soft'],
  },

  // ── 高端电商 · SSENSE / Farfetch ────────────────────────────────────
  {
    id: 'ed_ssense_001',
    image: 'https://images.unsplash.com/photo-1544957992-20514f595d6f?w=600&q=80&fit=crop',
    source: 'editorial',
    brand:  'SSENSE',
    style:  'luxury',
    type:   'coat',
    season: 'AW 2024',
    desc:   '设计师精选，奢华大衣',
    score: 84, usable: true, layer: 'inspiration', tags: ['luxury','premium'],
  },
  {
    id: 'ed_ssense_002',
    image: 'https://images.unsplash.com/photo-1516762689617-e1cffcef479d?w=600&q=80&fit=crop',
    source: 'editorial',
    brand:  'SSENSE',
    style:  'minimal',
    type:   'dress',
    season: 'SS 2024',
    desc:   'SSENSE 编辑精选极简连衣裙',
    score: 78, usable: true, layer: 'inspiration', tags: ['clean','minimal'],
  },
  {
    id: 'ed_farfetch_001',
    image: 'https://images.unsplash.com/photo-1506634572416-48cdfe530110?w=600&q=80&fit=crop',
    source: 'editorial',
    brand:  'Farfetch',
    style:  'luxury',
    type:   'suit',
    season: 'SS 2025',
    desc:   'Farfetch 精品奢华西装',
    score: 82, usable: true, layer: 'inspiration', tags: ['luxury','premium'],
  },
  {
    id: 'ed_farfetch_002',
    image: 'https://images.unsplash.com/photo-1487222477894-8943e31ef7b2?w=600&q=80&fit=crop',
    source: 'editorial',
    brand:  'Farfetch',
    style:  'streetwear',
    type:   'jacket',
    season: 'AW 2024',
    desc:   '街头潮牌夹克，高端配色',
    score: 70, usable: true, layer: 'inspiration', tags: ['trendy','bold'],
  },
]

/** 分类过滤器 */
export const INSPIRATION_CATEGORIES = [
  { key: 'all',            label: '全部' },
  { key: 'fashion_week',   label: '时装周' },
  { key: 'brand_lookbook', label: '品牌新品' },
  { key: 'editorial',      label: '大片精选' },
]

/** 风格标签过滤器 */
export const INSPIRATION_STYLES = [
  { key: '',            label: '全部风格' },
  { key: 'minimal',    label: '极简' },
  { key: 'luxury',     label: '高端' },
  { key: 'trendy',     label: '潮流' },
  { key: 'streetwear', label: '街头' },
  { key: 'elegant',    label: '优雅' },
]

/** Source → display label */
export const SOURCE_LABELS = {
  fashion_week:   '时装周',
  brand_lookbook: '品牌新品',
  editorial:      '大片',
}

export const DESIGN_STYLES = [
  { key: '',        label: '全部' },
  { key: 'minimal', label: '极简' },
  { key: 'modern',  label: '现代' },
  { key: 'avant',   label: '前卫' },
  { key: 'classic', label: '基础' },
  { key: 'luxury',  label: '高端' },
]

export const LAYER_LABELS = {
  design:      '秀场',
  commercial:  '品牌',
  inspiration: '灵感',
}

export const SCORE_THRESHOLD = 70  // AI only uses images above this
