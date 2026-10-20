#!/usr/bin/env bash
# Genera trafico sobre la API para alimentar las Golden Signals del dashboard.
# Mezcla pedidos exitosos (2xx) con errores del cliente (4xx).
# Uso: ./scripts/generar-trafico.sh [URL_BASE] [DURACION_SEGUNDOS]
set -euo pipefail

BASE_URL="${1:-https://vet-system-1-0-0.onrender.com}"
DURACION="${2:-120}"
FIN=$((SECONDS + DURACION))

# Un pedido fallido (timeout, corte de conexion) se cuenta como 000 y no corta el script
pedido() {
  curl -s -o /dev/null --max-time 30 -w '%{http_code}\n' "$@" || true
}

echo "Generando trafico contra $BASE_URL durante $DURACION segundos..."
while [ $SECONDS -lt $FIN ]; do
  # 2xx: lecturas validas
  pedido "$BASE_URL/api/duenios"
  pedido "$BASE_URL/api/veterinarios"
  pedido "$BASE_URL/api/medicamentos"
  # 404: ruta inexistente
  pedido "$BASE_URL/api/no-existe"
  # 400: JSON mal formado
  pedido -X POST "$BASE_URL/api/duenios" -H 'Content-Type: application/json' -d '{mal formado'
  # 405: metodo no soportado
  pedido -X PATCH "$BASE_URL/api/duenios"
  sleep 1
done | sort | uniq -c | awk '{print "HTTP " $2 ": " $1 " pedidos"}'
