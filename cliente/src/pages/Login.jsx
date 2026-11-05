import { useState } from 'react'
import { iniciarSesion } from '../services/auth'
import { mensajeDe } from '../utils/formato'

export default function Login({ onLogin, avisoInicial }) {
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState(avisoInicial || '')
  const [enviando, setEnviando] = useState(false)

  async function enviar(event) {
    event.preventDefault()
    setError('')
    if (!username.trim() || !password) {
      setError('Ingresá tu usuario y contraseña.')
      return
    }
    setEnviando(true)
    try {
      const usuario = await iniciarSesion(username, password)
      setPassword('')
      onLogin(usuario)
    } catch (err) {
      setError(mensajeDe(err))
    } finally {
      setEnviando(false)
    }
  }

  return (
    <div className="login-pantalla">
      <div className="login-tarjeta">
        <div className="login-marca">
          <div className="marca-logo-fila">
            <img src="/logo.png" alt="Invercalma" />
          </div>
          <h1 className="marca-titulo">Sistema de Solicitud de Permisos</h1>
          <p className="marca-descripcion">Gestiona tus solicitudes.</p>
        </div>

        <div className="login-formulario-panel">
          <div className="login-formulario">
            <h2>Bienvenido</h2>
            <p className="subtitulo">Ingresa con tu usuario y contraseña.</p>
            {error && <div role="alert" className="mensaje-error">{error}</div>}

            <form onSubmit={enviar}>
              <label htmlFor="cliente-username">Usuario</label>
              <input
                id="cliente-username" type="text" value={username}
                autoComplete="username" autoFocus disabled={enviando} required
                onChange={event => setUsername(event.target.value)}
              />
              <label htmlFor="cliente-password">Contraseña</label>
              <input
                id="cliente-password" type="password" value={password}
                autoComplete="current-password" disabled={enviando} required
                onChange={event => setPassword(event.target.value)}
              />
              <button className="login-boton" type="submit" disabled={enviando}>
                {enviando ? 'Ingresando...' : 'Ingresar'}
              </button>
            </form>
            <div className="login-divisor" />
          </div>
        </div>
      </div>
    </div>
  )
}
