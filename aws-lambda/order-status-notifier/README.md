# Lambda — order-status-notifier

Función AWS Lambda (Node.js 20) que **consume mensajes de la cola SQS** de cambios
de estado de pedido y **envía un correo al comprador vía SES**.

Flujo: `Backend → SQS → Lambda (este código) → SES → correo al comprador`.

## Archivos
- `index.mjs` — handler. Itera `event.Records`, parsea el JSON y envía el correo. Usa
  **respuestas parciales de lote** (`batchItemFailures`) para que solo los mensajes
  fallidos se reintenten / vayan a la DLQ.
- `email-template.mjs` — construye asunto, cuerpo HTML y texto plano según el estado.
- `package.json` — metadatos (Node ≥ 20, `type: module`).

## Contrato del mensaje (body SQS)
```json
{
  "orderId": "uuid",
  "orderCode": "WCM-2026-000123",
  "previousStatus": "confirmed",
  "newStatus": "shipped",
  "buyerEmail": "cliente@example.com",
  "buyerId": "uuid",
  "totalAmount": 85000,
  "note": "Enviado por Servientrega",
  "changedAt": "2026-06-23T10:15:00Z"
}
```

## Variables de entorno
| Variable | Descripción |
|---|---|
| `SES_SENDER` | Identidad **verificada** en SES usada como remitente (ej. `no-reply@tudominio.com`). **Obligatoria.** |
| `AWS_REGION` | La establece el runtime de Lambda automáticamente; SES usa esta región. |

## Permisos IAM (rol de ejecución de la Lambda)
- `sqs:ReceiveMessage`, `sqs:DeleteMessage`, `sqs:GetQueueAttributes` sobre la cola (lo
  exige el *event source mapping*).
- `ses:SendEmail` (y `ses:SendRawEmail`).
- `logs:CreateLogGroup`, `logs:CreateLogStream`, `logs:PutLogEvents` (CloudWatch).

> La infraestructura (cola, Lambda, rol, *event source mapping* y permisos) se define en
> `infra/cloudformation/marketplace-aws.yaml` (Fase 11). Este README cubre el despliegue manual.

## SES — verificación del remitente
1. En SES, verifica el dominio o la dirección usada en `SES_SENDER`.
2. Si la cuenta SES está en **sandbox**, también debes verificar las direcciones de los
   **destinatarios** o solicitar salir del sandbox para enviar a cualquier comprador.
   La capa gratuita de SES cubre 62.000 correos salientes/mes desde Lambda/EC2.

## Despliegue manual (sin CloudFormation)
El runtime de Node 20 de Lambda **ya incluye el AWS SDK v3**, por lo que no es necesario
empaquetar `node_modules`.

```bash
# 1. Empaquetar
cd aws-lambda/order-status-notifier
zip -r function.zip index.mjs email-template.mjs package.json

# 2. Crear la función (ajusta ROLE_ARN, REGION y SES_SENDER)
aws lambda create-function \
  --function-name order-status-notifier \
  --runtime nodejs20.x \
  --handler index.handler \
  --zip-file fileb://function.zip \
  --role arn:aws:iam::<ACCOUNT_ID>:role/order-status-notifier-role \
  --environment "Variables={SES_SENDER=no-reply@tudominio.com}" \
  --region us-east-1

# 3. Conectar la cola SQS como disparador (con respuestas parciales de lote)
aws lambda create-event-source-mapping \
  --function-name order-status-notifier \
  --event-source-arn arn:aws:sqs:us-east-1:<ACCOUNT_ID>:wcm-order-status \
  --function-response-types ReportBatchItemFailures \
  --batch-size 10 \
  --region us-east-1

# Actualizaciones posteriores del código
aws lambda update-function-code \
  --function-name order-status-notifier \
  --zip-file fileb://function.zip \
  --region us-east-1
```

## Prueba local rápida
```bash
node --input-type=module -e "
import { handler } from './index.mjs';
const event = { Records: [{ messageId: '1', body: JSON.stringify({
  orderId:'o1', orderCode:'WCM-2026-001', previousStatus:'confirmed',
  newStatus:'shipped', buyerEmail:'test@example.com', totalAmount:85000,
  note:'demo', changedAt:new Date().toISOString() }) }] };
handler(event).then(r => console.log('result', r));
"
```
> Requiere `SES_SENDER` y credenciales AWS válidas (o un mock de SES) para enviar realmente.
