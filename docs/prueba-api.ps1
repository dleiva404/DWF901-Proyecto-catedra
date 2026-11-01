$base = "http://localhost:8080/SistemaRRHH/api"

function Llamar($metodo, $ruta, $token, $cuerpo) {
    $cab = @{}
    if ($token) { $cab["Authorization"] = "Bearer $token" }
    $p = @{ Method = $metodo; Uri = "$base$ruta"; Headers = $cab; UseBasicParsing = $true; ContentType = "application/json" }
    if ($cuerpo) { $p["Body"] = $cuerpo }
    try {
        $r = Invoke-WebRequest @p
        [pscustomobject]@{ Estado = [int]$r.StatusCode; Cuerpo = $r.Content; Location = $r.Headers["Location"] }
    } catch {
        $resp = $_.Exception.Response
        $estado = 0
        if ($resp) { $estado = [int]$resp.StatusCode }
        [pscustomobject]@{ Estado = $estado; Cuerpo = $_.ErrorDetails.Message; Location = $null }
    }
}

function Caso($nombre, $esperado, $r) {
    $marca = "FALLA"
    if ($r.Estado -eq $esperado) { $marca = "OK   " }
    "{0} {1,-62} esperado {2}  obtenido {3}" -f $marca, $nombre, $esperado, $r.Estado
    if ($marca -eq "FALLA") { "      -> $($r.Cuerpo)" }
}

function Fecha($d) { $d.ToString("yyyy-MM-dd") }
function Solicitud($tipo, $ini, $fin, $motivo) {
    @{ tipoSolicitudId = $tipo; fechaInicio = (Fecha $ini); fechaFin = (Fecha $fin); motivo = $motivo } | ConvertTo-Json
}

# --- fechas: próximo lunes (siempre futuro)
$hoy = (Get-Date).Date
$d = (8 - [int]$hoy.DayOfWeek) % 7
if ($d -eq 0) { $d = 7 }
$lunes = $hoy.AddDays($d)

# --- login de dos usuarios
$t  = (Invoke-RestMethod -Method Post -Uri "$base/auth/login" -ContentType "application/json" -Body (@{username="juan";   password="123456"} | ConvertTo-Json)).token
$tc = (Invoke-RestMethod -Method Post -Uri "$base/auth/login" -ContentType "application/json" -Body (@{username="carlos"; password="123456"} | ConvertTo-Json)).token
$tipos = Invoke-RestMethod -Uri "$base/tipos-solicitud" -Headers @{Authorization="Bearer $t"}
$idVac = ($tipos | Where-Object nombre -eq "VACACIONES").id
$idInc = ($tipos | Where-Object nombre -eq "INCAPACIDAD").id

# --- todo lo que se imprime a partir de aquí se guarda también en un archivo de resultados
& {
"Prueba de la API de empleado  |  $((Get-Date).ToString('yyyy-MM-dd HH:mm'))  |  $base  |  usuario: juan"
"--- Autenticación y catálogo"
Caso "ping sin token"                                    200 (Llamar GET "/ping" $null $null)
Caso "tipos-solicitud SIN token"                         401 (Llamar GET "/tipos-solicitud" $null $null)
Caso "tipos-solicitud con token"                         200 (Llamar GET "/tipos-solicitud" $t $null)

"--- Saldo de vacaciones"
Caso "saldo del año actual"                              200 (Llamar GET "/vacaciones/saldo" $t $null)
Caso "saldo con anio=26 (inválido)"                      400 (Llamar GET "/vacaciones/saldo?anio=26" $t $null)
Caso "saldo de un año sin registro (1999)"               404 (Llamar GET "/vacaciones/saldo?anio=1999" $t $null)

"--- Crear solicitud"
$crear = Llamar POST "/solicitudes" $t (Solicitud $idVac $lunes $lunes.AddDays(2) "Prueba de API")
Caso "crear vacaciones de 3 días"                        201 $crear
$id = ($crear.Cuerpo | ConvertFrom-Json).id
"      Location: $($crear.Location)   id creado: $id"
$saldo = Invoke-RestMethod -Uri "$base/vacaciones/saldo" -Headers @{Authorization="Bearer $t"}
"      Saldo ahora -> pendientes: $($saldo.diasPendientes) (esperado 3) | disponibles: $($saldo.diasDisponibles) (esperado 15, no baja hasta aprobar)"
Caso "crear con cuerpo vacío {}"                         400 (Llamar POST "/solicitudes" $t '{}')
Caso "crear con fecha pasada"                            400 (Llamar POST "/solicitudes" $t (Solicitud $idVac $hoy.AddDays(-3) $hoy.AddDays(-1) "Prueba de API"))
Caso "crear con tipo INCAPACIDAD"                        400 (Llamar POST "/solicitudes" $t (Solicitud $idInc $lunes $lunes.AddDays(1) "Prueba de API"))
Caso "crear pidiendo más días que el saldo"              409 (Llamar POST "/solicitudes" $t (Solicitud $idVac $lunes $lunes.AddDays(30) "Prueba de API"))

"--- Consultar"
Caso "listar mis solicitudes"                            200 (Llamar GET "/solicitudes" $t $null)
Caso "listar con estado=PENDIENTE"                       200 (Llamar GET "/solicitudes?estado=PENDIENTE" $t $null)
Caso "listar con estado inválido"                        400 (Llamar GET "/solicitudes?estado=ARCHIVADA" $t $null)
Caso "ver mi solicitud por id"                           200 (Llamar GET "/solicitudes/$id" $t $null)
Caso "ver una solicitud que no existe"                   404 (Llamar GET "/solicitudes/999999" $t $null)
Caso "ver id 0"                                          400 (Llamar GET "/solicitudes/0" $t $null)
Caso "ver id no numérico (abc)"                          400 (Llamar GET "/solicitudes/abc" $t $null)
Caso "ver id enorme (99999999999)"                       400 (Llamar GET "/solicitudes/99999999999" $t $null)
Caso "carlos intenta ver la solicitud de juan"           404 (Llamar GET "/solicitudes/$id" $tc $null)

"--- Cancelar"
Caso "cancelar mi solicitud pendiente"                   204 (Llamar DELETE "/solicitudes/$id" $t $null)
Caso "cancelar la misma otra vez"                        409 (Llamar DELETE "/solicitudes/$id" $t $null)
Caso "cancelar una que no existe"                        404 (Llamar DELETE "/solicitudes/999999" $t $null)
Caso "cancelar con id no numérico (abc)"                 400 (Llamar DELETE "/solicitudes/abc" $t $null)

"--- Incapacidades"
$cuerpoInc = @{ numeroDocumento = "ISSS-PRUEBA-1"; fechaInicio = (Fecha $lunes.AddDays(-14)); fechaFin = (Fecha $lunes.AddDays(-12)); observaciones = "Prueba de API" } | ConvertTo-Json
$inc = Llamar POST "/incapacidades" $t $cuerpoInc
Caso "registrar incapacidad (puede empezar en el pasado)" 201 $inc
"      Location: $($inc.Location)"
Caso "incapacidad sin numeroDocumento"                   400 (Llamar POST "/incapacidades" $t (@{ fechaInicio = (Fecha $lunes); fechaFin = (Fecha $lunes.AddDays(1)) } | ConvertTo-Json))
} | Tee-Object -Variable salida

$ok    = @($salida | Where-Object { $_ -like "OK*" }).Count
$falla = @($salida | Where-Object { $_ -like "FALLA*" }).Count
$resumen = "RESUMEN: $ok OK, $falla FALLA"
$resumen

$carpeta = $PSScriptRoot
if (-not $carpeta) { $carpeta = (Get-Location).Path }
$archivo = Join-Path $carpeta "resultados-prueba-api.txt"
($salida + "" + $resumen) | Set-Content -Path $archivo -Encoding UTF8
"Resultados guardados en: $archivo"
