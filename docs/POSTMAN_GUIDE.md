# POSTMAN GUIDE — World Coffee Marketplace (WCM)

Base URL: `http://localhost:8080`

All authenticated endpoints require the header:
```
Authorization: Bearer <accessToken>
```

---

## Flujo de prueba sugerido

1. `POST /api/auth/login` con `buyer@wcm.co` — guardar `accessToken` y `refreshToken`
2. `GET /api/catalog/products` — explorar el catálogo sin autenticación
3. `POST /api/cart/items` — agregar producto al carrito (BUYER)
4. `POST /api/orders` — crear orden con `addressId` y `paymentMethodCode`
5. `POST /api/orders/{id}/payment-proof` — subir comprobante de pago
6. Login como `admin@wcm.co` — verificar pago y cambiar estado
7. Login como `producer@wcm.co` — ver órdenes y avanzar estado
8. `POST /api/reviews` — dejar reseña del producto entregado

---

## Credenciales de prueba

| Usuario | Email | Password | Rol |
|---------|-------|----------|-----|
| Comprador | buyer@wcm.co | Cafe#2025 | BUYER |
| Productor | producer@wcm.co | Cafe#2025 | PRODUCER |
| Administrador | admin@wcm.co | Cafe#2025 | ADMIN |

---

## Auth

### POST /api/auth/register/buyer 🌐

- **201 Created** / 409 Conflict (email ya registrado)

```bash
curl -X POST http://localhost:8080/api/auth/register/buyer \
  -H "Content-Type: application/json" \
  -d '{
    "email": "nuevocliente@example.com",
    "password": "MiClave#123",
    "fullName": "Juan Perez",
    "phone": "3001234567"
  }'
```

---

### POST /api/auth/register/producer 🌐

- **201 Created** / 409 Conflict

```bash
curl -X POST http://localhost:8080/api/auth/register/producer \
  -H "Content-Type: application/json" \
  -d '{
    "email": "nuevoproductor@example.com",
    "password": "MiClave#123",
    "fullName": "Maria Gomez",
    "phone": "3109876543",
    "bio": "Finca familiar en el Huila con 20 años de experiencia.",
    "city": "Pitalito",
    "department": "Huila"
  }'
```

---

### POST /api/auth/login 🌐

- **200 OK** / 401 Unauthorized

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "buyer@wcm.co",
    "password": "Cafe#2025"
  }'
```

Respuesta esperada:
```json
{
  "accessToken": "<jwt>",
  "refreshToken": "<uuid>",
  "expiresIn": 3600
}
```

---

### POST /api/auth/refresh 🌐

- **200 OK** / 401 Unauthorized

```bash
curl -X POST http://localhost:8080/api/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "<refreshToken>"
  }'
```

---

### POST /api/auth/logout 🔒

- **204 No Content** / 401 Unauthorized

```bash
curl -X POST http://localhost:8080/api/auth/logout \
  -H "Authorization: Bearer <accessToken>"
```

---

### POST /api/auth/password-reset/request 🌐

- **204 No Content** / 404 Not Found

```bash
curl -X POST http://localhost:8080/api/auth/password-reset/request \
  -H "Content-Type: application/json" \
  -d '{
    "email": "buyer@wcm.co"
  }'
```

---

### POST /api/auth/password-reset/confirm 🌐

- **204 No Content** / 400 Bad Request (token inválido o expirado)

```bash
curl -X POST http://localhost:8080/api/auth/password-reset/confirm \
  -H "Content-Type: application/json" \
  -d '{
    "token": "<resetToken>",
    "newPassword": "NuevaClave#456"
  }'
```

---

### GET /api/auth/me 🔒

- **200 OK** / 401 Unauthorized

```bash
curl http://localhost:8080/api/auth/me \
  -H "Authorization: Bearer <accessToken>"
```

---

### PATCH /api/auth/me/password 🔒

- **204 No Content** / 400 Bad Request

```bash
curl -X PATCH http://localhost:8080/api/auth/me/password \
  -H "Authorization: Bearer <accessToken>" \
  -H "Content-Type: application/json" \
  -d '{
    "oldPassword": "Cafe#2025",
    "newPassword": "NuevaClave#456"
  }'
```

---

### POST /api/auth/consents 🔒

- **201 Created** / 401 Unauthorized

```bash
curl -X POST http://localhost:8080/api/auth/consents \
  -H "Authorization: Bearer <accessToken>" \
  -H "Content-Type: application/json" \
  -d '{
    "policyVersion": "v1.0"
  }'
```

---

## Profile

### GET /api/profile/buyer 🔒 [BUYER]

- **200 OK** / 403 Forbidden

```bash
curl http://localhost:8080/api/profile/buyer \
  -H "Authorization: Bearer <buyerToken>"
```

---

### PATCH /api/profile/buyer 🔒 [BUYER]

- **200 OK** / 403 Forbidden

```bash
curl -X PATCH http://localhost:8080/api/profile/buyer \
  -H "Authorization: Bearer <buyerToken>" \
  -H "Content-Type: application/json" \
  -d '{
    "city": "Bogota",
    "department": "Cundinamarca",
    "preferredPayment": "nequi",
    "newsletterOptIn": true,
    "avatarInitials": "JP"
  }'
```

---

### GET /api/profile/producer 🔒 [PRODUCER]

- **200 OK** / 403 Forbidden

```bash
curl http://localhost:8080/api/profile/producer \
  -H "Authorization: Bearer <producerToken>"
```

---

### PATCH /api/profile/producer 🔒 [PRODUCER]

- **200 OK** / 403 Forbidden

```bash
curl -X PATCH http://localhost:8080/api/profile/producer \
  -H "Authorization: Bearer <producerToken>" \
  -H "Content-Type: application/json" \
  -d '{
    "bio": "Productores de cafe especial en el Huila desde 2003.",
    "city": "San Agustin",
    "department": "Huila",
    "avatarInitials": "MG"
  }'
```

---

## Addresses 🔒 [BUYER]

### GET /api/addresses

- **200 OK** / 401 Unauthorized

```bash
curl http://localhost:8080/api/addresses \
  -H "Authorization: Bearer <buyerToken>"
```

---

### POST /api/addresses

- **201 Created** / 400 Bad Request

```bash
curl -X POST http://localhost:8080/api/addresses \
  -H "Authorization: Bearer <buyerToken>" \
  -H "Content-Type: application/json" \
  -d '{
    "label": "Casa",
    "line1": "Calle 45 #12-30",
    "line2": "Apto 301",
    "city": "Bogota",
    "department": "Cundinamarca",
    "zipCode": "110111",
    "isDefault": true
  }'
```

---

### PUT /api/addresses/{id}

- **200 OK** / 404 Not Found

```bash
curl -X PUT http://localhost:8080/api/addresses/550e8400-e29b-41d4-a716-446655440000 \
  -H "Authorization: Bearer <buyerToken>" \
  -H "Content-Type: application/json" \
  -d '{
    "label": "Oficina",
    "line1": "Carrera 7 #72-41",
    "line2": "Piso 3",
    "city": "Bogota",
    "department": "Cundinamarca",
    "zipCode": "110221",
    "isDefault": false
  }'
```

---

### DELETE /api/addresses/{id}

- **204 No Content** / 404 Not Found

```bash
curl -X DELETE http://localhost:8080/api/addresses/550e8400-e29b-41d4-a716-446655440000 \
  -H "Authorization: Bearer <buyerToken>"
```

---

### PATCH /api/addresses/{id}/default

- **200 OK** / 404 Not Found

```bash
curl -X PATCH http://localhost:8080/api/addresses/550e8400-e29b-41d4-a716-446655440000/default \
  -H "Authorization: Bearer <buyerToken>"
```

---

## Catalog 🌐

### GET /api/catalog/products

Query params: `search`, `category` (UUID), `region`, `minPrice`, `maxPrice`, `certification`, `roast`, `page` (default 0), `size` (default 20), `sort` (default "createdAt,desc")

Valores validos para `certification`: `RA`, `FT`, `ORG`, `4C`, `UTZ`, `BAP`, `CAFE`, `DOP`
Valores validos para `roast`: `LIGHT`, `MEDIUM_LIGHT`, `MEDIUM`, `MEDIUM_DARK`, `DARK`

- **200 OK**

```bash
curl "http://localhost:8080/api/catalog/products?search=huila&minPrice=50000&maxPrice=150000&roast=MEDIUM&page=0&size=10"
```

---

### GET /api/catalog/products/featured

Query params: `limit` (default 8)

- **200 OK**

```bash
curl "http://localhost:8080/api/catalog/products/featured?limit=6"
```

---

### GET /api/catalog/products/{id}

- **200 OK** / 404 Not Found

```bash
curl http://localhost:8080/api/catalog/products/550e8400-e29b-41d4-a716-446655440000
```

---

### GET /api/catalog/products/by-slug/{slug}

- **200 OK** / 404 Not Found

```bash
curl http://localhost:8080/api/catalog/products/by-slug/cafe-especial-huila-washed
```

---

### GET /api/catalog/products/{id}/reviews

Query params: `page`, `size`

- **200 OK** / 404 Not Found

```bash
curl "http://localhost:8080/api/catalog/products/550e8400-e29b-41d4-a716-446655440000/reviews?page=0&size=10"
```

---

### GET /api/catalog/categories

- **200 OK**

```bash
curl http://localhost:8080/api/catalog/categories
```

Slugs disponibles: `cafe-especial`, `cafe-organico`, `cafe-de-origen`, `cafe-sostenible`, `kits-accesorios`

---

### GET /api/catalog/categories/{slug}

- **200 OK** / 404 Not Found

```bash
curl http://localhost:8080/api/catalog/categories/cafe-especial
```

---

### GET /api/catalog/certifications

- **200 OK**

```bash
curl http://localhost:8080/api/catalog/certifications
```

---

### GET /api/catalog/roast-levels

- **200 OK**

```bash
curl http://localhost:8080/api/catalog/roast-levels
```

---

## Cart 🔒 [BUYER]

### GET /api/cart

- **200 OK** / 401 Unauthorized

```bash
curl http://localhost:8080/api/cart \
  -H "Authorization: Bearer <buyerToken>"
```

---

### POST /api/cart/items

- **201 Created** / 400 Bad Request / 404 Not Found

```bash
curl -X POST http://localhost:8080/api/cart/items \
  -H "Authorization: Bearer <buyerToken>" \
  -H "Content-Type: application/json" \
  -d '{
    "productId": "550e8400-e29b-41d4-a716-446655440000",
    "quantity": 2,
    "unitPriceSnapshot": 68000
  }'
```

---

### PATCH /api/cart/items/{itemId}

- **200 OK** / 400 Bad Request / 404 Not Found

```bash
curl -X PATCH http://localhost:8080/api/cart/items/550e8400-e29b-41d4-a716-446655440001 \
  -H "Authorization: Bearer <buyerToken>" \
  -H "Content-Type: application/json" \
  -d '{
    "quantity": 3
  }'
```

---

### DELETE /api/cart/items/{itemId}

- **204 No Content** / 404 Not Found

```bash
curl -X DELETE http://localhost:8080/api/cart/items/550e8400-e29b-41d4-a716-446655440001 \
  -H "Authorization: Bearer <buyerToken>"
```

---

### DELETE /api/cart

- **204 No Content** / 401 Unauthorized

```bash
curl -X DELETE http://localhost:8080/api/cart \
  -H "Authorization: Bearer <buyerToken>"
```

---

### POST /api/cart/coupon

- **200 OK** / 400 Bad Request (codigo invalido o expirado)

```bash
curl -X POST http://localhost:8080/api/cart/coupon \
  -H "Authorization: Bearer <buyerToken>" \
  -H "Content-Type: application/json" \
  -d '{
    "code": "CAFE10"
  }'
```

---

### DELETE /api/cart/coupon

- **204 No Content** / 401 Unauthorized

```bash
curl -X DELETE http://localhost:8080/api/cart/coupon \
  -H "Authorization: Bearer <buyerToken>"
```

---

### PATCH /api/cart/shipping

- **200 OK** / 400 Bad Request

Opciones validas: `standard`, `express`, `pickup`

```bash
curl -X PATCH http://localhost:8080/api/cart/shipping \
  -H "Authorization: Bearer <buyerToken>" \
  -H "Content-Type: application/json" \
  -d '{
    "shippingOptionId": "standard"
  }'
```

---

## Favorites 🔒 [BUYER]

### GET /api/favorites

- **200 OK** / 401 Unauthorized

```bash
curl http://localhost:8080/api/favorites \
  -H "Authorization: Bearer <buyerToken>"
```

---

### POST /api/favorites/{productId}

- **201 Created** / 409 Conflict (ya en favoritos)

```bash
curl -X POST http://localhost:8080/api/favorites/550e8400-e29b-41d4-a716-446655440000 \
  -H "Authorization: Bearer <buyerToken>"
```

---

### DELETE /api/favorites/{productId}

- **204 No Content** / 404 Not Found

```bash
curl -X DELETE http://localhost:8080/api/favorites/550e8400-e29b-41d4-a716-446655440000 \
  -H "Authorization: Bearer <buyerToken>"
```

---

## Orders 🔒

### POST /api/orders [BUYER]

- **201 Created** / 400 Bad Request / 404 Not Found

Codigos de pago validos: `nequi`, `bancolombia`, `daviplata`, `breb`

```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Authorization: Bearer <buyerToken>" \
  -H "Content-Type: application/json" \
  -d '{
    "addressId": "550e8400-e29b-41d4-a716-446655440000",
    "shippingOptionId": "standard",
    "paymentMethodCode": "nequi",
    "notes": "Dejar en porteria si no hay nadie."
  }'
```

---

### GET /api/orders [BUYER]

Query params: `status`, `page`, `size`

Estados validos: `pending_verification`, `confirmed`, `preparing`, `shipped`, `delivered`, `completed`, `cancelled`

- **200 OK** / 401 Unauthorized

```bash
curl "http://localhost:8080/api/orders?status=pending_verification&page=0&size=10" \
  -H "Authorization: Bearer <buyerToken>"
```

---

### GET /api/orders/{id} [BUYER/PRODUCER/ADMIN]

- **200 OK** / 403 Forbidden / 404 Not Found

```bash
curl http://localhost:8080/api/orders/550e8400-e29b-41d4-a716-446655440000 \
  -H "Authorization: Bearer <accessToken>"
```

---

### POST /api/orders/{id}/cancel [BUYER]

- **200 OK** / 400 Bad Request (estado no cancelable) / 404 Not Found

```bash
curl -X POST http://localhost:8080/api/orders/550e8400-e29b-41d4-a716-446655440000/cancel \
  -H "Authorization: Bearer <buyerToken>" \
  -H "Content-Type: application/json" \
  -d '{
    "reason": "Cambio de opinion."
  }'
```

---

### GET /api/orders/{id}/timeline [BUYER/PRODUCER]

- **200 OK** / 404 Not Found

```bash
curl http://localhost:8080/api/orders/550e8400-e29b-41d4-a716-446655440000/timeline \
  -H "Authorization: Bearer <accessToken>"
```

---

### GET /api/orders/{id}/invoice [BUYER]

- **200 OK** / 404 Not Found

```bash
curl http://localhost:8080/api/orders/550e8400-e29b-41d4-a716-446655440000/invoice \
  -H "Authorization: Bearer <buyerToken>"
```

---

### POST /api/orders/{id}/payment-proof [BUYER]

- **201 Created** / 400 Bad Request / 404 Not Found

```bash
curl -X POST http://localhost:8080/api/orders/550e8400-e29b-41d4-a716-446655440000/payment-proof \
  -H "Authorization: Bearer <buyerToken>" \
  -H "Content-Type: application/json" \
  -d '{
    "paymentMethodCode": "nequi",
    "amount": 145000,
    "reference": "REF-20260501-001",
    "proofUrl": "https://storage.wcm.co/proofs/comprobante.jpg"
  }'
```

---

### GET /api/orders/{id}/payment [BUYER/PRODUCER]

- **200 OK** / 403 Forbidden / 404 Not Found

```bash
curl http://localhost:8080/api/orders/550e8400-e29b-41d4-a716-446655440000/payment \
  -H "Authorization: Bearer <accessToken>"
```

---

## Payment Methods 🔒

### GET /api/payment-methods

- **200 OK** / 401 Unauthorized

```bash
curl http://localhost:8080/api/payment-methods \
  -H "Authorization: Bearer <accessToken>"
```

Codigos disponibles: `nequi`, `bancolombia`, `daviplata`, `breb`

---

## Reviews 🔒

### POST /api/reviews [BUYER]

- **201 Created** / 400 Bad Request / 409 Conflict (ya existe reseña para esa orden+producto)

```bash
curl -X POST http://localhost:8080/api/reviews \
  -H "Authorization: Bearer <buyerToken>" \
  -H "Content-Type: application/json" \
  -d '{
    "productId": "550e8400-e29b-41d4-a716-446655440000",
    "orderId": "550e8400-e29b-41d4-a716-446655440001",
    "rating": 5,
    "title": "Excelente cafe",
    "body": "Notas a chocolate y caramelo, muy bien procesado. Recomendado."
  }'
```

---

### POST /api/reviews/{id}/reply [PRODUCER/ADMIN]

- **201 Created** / 403 Forbidden / 404 Not Found

```bash
curl -X POST http://localhost:8080/api/reviews/550e8400-e29b-41d4-a716-446655440000/reply \
  -H "Authorization: Bearer <producerToken>" \
  -H "Content-Type: application/json" \
  -d '{
    "body": "Muchas gracias por tu compra. Nos alegra que hayas disfrutado el cafe."
  }'
```

---

### PATCH /api/reviews/{id}/moderate [ADMIN]

- **200 OK** / 403 Forbidden / 404 Not Found

Valores validos para `action`: `published`, `hidden`, `reported`

```bash
curl -X PATCH http://localhost:8080/api/reviews/550e8400-e29b-41d4-a716-446655440000/moderate \
  -H "Authorization: Bearer <adminToken>" \
  -H "Content-Type: application/json" \
  -d '{
    "action": "hidden"
  }'
```

---

## Producer 🔒 [PRODUCER]

### GET /api/producer/products

Query params: `status`, `page`, `size`

- **200 OK** / 403 Forbidden

```bash
curl "http://localhost:8080/api/producer/products?page=0&size=20" \
  -H "Authorization: Bearer <producerToken>"
```

---

### POST /api/producer/products

- **201 Created** / 400 Bad Request

Precios en COP. `unit` ej: `500g`, `1kg`, `250g`.

```bash
curl -X POST http://localhost:8080/api/producer/products \
  -H "Authorization: Bearer <producerToken>" \
  -H "Content-Type: application/json" \
  -d '{
    "categoryId": "550e8400-e29b-41d4-a716-446655440000",
    "name": "Cafe Especial Huila Washed",
    "description": "Proceso lavado, notas a ciruela y chocolate amargo.",
    "price": 68000,
    "unit": "500g",
    "region": "Huila",
    "emoji": "☕"
  }'
```

---

### PUT /api/producer/products/{id}

- **200 OK** / 403 Forbidden / 404 Not Found

```bash
curl -X PUT http://localhost:8080/api/producer/products/550e8400-e29b-41d4-a716-446655440000 \
  -H "Authorization: Bearer <producerToken>" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Cafe Especial Huila Honey",
    "description": "Proceso honey, notas a miel y frutas tropicales.",
    "price": 75000,
    "unit": "500g",
    "region": "Huila",
    "emoji": "☕",
    "categoryId": "550e8400-e29b-41d4-a716-446655440000"
  }'
```

---

### POST /api/producer/products/{id}/archive

- **200 OK** / 403 Forbidden / 404 Not Found

```bash
curl -X POST http://localhost:8080/api/producer/products/550e8400-e29b-41d4-a716-446655440000/archive \
  -H "Authorization: Bearer <producerToken>"
```

---

### GET /api/producer/orders

Query params: `status`, `page`, `size`

- **200 OK** / 403 Forbidden

```bash
curl "http://localhost:8080/api/producer/orders?status=confirmed&page=0&size=20" \
  -H "Authorization: Bearer <producerToken>"
```

---

### PATCH /api/producer/orders/{id}/status

- **200 OK** / 400 Bad Request (transicion invalida) / 404 Not Found

Flujo de estados: `pending_verification` → `confirmed` → `preparing` → `shipped` → `delivered` → `completed`

```bash
curl -X PATCH http://localhost:8080/api/producer/orders/550e8400-e29b-41d4-a716-446655440000/status \
  -H "Authorization: Bearer <producerToken>" \
  -H "Content-Type: application/json" \
  -d '{
    "newStatus": "preparing",
    "note": "Iniciando empaque del pedido."
  }'
```

---

### POST /api/producer/orders/{id}/payment/confirm

- **200 OK** / 403 Forbidden / 404 Not Found

```bash
curl -X POST http://localhost:8080/api/producer/orders/550e8400-e29b-41d4-a716-446655440000/payment/confirm \
  -H "Authorization: Bearer <producerToken>" \
  -H "Content-Type: application/json" \
  -d '{
    "verified": true,
    "note": "Pago verificado en Nequi."
  }'
```

---

### GET /api/producer/farm

- **200 OK** / 403 Forbidden

```bash
curl http://localhost:8080/api/producer/farm \
  -H "Authorization: Bearer <producerToken>"
```

---

### PATCH /api/producer/farm

- **200 OK** / 400 Bad Request

```bash
curl -X PATCH http://localhost:8080/api/producer/farm \
  -H "Authorization: Bearer <producerToken>" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Finca La Esperanza",
    "municipality": "Acevedo",
    "department": "Huila",
    "altitudeMasl": 1850.0,
    "areaHectares": 12.5,
    "mainVariety": "Caturra",
    "process": "Lavado",
    "description": "Finca familiar ubicada en las laderas del macizo colombiano."
  }'
```

---

### GET /api/producer/reviews

- **200 OK** / 403 Forbidden

```bash
curl http://localhost:8080/api/producer/reviews \
  -H "Authorization: Bearer <producerToken>"
```

---

### GET /api/producer/inventory

Query params: `productId` (UUID, opcional)

- **200 OK** / 403 Forbidden

```bash
curl "http://localhost:8080/api/producer/inventory?productId=550e8400-e29b-41d4-a716-446655440000" \
  -H "Authorization: Bearer <producerToken>"
```

---

### POST /api/producer/inventory/adjust

- **200 OK** / 400 Bad Request / 404 Not Found

`delta` positivo aumenta stock, negativo lo reduce.

```bash
curl -X POST http://localhost:8080/api/producer/inventory/adjust \
  -H "Authorization: Bearer <producerToken>" \
  -H "Content-Type: application/json" \
  -d '{
    "productId": "550e8400-e29b-41d4-a716-446655440000",
    "delta": 50
  }'
```

---

## Admin 🔒 [ADMIN]

### GET /api/admin/users

Query params: `role`, `status`, `search`, `page`, `size`

- **200 OK** / 403 Forbidden

```bash
curl "http://localhost:8080/api/admin/users?role=PRODUCER&status=active&page=0&size=20" \
  -H "Authorization: Bearer <adminToken>"
```

---

### PATCH /api/admin/users/{id}/ban

- **200 OK** / 403 Forbidden / 404 Not Found

```bash
curl -X PATCH http://localhost:8080/api/admin/users/550e8400-e29b-41d4-a716-446655440000/ban \
  -H "Authorization: Bearer <adminToken>" \
  -H "Content-Type: application/json" \
  -d '{
    "reason": "Incumplimiento de terminos de uso."
  }'
```

---

### PATCH /api/admin/users/{id}/unban

- **200 OK** / 403 Forbidden / 404 Not Found

```bash
curl -X PATCH http://localhost:8080/api/admin/users/550e8400-e29b-41d4-a716-446655440000/unban \
  -H "Authorization: Bearer <adminToken>"
```

---

### GET /api/admin/producer-approvals

Query params: `page`, `size`

- **200 OK** / 403 Forbidden

```bash
curl "http://localhost:8080/api/admin/producer-approvals?page=0&size=20" \
  -H "Authorization: Bearer <adminToken>"
```

---

### PATCH /api/admin/producer-approvals/{id}/approve

- **200 OK** / 403 Forbidden / 404 Not Found

```bash
curl -X PATCH http://localhost:8080/api/admin/producer-approvals/550e8400-e29b-41d4-a716-446655440000/approve \
  -H "Authorization: Bearer <adminToken>" \
  -H "Content-Type: application/json" \
  -d '{
    "notes": "Documentacion verificada. Finca registrada en camara de comercio."
  }'
```

---

### PATCH /api/admin/producer-approvals/{id}/reject

- **200 OK** / 403 Forbidden / 404 Not Found

```bash
curl -X PATCH http://localhost:8080/api/admin/producer-approvals/550e8400-e29b-41d4-a716-446655440000/reject \
  -H "Authorization: Bearer <adminToken>" \
  -H "Content-Type: application/json" \
  -d '{
    "reason": "Documentacion incompleta. Falta RUT actualizado."
  }'
```

---

### POST /api/admin/categories

- **201 Created** / 400 Bad Request / 409 Conflict (slug duplicado)

```bash
curl -X POST http://localhost:8080/api/admin/categories \
  -H "Authorization: Bearer <adminToken>" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Cafe de Altura",
    "slug": "cafe-de-altura",
    "description": "Cafes cultivados sobre los 1800 msnm.",
    "parentId": null,
    "iconEmoji": "⛰️",
    "isActive": true
  }'
```

---

### PUT /api/admin/categories/{id}

- **200 OK** / 404 Not Found

```bash
curl -X PUT http://localhost:8080/api/admin/categories/550e8400-e29b-41d4-a716-446655440000 \
  -H "Authorization: Bearer <adminToken>" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Cafe Especial Premium",
    "slug": "cafe-especial-premium",
    "description": "Seleccion premium de cafes especiales.",
    "parentId": null,
    "iconEmoji": "🏆",
    "isActive": true
  }'
```

---

### DELETE /api/admin/categories/{id}

- **204 No Content** / 404 Not Found / 409 Conflict (tiene productos asociados)

```bash
curl -X DELETE http://localhost:8080/api/admin/categories/550e8400-e29b-41d4-a716-446655440000 \
  -H "Authorization: Bearer <adminToken>"
```

---

### GET /api/admin/activity

Query params: `actorId`, `action`, `from` (ISO 8601), `to` (ISO 8601), `page`, `size`

- **200 OK** / 403 Forbidden

```bash
curl "http://localhost:8080/api/admin/activity?action=USER_BANNED&from=2026-05-01T00:00:00Z&to=2026-05-02T23:59:59Z&page=0&size=20" \
  -H "Authorization: Bearer <adminToken>"
```

---

## Notifications 🔒

### GET /api/notifications

Query params: `page`, `size`

- **200 OK** / 401 Unauthorized

```bash
curl "http://localhost:8080/api/notifications?page=0&size=20" \
  -H "Authorization: Bearer <accessToken>"
```

---

### PATCH /api/notifications/{id}/read

- **200 OK** / 404 Not Found

```bash
curl -X PATCH http://localhost:8080/api/notifications/550e8400-e29b-41d4-a716-446655440000/read \
  -H "Authorization: Bearer <accessToken>"
```

---

### POST /api/notifications/read-all

- **204 No Content** / 401 Unauthorized

```bash
curl -X POST http://localhost:8080/api/notifications/read-all \
  -H "Authorization: Bearer <accessToken>"
```

---

## Referencia rapida de codigos HTTP

| Codigo | Significado |
|--------|-------------|
| 200 | OK — operacion exitosa |
| 201 | Created — recurso creado |
| 204 | No Content — eliminado o accion sin respuesta |
| 400 | Bad Request — validacion fallida o logica de negocio |
| 401 | Unauthorized — token ausente o expirado |
| 403 | Forbidden — rol insuficiente |
| 404 | Not Found — recurso no existe |
| 409 | Conflict — duplicado o estado incompatible |
| 422 | Unprocessable Entity — datos semanticamente invalidos |
| 500 | Internal Server Error |

---

## Variables de entorno sugeridas para Postman

Crear un environment `WCM Local` con las siguientes variables:

| Variable | Valor inicial |
|----------|--------------|
| `baseUrl` | http://localhost:8080 |
| `buyerToken` | (llenar tras login como buyer@wcm.co) |
| `producerToken` | (llenar tras login como producer@wcm.co) |
| `adminToken` | (llenar tras login como admin@wcm.co) |
| `refreshToken` | (llenar tras login) |
| `productId` | 550e8400-e29b-41d4-a716-446655440000 |
| `orderId` | (llenar tras crear orden) |
| `addressId` | (llenar tras crear direccion) |

Usar el siguiente script en la pestaña "Tests" del request de login para auto-guardar el token:

```javascript
const res = pm.response.json();
pm.environment.set("buyerToken", res.accessToken);
pm.environment.set("refreshToken", res.refreshToken);
```
