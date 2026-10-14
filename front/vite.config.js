import react from '@vitejs/plugin-react'
import { defineConfig } from 'vite'

// 백엔드(Spring Boot) 개발 서버.
// changeOrigin을 끄고 Host를 그대로 넘겨야 OAuth redirect_uri·로그인 후 이동 주소가 프론트 주소로 만들어진다
const backend = { target: 'http://localhost:8080', changeOrigin: false }

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    // 인증 쿠키를 같은 출처로 주고받도록 API·소셜 로그인 경로를 백엔드로 넘긴다
    proxy: {
      '/api': backend,
      '/oauth2': backend,
      '/login/oauth2': backend,
    },
  },
})
