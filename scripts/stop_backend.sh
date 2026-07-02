#!/bin/bash
# Detiene el backend antes de reemplazar el jar (no falla si aun no existe el servicio).
systemctl stop marketplace-back || true
