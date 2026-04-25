/**
 * templates.js — 模板数据（每个模板绑定完整设计系统）
 *
 * theme        → UI颜色规则（固定，不被AI改变）
 * style        → AI生成 prompt 规则（内容可调，风格锁定）
 * type         → 布局规则：single | grid
 * products[*]  → name/title, price, gradient, badge, rating, reviews, desc, originalPrice
 */
export const templates = [
  /* ─────────────────────────────────────────────────────────
     1. 极简黑  —  Argon Dark Grid
  ───────────────────────────────────────────────────────── */
  {
    id: 'minimal',
    name: '极简黑',
    tag: '极简',
    type: 'grid',
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
      { name: '极简连帽衫',  title: '极简连帽衫',  price: 29.99, badge: 'NEW',  rating: 4.8, reviews: 124, desc: '纯棉面料，极简剪裁，百搭首选',         gradient: 'linear-gradient(135deg,#111 0%,#1e2a3a 100%)' },
      { name: '经典高领衫',  title: '经典高领衫',  price: 35.99, badge: 'HOT',  rating: 4.9, reviews: 87,  desc: '精梳棉，修身版型，商务休闲两用',       gradient: 'linear-gradient(135deg,#0d1520 0%,#1a2535 100%)' },
      { name: '宽松卫衣',    title: '宽松卫衣',    price: 39.99, originalPrice: 52.00, badge: 'SALE', rating: 4.7, reviews: 203, desc: '650g重磅，保暖舒适', gradient: 'linear-gradient(135deg,#111420 0%,#1c2030 100%)' },
      { name: '简约T恤',     title: '简约T恤',     price: 19.99, badge: 'NEW',  rating: 4.6, reviews: 56,  desc: '200g纯棉，日常必备单品',               gradient: 'linear-gradient(135deg,#131318 0%,#1e1e28 100%)' },
      { name: '直筒长裤',    title: '直筒长裤',    price: 44.99, badge: null,   rating: 4.8, reviews: 311, desc: '垂感面料，版型修身，多色可选',           gradient: 'linear-gradient(135deg,#111 0%,#1a2a20 100%)' },
      { name: '宽肩外套',    title: '宽肩外套',    price: 59.99, badge: 'HOT',  rating: 4.9, reviews: 178, desc: '廓型设计，秋冬爆款，时尚感十足',         gradient: 'linear-gradient(135deg,#0e0e1a 0%,#1a1a2e 100%)' },
      { name: '针织背心',    title: '针织背心',    price: 24.99, originalPrice: 34.99, badge: 'SALE', rating: 4.5, reviews: 92, desc: '轻薄透气，叠穿利器', gradient: 'linear-gradient(135deg,#131320 0%,#1f1f30 100%)' },
      { name: '运动短裤',    title: '运动短裤',    price: 22.99, badge: null,   rating: 4.7, reviews: 145, desc: '速干面料，运动休闲两用',                 gradient: 'linear-gradient(135deg,#0b1520 0%,#182030 100%)' },
    ],
  },

  /* ─────────────────────────────────────────────────────────
     2. 街头风  —  Argon Street Drop
  ───────────────────────────────────────────────────────── */
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
      { name: '工装外套',    title: '工装外套',    price: 49.99, badge: 'DROP', rating: 4.9, reviews: 312, desc: '多口袋设计，机能风格，限量发售',         gradient: 'linear-gradient(135deg,#10172a 0%,#1e3050 100%)' },
      { name: '宽松卫裤',    title: '宽松卫裤',    price: 35.99, badge: 'HOT',  rating: 4.7, reviews: 198, desc: '重磅棉裤，宽松版型，街头必备',           gradient: 'linear-gradient(135deg,#0d1420 0%,#182535 100%)' },
      { name: '印花卫衣',    title: '印花卫衣',    price: 42.99, badge: 'NEW',  rating: 4.8, reviews: 87,  desc: '限量印花，超厚克重，潮流必购',           gradient: 'linear-gradient(135deg,#150505 0%,#2a0a0a 100%)' },
      { name: '破洞牛仔裤',  title: '破洞牛仔裤',  price: 38.99, badge: null,   rating: 4.6, reviews: 254, desc: '做旧工艺，个性剪裁，搭配无限',           gradient: 'linear-gradient(135deg,#0a0f18 0%,#141e30 100%)' },
      { name: '棒球夹克',    title: '棒球夹克',    price: 65.99, originalPrice: 89.99, badge: 'SALE', rating: 4.9, reviews: 143, desc: '经典棒球款，刺绣logo', gradient: 'linear-gradient(135deg,#1a0808 0%,#2e1010 100%)' },
      { name: '超宽T恤',     title: '超宽T恤',     price: 25.99, badge: 'DROP', rating: 4.7, reviews: 421, desc: '300g厚棉，oversize版型，百搭款',         gradient: 'linear-gradient(135deg,#0d0d0d 0%,#1a1a1a 100%)' },
      { name: '工装短裤',    title: '工装短裤',    price: 32.99, badge: null,   rating: 4.5, reviews: 76,  desc: '多口袋，机能设计，夏季爆款',             gradient: 'linear-gradient(135deg,#101520 0%,#1c2535 100%)' },
      { name: '运动裤',      title: '运动裤',      price: 29.99, badge: 'HOT',  rating: 4.8, reviews: 189, desc: '锥形剪裁，弹力腰头，运动街头两穿',       gradient: 'linear-gradient(135deg,#0e0e0e 0%,#1e1e1e 100%)' },
    ],
  },

  /* ─────────────────────────────────────────────────────────
     3. 高奢风  —  完整高端电商平台体验
  ───────────────────────────────────────────────────────── */
  {
    id: 'luxury',
    name: '高奢风',
    tag: '高端',
    type: 'grid',
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
      { name: '奢华风衣',    title: '奢华风衣',    price: 189.99, badge: 'NEW',  rating: 5.0, reviews: 56,  desc: '羊毛混纺，手工缝制，匠心之作',           gradient: 'linear-gradient(135deg,#1a1505 0%,#2a2008 100%)' },
      { name: '真丝衬衫',    title: '真丝衬衫',    price: 149.99, badge: null,   rating: 4.9, reviews: 44,  desc: '100%真丝，飘逸垂感，优雅必备',           gradient: 'linear-gradient(135deg,#1c150a 0%,#2e200c 100%)' },
      { name: '皮质夹克',    title: '皮质夹克',    price: 299.99, originalPrice: 420.00, badge: 'SALE', rating: 4.9, reviews: 31, desc: '顶级头层牛皮，精工细作', gradient: 'linear-gradient(135deg,#151005 0%,#251a08 100%)' },
      { name: '高腰长裙',    title: '高腰长裙',    price: 129.99, badge: 'HOT',  rating: 4.8, reviews: 89,  desc: '醋酸面料，流动感强，气质款',             gradient: 'linear-gradient(135deg,#120f08 0%,#1e180c 100%)' },
      { name: '羊绒毛衣',    title: '羊绒毛衣',    price: 159.99, badge: 'NEW',  rating: 5.0, reviews: 28,  desc: '意大利羊绒，亲肤柔软，奢享保暖',         gradient: 'linear-gradient(135deg,#1a1208 0%,#2c1e0a 100%)' },
      { name: '手工皮带',    title: '手工皮带',    price: 89.99,  badge: null,   rating: 4.8, reviews: 67,  desc: '全粒面皮革，金色扣头，精工定制',         gradient: 'linear-gradient(135deg,#181008 0%,#281808 100%)' },
      { name: '精品礼盒',    title: '精品礼盒',    price: 249.99, badge: 'HOT',  rating: 4.9, reviews: 42,  desc: '限量礼盒套装，精美包装，赠礼首选',       gradient: 'linear-gradient(135deg,#201808 0%,#342510 100%)' },
      { name: '丝绒晚礼裙',  title: '丝绒晚礼裙',  price: 219.99, originalPrice: 320.00, badge: 'SALE', rating: 5.0, reviews: 19, desc: '顶级丝绒，珠片点缀，红毯级别', gradient: 'linear-gradient(135deg,#1c1008 0%,#301c0a 100%)' },
    ],
  },

  /* ─────────────────────────────────────────────────────────
     4. 电商爆款  —  Argon Commerce
  ───────────────────────────────────────────────────────── */
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
      primary: '#111111',
    },
    style: {
      prompt:    'product photography, ecommerce style, clean white background, commercial',
      colorRule: 'white background, black typography, clean product shot, no distractions',
    },
    gradient: 'linear-gradient(150deg, #f0f0f0 0%, #e0e0e0 100%)',
    products: [
      { name: '极简白T', title: '极简白T', price: 19.99, badge: 'HOT',  rating: 4.8, reviews: 1240, desc: '爆款纯棉短袖，极简剪裁，百搭不过时', gradient: 'linear-gradient(135deg,#e8e8e8 0%,#d5d5d5 100%)', limited: true,  limitedNumber: 12, totalLimited: 50,  exclusive: false, sustainable: true,  customizable: false, material: '100% 有机棉', story: '由葡萄牙有机棉农场直供，每件均经独立编号，极简主义的极致呈现。', overlayGradient: 'linear-gradient(to top,rgba(0,0,0,0.55) 0%,transparent 60%)' },
      { name: '廓形西裤', title: '廓形西裤', price: 49.99, badge: 'NEW',  rating: 4.7, reviews: 876, desc: '垂感精纺，廓形版型，通勤时髦两用', gradient: 'linear-gradient(135deg,#ebebeb 0%,#d8d8d8 100%)', limited: false, limitedNumber: 0,  totalLimited: 0,   exclusive: true,  sustainable: false, customizable: true,  material: '70% 羊毛 30% 真丝', story: '意大利工厂手工缝制，可定制裤腿长度与腰围，专属服务体验。', overlayGradient: 'linear-gradient(to top,rgba(0,0,0,0.5) 0%,transparent 60%)' },
      { name: '经典条纹衫', title: '经典条纹衫', price: 29.99, originalPrice: 39.99, badge: 'SALE', rating: 4.9, reviews: 532, desc: '布列塔尼条纹，经典百搭，通勤休闲两用', gradient: 'linear-gradient(135deg,#e5e5e5 0%,#d0d0d0 100%)', limited: true,  limitedNumber: 5,  totalLimited: 30,  exclusive: true,  sustainable: true,  customizable: false, material: '100% 精梳棉', story: '致敬法式海军衬衫百年经典，精梳棉面料手感细腻，复古与现代的完美融合。', overlayGradient: 'linear-gradient(to top,rgba(0,0,0,0.5) 0%,transparent 60%)' },
      { name: '运动套装', title: '运动套装', price: 44.99, badge: 'HOT',  rating: 4.8, reviews: 743, desc: '高弹速干，上衣+裤子套装，健身专属', gradient: 'linear-gradient(135deg,#e0e0e0 0%,#cccccc 100%)', limited: false, limitedNumber: 0,  totalLimited: 0,   exclusive: false, sustainable: true,  customizable: false, material: '88% 涤纶 12% 氨纶', story: '采用瑞士 Schoeller 速干技术面料，排汗、弹力、透气三重升级，专业运动必备。', overlayGradient: 'linear-gradient(to top,rgba(0,0,0,0.45) 0%,transparent 60%)' },
      { name: '轻奢针织', title: '轻奢针织', price: 59.99, badge: null,   rating: 4.6, reviews: 421, desc: '软糯手感，复古色系，秋冬显白款', gradient: 'linear-gradient(135deg,#eaeaea 0%,#d6d6d6 100%)', limited: true,  limitedNumber: 8,  totalLimited: 40,  exclusive: true,  sustainable: false, customizable: true,  material: '80% 羊绒 20% 真丝', story: '蒙古高原顶级羊绒与意大利真丝混纺，每件均经过独立质检编号，奢华触感难以言喻。', overlayGradient: 'linear-gradient(to top,rgba(0,0,0,0.5) 0%,transparent 60%)' },
      { name: '廓形大衣', title: '廓形大衣', price: 129.99, badge: 'NEW', rating: 4.7, reviews: 289, desc: '宽肩廓形，经典剪裁，秋冬搭配神器', gradient: 'linear-gradient(135deg,#e3e3e3 0%,#d0d0d0 100%)', limited: true,  limitedNumber: 3,  totalLimited: 20,  exclusive: true,  sustainable: false, customizable: true,  material: '60% 羊毛 40% 聚酯', story: '由首席设计师手稿转化，宽肩轮廓凸显力量感，每件均赠送品牌皮质手提袋。', overlayGradient: 'linear-gradient(to top,rgba(0,0,0,0.55) 0%,transparent 60%)' },
      { name: '阔腿牛仔', title: '阔腿牛仔', price: 39.99, originalPrice: 55.00, badge: 'SALE', rating: 4.5, reviews: 654, desc: '日本织机面料，阔腿版型，复古潮流', gradient: 'linear-gradient(135deg,#eee 0%,#ddd 100%)', limited: false, limitedNumber: 0,  totalLimited: 0,   exclusive: false, sustainable: true,  customizable: false, material: '98% 棉 2% 氨纶', story: '采用日本 KAIHARA 牛仔布厂经典缸染工艺，洗涤后别具一格的做旧纹理令每件独一无二。', overlayGradient: 'linear-gradient(to top,rgba(0,0,0,0.5) 0%,transparent 60%)' },
      { name: '重磅卫衣', title: '重磅卫衣', price: 59.99, badge: 'HOT',  rating: 4.9, reviews: 1089, desc: '450g重磅，宽松版型，冬季保暖爆款', gradient: 'linear-gradient(135deg,#e5e5e5 0%,#d0d0d0 100%)', limited: false, limitedNumber: 0,  totalLimited: 0,   exclusive: false, sustainable: true,  customizable: true,  material: '100% 有机棉绒', story: '450g GSM 厚棉，每件均经历 20 道质检，拉绒工艺令内里如云朵般柔软，可定制刺绣文字。', overlayGradient: 'linear-gradient(to top,rgba(0,0,0,0.45) 0%,transparent 60%)' },
    ],
  },

  /* ─────────────────────────────────────────────────────────
     5. 单品爆款  —  Argon Hero Spotlight
  ───────────────────────────────────────────────────────── */
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
      { name: '爆款主打款',  title: '爆款主打款',  price: 39.99, stock: 7, badge: 'HOT', rating: 4.9, reviews: 387, desc: '限量款式，聚焦品质，高端工艺，一件即是品牌', gradient: 'linear-gradient(135deg,#111 0%,#1a3a1a 100%)' },
      { name: '人气次推款',  title: '人气次推款',  price: 29.99, stock: 15, badge: 'NEW', rating: 4.7, reviews: 210, desc: '精选面料，匠心剪裁，日常穿搭首选',           gradient: 'linear-gradient(135deg,#111 0%,#1a2a2a 100%)' },
      { name: '经典常备款',  title: '经典常备款',  price: 24.99, stock: 30, badge: null,  rating: 4.6, reviews: 145, desc: '经典百搭，多色可选，必备单品',               gradient: 'linear-gradient(135deg,#111 0%,#1e1e2a 100%)' },
      { name: '联名限定款',  title: '联名限定款',  price: 59.99, originalPrice: 89.99, stock: 3, badge: 'SALE', rating: 5.0, reviews: 88, desc: '品牌联名，限量发售，收藏价值极高', gradient: 'linear-gradient(135deg,#111 0%,#2a1a1a 100%)' },
      { name: '季节新款',    title: '季节新款',    price: 44.99, stock: 20, badge: 'NEW', rating: 4.8, reviews: 62,  desc: '本季主推，设计感十足，潮流前沿',           gradient: 'linear-gradient(135deg,#111 0%,#1a2a1a 100%)' },
      { name: '基础必备款',  title: '基础必备款',  price: 19.99, stock: 50, badge: null,  rating: 4.5, reviews: 523, desc: '百搭基础款，高性价比，入手无悔',             gradient: 'linear-gradient(135deg,#111 0%,#181828 100%)' },
    ],
  },
]
