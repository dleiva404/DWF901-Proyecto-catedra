import { apiRequest } from './api'

function query(params = {}) {
  const valores = new URLSearchParams()
  Object.entries(params).forEach(([clave, valor]) => {
    if (valor !== '' && valor !== null && valor !== undefined) {
      valores.set(clave, String(valor))
    }
  })
  const texto = valores.toString()
  return texto ? `?${texto}` : ''
}

// Contrato REST v1.2 del equipo, docs/contrato-api-rest.md
export const listarSolicitudes = (filtros) =>
  apiRequest(`/solicitudes${query(filtros)}`)

export const crearSolicitud = (datos) =>
  apiRequest('/solicitudes', { method: 'POST', body: datos })

export const cancelarSolicitud = (id) =>
  apiRequest(`/solicitudes/${id}`, { method: 'DELETE' })

export const aprobarSolicitud = (id) =>
  apiRequest(`/solicitudes/${id}/aprobar`, { method: 'POST' })

export const rechazarSolicitud = (id, motivoRechazo) =>
  apiRequest(`/solicitudes/${id}/rechazar`, {
    method: 'POST',
    body: { motivoRechazo },
  })

export const listarTipos = () => apiRequest('/tipos-solicitud')
export const obtenerSaldoVacaciones = () => apiRequest('/vacaciones/saldo')
