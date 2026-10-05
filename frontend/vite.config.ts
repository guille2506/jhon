import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

// The backend base URL can be overridden with VITE_API_URL at build/run time.
// In dev we proxy /api to the Spring Boot backend so no CORS handling is needed.
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: process.env.VITE_BACKEND_URL || 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
  // Vitest extends the Vite config with a `test` field that the base Vite
  // types don't know about; the cast keeps `tsc` happy during `npm run build`.
  ...({
    test: {
      globals: true,
      environment: 'jsdom',
      setupFiles: './src/test/setup.ts',
      css: false,
    },
  } as object),
});
