import { apiRequest } from './api'

export const listarEmpleados = () => apiRequest('/empleados')
