declare interface Language {
  el: Recordable
  name: string
}

declare interface LocaleDropdownType {
  lang: LocaleType
  name?: string
  elLocale?: Language
}
