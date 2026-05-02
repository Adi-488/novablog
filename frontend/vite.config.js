import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      // Forward all /api calls to the Spring Boot backend
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: false,
        secure: false,
      },
    },
  },
})
