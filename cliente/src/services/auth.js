
import {
    apiRequest,
    guardarToken,
    obtenerToken,
    eliminarToken,
} from './api'

const USUARIO_KEY = 'dwf901_usuario'

// Iniciar sesión consumiendo la API REST del equipo
export async function iniciarSesion(username, password) {
    const respuesta = await apiRequest('/auth/login', {
        method: 'POST',
        body: {
            username: username.trim(),
            password,
        },
    })

    if (!respuesta?.token || !respuesta?.usuario?.rol) {
        throw new Error('No fue posible iniciar sesión. Intentá nuevamente.')
    }

    guardarToken(respuesta.token)

    sessionStorage.setItem(
        USUARIO_KEY,
        JSON.stringify(respuesta.usuario)
    )

    return respuesta.usuario
}

// Recuperar los datos del usuario autenticado
export function obtenerUsuario() {
    const usuario = sessionStorage.getItem(USUARIO_KEY)

    if (!usuario || !obtenerToken()) {
        return null
    }

    try {
        return JSON.parse(usuario)
    } catch {
        return null
    }
}

// Cerrar sesión y eliminar los datos locales
export async function cerrarSesion() {
    try {
        if (obtenerToken()) {
            await apiRequest('/auth/logout', {
                method: 'POST',
            })
        }
    } finally {
        eliminarToken()
        sessionStorage.removeItem(USUARIO_KEY)
    }
}
