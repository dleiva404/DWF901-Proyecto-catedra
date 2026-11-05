import { useCallback, useEffect, useState } from 'react'
import { listarConstancias, aprobarConstancia, rechazarConstancia } from '../services/constancias'
import { listarEmpleados } from '../services/empleados'
import { fecha, mensajeDe } from '../utils/formato'
import Aviso from '../components/Aviso'
import Chip from '../components/Chip'

const ESTADOS_FILTRO = [
  { valor: 'PENDIENTE', texto: 'Pendientes' },
  { valor: 'TODAS', texto: 'Todas' },
  { valor: 'APROBADA', texto: 'Aprobadas' },
  { valor: 'RECHAZADA', texto: 'Rechazadas' },
]

export default function GestionConstancias() {
  const [constancias, setConstancias] = useState([])
  const [nombresEmpleados, setNombresEmpleados] = useState({})
  const [filtro, setFiltro] = useState('PENDIENTE')
  const [busqueda, setBusqueda] = useState('')
  const [detalleId, setDetalleId] = useState(null)
  const [motivoRechazo, setMotivoRechazo] = useState('')
  const [mostrarRechazo, setMostrarRechazo] = useState(false)
  const [cargando, setCargando] = useState(true)
  const [procesando, setProcesando] = useState(false)
  const [error, setError] = useState('')
  const [aviso, setAviso] = useState('')

  const actualizarListado = useCallback(async () => {
    const [resultadoConstancias, resultadoEmpleados] = await Promise.allSettled([
      listarConstancias(false), listarEmpleados(),
    ])

    if (resultadoConstancias.status === 'rejected') throw resultadoConstancias.reason
    setConstancias(Array.isArray(resultadoConstancias.value) ? resultadoConstancias.value : [])

    if (resultadoEmpleados.status === 'fulfilled' && Array.isArray(resultadoEmpleados.value)) {
      const nombres = Object.fromEntries(resultadoEmpleados.value.map(empleado => [
        String(empleado.id),
        `${empleado.nombre || ''} ${empleado.apellido || ''}`.trim(),
      ]))
      setNombresEmpleados(nombres)
    }
  }, [])

  useEffect(() => {
    let activo = true

    async function cargarInicial() {
      try {
        await actualizarListado()
      } catch (err) {
        if (activo) setError(mensajeDe(err))
      } finally {
        if (activo) setCargando(false)
      }
    }

    void cargarInicial()
    return () => { activo = false }
  }, [actualizarListado])

  const nombreEmpleado = (id) => nombresEmpleados[String(id)] || `Empleado #${id}`
  const detalle = constancias.find(constancia => constancia.id === detalleId)
  const pendientes = constancias.filter(constancia => constancia.estado === 'PENDIENTE').length
  const textoBuscado = busqueda.trim().toLocaleLowerCase('es')
  const visibles = constancias.filter(constancia => {
    if (filtro !== 'TODAS' && constancia.estado !== filtro) return false
    if (!textoBuscado) return true

    return [
      nombreEmpleado(constancia.empleadoId), constancia.institucion,
      constancia.tipo, constancia.motivo, constancia.empresaEmisora,
    ].some(valor => String(valor || '').toLocaleLowerCase('es').includes(textoBuscado))
  })

  function verDetalle(id) {
    setDetalleId(id)
    setMotivoRechazo('')
    setMostrarRechazo(false)
    setError('')
    setAviso('')
  }

  async function resolver(accion) {
    if (!detalle || detalle.estado !== 'PENDIENTE' || procesando) return
    setError('')
    setAviso('')

    const motivo = motivoRechazo.trim()
    if (accion === 'rechazar' && (motivo.length < 5 || motivo.length > 250)) {
      setError('El motivo del rechazo debe tener entre 5 y 250 caracteres.')
      return
    }

    setProcesando(true)
    try {
      if (accion === 'aprobar') await aprobarConstancia(detalle.id)
      else await rechazarConstancia(detalle.id, motivo)

      setAviso(`Constancia ${accion === 'aprobar' ? 'aprobada' : 'rechazada'} correctamente.`)
      setDetalleId(null)
      setMostrarRechazo(false)
      setMotivoRechazo('')

      try {
        await actualizarListado()
      } catch (err) {
        setError(`La decisión se guardó, pero no se pudo actualizar el listado: ${mensajeDe(err)}`)
      }
    } catch (err) {
      setError(mensajeDe(err))
    } finally {
      setProcesando(false)
    }
  }

  async function actualizarManualmente() {
    setCargando(true)
    setError('')
    try {
      await actualizarListado()
    } catch (err) {
      setError(mensajeDe(err))
    } finally {
      setCargando(false)
    }
  }

  return (
    <>
      <h1 className="rh-titulo">Gestión de constancias</h1>
      <p className="rh-subtitulo">Revisá las constancias solicitadas por los empleados.</p>
      <Aviso>{error}</Aviso>
      <Aviso tipo="ok">{aviso}</Aviso>

      <section className="rh-panel-tabla">
        <div className="rh-panel-cab">
          <div>
            <h2 className="rh-h2 rh-h2-0">Constancias recibidas</h2>
            <span className="rh-panel-resumen">{pendientes} pendiente(s) de revisión</span>
          </div>
          <button className="rh-btn-pag" type="button" disabled={cargando || procesando} onClick={actualizarManualmente}>
            {cargando ? 'Actualizando…' : 'Actualizar listado'}
          </button>
        </div>

        <div className="cliente-gestion-filtros">
          <label className="rh-campo cliente-gestion-buscar">
            Buscar constancia
            <input
              type="search" value={busqueda} disabled={cargando}
              placeholder="Empleado, institución o motivo"
              onChange={event => setBusqueda(event.target.value)}
            />
          </label>
          <label className="rh-campo cliente-gestion-estado">
            Estado
            <select value={filtro} disabled={cargando} onChange={event => setFiltro(event.target.value)}>
              {ESTADOS_FILTRO.map(estado => (
                <option key={estado.valor} value={estado.valor}>{estado.texto}</option>
              ))}
            </select>
          </label>
        </div>

        {cargando ? (
          <div className="rh-vacio">Cargando constancias…</div>
        ) : visibles.length === 0 ? (
          <div className="rh-vacio">No hay constancias que coincidan con la búsqueda.</div>
        ) : (
          <div className="rh-tabla-scroll">
            <table className="rh-tabla rh-tabla-hist">
              <thead>
                <tr>
                  <th>EMPLEADO</th>
                  <th>TIPO</th>
                  <th>INSTITUCIÓN</th>
                  <th>FECHA</th>
                  <th>ESTADO</th>
                  <th className="rh-der">ACCIÓN</th>
                </tr>
              </thead>
              <tbody>
                {visibles.map(constancia => (
                  <tr key={constancia.id}>
                    <td className="rh-td-tipo">{nombreEmpleado(constancia.empleadoId)}</td>
                    <td>{constancia.tipo}</td>
                    <td>{constancia.institucion}</td>
                    <td className="rh-nowrap">{fecha(constancia.fechaSolicitud)}</td>
                    <td><Chip estado={constancia.estado} /></td>
                    <td className="rh-der">
                      <button
                        type="button" className="rh-btn-ver"
                        disabled={procesando} onClick={() => verDetalle(constancia.id)}
                      >
                        Ver detalle
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
        <div className="rh-pie-tabla">{visibles.length} constancia(s) mostrada(s)</div>
      </section>

      {detalle && (
        <div className="rh-detalle-cols cliente-gestion-detalle" id="cliente-detalle-constancia">
          <section className="rh-col-form">
            <div className="rh-panel-cab rh-cab-sin-pad">
              <h2 className="rh-h2 rh-h2-0">Detalle de la constancia</h2>
              <Chip estado={detalle.estado} />
            </div>
            <dl className="rh-datos">
              <div><dt>Empleado</dt><dd>{nombreEmpleado(detalle.empleadoId)}</dd></div>
              <div><dt>Tipo de constancia</dt><dd>{detalle.tipo}</dd></div>
              <div><dt>Empresa emisora</dt><dd>{detalle.empresaEmisora || 'No indicada'}</dd></div>
              <div><dt>Institución de destino</dt><dd>{detalle.institucion}</dd></div>
              <div><dt>Fecha de solicitud</dt><dd>{fecha(detalle.fechaSolicitud)}</dd></div>
              <div className="rh-dato-ancho"><dt>Motivo</dt><dd>{detalle.motivo || 'No especificado'}</dd></div>
            </dl>
            <button className="cliente-enlace" type="button" disabled={procesando} onClick={() => setDetalleId(null)}>
              Cerrar detalle
            </button>
          </section>

          {detalle.estado === 'PENDIENTE' && (
            <section className="rh-col-form">
              <h2 className="rh-h2">Tu decisión</h2>
              <div className="cliente-gestion-decision">
                <button
                  className="rh-btn-aprobar" type="button" disabled={procesando}
                  onClick={() => resolver('aprobar')}
                >
                  {procesando ? 'Procesando…' : 'Aprobar constancia'}
                </button>
                <div className="rh-separador"><span>o</span></div>
                {mostrarRechazo ? (
                  <div className="rh-form">
                    <label className="rh-campo">
                      Motivo del rechazo
                      <textarea
                        rows="4" minLength="5" maxLength="250" value={motivoRechazo}
                        disabled={procesando} placeholder="Explicá el motivo del rechazo"
                        onChange={event => setMotivoRechazo(event.target.value)}
                      />
                    </label>
                    <div className="cliente-gestion-acciones">
                      <button
                        className="rh-btn-rechazar" type="button" disabled={procesando}
                        onClick={() => resolver('rechazar')}
                      >
                        {procesando ? 'Procesando…' : 'Confirmar rechazo'}
                      </button>
                      <button
                        className="rh-btn-cancelar" type="button" disabled={procesando}
                        onClick={() => { setMostrarRechazo(false); setMotivoRechazo('') }}
                      >
                        Volver
                      </button>
                    </div>
                  </div>
                ) : (
                  <button
                    className="rh-btn-rechazar" type="button" disabled={procesando}
                    onClick={() => setMostrarRechazo(true)}
                  >
                    Rechazar constancia
                  </button>
                )}
              </div>
            </section>
          )}
        </div>
      )}
    </>
  )
}
