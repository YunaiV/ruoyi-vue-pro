import { GetManifestResult, GetManifestOptions } from '../types';
export declare function getFileManifestEntries({ additionalManifestEntries, dontCacheBustURLsMatching, globDirectory, globFollow, globIgnores, globPatterns, manifestTransforms, maximumFileSizeToCacheInBytes, modifyURLPrefix, templatedURLs, }: GetManifestOptions): Promise<GetManifestResult>;
