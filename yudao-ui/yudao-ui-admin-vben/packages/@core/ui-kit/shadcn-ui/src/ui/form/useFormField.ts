import { inject } from 'vue';

import {
  FieldContextKey,
  useFieldError,
  useIsFieldDirty,
  useIsFieldTouched,
  useIsFieldValid,
} from 'vee-validate';

import { FORM_ITEM_INJECTION_KEY } from './injectionKeys';

export function useFormField() {
  const fieldContext = inject(FieldContextKey);
  const fieldItemContext = inject(FORM_ITEM_INJECTION_KEY);

  if (!fieldContext)
    throw new Error('useFormField should be used within <FormField>');

  const { name } = fieldContext;
  const id = fieldItemContext;

  const fieldState = {
    error: useFieldError(name),
    isDirty: useIsFieldDirty(name),
    isTouched: useIsFieldTouched(name),
    valid: useIsFieldValid(name),
  };

  return {
    formDescriptionId: `${id}-form-item-description`,
    formItemId: `${id}-form-item`,
    formMessageId: `${id}-form-item-message`,
    id,
    name,
    ...fieldState,
  };
}
