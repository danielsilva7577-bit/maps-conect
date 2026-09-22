param(
    [string]$Tipo = "dudas",
    [string]$Ciclo = "current"
)

$loginResp = curl -s -X POST "http://localhost:8080/api/auth/login" -H "Content-Type: application/json" -d '{"email":"admin@tecmilenio.mx","contrasena":"DemoMaps2026!"}'
$token = ($loginResp | ConvertFrom-Json).data.token

# PDF
$pdfOut = "reporte-fresco-$Tipo-$Ciclo.pdf"
curl -s -L -o "$pdfOut" "http://localhost:8080/api/admin/reporte/$Tipo/$Ciclo/pdf" -H "Authorization: Bearer $token"

# CSV
$csvOut = "reporte-fresco-$Tipo-$Ciclo.csv"
curl -s -L -o "$csvOut" "http://localhost:8080/api/admin/reporte/$Tipo/$Ciclo/csv" -H "Authorization: Bearer $token"

Write-Host "Downloaded: $pdfOut and $csvOut"
