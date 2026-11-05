import { apiRequest } from './api'

// La API devuelve solo constancias autorizadas para el usuario autenticado.
export const listarConstancias = (propias = true) =>
  apiRequest(`/constancias${propias ? '?propias=true' : ''}`)

export const solicitarConstancia = (datos) =>
  apiRequest('/constancias', { method: 'POST', body: datos })

export const descargarConstancia = (id) =>
  apiRequest(`/constancias/${id}/pdf`, { responseType: 'blob' })

export const aprobarConstancia = (id) =>
  apiRequest(`/constancias/${id}/aprobar`, { method: 'POST', body: {} })

export const rechazarConstancia = (id, motivoRechazo) =>
  apiRequest(`/constancias/${id}/rechazar`, { method: 'POST', body: { motivoRechazo } })
