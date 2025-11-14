declare module 'vue' {
  export interface GlobalComponents {
    ['z-paging']: typeof import('./comps/z-paging')['ZPaging']
    ['z-paging-swiper']: typeof import('./comps/z-paging-swiper')['ZPagingSwiper']
    ['z-paging-swiper-item']: typeof import('./comps/z-paging-swiper-item')['ZPagingSwiperItem']
    ['z-paging-empty-view']: typeof import('./comps/z-paging-empty-view')['ZPagingEmptyView']
    ['z-paging-cell']: typeof import('./comps/z-paging-cell')['ZPagingCell']
  }
}

export {}
