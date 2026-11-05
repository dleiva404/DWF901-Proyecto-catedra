
import react from '@vitejs/plugin-react'
import { defineConfig } from 'vite'

// Configuración del cliente consumidor DWF901
export default defineConfig({
    plugins: [react()],
    server: {
        port: 5500,
        strictPort: true,
    },
})
