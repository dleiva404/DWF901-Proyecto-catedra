
import { API_BASE_URL } from '../config'

// Almacenamiento del token durante la sesión
const TOKEN_KEY = 'dwf901_token'

export function obtenerToken() {
    return sessionStorage.getItem(TOKEN_KEY)
}

export function guardarToken(token) {
    sessionStorage.setItem(TOKEN_KEY, token)
}

export function eliminarToken() {
    sessionStorage.removeItem(TOKEN_KEY)
}

// Comunicación centralizada con la API REST
export async function apiRequest(
    ruta,
    { method = 'GET', body, responseType = 'json' } = {}
) {
    const token = obtenerToken()

    const opciones = {
        method,
        headers: {
            Accept: responseType === 'blob'
                ? 'application/pdf'
                : 'application/json',
        },
    }

    if (token) {
        opciones.headers.Authorization = `Bearer ${token}`
    }

    if (body !== undefined) {
        opciones.headers['Content-Type'] = 'application/json'
        opciones.body = JSON.stringify(body)
    }

    let respuesta

    try {
        respuesta = await fetch(`${API_BASE_URL}${ruta}`, opciones)
    } catch {
        throw new Error(
            'No pudimos conectarnos con el sistema. Intentá nuevamente en unos momentos.'
        )
    }

    // Token inválido o vencido
    if (respuesta.status === 401 && ruta !== '/auth/login') {
        eliminarToken()
        window.dispatchEvent(new Event('sesion-expirada'))
    }

    // Errores devueltos por la API
    if (!respuesta.ok) {
        let error = {}

        try {
            error = await respuesta.json()
        } catch {
            // La API no devolvió un error en formato JSON
        }

        const mensaje = error.mensaje ||
            `No se pudo completar la operación. Intentá nuevamente.`

        const campos = Array.isArray(error.campos)
            ? ` ${error.campos.join('. ')}`
            : ''

        throw new Error(mensaje + campos)
    }

    // Operaciones sin contenido
    if (respuesta.status === 204) {
        return null
    }

    // Descarga de documentos PDF
    if (responseType === 'blob') {
        return respuesta.blob()
    }

    return respuesta.json()
}
