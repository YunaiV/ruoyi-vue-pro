{
  "name": "idb",
  "version": "7.1.1",
  "description": "A small wrapper that makes IndexedDB usable",
  "main": "./build/index.cjs",
  "module": "./build/index.js",
  "types": "./build/index.d.ts",
  "exports": {
    ".": {
      "types": "./build/index.d.ts",
      "module": "./build/index.js",
      "import": "./build/index.js",
      "default": "./build/index.cjs"
    },
    "./with-async-ittr": {
      "types": "./with-async-ittr.d.ts",
      "module": "./with-async-ittr.js",
      "import": "./with-async-ittr.js",
      "default": "./with-async-ittr.cjs"
    },
    "./build/*": "./build/*",
    "./package.json": "./package.json"
  },
  "files": [
    "build/**",
    "with-*",
    "CHANGELOG.md"
  ],
  "type": "module",
  "scripts": {
    "build": "PRODUCTION=1 rollup -c && node --experimental-modules lib/size-report.mjs",
    "dev": "rollup -c --watch",
    "prepack": "npm run build"
  },
  "repository": {
    "type": "git",
    "url": "git://github.com/jakearchibald/idb.git"
  },
  "author": "Jake Archibald",
  "license": "ISC",
  "devDependencies": {
    "@rollup/plugin-commonjs": "^22.0.2",
    "@rollup/plugin-node-resolve": "^14.1.0",
    "@types/chai": "^4.3.3",
    "@types/estree": "^1.0.0",
    "@types/mocha": "^9.1.1",
    "chai": "^4.3.6",
    "conditional-type-checks": "^1.0.6",
    "del": "^7.0.0",
    "filesize": "^9.0.11",
    "glob": "^8.0.3",
    "mocha": "^10.0.0",
    "prettier": "^2.7.1",
    "rollup": "^2.79.0",
    "rollup-plugin-terser": "^7.0.2",
    "typescript": "^4.8.3"
  }
}
