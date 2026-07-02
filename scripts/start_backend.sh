#!/bin/bash
# Recarga systemd y arranca/reinicia el backend.
set -e
systemctl daemon-reload
systemctl enable marketplace-back
systemctl restart marketplace-back
