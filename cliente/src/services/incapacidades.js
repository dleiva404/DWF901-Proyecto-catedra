import { apiRequest } from './api'

export const registrarIncapacidad = (datos) =>
  apiRequest('/incapacidades', { method: 'POST', body: datos })
