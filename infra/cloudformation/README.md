# Infraestructura AWS — CloudFormation

Plantilla `marketplace-aws.yaml` que crea, **dentro de la capa gratuita**, toda la
infraestructura para las dos funcionalidades:

| Recurso | Para qué |
|---|---|
| **S3** `ImagesBucket` + bucket policy | Imágenes de portada con lectura pública. |
| **SQS** `OrderStatusQueue` + `OrderStatusDLQ` | Cola de cambios de estado de pedido (+ DLQ). |
| **Lambda** `NotifierFunction` (Node 20) | Consume la cola y envía el correo por SES. |
| **IAM** `LambdaExecutionRole` | Permisos de la Lambda (SQS, SES, logs). |
| **IAM** `BackendAccessPolicy` | Permisos del **backend** (S3 put/delete + SQS send). |
| **EventSourceMapping** | Conecta la cola con la Lambda (respuestas parciales de lote). |

## Encaje en la capa gratuita
- **S3**: 5 GB / 20k GET / 2k PUT al mes.
- **SQS**: 1 millón de solicitudes/mes (cola estándar, no FIFO).
- **Lambda**: 1 millón de invocaciones + 400.000 GB-s/mes (128 MB, 30 s).
- **SES**: 62.000 correos salientes/mes desde Lambda.
- **CloudWatch Logs**: 5 GB/mes.

## Requisitos previos
1. AWS CLI configurada con credenciales.
2. La identidad `SesSender` **verificada en SES** (y, si SES está en *sandbox*, también
   los destinatarios — o solicita salir del sandbox).
3. El paquete de la Lambda subido a un bucket S3:
   ```bash
   cd aws-lambda/order-status-notifier
   zip -r function.zip index.mjs email-template.mjs package.json
   aws s3 cp function.zip s3://<TU_BUCKET_DE_CODIGO>/order-status-notifier/function.zip
   ```
   > El runtime Node 20 ya incluye el AWS SDK v3; no se empaqueta `node_modules`.

## Despliegue
```bash
aws cloudformation deploy \
  --template-file infra/cloudformation/marketplace-aws.yaml \
  --stack-name wcm-aws \
  --capabilities CAPABILITY_NAMED_IAM \
  --parameter-overrides \
      ImagesBucketName=wcm-product-images-12345 \
      OrderStatusQueueName=wcm-order-status \
      SesSender=no-reply@tudominio.com \
      LambdaCodeS3Bucket=<TU_BUCKET_DE_CODIGO> \
      LambdaCodeS3Key=order-status-notifier/function.zip \
      AllowedImageOrigin=https://tudominio.com \
  --region us-east-1
```
> `ImagesBucketName` debe ser **globalmente único**.

## Validar la plantilla
```bash
aws cloudformation validate-template \
  --template-body file://infra/cloudformation/marketplace-aws.yaml
```

## Después del despliegue — variables del backend
Toma los *Outputs* del stack y expórtalos como variables de entorno del backend:
```bash
aws cloudformation describe-stacks --stack-name wcm-aws \
  --query "Stacks[0].Outputs" --output table

# Mapeo:
# ImagesBucketNameOut    -> AWS_S3_BUCKET
# OrderStatusQueueUrl    -> AWS_SQS_QUEUE_URL
# AwsRegionOut           -> AWS_REGION
```
Adjunta además `BackendAccessPolicyArn` al usuario/rol IAM con el que corre el backend:
```bash
aws iam attach-user-policy --user-name <USUARIO_BACKEND> \
  --policy-arn <BackendAccessPolicyArn>
```

## Eliminar todo
```bash
# Vacía el bucket antes de borrar el stack (S3 no se borra si tiene objetos)
aws s3 rm s3://wcm-product-images-12345 --recursive
aws cloudformation delete-stack --stack-name wcm-aws
```
