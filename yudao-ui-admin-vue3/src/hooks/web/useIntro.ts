import introJs from 'intro.js'
import { IntroJs, Step, Options } from 'intro.js'
import 'intro.js/introjs.css'

import { useDesign } from '@/hooks/web/useDesign'

export const useIntro = (setps?: Step[], options?: Options) => {
  const { t } = useI18n()

  const { variables } = useDesign()

  const defaultSetps: Step[] = setps || [
    {
      element: `#${variables.namespace}-menu`,
      title: t('common.menu'),
      intro: t('common.menuDes'),
      position: 'right'
    },
    {
      element: `#${variables.namespace}-tool-header`,
      title: t('common.tool'),
      intro: t('common.toolDes'),
      position: 'left'
    },
    {
      element: `#${variables.namespace}-tags-view`,
      title: t('common.tagsView'),
      intro: t('common.tagsViewDes'),
      position: 'bottom'
    }
  ]

  const defaultOptions: Options = options || {
    prevLabel: t('common.prevLabel'),
    nextLabel: t('common.nextLabel'),
    skipLabel: t('common.skipLabel'),
    doneLabel: t('common.doneLabel')
  }

  const introRef: IntroJs = introJs()

  introRef.addSteps(defaultSetps).setOptions(defaultOptions)

  return {
    introRef
  }
}
