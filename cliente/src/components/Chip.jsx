import { ESTADOS } from '../utils/formato'

export default function Chip({ estado }) {
  const normalizado = String(estado || 'PENDIENTE').toUpperCase()
  return <span className={`rh-chip rh-chip-${ESTADOS.includes(normalizado) ? normalizado.toLowerCase() : 'pendiente'}`}>
    <span className="rh-punto" />{normalizado}
  </span>
}
