import { useCallback, useEffect, useState } from 'react'
import { listarConstancias, solicitarConstancia, descargarConstancia } from '../services/constancias'
import { mensajeDe } from '../utils/formato'
import Aviso from '../components/Aviso'
import Chip from '../components/Chip'

export default function Constancias({ usuario }) {
  const [lista, setLista] = useState([])
  const [datos, setDatos] = useState({ tipo: 'Laboral', institucion: '', motivo: '' })
  const [error, setError] = useState('')
  const [aviso, setAviso] = useState('')
  const [cargando, setCargando] = useState(true)
  const [enviando, setEnviando] = useState(false)
  const [descargandoId, setDescargandoId] = useState(null)

  const recargar = useCallback(async () => {
    const resultado = await listarConstancias(true)
    const propias = String(usuario.rol).toUpperCase() === 'EMPLEADO'
      ? resultado : (Array.isArray(resultado) ? resultado.filter(c => Number(c.empleadoId) === Number(usuario.empleadoId)) : [])
    setLista(Array.isArray(propias) ? propias : [])
  }, [usuario.empleadoId, usuario.rol])

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
    if (!datos.institucion.trim()) { setError('Indicá la institución de destino.'); return }
    setEnviando(true)
    try {
      await solicitarConstancia({ ...datos, institucion: datos.institucion.trim(), motivo: datos.motivo.trim() })
      setAviso('Constancia solicitada correctamente.')
      setDatos({ tipo: 'Laboral', institucion: '', motivo: '' })
      await recargar()
    } catch (err) {
      setError(mensajeDe(err))
    } finally {
      setEnviando(false)
    }
  }

  async function descargar(id) {
    setError('')
    setDescargandoId(id)
    try {
      const blob = await descargarConstancia(id)
      const enlace = document.createElement('a')
      const url = URL.createObjectURL(blob)
      enlace.href = url
      enlace.download = `constancia_${id}.pdf`
      document.body.appendChild(enlace)
      enlace.click()
      enlace.remove()
      setTimeout(() => URL.revokeObjectURL(url), 1000)
    } catch (err) {
      setError(mensajeDe(err))
    } finally {
      setDescargandoId(null)
    }
  }

  return (
    <>
      <h1 className="rh-titulo">Constancias</h1>
      <Aviso>{error}</Aviso>
      <Aviso tipo="ok">{aviso}</Aviso>

      <div className="rh-const-grid">
        <section className="rh-col-form">
          <h2 className="rh-h2">Nueva solicitud</h2>
          <form onSubmit={enviar} className="rh-form">
            <div>
              <div className="rh-etiqueta">Tipo de constancia</div>
              <div className="cliente-radio-grupo">
                {['Salarial', 'Laboral'].map(tipo => (
                  <label
                    className={`cliente-radio ${datos.tipo === tipo ? 'activo' : ''}`}
                    key={tipo}
                  >
                    <input
                      type="radio" name="tipo" value={tipo}
                      checked={datos.tipo === tipo} disabled={enviando}
                      onChange={() => setDatos(actual => ({ ...actual, tipo }))}
                    />
                    {tipo}
                  </label>
                ))}
              </div>
            </div>

            <label className="rh-campo">
              Dirigido a (institución)
              <input
                type="text" value={datos.institucion} maxLength="150"
                required disabled={enviando} placeholder="Ej. Banco Agrícola"
                onChange={event => setDatos(actual => ({
                  ...actual, institucion: event.target.value,
                }))}
              />
            </label>

            <label className="rh-campo">
              Motivo (opcional)
              <textarea
                rows="3" value={datos.motivo} maxLength="500"
                disabled={enviando} placeholder="Ej. Trámite de crédito"
                onChange={event => setDatos(actual => ({
                  ...actual, motivo: event.target.value,
                }))}
              />
            </label>
            <div className="rh-nota-azul">
              La constancia se emitirá con los datos de la empresa registrados en tu expediente.
            </div>
            <button type="submit" className="rh-btn-enviar" disabled={enviando}>
              {enviando ? 'Enviando…' : 'Enviar solicitud'}
            </button>
          </form>
        </section>

        <section className="rh-panel-tabla">
          <div className="rh-panel-cab">
            <h2 className="rh-h2 rh-h2-0">Mis constancias</h2>
            <span className="rh-panel-resumen">{lista.length} solicitud(es)</span>
          </div>

          {cargando ? (
            <div className="rh-vacio">Cargando constancias…</div>
          ) : lista.length === 0 ? (
            <div className="rh-vacio">Aún no has solicitado constancias.</div>
          ) : (
            <div className="rh-tabla-scroll">
              <table className="rh-tabla">
                <thead>
                  <tr>
                    <th>TIPO</th>
                    <th>INSTITUCIÓN</th>
                    <th>ESTADO</th>
                    <th className="rh-der">ACCIÓN</th>
                  </tr>
                </thead>
                <tbody>
                  {lista.map(constancia => (
                    <tr key={constancia.id}>
                      <td className="rh-td-tipo">
                        {constancia.tipo}
                        <div className="rh-td-sub">{constancia.motivo}</div>
                      </td>
                      <td>{constancia.institucion}</td>
                      <td><Chip estado={constancia.estado} /></td>
                      <td className="rh-der">
                        {constancia.estado === 'APROBADA' ? (
                          <button
                            className="rh-btn-pdf" type="button"
                            disabled={descargandoId !== null}
                            onClick={() => descargar(constancia.id)}
                          >
                            {descargandoId === constancia.id ? 'Descargando…' : 'Descargar PDF'}
                          </button>
                        ) : constancia.estado === 'PENDIENTE' ? (
                          <span className="rh-td-nota">En revisión por RRHH</span>
                        ) : (
                          <span className="rh-td-nota rh-td-nota-rojo">Rechazada</span>
                        )}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </section>
      </div>
    </>
  )
}
