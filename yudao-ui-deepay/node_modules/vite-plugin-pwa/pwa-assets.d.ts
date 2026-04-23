/* WARNING: these virtual modules are meant to be used by integrations and not being consumed directly from applications */

declare module 'virtual:pwa-assets/head' {
  export interface PWAAssetHeadLink {
    id?: string
    rel: 'apple-touch-startup-image' | 'apple-touch-icon' | 'icon'
    href: string
    media?: string
    sizes?: string
    type?: string
  }

  export interface ColorSchemeMeta {
    name: string
    content: string
  }

  export interface PWAAssetsHead {
    links: PWAAssetHeadLink[]
    themeColor?: ColorSchemeMeta
  }

  export const pwaAssetsHead: PWAAssetsHead
}

declare module 'virtual:pwa-assets/icons' {
  import type { AppleSplashScreenLink, FaviconLink, HtmlLink, IconAsset } from '@vite-pwa/assets-generator/api'

  export type PWAAssetIcon<T extends HtmlLink> = Omit<IconAsset<T>, 'buffer'>

  export interface PWAAssetsIcons {
    favicon: Record<string, PWAAssetIcon<FaviconLink>>
    transparent: Record<string, PWAAssetIcon<HtmlLink>>
    maskable: Record<string, PWAAssetIcon<HtmlLink>>
    apple: Record<string, PWAAssetIcon<HtmlLink>>
    appleSplashScreen: Record<string, PWAAssetIcon<AppleSplashScreenLink>>
  }

  export const pwaAssetsIcons: PWAAssetsIcons
}
