$base = "http://localhost:8080/SistemaRRHH/api"

function Llamar($metodo, $ruta, $token, $cuerpo) {
    $cab = @{}
    if ($token) { $cab["Authorization"] = "Bearer $token" }
    $p = @{ Method = $metodo; Uri = "$base$ruta"; Headers = $cab; UseBasicParsing = $true; ContentType = "application/json" }
    if ($cuerpo) { $p["Body"] = [System.Text.Encoding]::UTF8.GetBytes($cuerpo) }
    try {
        $r = Invoke-WebRequest @p
        [pscustomobject]@{ Estado = [int]$r.StatusCode; Cuerpo = $r.Content; Location = $r.Headers["Location"]; Tipo = $r.Headers["Content-Type"] }
    } catch {
        $resp = $_.Exception.Response
        $estado = 0
        if ($resp) { $estado = [int]$resp.StatusCode }
        [pscustomobject]@{ Estado = $estado; Cuerpo = $_.ErrorDetails.Message; Location = $null; Tipo = $null }
    }
}

function Caso($nombre, $esperado, $r) {
    $marca = "FALLA"
    if ($r.Estado -eq $esperado) { $marca = "OK   " }
    "{0} {1,-62} esperado {2}  obtenido {3}" -f $marca, $nombre, $esperado, $r.Estado
    if ($marca -eq "FALLA") { "      -> $($r.Cuerpo)" }
}

function Dato($nombre, $esperado, $obtenido) {
    $marca = "FALLA"
    if ("$obtenido" -eq "$esperado") { $marca = "OK   " }
    "{0} {1,-62} esperado {2}  obtenido {3}" -f $marca, $nombre, $esperado, $obtenido
}

function Login($usuario) {
    (Invoke-RestMethod -Method Post -Uri "$base/auth/login" -ContentType "application/json" -Body (@{username=$usuario; password="123456"} | ConvertTo-Json)).token
}

function Constancia($tipo, $institucion, $motivo) {
    @{ tipo = $tipo; institucion = $institucion; motivo = $motivo } | ConvertTo-Json
}

# --- login: juan (empleado), david.leiva (otro empleado), carlos (jefatura), ana (RRHH)
$tj = Login "juan"
$td = Login "david.leiva"
$tc = Login "carlos"
$tr = Login "ana"

# --- todo lo que se imprime a partir de aquí se guarda también en un archivo de resultados
& {
"Prueba de la API de constancias  |  $((Get-Date).ToString('yyyy-MM-dd HH:mm'))  |  $base  |  usuarios: juan, david.leiva, carlos, ana"
"--- Autenticación"
Caso "listar constancias SIN token"                      401 (Llamar GET "/constancias" $null $null)

"--- Solicitar constancia"
$c1 = Llamar POST "/constancias" $tj (Constancia "Laboral" "Banco de prueba" "Prueba de API")
Caso "solicitar constancia Laboral"                      201 $c1
$id1 = ($c1.Cuerpo | ConvertFrom-Json).id
"      Location: $($c1.Location)   id creado: $id1"
Dato "estado inicial de la constancia"                   "PENDIENTE" ($c1.Cuerpo | ConvertFrom-Json).estado
$c2 = Llamar POST "/constancias" $tj (Constancia "salarial" "Institución de prueba" $null)
Caso "solicitar Salarial en minúsculas, sin motivo"      201 $c2
$id2 = ($c2.Cuerpo | ConvertFrom-Json).id
Caso "solicitar con cuerpo vacío {}"                     400 (Llamar POST "/constancias" $tj '{}')
Caso "solicitar con tipo inválido"                       400 (Llamar POST "/constancias" $tj (Constancia "Medica" "Banco de prueba" "x"))
Caso "solicitar sin institución"                         400 (Llamar POST "/constancias" $tj (Constancia "Laboral" "" "x"))
Caso "solicitar con motivo de más de 500 caracteres"     400 (Llamar POST "/constancias" $tj (Constancia "Laboral" "Banco de prueba" ("x" * 501)))

"--- Consultar"
Caso "listar mis constancias"                            200 (Llamar GET "/constancias" $tj $null)
Caso "ver mi constancia por id"                          200 (Llamar GET "/constancias/$id1" $tj $null)
Caso "ver una constancia que no existe"                  404 (Llamar GET "/constancias/999999" $tj $null)
Caso "ver id 0"                                          400 (Llamar GET "/constancias/0" $tj $null)
Caso "ver id no numérico (abc)"                          400 (Llamar GET "/constancias/abc" $tj $null)
Caso "david.leiva intenta ver la constancia de juan"     404 (Llamar GET "/constancias/$id1" $td $null)
Caso "ana (RRHH) ve la constancia de juan"               200 (Llamar GET "/constancias/$id1" $tr $null)
Caso "ana (RRHH) lista todas"                            200 (Llamar GET "/constancias" $tr $null)
Caso "ana (RRHH) lista solo las suyas (propias=true)"    200 (Llamar GET "/constancias?propias=true" $tr $null)

"--- PDF antes de aprobar"
Caso "pdf de una constancia pendiente"                   409 (Llamar GET "/constancias/$id1/pdf" $tj $null)

"--- Permisos para aprobar y rechazar"
Caso "juan intenta aprobar"                              403 (Llamar POST "/constancias/$id1/aprobar" $tj '{}')
Caso "carlos (jefatura) intenta aprobar"                 403 (Llamar POST "/constancias/$id1/aprobar" $tc '{}')
Caso "juan intenta rechazar"                             403 (Llamar POST "/constancias/$id1/rechazar" $tj (@{ motivoRechazo = "x" } | ConvertTo-Json))
Caso "aprobar sin token"                                 401 (Llamar POST "/constancias/$id1/aprobar" $null '{}')

"--- Aprobar"
Caso "ana aprueba la constancia de juan"                 204 (Llamar POST "/constancias/$id1/aprobar" $tr (@{ observaciones = "Revisada en prueba de API" } | ConvertTo-Json))
Caso "aprobar la misma otra vez"                         409 (Llamar POST "/constancias/$id1/aprobar" $tr '{}')
Caso "aprobar una que no existe"                         404 (Llamar POST "/constancias/999999/aprobar" $tr '{}')
Caso "aprobar con id no numérico (abc)"                  400 (Llamar POST "/constancias/abc/aprobar" $tr '{}')
Dato "estado después de aprobar"                         "APROBADA" ((Llamar GET "/constancias/$id1" $tj $null).Cuerpo | ConvertFrom-Json).estado

"--- Rechazar"
Caso "rechazar sin motivoRechazo"                        400 (Llamar POST "/constancias/$id2/rechazar" $tr '{}')
Caso "ana rechaza la segunda constancia"                 204 (Llamar POST "/constancias/$id2/rechazar" $tr (@{ motivoRechazo = "Falta información necesaria."; observaciones = "Prueba de API" } | ConvertTo-Json))
Caso "rechazar la misma otra vez"                        409 (Llamar POST "/constancias/$id2/rechazar" $tr (@{ motivoRechazo = "x" } | ConvertTo-Json))
Caso "aprobar una ya rechazada"                          409 (Llamar POST "/constancias/$id2/aprobar" $tr '{}')
Dato "estado después de rechazar"                        "RECHAZADA" ((Llamar GET "/constancias/$id2" $tj $null).Cuerpo | ConvertFrom-Json).estado

"--- PDF"
$pdf = Llamar GET "/constancias/$id1/pdf" $tj $null
Caso "juan descarga el pdf de su constancia aprobada"    200 $pdf
Dato "Content-Type del pdf"                              "application/pdf" (([string]$pdf.Tipo).Split(';')[0])
Caso "ana (RRHH) descarga el pdf"                        200 (Llamar GET "/constancias/$id1/pdf" $tr $null)
Caso "david.leiva intenta descargar el pdf de juan"      404 (Llamar GET "/constancias/$id1/pdf" $td $null)
Caso "pdf de una constancia rechazada"                   409 (Llamar GET "/constancias/$id2/pdf" $tj $null)
Caso "pdf de una que no existe"                          404 (Llamar GET "/constancias/999999/pdf" $tj $null)
Caso "pdf con id no numérico (abc)"                      400 (Llamar GET "/constancias/abc/pdf" $tj $null)
} | Tee-Object -Variable salida

$ok    = @($salida | Where-Object { $_ -like "OK*" }).Count
$falla = @($salida | Where-Object { $_ -like "FALLA*" }).Count
$resumen = "RESUMEN: $ok OK, $falla FALLA"
$resumen

$carpeta = $PSScriptRoot
if (-not $carpeta) { $carpeta = (Get-Location).Path }
$archivo = Join-Path $carpeta "resultados-prueba-constancias.txt"
($salida + "" + $resumen) | Set-Content -Path $archivo -Encoding UTF8
"Resultados guardados en: $archivo"
