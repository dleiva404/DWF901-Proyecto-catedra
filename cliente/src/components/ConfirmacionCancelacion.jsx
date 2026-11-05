import { fecha } from '../utils/formato'

export default function ConfirmacionCancelacion({ referencia, solicitud, procesando, onConfirmar, onCerrar }) {
  return (
    <dialog
      ref={referencia}
      className="cliente-modal"
      aria-labelledby="cliente-modal-titulo"
      aria-describedby="cliente-modal-mensaje"
      onCancel={event => { if (procesando) event.preventDefault() }}
      onClose={onCerrar}
    >
      <div className="cliente-modal-contenido">
        <div className="cliente-modal-encabezado">
          <span className="cliente-modal-icono" aria-hidden="true">!</span>
          <h2 id="cliente-modal-titulo">¿Cancelar esta solicitud?</h2>
        </div>
        <p id="cliente-modal-mensaje">
          {solicitud && (
            <>La solicitud del <strong>{fecha(solicitud.fechaInicio)} al {fecha(solicitud.fechaFin)}</strong> quedará cancelada.</>
          )}
        </p>
        <p className="cliente-modal-nota">Esta acción no se puede deshacer.</p>
        <div className="cliente-modal-acciones">
          <button
            className="cliente-modal-volver"
            type="button"
            autoFocus
            disabled={procesando}
            onClick={() => referencia.current?.close()}
          >
            Volver
          </button>
          <button
            className="cliente-modal-confirmar"
            type="button"
            disabled={procesando}
            onClick={onConfirmar}
          >
            {procesando ? 'Cancelando…' : 'Sí, cancelar solicitud'}
          </button>
        </div>
      </div>
    </dialog>
  )
}
