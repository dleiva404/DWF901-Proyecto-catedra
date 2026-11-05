import { useEffect, useState } from 'react'
import { cerrarSesion, obtenerUsuario } from './services/auth'
import Layout from './components/Layout'
import Login from './pages/Login'
import MisSolicitudes from './pages/MisSolicitudes'
import Jefatura from './pages/Jefatura'
import Constancias from './pages/Constancias'
import Incapacidad from './pages/Incapacidad'
import './login.css'
import './plantilla.css'
import './cliente.css'

export default function ClienteRRHH() {
  const [usuario, setUsuario] = useState(() => obtenerUsuario())
  const [seccion, setSeccion] = useState(() => obtenerUsuario()?.rol?.toUpperCase() === 'JEFATURA' ? 'jefatura' : 'solicitudes')
  const [errorSesion, setErrorSesion] = useState('')
  const [saliendo, setSaliendo] = useState(false)

  useEffect(() => {
    document.title = 'Sistema de Solicitud de Permisos'
    document.body.classList.toggle('login-page', !usuario)
    document.body.classList.toggle('rh-body', Boolean(usuario))
    return () => { document.body.classList.remove('login-page', 'rh-body') }
  }, [usuario])

  useEffect(() => {
    const caducada = () => {
      sessionStorage.removeItem('dwf901_usuario')
      setUsuario(null)
      setErrorSesion('Tu sesión venció. Iniciá sesión nuevamente.')
    }
    window.addEventListener('sesion-expirada', caducada)
    return () => window.removeEventListener('sesion-expirada', caducada)
  }, [])

  async function salir() {
    setSaliendo(true)
    try {
      await cerrarSesion()
    } catch {
      // Siempre se cierra la sesión local, aunque no haya conexión.
    } finally {
      setUsuario(null)
      setErrorSesion('')
      setSaliendo(false)
    }
  }

  function entrar(datosUsuario) {
    setUsuario(datosUsuario)
    setErrorSesion('')
    setSeccion(datosUsuario.rol?.toUpperCase() === 'JEFATURA' ? 'jefatura' : 'solicitudes')
  }

  if (!usuario) return <Login key={errorSesion} avisoInicial={errorSesion} onLogin={entrar} />

  return <Layout usuario={usuario} seccion={seccion} setSeccion={setSeccion} onLogout={salir} ocupado={saliendo}>
    {seccion === 'jefatura' && usuario.rol?.toUpperCase() === 'JEFATURA' ? <Jefatura usuario={usuario} /> :
      seccion === 'incapacidad' ? <Incapacidad volver={() => setSeccion('solicitudes')} /> :
      seccion === 'constancias' ? <Constancias usuario={usuario} /> :
      <MisSolicitudes usuario={usuario} irAIncapacidad={() => setSeccion('incapacidad')} />}
  </Layout>
}
