#!/bin/bash
# Ajusta permisos del jar recien copiado.
set -e
chown ec2-user:ec2-user /opt/marketplace/back/marketplacecafe.jar
