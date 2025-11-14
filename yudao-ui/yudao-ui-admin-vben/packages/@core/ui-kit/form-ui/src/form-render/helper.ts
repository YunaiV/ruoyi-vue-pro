import type {
  AnyZodObject,
  ZodDefault,
  ZodEffects,
  ZodNumber,
  ZodString,
  ZodTypeAny,
} from 'zod';

import { isObject, isString } from '@vben-core/shared/utils';

/**
 * Get the lowest level Zod type.
 * This will unpack optionals, refinements, etc.
 */
export function getBaseRules<
  ChildType extends AnyZodObject | ZodTypeAny = ZodTypeAny,
>(schema: ChildType | ZodEffects<ChildType>): ChildType | null {
  if (!schema || isString(schema)) return null;
  if ('innerType' in schema._def)
    return getBaseRules(schema._def.innerType as ChildType);

  if ('schema' in schema._def)
    return getBaseRules(schema._def.schema as ChildType);

  return schema as ChildType;
}

/**
 * Search for a "ZodDefault" in the Zod stack and return its value.
 */
export function getDefaultValueInZodStack(schema: ZodTypeAny): any {
  if (!schema || isString(schema)) {
    return;
  }
  const typedSchema = schema as unknown as ZodDefault<ZodNumber | ZodString>;

  if (typedSchema._def.typeName === 'ZodDefault')
    return typedSchema._def.defaultValue();

  if ('innerType' in typedSchema._def) {
    return getDefaultValueInZodStack(
      typedSchema._def.innerType as unknown as ZodTypeAny,
    );
  }
  if ('schema' in typedSchema._def) {
    return getDefaultValueInZodStack(
      (typedSchema._def as any).schema as ZodTypeAny,
    );
  }

  return undefined;
}

export function isEventObjectLike(obj: any) {
  if (!obj || !isObject(obj)) {
    return false;
  }
  return Reflect.has(obj, 'target') && Reflect.has(obj, 'stopPropagation');
}
