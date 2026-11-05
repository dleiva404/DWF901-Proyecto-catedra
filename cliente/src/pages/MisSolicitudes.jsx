import { useCallback, useEffect, useRef, useState } from 'react'
import { listarSolicitudes, crearSolicitud, cancelarSolicitud, listarTipos, obtenerSaldoVacaciones } from '../services/solicitudes'
import { ESTADOS, fecha, hoy, mensajeDe } from '../utils/formato'
import Aviso from '../components/Aviso'
import Chip from '../components/Chip'
import ConfirmacionCancelacion from '../components/ConfirmacionCancelacion'

export default function MisSolicitudes({ usuario, irAIncapacidad }) {
  const [solicitudes, setSolicitudes] = useState([])
  const [tipos, setTipos] = useState([])
  const [saldo, setSaldo] = useState(null)
  const [filtro, setFiltro] = useState('')
  const [datos, setDatos] = useState({ tipoSolicitudId: '', fechaInicio: '', fechaFin: '', motivo: '' })
  const [error, setError] = useState('')
  const [aviso, setAviso] = useState('')
  const [cargando, setCargando] = useState(true)
  const [enviando, setEnviando] = useState(false)
  const [solicitudParaCancelar, setSolicitudParaCancelar] = useState(null)
  const [cancelando, setCancelando] = useState(false)
  const dialogoCancelacion = useRef(null)

  const recargar = useCallback(async () => {
    const propias = String(usuario.rol).toUpperCase() !== 'EMPLEADO' ? { propias: true } : {}
    const [lista, catalogo, vacaciones] = await Promise.allSettled([
      listarSolicitudes(propias), listarTipos(), obtenerSaldoVacaciones(),
    ])
    if (lista.status === 'fulfilled') setSolicitudes(Array.isArray(lista.value) ? lista.value : [])
    else throw lista.reason
    if (catalogo.status === 'fulfilled') setTipos(Array.isArray(catalogo.value) ? catalogo.value : [])
    else setError(`No se pudieron cargar los tipos de solicitud: ${mensajeDe(catalogo.reason)}`)
    if (vacaciones.status === 'fulfilled') setSaldo(vacaciones.value)
    else setSaldo(null)
  }, [usuario.rol])

  useEffect(() => {
    let activo = true

    async function cargarInicial() {
      try {
        await recargar()
      } catch (err) {
        if (activo) setError(mensajeDe(err))
      } finally {
        if (activo) setCargando(false)
      }
    }

    void cargarInicial()
    return () => { activo = false }
  }, [recargar])

  async function enviar(event) {
    event.preventDefault()
    setError('')
    setAviso('')
    if (!datos.tipoSolicitudId || !datos.fechaInicio || !datos.fechaFin || !datos.motivo.trim()) {
      setError('Completá todos los campos de la solicitud.')
      return
    }
    if (datos.fechaFin < datos.fechaInicio) { setError('La fecha final no puede ser anterior a la inicial.'); return }
    if (datos.fechaInicio < hoy()) { setError('La fecha inicial no puede estar en el pasado.'); return }
    if (datos.motivo.trim().length < 5 || datos.motivo.trim().length > 250) {
      setError('El motivo debe contener entre 5 y 250 caracteres.'); return
    }
    setEnviando(true)
    try {
      await crearSolicitud({ ...datos, tipoSolicitudId: Number(datos.tipoSolicitudId), motivo: datos.motivo.trim() })
      setAviso('Solicitud enviada correctamente.')
      setDatos({ tipoSolicitudId: '', fechaInicio: '', fechaFin: '', motivo: '' })
      await recargar()
    } catch (err) {
      setError(mensajeDe(err))
    } finally {
      setEnviando(false)
    }
  }

  function pedirCancelacion(solicitud) {
    setSolicitudParaCancelar(solicitud)
    dialogoCancelacion.current?.showModal()
  }

  async function confirmarCancelacion() {
    if (!solicitudParaCancelar || cancelando) return
    setError('')
    setAviso('')
    setCancelando(true)
    try {
      await cancelarSolicitud(solicitudParaCancelar.id)
      setAviso('Solicitud cancelada.')
      await recargar()
    } catch (err) {
      setError(mensajeDe(err))
    } finally {
      dialogoCancelacion.current?.close()
      setCancelando(false)
    }
  }

  const tipoNombre = (id) => tipos.find(t => Number(t.id) === Number(id))?.nombre || 'Tipo de solicitud'
  const visibles = solicitudes.filter(s => !filtro || s.estado === filtro)
  const pendientes = solicitudes.filter(s => s.estado === 'PENDIENTE').length
  const aprobadas = solicitudes.filter(s => s.estado === 'APROBADA').length
  const porcentajeSaldo = saldo?.diasAsignados ? 100 * (saldo.diasDisponibles / saldo.diasAsignados) : 0

  return (
    <>
      <h1 className="rh-titulo">Mis solicitudes</h1>
      <Aviso>{error.includes('fechaFin') && error.includes('domingos') ? 'La solicitud debe incluir al menos un día de lunes a sábado.' : error}</Aviso>
      <Aviso tipo="ok">{aviso}</Aviso>

      <div className="rh-bloque">
        {saldo && (
          <div className="rh-hero">
            <div className="rh-hero-texto">
              <div className="rh-hero-etiqueta">VACACIONES {saldo.anio}</div>
              <h2 className="rh-hero-titulo">Tu saldo de vacaciones</h2>
              <p>Disponible para nuevas solicitudes</p>
            </div>
            <div className="rh-anillo">
              <svg width="140" height="140" viewBox="0 0 120 120" aria-hidden="true">
                <circle cx="60" cy="60" r="48" fill="none" stroke="#343e57" strokeWidth="12" />
                <circle
                  cx="60" cy="60" r="48" fill="none" stroke="#f2792e"
                  strokeWidth="12" strokeLinecap="round"
                  strokeDasharray={`${Math.max(0, Math.min(100, porcentajeSaldo)) * 3.0159} 301.59`}
                  transform="rotate(-90 60 60)"
                />
              </svg>
              <div className="rh-anillo-centro">
                <div className="rh-anillo-num">{saldo.diasDisponibles}</div>
                <div className="rh-anillo-sub">de {saldo.diasAsignados} días</div>
              </div>
            </div>
          </div>
        )}

        <div className="rh-tiles">
          <div className="rh-tile">
            <div className="rh-tile-etq">Pendientes de aprobación</div>
            <div className="rh-tile-num">{pendientes}</div>
            <div className="rh-tile-pie">Esperando a tu jefatura</div>
          </div>
          <div className="rh-tile">
            <div className="rh-tile-etq">Solicitudes aprobadas</div>
            <div className="rh-tile-num">{aprobadas}</div>
            <div className="rh-tile-pie">Autorizadas por tu jefatura</div>
          </div>
        </div>
      </div>

      <div className="rh-dos-col">
        <section className="rh-col-act">
          <div className="cliente-cab-seccion">
            <h2 className="rh-h2">Actividad reciente</h2>
            <label className="cliente-filtro">
              Estado
              <select value={filtro} onChange={event => setFiltro(event.target.value)}>
                <option value="">Todos</option>
                {ESTADOS.map(estado => (
                  <option key={estado} value={estado}>{estado}</option>
                ))}
              </select>
            </label>
          </div>

          {cargando ? (
            <div className="rh-vacio">Cargando solicitudes…</div>
          ) : visibles.length === 0 ? (
            <div className="rh-vacio">No hay solicitudes para mostrar.</div>
          ) : (
            <div className="rh-grid-act">
              {visibles.map(solicitud => (
                <article className="rh-card-act" key={solicitud.id}>
                  <div className="rh-card-fila">
                    <span className="rh-card-tipo">{tipoNombre(solicitud.tipoSolicitudId)}</span>
                    <Chip estado={solicitud.estado} />
                  </div>
                  <div className="rh-card-fecha">
                    {fecha(solicitud.fechaInicio)} al {fecha(solicitud.fechaFin)}
                  </div>
                  <div className="cliente-texto-secundario">{solicitud.motivo}</div>
                  {solicitud.diasSolicitados !== null && (
                    <div className="cliente-texto-secundario">
                      {solicitud.diasSolicitados} día(s)
                    </div>
                  )}
                  {solicitud.motivoRechazo && (
                    <div className="rh-card-rechazo">
                      Motivo del rechazo: {solicitud.motivoRechazo}
                    </div>
                  )}
                  {solicitud.estado === 'PENDIENTE' && (
                    <button
                      className="rh-btn-cancelar-sol"
                      disabled={enviando || cancelando}
                      onClick={() => pedirCancelacion(solicitud)}
                    >
                      Cancelar solicitud
                    </button>
                  )}
                  {solicitud.estado === 'APROBADA' && (
                    <p className="cliente-nota-cancelacion">
                      Para cancelar una solicitud aprobada, comunicate con Recursos Humanos.
                    </p>
                  )}
                </article>
              ))}
            </div>
          )}
        </section>

        <section className="rh-col-form">
          <h2 className="rh-h2">Nueva solicitud</h2>
          <form className="rh-form" onSubmit={enviar}>
            <label className="rh-campo">
              Tipo de solicitud
              <select
                required
                disabled={enviando || cargando}
                value={datos.tipoSolicitudId}
                onChange={event => setDatos(actual => ({
                  ...actual, tipoSolicitudId: event.target.value,
                }))}
              >
                <option value="">Seleccioná un tipo</option>
                {tipos
                  .filter(tipo => tipo.nombre?.toUpperCase() !== 'INCAPACIDAD')
                  .map(tipo => <option value={tipo.id} key={tipo.id}>{tipo.nombre}</option>)}
              </select>
            </label>

            <div className="rh-fila-fechas">
              <label className="rh-campo">
                Fecha inicio
                <input
                  type="date" min={hoy()} required disabled={enviando}
                  value={datos.fechaInicio}
                  onChange={event => setDatos(actual => ({
                    ...actual, fechaInicio: event.target.value,
                  }))}
                />
              </label>
              <label className="rh-campo">
                Fecha fin
                <input
                  type="date" min={datos.fechaInicio || hoy()} required disabled={enviando}
                  value={datos.fechaFin}
                  onChange={event => setDatos(actual => ({
                    ...actual, fechaFin: event.target.value,
                  }))}
                />
              </label>
            </div>

            <label className="rh-campo">
              Motivo
              <textarea
                rows="4" maxLength="250" minLength="5" required
                disabled={enviando} value={datos.motivo}
                onChange={event => setDatos(actual => ({
                  ...actual, motivo: event.target.value,
                }))}
              />
            </label>
            <div className="rh-nota-azul">
              Los domingos no cuentan como días solicitados. Las fechas y el saldo disponible se verificarán al enviar tu solicitud.
            </div>
            <button
              className="rh-btn-enviar" type="submit"
              disabled={enviando || cargando || tipos.length === 0}
            >
              {enviando ? 'Enviando…' : 'Enviar solicitud'}
            </button>
            <button type="button" className="cliente-enlace" onClick={irAIncapacidad}>
              Registrar una incapacidad
            </button>
          </form>
        </section>
      </div>

      <ConfirmacionCancelacion
        referencia={dialogoCancelacion}
        solicitud={solicitudParaCancelar}
        procesando={cancelando}
        onConfirmar={confirmarCancelacion}
        onCerrar={() => setSolicitudParaCancelar(null)}
      />
    </>
  )
}
