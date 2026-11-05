export const ESTADOS = ['PENDIENTE', 'APROBADA', 'RECHAZADA', 'CANCELADA']

export function hoy() {
  const fechaActual = new Date()
  const anio = fechaActual.getFullYear()
  const mes = String(fechaActual.getMonth() + 1).padStart(2, '0')
  const dia = String(fechaActual.getDate()).padStart(2, '0')

  return `${anio}-${mes}-${dia}`
}

export const fecha = (valor) => valor ? String(valor).slice(0, 10) : '—'

export const mensajeDe = (error) => error instanceof Error
  ? error.message
  : 'Ocurrió un error inesperado.'

export const iniciales = (nombre = '') => nombre.trim()
  .split(/\s+/)
  .slice(0, 2)
  .map(parte => parte[0]?.toUpperCase())
  .join('') || 'RH'
