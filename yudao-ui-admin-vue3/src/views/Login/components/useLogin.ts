import { ref, computed, unref, Ref } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'

export enum LoginStateEnum {
  LOGIN,
  REGISTER,
  RESET_PASSWORD,
  MOBILE,
  QR_CODE
}

const currentState = ref(LoginStateEnum.LOGIN)

export function useLoginState() {
  function setLoginState(state: LoginStateEnum) {
    currentState.value = state
  }
  const getLoginState = computed(() => currentState.value)

  function handleBackLogin() {
    setLoginState(LoginStateEnum.LOGIN)
  }

  return {
    setLoginState,
    getLoginState,
    handleBackLogin
  }
}

export function useFormValid<T extends Object = any>(formRef: Ref<any>) {
  async function validForm() {
    const form = unref(formRef)
    if (!form) return
    const data = await form.validate()
    return data as T
  }

  return {
    validForm
  }
}

// TODO @jinz：多余的，是不是可以删除哈
export function useFormRules(formData?: Recordable) {
  const { t } = useI18n()

  const getAccountFormRule = computed(() => createRule(t('sys.login.accountPlaceholder')))
  const getPasswordFormRule = computed(() => createRule(t('sys.login.passwordPlaceholder')))
  const getSmsFormRule = computed(() => createRule(t('sys.login.smsPlaceholder')))
  const getMobileFormRule = computed(() => createRule(t('sys.login.mobilePlaceholder')))

  const validatePolicy = async (_: RuleObject, value: boolean) => {
    return !value ? Promise.reject(t('sys.login.policyPlaceholder')) : Promise.resolve()
  }

  const validateConfirmPassword = (password: string) => {
    return async (_: RuleObject, value: string) => {
      if (!value) {
        return Promise.reject(t('sys.login.passwordPlaceholder'))
      }
      if (value !== password) {
        return Promise.reject(t('sys.login.diffPwd'))
      }
      return Promise.resolve()
    }
  }

  const getFormRules = computed(
    (): {
      [k: string]: ValidationRule | ValidationRule[]
    } => {
      const accountFormRule = unref(getAccountFormRule)
      const passwordFormRule = unref(getPasswordFormRule)
      const smsFormRule = unref(getSmsFormRule)
      const mobileFormRule = unref(getMobileFormRule)

      const mobileRule = {
        sms: smsFormRule,
        mobile: mobileFormRule
      }
      switch (unref(currentState)) {
        // register form rules
        case LoginStateEnum.REGISTER:
          return {
            account: accountFormRule,
            password: passwordFormRule,
            confirmPassword: [
              {
                validator: validateConfirmPassword(formData?.password),
                trigger: 'change'
              }
            ],
            policy: [
              {
                validator: validatePolicy,
                trigger: 'change'
              }
            ],
            ...mobileRule
          }

        // reset password form rules
        case LoginStateEnum.RESET_PASSWORD:
          return {
            account: accountFormRule,
            ...mobileRule
          }

        // mobile form rules
        case LoginStateEnum.MOBILE:
          return mobileRule

        // login form rules
        default:
          return {
            account: accountFormRule,
            password: passwordFormRule
          }
      }
    }
  )
  return {
    getFormRules
  }
}

function createRule(message: string) {
  return [
    {
      required: true,
      message,
      trigger: 'change'
    }
  ]
}
