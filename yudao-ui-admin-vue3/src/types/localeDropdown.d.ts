export interface Language {
  el: Recordable
  name: string
}

export interface LocaleDropdownType {
  lang: LocaleType
  name?: string
  elLocale?: Language
}
