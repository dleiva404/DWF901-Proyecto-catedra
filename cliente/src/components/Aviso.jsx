export default function Aviso({ tipo = 'error', children }) {
  if (!children) return null
  return <div role={tipo === 'error' ? 'alert' : 'status'} className={`cliente-aviso cliente-aviso-${tipo}`}>{children}</div>
}
