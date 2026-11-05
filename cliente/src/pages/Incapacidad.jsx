import { useState } from 'react'
import { registrarIncapacidad } from '../services/incapacidades'
import { mensajeDe } from '../utils/formato'
import Aviso from '../components/Aviso'

export default function Incapacidad({ volver }) {
  const [datos, setDatos] = useState({ numeroDocumento: '', fechaInicio: '', fechaFin: '', observaciones: '' })
  const [error, setError] = useState('')
  const [aviso, setAviso] = useState('')
  const [enviando, setEnviando] = useState(false)
  async function enviar(event) {
    event.preventDefault()
    setError('')
    setAviso('')
    if (!datos.numeroDocumento.trim() || !datos.fechaInicio || !datos.fechaFin) {
      setError('Completá el documento y las fechas.'); return
    }
    if (datos.fechaFin < datos.fechaInicio) { setError('La fecha final no puede ser anterior a la inicial.'); return }
    setEnviando(true)
    try {
      await registrarIncapacidad({ ...datos, numeroDocumento: datos.numeroDocumento.trim(), observaciones: datos.observaciones.trim() })
      setAviso('Incapacidad registrada correctamente.')
      setDatos({ numeroDocumento: '', fechaInicio: '', fechaFin: '', observaciones: '' })
    } catch (err) {
      setError(mensajeDe(err))
    } finally {
      setEnviando(false)
    }
  }
  return (
    <div className="rh-estrecho">
      <div>
        <button className="rh-volver cliente-enlace" type="button" onClick={volver}>
          ‹ Volver a mis solicitudes
        </button>
        <h1 className="rh-titulo">Registrar incapacidad</h1>
      </div>
      <Aviso>{error}</Aviso>
      <Aviso tipo="ok">{aviso}</Aviso>

      <section className="rh-col-form rh-form-ancho">
        <form className="rh-form rh-form-grande" onSubmit={enviar}>
          <label className="rh-campo">
            Número de documento (ISSS)
            <input
              type="text" maxLength="100" value={datos.numeroDocumento}
              required disabled={enviando} placeholder="Ej. ISSS-2026-004512"
              onChange={event => setDatos(actual => ({
                ...actual, numeroDocumento: event.target.value,
              }))}
            />
          </label>

          <div className="rh-fila-fechas">
            <label className="rh-campo">
              Fecha inicio
              <input
                type="date" required disabled={enviando} value={datos.fechaInicio}
                onChange={event => setDatos(actual => ({
                  ...actual, fechaInicio: event.target.value,
                }))}
              />
            </label>
            <label className="rh-campo">
              Fecha fin
              <input
                type="date" required disabled={enviando}
                min={datos.fechaInicio} value={datos.fechaFin}
                onChange={event => setDatos(actual => ({
                  ...actual, fechaFin: event.target.value,
                }))}
              />
            </label>
          </div>

          <label className="rh-campo">
            Observaciones (opcional)
            <textarea
              rows="3" maxLength="500" disabled={enviando}
              value={datos.observaciones}
              onChange={event => setDatos(actual => ({
                ...actual, observaciones: event.target.value,
              }))}
            />
          </label>

          <div className="rh-dropzone">
            <div className="rh-dropzone-ico">↑</div>
            <div className="rh-dropzone-texto">
              <div className="rh-dropzone-titulo">Documento del ISSS (PDF)</div>
              <div className="rh-dropzone-sub">
                La carga de documentos estará disponible próximamente.
              </div>
            </div>
            <span className="rh-pronto">Próximamente</span>
          </div>

          <div className="rh-acciones">
            <button type="button" className="rh-btn-cancelar" onClick={volver}>
              Volver
            </button>
            <button type="submit" className="rh-btn-enviar rh-btn-ancho" disabled={enviando}>
              {enviando ? 'Registrando…' : 'Registrar incapacidad'}
            </button>
          </div>
        </form>
      </section>
    </div>
  )
}
