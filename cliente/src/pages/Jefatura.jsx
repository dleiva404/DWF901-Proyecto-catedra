import { useCallback, useEffect, useState } from 'react'
import { listarSolicitudes, aprobarSolicitud, rechazarSolicitud } from '../services/solicitudes'
import { fecha, mensajeDe } from '../utils/formato'
import Aviso from '../components/Aviso'
import Chip from '../components/Chip'

export default function Jefatura({ usuario }) {
  const [lista, setLista] = useState([])
  const [error, setError] = useState('')
  const [aviso, setAviso] = useState('')
  const [cargando, setCargando] = useState(true)
  const [resolviendo, setResolviendo] = useState(false)
  const [rechazoId, setRechazoId] = useState(null)
  const [motivoRechazo, setMotivoRechazo] = useState('')

  const cargar = useCallback(async () => {
    const resultado = await listarSolicitudes({ estado: 'PENDIENTE' })
    setLista(Array.isArray(resultado) ? resultado.filter(s => Number(s.empleadoId) !== Number(usuario.empleadoId)) : [])
  }, [usuario.empleadoId])
  useEffect(() => {
    let activo = true

    async function cargarInicial() {
      try {
        await cargar()
      } catch (err) {
        if (activo) setError(mensajeDe(err))
      } finally {
        if (activo) setCargando(false)
      }
    }

    void cargarInicial()
    return () => { activo = false }
  }, [cargar])

  async function resolver(id, accion) {
    setError('')
    setAviso('')
    if (accion === 'rechazar' && !motivoRechazo.trim()) { setError('Ingresá el motivo del rechazo.'); return }
    setResolviendo(true)
    try {
      if (accion === 'aprobar') await aprobarSolicitud(id)
      else await rechazarSolicitud(id, motivoRechazo.trim())
      setAviso(`Solicitud ${accion === 'aprobar' ? 'aprobada' : 'rechazada'} correctamente.`)
      setRechazoId(null)
      setMotivoRechazo('')
      await cargar()
    } catch (err) {
      setError(mensajeDe(err))
    } finally {
      setResolviendo(false)
    }
  }

  return (
    <>
      <h1 className="rh-titulo">Solicitudes de empleados</h1>
      <p className="rh-subtitulo">Bandeja de solicitudes pendientes del departamento</p>
      <Aviso>{error}</Aviso>
      <Aviso tipo="ok">{aviso}</Aviso>

      <section className="rh-panel-tabla">
        <div className="rh-panel-cab">
          <h2 className="rh-h2 rh-h2-0">Pendientes</h2>
          <span className="rh-panel-resumen">{lista.length} solicitud(es)</span>
        </div>

        {cargando ? (
          <div className="rh-vacio">Cargando…</div>
        ) : lista.length === 0 ? (
          <div className="rh-vacio">No hay solicitudes pendientes.</div>
        ) : (
          <div className="rh-tabla-scroll">
            <table className="rh-tabla">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>EMPLEADO</th>
                  <th>FECHAS</th>
                  <th>MOTIVO</th>
                  <th>ESTADO</th>
                  <th>ACCIÓN</th>
                </tr>
              </thead>
              <tbody>
                {lista.map(solicitud => (
                  <tr key={solicitud.id}>
                    <td>#{solicitud.id}</td>
                    <td>{solicitud.empleadoNombre || 'Nombre no disponible'}</td>
                    <td className="rh-nowrap">
                      {fecha(solicitud.fechaInicio)}<br />{fecha(solicitud.fechaFin)}
                    </td>
                    <td>{solicitud.motivo}</td>
                    <td><Chip estado={solicitud.estado} /></td>
                    <td>
                      {rechazoId === solicitud.id ? (
                        <div className="cliente-rechazo">
                          <label>
                            Motivo del rechazo
                            <textarea
                              rows="2" maxLength="500" value={motivoRechazo}
                              disabled={resolviendo}
                              onChange={event => setMotivoRechazo(event.target.value)}
                            />
                          </label>
                          <button
                            type="button" className="rh-btn-rechazar"
                            disabled={resolviendo}
                            onClick={() => resolver(solicitud.id, 'rechazar')}
                          >
                            Confirmar rechazo
                          </button>
                          <button
                            type="button" className="rh-btn-cancelar"
                            disabled={resolviendo}
                            onClick={() => setRechazoId(null)}
                          >
                            Volver
                          </button>
                        </div>
                      ) : (
                        <div className="cliente-acciones">
                          <button
                            type="button" className="rh-btn-aprobar"
                            disabled={resolviendo}
                            onClick={() => resolver(solicitud.id, 'aprobar')}
                          >
                            Aprobar
                          </button>
                          <button
                            type="button" className="rh-btn-rechazar"
                            disabled={resolviendo}
                            onClick={() => {
                              setRechazoId(solicitud.id)
                              setMotivoRechazo('')
                            }}
                          >
                            Rechazar
                          </button>
                        </div>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </section>
    </>
  )
}
