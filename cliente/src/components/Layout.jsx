import { iniciales } from '../utils/formato'
import Icono from './Icono'
import GestionConstancias from '../pages/GestionConstancias'

export default function Layout({ usuario, seccion, setSeccion, onLogout, ocupado, children }) {
  const rol = String(usuario.rol || '').toUpperCase()
  const opciones = [
    ...(rol === 'JEFATURA' ? [{ id: 'jefatura', nombre: 'Solicitudes de empleados', icono: 'equipo' }] : []),
    ...(rol === 'RRHH' ? [{ id: 'gestionConstancias', nombre: 'Gestión de constancias', icono: 'documento' }] : []),
    { id: 'solicitudes', nombre: 'Mis solicitudes', icono: 'calendario' },
    { id: 'incapacidad', nombre: 'Registrar incapacidad', icono: 'documento' },
    { id: 'constancias', nombre: rol === 'RRHH' ? 'Mis constancias' : 'Constancias', icono: 'documento' },
  ]
  return (
    <div className="rh-layout">
      <aside className="rh-side">
        <div className="rh-logo">
          <img src="/logo.png" alt="Invercalma" />
        </div>

        <nav className="rh-nav" aria-label="Secciones del sistema">
          <div className="rh-nav-titulo">SISTEMA DE RRHH</div>
          {opciones.map(opcion => (
            <button
              key={opcion.id} type="button"
              className={`rh-item ${seccion === opcion.id ? 'rh-activo' : ''}`}
              onClick={() => setSeccion(opcion.id)}
              aria-current={seccion === opcion.id ? 'page' : undefined}
            >
              <span className="rh-ico"><Icono tipo={opcion.icono} /></span>
              {opcion.nombre}
            </button>
          ))}
        </nav>

        <div className="rh-user">
          <div className="rh-avatar">{iniciales(usuario.nombre || usuario.username)}</div>
          <div className="rh-user-datos">
            <div className="rh-user-nombre">{usuario.nombre || usuario.username}</div>
            <div className="rh-user-rol">
              {rol === 'RRHH' ? 'Recursos Humanos' : rol === 'JEFATURA' ? 'Jefatura' : 'Empleado'}
            </div>
          </div>
          <button
            className="rh-salir" onClick={onLogout} disabled={ocupado}
            type="button" title="Cerrar sesión" aria-label="Cerrar sesión"
          >
            <Icono tipo="salir" />
          </button>
        </div>
      </aside>

      <main className="rh-main">
        <div className="rh-contenido">
          {seccion === 'gestionConstancias' && rol === 'RRHH' ? <GestionConstancias /> : children}
        </div>
      </main>
    </div>
  )
}
