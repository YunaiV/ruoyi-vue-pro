declare function safeArrayConcat<T = unknown>(
    item: T | readonly T[],
    ...items: (T | readonly T[])[],
): T[];

export = safeArrayConcat;
