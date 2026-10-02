import { defineConfig } from 'vite';
import path from 'path';
import { fileURLToPath } from 'url';

const __dirname = path.dirname(fileURLToPath(import.meta.url));

export default defineConfig({
  root: __dirname,
  base: './',
  server: {
    port: 3000,
    host: true
  },
  build: {
    outDir: path.resolve(__dirname, 'dist'),
    assetsDir: 'assets',
    emptyOutDir: true,
    sourcemap: false
  }
});
