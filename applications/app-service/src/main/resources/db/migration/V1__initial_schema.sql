-- ============================================================
--  World Coffee Marketplace — Esquema de Base de Datos v3
--  PostgreSQL 15+  |  Esquema: marketplace
--  Idempotente: puede ejecutarse múltiples veces sin errores
--  Generado: 2026-04-30
--  Basado en: erd_marketplace_v2.mmd
-- ============================================================

-- ────────────────────────────────────────────────────────────
--  EXTENSIONES  (idempotentes por definición)
-- ────────────────────────────────────────────────────────────
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pg_trgm";

-- ────────────────────────────────────────────────────────────
--  ESQUEMA PROPIO
-- ────────────────────────────────────────────────────────────
CREATE SCHEMA IF NOT EXISTS marketplace;

-- Establece el esquema por defecto para el resto del script.
-- Los FK REFERENCES sin prefijo se resuelven aquí primero.
SET search_path TO marketplace, public;

-- ────────────────────────────────────────────────────────────
--  TIPOS ENUMERADOS
--  CREATE TYPE no admite IF NOT EXISTS; se usa el bloque
--  DO $$ ... EXCEPTION WHEN duplicate_object THEN NULL; END $$
-- ────────────────────────────────────────────────────────────

DO $$ BEGIN
    CREATE TYPE marketplace.user_status AS ENUM (
        'active', 'inactive', 'banned'
    );
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

DO $$ BEGIN
    CREATE TYPE marketplace.producer_status AS ENUM (
        'pending', 'approved', 'rejected'
    );
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

DO $$ BEGIN
    CREATE TYPE marketplace.order_status AS ENUM (
        'pending_verification',  -- estado inicial: pago no verificado
        'confirmed',             -- admin verificó el comprobante
        'preparing',             -- productor preparando el envío
        'shipped',               -- en tránsito
        'delivered',             -- entregado al comprador
        'completed',             -- ciclo cerrado (reseña opcional)
        'cancelled'              -- cancelado en cualquier etapa
    );
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

DO $$ BEGIN
    CREATE TYPE marketplace.payment_status AS ENUM (
        'submitted',   -- comprobante enviado, pendiente verificación
        'verified',    -- pago verificado por admin
        'rejected',    -- comprobante rechazado
        'refunded'     -- devolución efectuada
    );
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

DO $$ BEGIN
    CREATE TYPE marketplace.review_status AS ENUM (
        'published', 'hidden', 'reported'
    );
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

DO $$ BEGIN
    CREATE TYPE marketplace.coupon_discount_type AS ENUM (
        'percentage',   -- porcentaje sobre subtotal
        'fixed'         -- valor fijo en COP
    );
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

DO $$ BEGIN
    CREATE TYPE marketplace.doc_status AS ENUM (
        'pending', 'approved', 'rejected'
    );
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

-- ────────────────────────────────────────────────────────────
--  FUNCIÓN AUXILIAR: updated_at automático
--  CREATE OR REPLACE es idempotente
-- ────────────────────────────────────────────────────────────
CREATE OR REPLACE FUNCTION marketplace.fn_set_updated_at()
RETURNS TRIGGER LANGUAGE plpgsql AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$;

-- ============================================================
--  MÓDULO 1 — USUARIOS E IDENTIDAD
-- ============================================================

-- ────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS marketplace.roles (
    id          SERIAL       PRIMARY KEY,
    name        VARCHAR(50)  NOT NULL,
    description VARCHAR(255),

    CONSTRAINT uq_roles_name UNIQUE (name)
);

COMMENT ON TABLE  marketplace.roles      IS 'Catálogo de roles del sistema (BUYER, PRODUCER, ADMIN).';
COMMENT ON COLUMN marketplace.roles.name IS 'Nombre único del rol: BUYER | PRODUCER | ADMIN.';

CREATE INDEX IF NOT EXISTS idx_roles_name ON marketplace.roles (name);

-- ────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS marketplace.users (
    id              UUID                    PRIMARY KEY DEFAULT gen_random_uuid(),
    email           VARCHAR(255)            NOT NULL,
    password_hash   VARCHAR(255)            NOT NULL,
    full_name       VARCHAR(255)            NOT NULL,
    phone           VARCHAR(20),
    status          marketplace.user_status NOT NULL DEFAULT 'active',
    privacy_consent BOOLEAN                 NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMPTZ             NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ             NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_users_email UNIQUE (email)
);

COMMENT ON TABLE  marketplace.users                 IS 'Tabla raíz de identidad. Todo usuario tiene exactamente un registro aquí.';
COMMENT ON COLUMN marketplace.users.password_hash   IS 'Hash bcrypt con salt 12. Nunca almacenar texto plano.';
COMMENT ON COLUMN marketplace.users.privacy_consent IS 'TRUE cuando el usuario aceptó la política de privacidad vigente.';

CREATE INDEX IF NOT EXISTS idx_users_email  ON marketplace.users (email);
CREATE INDEX IF NOT EXISTS idx_users_status ON marketplace.users (status);

CREATE OR REPLACE TRIGGER trg_users_updated_at
    BEFORE UPDATE ON marketplace.users
    FOR EACH ROW EXECUTE FUNCTION marketplace.fn_set_updated_at();

-- ────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS marketplace.user_roles (
    user_id     UUID        NOT NULL,
    role_id     INTEGER     NOT NULL,
    assigned_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_user_roles        PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_ur_user   FOREIGN KEY (user_id) REFERENCES marketplace.users(id)  ON DELETE CASCADE,
    CONSTRAINT fk_ur_role   FOREIGN KEY (role_id) REFERENCES marketplace.roles(id)  ON DELETE RESTRICT
);

COMMENT ON TABLE marketplace.user_roles IS 'Pivote N:M usuario ↔ rol. Un usuario puede tener varios roles simultáneos.';

CREATE INDEX IF NOT EXISTS idx_user_roles_user ON marketplace.user_roles (user_id);
CREATE INDEX IF NOT EXISTS idx_user_roles_role ON marketplace.user_roles (role_id);

-- ────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS marketplace.buyer_profiles (
    id                UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id           UUID        NOT NULL,
    city              VARCHAR(100),
    department        VARCHAR(100),
    preferred_payment VARCHAR(50),
    newsletter_opt_in BOOLEAN     NOT NULL DEFAULT FALSE,
    avatar_initials   VARCHAR(5),
    created_at        TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_buyer_profiles_user UNIQUE (user_id),
    CONSTRAINT fk_bp_user FOREIGN KEY (user_id) REFERENCES marketplace.users(id) ON DELETE CASCADE
);

COMMENT ON TABLE  marketplace.buyer_profiles                 IS 'Perfil extendido del comprador (1:1 con users).';
COMMENT ON COLUMN marketplace.buyer_profiles.avatar_initials IS 'Iniciales para avatar generado (ej: "JD").';

CREATE INDEX IF NOT EXISTS idx_buyer_profiles_user ON marketplace.buyer_profiles (user_id);

CREATE OR REPLACE TRIGGER trg_buyer_profiles_updated_at
    BEFORE UPDATE ON marketplace.buyer_profiles
    FOR EACH ROW EXECUTE FUNCTION marketplace.fn_set_updated_at();

-- ────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS marketplace.producer_profiles (
    id               UUID                        PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id          UUID                        NOT NULL,
    bio              TEXT,
    city             VARCHAR(100),
    department       VARCHAR(100),
    status           marketplace.producer_status NOT NULL DEFAULT 'pending',
    rejection_reason TEXT,
    approved_by      UUID,
    approved_at      TIMESTAMPTZ,
    avatar_initials  VARCHAR(5),
    created_at       TIMESTAMPTZ                 NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMPTZ                 NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_producer_profiles_user  UNIQUE (user_id),
    CONSTRAINT fk_pp_user       FOREIGN KEY (user_id)      REFERENCES marketplace.users(id) ON DELETE CASCADE,
    CONSTRAINT fk_pp_approved   FOREIGN KEY (approved_by)  REFERENCES marketplace.users(id) ON DELETE SET NULL,
    CONSTRAINT chk_producer_approval CHECK (
        (status = 'approved' AND approved_by IS NOT NULL AND approved_at IS NOT NULL)
        OR status <> 'approved'
    )
);

COMMENT ON TABLE  marketplace.producer_profiles        IS 'Perfil extendido del productor (1:1 con users). Requiere aprobación admin.';
COMMENT ON COLUMN marketplace.producer_profiles.status IS 'pending → aprobación pendiente; approved → activo; rejected → rechazado.';

CREATE INDEX IF NOT EXISTS idx_producer_profiles_user   ON marketplace.producer_profiles (user_id);
CREATE INDEX IF NOT EXISTS idx_producer_profiles_status ON marketplace.producer_profiles (status);

CREATE OR REPLACE TRIGGER trg_producer_profiles_updated_at
    BEFORE UPDATE ON marketplace.producer_profiles
    FOR EACH ROW EXECUTE FUNCTION marketplace.fn_set_updated_at();

-- ────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS marketplace.producer_documents (
    id            UUID                    PRIMARY KEY DEFAULT gen_random_uuid(),
    producer_id   UUID                    NOT NULL,
    document_type VARCHAR(50)             NOT NULL,
    file_name     VARCHAR(255)            NOT NULL,
    file_url      VARCHAR(500)            NOT NULL,
    status        marketplace.doc_status  NOT NULL DEFAULT 'pending',
    uploaded_at   TIMESTAMPTZ             NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_pd_producer FOREIGN KEY (producer_id)
        REFERENCES marketplace.producer_profiles(id) ON DELETE CASCADE
);

COMMENT ON TABLE marketplace.producer_documents IS 'Documentos de verificación cargados por el productor (RUT, cédula, registros sanitarios, etc.).';

CREATE INDEX IF NOT EXISTS idx_producer_docs_producer ON marketplace.producer_documents (producer_id);
CREATE INDEX IF NOT EXISTS idx_producer_docs_status   ON marketplace.producer_documents (status);

-- ────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS marketplace.password_reset_tokens (
    id         UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id    UUID        NOT NULL,
    token_hash VARCHAR(255) NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    used_at    TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_prt_token  UNIQUE (token_hash),
    CONSTRAINT fk_prt_user   FOREIGN KEY (user_id) REFERENCES marketplace.users(id) ON DELETE CASCADE,
    CONSTRAINT chk_prt_expiry CHECK (expires_at > created_at)
);

COMMENT ON TABLE  marketplace.password_reset_tokens            IS 'Tokens de restablecimiento de contraseña con expiración de 1 hora.';
COMMENT ON COLUMN marketplace.password_reset_tokens.token_hash IS 'SHA-256 del token enviado por email. Nunca almacenar el token raw.';

CREATE INDEX IF NOT EXISTS idx_prt_user    ON marketplace.password_reset_tokens (user_id);
CREATE INDEX IF NOT EXISTS idx_prt_expires ON marketplace.password_reset_tokens (expires_at)
    WHERE used_at IS NULL;

-- ────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS marketplace.privacy_consents (
    id             UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id        UUID        NOT NULL,
    policy_version VARCHAR(20) NOT NULL,
    accepted_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    ip_address     INET,

    CONSTRAINT fk_pc_user FOREIGN KEY (user_id) REFERENCES marketplace.users(id) ON DELETE CASCADE
);

COMMENT ON TABLE marketplace.privacy_consents IS 'Registro de aceptaciones de política de privacidad (trazabilidad GDPR/LFPDPPP).';

CREATE INDEX IF NOT EXISTS idx_privacy_consents_user ON marketplace.privacy_consents (user_id);

-- ────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS marketplace.addresses (
    id         UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id    UUID        NOT NULL,
    label      VARCHAR(50),
    line1      VARCHAR(255) NOT NULL,
    line2      VARCHAR(255),
    city       VARCHAR(100) NOT NULL,
    department VARCHAR(100) NOT NULL,
    zip_code   VARCHAR(20),
    is_default BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ  NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_addr_user FOREIGN KEY (user_id) REFERENCES marketplace.users(id) ON DELETE CASCADE
);

COMMENT ON TABLE  marketplace.addresses            IS 'Direcciones de entrega registradas por el usuario. Máximo 1 is_default=TRUE por usuario.';
COMMENT ON COLUMN marketplace.addresses.label      IS 'Etiqueta descriptiva: "Casa", "Oficina", etc.';
COMMENT ON COLUMN marketplace.addresses.is_default IS 'Solo una dirección puede ser default por usuario (garantizado por índice único parcial).';

CREATE INDEX IF NOT EXISTS idx_addresses_user    ON marketplace.addresses (user_id);
CREATE INDEX IF NOT EXISTS idx_addresses_default ON marketplace.addresses (user_id, is_default)
    WHERE is_default = TRUE;

-- Índice único parcial: garantiza una sola dirección predeterminada por usuario
CREATE UNIQUE INDEX IF NOT EXISTS idx_addresses_one_default
    ON marketplace.addresses (user_id)
    WHERE is_default = TRUE;

CREATE OR REPLACE TRIGGER trg_addresses_updated_at
    BEFORE UPDATE ON marketplace.addresses
    FOR EACH ROW EXECUTE FUNCTION marketplace.fn_set_updated_at();

-- ============================================================
--  MÓDULO 2 — FINCA Y CATÁLOGO
-- ============================================================

-- ────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS marketplace.certifications (
    id           SERIAL       PRIMARY KEY,
    code         VARCHAR(20)  NOT NULL,
    name         VARCHAR(255) NOT NULL,
    issuing_body VARCHAR(255),
    description  TEXT,

    CONSTRAINT uq_certifications_code UNIQUE (code)
);

COMMENT ON TABLE marketplace.certifications IS 'Catálogo de certificaciones reconocidas (Rainforest Alliance, FairTrade, Orgánico, etc.).';

CREATE INDEX IF NOT EXISTS idx_certifications_code ON marketplace.certifications (code);

-- ────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS marketplace.roast_levels (
    id          SERIAL       PRIMARY KEY,
    code        VARCHAR(20)  NOT NULL,
    name        VARCHAR(50)  NOT NULL,
    description VARCHAR(255),
    icon        VARCHAR(10),

    CONSTRAINT uq_roast_levels_code UNIQUE (code)
);

COMMENT ON TABLE marketplace.roast_levels IS 'Catálogo de niveles de tueste estándar.';

CREATE INDEX IF NOT EXISTS idx_roast_levels_code ON marketplace.roast_levels (code);

-- ────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS marketplace.farms (
    id                       UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    producer_id              UUID          NOT NULL,
    name                     VARCHAR(255)  NOT NULL,
    municipality             VARCHAR(100)  NOT NULL,
    department               VARCHAR(100)  NOT NULL,
    altitude_masl            NUMERIC(7,1),
    area_hectares            NUMERIC(10,2),
    main_variety             VARCHAR(100),
    process                  VARCHAR(50),
    tree_count               INTEGER,
    harvest_season           VARCHAR(100),
    annual_production_sacos  NUMERIC(10,2),
    yield_per_ha             NUMERIC(10,2),
    cupping_score            NUMERIC(4,2),
    description              TEXT,
    created_at               TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    updated_at               TIMESTAMPTZ   NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_farms_producer         UNIQUE (producer_id),
    CONSTRAINT fk_farms_producer         FOREIGN KEY (producer_id)
        REFERENCES marketplace.producer_profiles(id) ON DELETE CASCADE,
    CONSTRAINT chk_farms_area            CHECK (area_hectares IS NULL OR area_hectares > 0),
    CONSTRAINT chk_farms_trees           CHECK (tree_count IS NULL OR tree_count >= 0),
    CONSTRAINT chk_farms_production      CHECK (annual_production_sacos IS NULL OR annual_production_sacos >= 0),
    CONSTRAINT chk_farms_yield           CHECK (yield_per_ha IS NULL OR yield_per_ha >= 0),
    CONSTRAINT chk_farms_cupping         CHECK (cupping_score IS NULL OR cupping_score BETWEEN 50 AND 100)
);

COMMENT ON TABLE  marketplace.farms                         IS 'Finca del productor (1:1). Un productor registra una sola finca.';
COMMENT ON COLUMN marketplace.farms.altitude_masl           IS 'Altitud en metros sobre el nivel del mar.';
COMMENT ON COLUMN marketplace.farms.annual_production_sacos IS 'Producción anual en sacos de 60 kg.';
COMMENT ON COLUMN marketplace.farms.cupping_score           IS 'Puntuación SCA de cata (50–100).';

CREATE INDEX IF NOT EXISTS idx_farms_producer   ON marketplace.farms (producer_id);
CREATE INDEX IF NOT EXISTS idx_farms_department ON marketplace.farms (department);

CREATE OR REPLACE TRIGGER trg_farms_updated_at
    BEFORE UPDATE ON marketplace.farms
    FOR EACH ROW EXECUTE FUNCTION marketplace.fn_set_updated_at();

-- ────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS marketplace.farm_certifications (
    id               UUID                   PRIMARY KEY DEFAULT gen_random_uuid(),
    farm_id          UUID                   NOT NULL,
    certification_id INTEGER                NOT NULL,
    issuer           VARCHAR(255),
    issue_date       DATE,
    expiry_date      DATE,
    status           marketplace.doc_status NOT NULL DEFAULT 'approved',
    document_url     VARCHAR(500),
    notes            TEXT,

    CONSTRAINT uq_farm_certifications         UNIQUE (farm_id, certification_id),
    CONSTRAINT fk_fc_farm         FOREIGN KEY (farm_id)          REFERENCES marketplace.farms(id)          ON DELETE CASCADE,
    CONSTRAINT fk_fc_cert         FOREIGN KEY (certification_id) REFERENCES marketplace.certifications(id) ON DELETE RESTRICT,
    CONSTRAINT chk_fc_dates       CHECK (expiry_date IS NULL OR expiry_date > issue_date)
);

COMMENT ON TABLE marketplace.farm_certifications IS 'Pivote finca ↔ certificación con fechas de vigencia.';

CREATE INDEX IF NOT EXISTS idx_fc_farm   ON marketplace.farm_certifications (farm_id);
CREATE INDEX IF NOT EXISTS idx_fc_expiry ON marketplace.farm_certifications (expiry_date)
    WHERE expiry_date IS NOT NULL;

-- ────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS marketplace.categories (
    id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    name        VARCHAR(100) NOT NULL,
    slug        VARCHAR(100) NOT NULL,
    description TEXT,
    parent_id   UUID,
    is_active   BOOLEAN     NOT NULL DEFAULT TRUE,
    icon_emoji  VARCHAR(10),
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_categories_name  UNIQUE (name),
    CONSTRAINT uq_categories_slug  UNIQUE (slug),
    CONSTRAINT fk_cat_parent FOREIGN KEY (parent_id) REFERENCES marketplace.categories(id) ON DELETE SET NULL
);

COMMENT ON TABLE  marketplace.categories           IS 'Árbol de categorías de productos (auto-referencial, máx. 2 niveles recomendados).';
COMMENT ON COLUMN marketplace.categories.slug      IS 'URL-friendly: "cafe-especial", "tostado-oscuro".';
COMMENT ON COLUMN marketplace.categories.parent_id IS 'NULL = categoría raíz; ID = subcategoría.';

CREATE INDEX IF NOT EXISTS idx_categories_parent ON marketplace.categories (parent_id);
CREATE INDEX IF NOT EXISTS idx_categories_slug   ON marketplace.categories (slug);
CREATE INDEX IF NOT EXISTS idx_categories_active ON marketplace.categories (is_active)
    WHERE is_active = TRUE;

-- ────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS marketplace.products (
    id               UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    producer_id      UUID          NOT NULL,
    category_id      UUID          NOT NULL,
    name             VARCHAR(255)  NOT NULL,
    description      TEXT,
    price            NUMERIC(12,2) NOT NULL,
    original_price   NUMERIC(12,2),
    discount_percent NUMERIC(5,2),
    unit             VARCHAR(20)   NOT NULL DEFAULT 'kg',
    region           VARCHAR(100),
    emoji            VARCHAR(10),
    sold_count       INTEGER       NOT NULL DEFAULT 0,
    status           VARCHAR(20)   NOT NULL DEFAULT 'active',
    created_at       TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMPTZ   NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_prod_producer  FOREIGN KEY (producer_id)  REFERENCES marketplace.producer_profiles(id) ON DELETE CASCADE,
    CONSTRAINT fk_prod_category  FOREIGN KEY (category_id)  REFERENCES marketplace.categories(id)        ON DELETE RESTRICT,
    CONSTRAINT chk_prod_price    CHECK (price > 0),
    CONSTRAINT chk_prod_orig     CHECK (original_price IS NULL OR original_price > 0),
    CONSTRAINT chk_prod_discount CHECK (discount_percent IS NULL OR discount_percent BETWEEN 0 AND 100),
    CONSTRAINT chk_prod_sold     CHECK (sold_count >= 0),
    CONSTRAINT chk_prod_status   CHECK (status IN ('active','inactive','draft'))
);

COMMENT ON TABLE  marketplace.products                IS 'Productos del catálogo publicados por productores.';
COMMENT ON COLUMN marketplace.products.original_price IS 'Precio antes del descuento. NULL si no hay promoción.';
COMMENT ON COLUMN marketplace.products.unit           IS '"kg", "250g", "500g", "lb".';

CREATE INDEX IF NOT EXISTS idx_products_producer  ON marketplace.products (producer_id);
CREATE INDEX IF NOT EXISTS idx_products_category  ON marketplace.products (category_id);
CREATE INDEX IF NOT EXISTS idx_products_status    ON marketplace.products (status);
CREATE INDEX IF NOT EXISTS idx_products_price     ON marketplace.products (price);
CREATE INDEX IF NOT EXISTS idx_products_name_trgm ON marketplace.products USING gin (name gin_trgm_ops);
CREATE INDEX IF NOT EXISTS idx_products_desc_trgm ON marketplace.products USING gin (description gin_trgm_ops);

CREATE OR REPLACE TRIGGER trg_products_updated_at
    BEFORE UPDATE ON marketplace.products
    FOR EACH ROW EXECUTE FUNCTION marketplace.fn_set_updated_at();

-- ────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS marketplace.product_images (
    id            UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    product_id    UUID         NOT NULL,
    image_url     VARCHAR(500) NOT NULL,
    display_order INTEGER      NOT NULL DEFAULT 0,
    uploaded_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_pi_product FOREIGN KEY (product_id) REFERENCES marketplace.products(id) ON DELETE CASCADE
);

COMMENT ON TABLE marketplace.product_images IS 'Imágenes del producto ordenadas. La primera (display_order=0) es la principal.';

CREATE INDEX IF NOT EXISTS idx_product_images_product ON marketplace.product_images (product_id, display_order);

-- ────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS marketplace.product_certifications (
    id               UUID    PRIMARY KEY DEFAULT gen_random_uuid(),
    product_id       UUID    NOT NULL,
    certification_id INTEGER NOT NULL,
    document_url     VARCHAR(500),
    issued_at        DATE,

    CONSTRAINT uq_product_certifications     UNIQUE (product_id, certification_id),
    CONSTRAINT fk_pcert_product  FOREIGN KEY (product_id)       REFERENCES marketplace.products(id)       ON DELETE CASCADE,
    CONSTRAINT fk_pcert_cert     FOREIGN KEY (certification_id) REFERENCES marketplace.certifications(id) ON DELETE RESTRICT
);

COMMENT ON TABLE marketplace.product_certifications IS 'Pivote producto ↔ certificación (N:M).';

CREATE INDEX IF NOT EXISTS idx_product_certs_product ON marketplace.product_certifications (product_id);
CREATE INDEX IF NOT EXISTS idx_product_certs_cert    ON marketplace.product_certifications (certification_id);

-- ────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS marketplace.product_presentations (
    id           UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    product_id   UUID          NOT NULL,
    presentation VARCHAR(50)   NOT NULL,
    extra_price  NUMERIC(12,2) NOT NULL DEFAULT 0,

    CONSTRAINT fk_pres_product  FOREIGN KEY (product_id) REFERENCES marketplace.products(id) ON DELETE CASCADE,
    CONSTRAINT chk_pres_price   CHECK (extra_price >= 0)
);

COMMENT ON TABLE  marketplace.product_presentations             IS 'Variantes de presentación del producto (250g, 500g, 1 kg, etc.).';
COMMENT ON COLUMN marketplace.product_presentations.extra_price IS 'Sobrecosto relativo al precio base. 0 = sin costo adicional.';

CREATE INDEX IF NOT EXISTS idx_product_presentations_product ON marketplace.product_presentations (product_id);

-- ────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS marketplace.product_roast_levels (
    product_id     UUID    NOT NULL,
    roast_level_id INTEGER NOT NULL,

    CONSTRAINT pk_product_roast_levels PRIMARY KEY (product_id, roast_level_id),
    CONSTRAINT fk_prl_product     FOREIGN KEY (product_id)     REFERENCES marketplace.products(id)     ON DELETE CASCADE,
    CONSTRAINT fk_prl_roast       FOREIGN KEY (roast_level_id) REFERENCES marketplace.roast_levels(id) ON DELETE RESTRICT
);

COMMENT ON TABLE marketplace.product_roast_levels IS 'Pivote producto ↔ nivel de tueste (N:M). Un producto puede ofrecerse en varios tuestes.';

CREATE INDEX IF NOT EXISTS idx_prl_product    ON marketplace.product_roast_levels (product_id);
CREATE INDEX IF NOT EXISTS idx_prl_roast      ON marketplace.product_roast_levels (roast_level_id);

-- ────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS marketplace.product_flavor_notes (
    id         UUID     PRIMARY KEY DEFAULT gen_random_uuid(),
    product_id UUID     NOT NULL,
    name       VARCHAR(50) NOT NULL,
    icon       VARCHAR(10),
    intensity  SMALLINT NOT NULL,

    CONSTRAINT fk_pfn_product  FOREIGN KEY (product_id) REFERENCES marketplace.products(id) ON DELETE CASCADE,
    CONSTRAINT chk_pfn_intensity CHECK (intensity BETWEEN 1 AND 5)
);

COMMENT ON TABLE  marketplace.product_flavor_notes           IS 'Notas de sabor del perfil sensorial (achocolatado, afrutado, floral, etc.).';
COMMENT ON COLUMN marketplace.product_flavor_notes.intensity IS 'Intensidad de 1 (débil) a 5 (muy intenso).';

CREATE INDEX IF NOT EXISTS idx_pfn_product ON marketplace.product_flavor_notes (product_id);

-- ────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS marketplace.product_cupping (
    product_id UUID     PRIMARY KEY,
    score      NUMERIC(4,2),
    aroma      SMALLINT,
    flavor     SMALLINT,
    body       SMALLINT,
    finish     SMALLINT,
    acidity    SMALLINT,

    CONSTRAINT fk_pc_product  FOREIGN KEY (product_id) REFERENCES marketplace.products(id) ON DELETE CASCADE,
    CONSTRAINT chk_pc_score   CHECK (score   IS NULL OR score   BETWEEN 50 AND 100),
    CONSTRAINT chk_pc_aroma   CHECK (aroma   IS NULL OR aroma   BETWEEN 1 AND 10),
    CONSTRAINT chk_pc_flavor  CHECK (flavor  IS NULL OR flavor  BETWEEN 1 AND 10),
    CONSTRAINT chk_pc_body    CHECK (body    IS NULL OR body    BETWEEN 1 AND 10),
    CONSTRAINT chk_pc_finish  CHECK (finish  IS NULL OR finish  BETWEEN 1 AND 10),
    CONSTRAINT chk_pc_acidity CHECK (acidity IS NULL OR acidity BETWEEN 1 AND 10)
);

COMMENT ON TABLE marketplace.product_cupping IS 'Perfil sensorial de cata SCA (1:1 con products). Atributos en escala 1–10.';

-- ────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS marketplace.inventory (
    id         UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    product_id UUID        NOT NULL,
    quantity   INTEGER     NOT NULL DEFAULT 0,
    max_stock  INTEGER,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_inventory_product UNIQUE (product_id),
    CONSTRAINT fk_inv_product FOREIGN KEY (product_id) REFERENCES marketplace.products(id) ON DELETE CASCADE,
    CONSTRAINT chk_inv_qty      CHECK (quantity >= 0),
    CONSTRAINT chk_inv_max      CHECK (max_stock IS NULL OR max_stock >= 0)
);

COMMENT ON TABLE  marketplace.inventory           IS 'Stock disponible por producto (1:1). Se actualiza en cada venta o reabastecimiento.';
COMMENT ON COLUMN marketplace.inventory.max_stock IS 'Capacidad máxima. NULL = sin límite definido.';

CREATE INDEX IF NOT EXISTS idx_inventory_product  ON marketplace.inventory (product_id);
CREATE INDEX IF NOT EXISTS idx_inventory_low_stock ON marketplace.inventory (quantity)
    WHERE quantity = 0;

-- ============================================================
--  MÓDULO 3 — CARRITO Y FAVORITOS
-- ============================================================

-- ────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS marketplace.coupons (
    id             SERIAL                          PRIMARY KEY,
    code           VARCHAR(50)                     NOT NULL,
    description    VARCHAR(255),
    discount_type  marketplace.coupon_discount_type NOT NULL,
    discount_value NUMERIC(10,2)                   NOT NULL,
    min_subtotal   NUMERIC(12,2)                   NOT NULL DEFAULT 0,
    usage_limit    INTEGER,
    used_count     INTEGER                         NOT NULL DEFAULT 0,
    valid_from     TIMESTAMPTZ,
    valid_until    TIMESTAMPTZ,
    is_active      BOOLEAN                         NOT NULL DEFAULT TRUE,

    CONSTRAINT uq_coupons_code    UNIQUE (code),
    CONSTRAINT chk_coupon_value   CHECK (discount_value > 0),
    CONSTRAINT chk_coupon_min     CHECK (min_subtotal >= 0),
    CONSTRAINT chk_coupon_limit   CHECK (usage_limit IS NULL OR usage_limit > 0),
    CONSTRAINT chk_coupon_used    CHECK (used_count >= 0),
    CONSTRAINT chk_coupon_dates   CHECK (valid_until IS NULL OR valid_until > valid_from),
    CONSTRAINT chk_coupon_usage   CHECK (usage_limit IS NULL OR used_count <= usage_limit),
    CONSTRAINT chk_pct_range      CHECK (
        discount_type <> 'percentage' OR discount_value BETWEEN 0.01 AND 100
    )
);

COMMENT ON TABLE  marketplace.coupons             IS 'Cupones de descuento aplicables al carrito o pedido.';
COMMENT ON COLUMN marketplace.coupons.code        IS 'Código que ingresa el comprador, ej: CAFE10.';
COMMENT ON COLUMN marketplace.coupons.usage_limit IS 'NULL = uso ilimitado.';

CREATE INDEX IF NOT EXISTS idx_coupons_code   ON marketplace.coupons (code);
CREATE INDEX IF NOT EXISTS idx_coupons_active ON marketplace.coupons (is_active, valid_until)
    WHERE is_active = TRUE;

-- ────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS marketplace.shipping_options (
    id              VARCHAR(50)   PRIMARY KEY,
    name            VARCHAR(100)  NOT NULL,
    delivery_window VARCHAR(100),
    price           NUMERIC(10,2) NOT NULL DEFAULT 0,
    is_active       BOOLEAN       NOT NULL DEFAULT TRUE,
    display_order   INTEGER       NOT NULL DEFAULT 0,

    CONSTRAINT chk_so_price CHECK (price >= 0)
);

COMMENT ON TABLE  marketplace.shipping_options                 IS 'Métodos de envío disponibles en el marketplace.';
COMMENT ON COLUMN marketplace.shipping_options.id              IS 'Slug legible: "standard", "express", "pickup".';
COMMENT ON COLUMN marketplace.shipping_options.delivery_window IS 'Descripción del plazo: "3-5 días hábiles".';

CREATE INDEX IF NOT EXISTS idx_shipping_options_active ON marketplace.shipping_options (is_active, display_order)
    WHERE is_active = TRUE;

-- ────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS marketplace.carts (
    id                 UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id            UUID        NOT NULL,
    coupon_id          INTEGER,
    shipping_option_id VARCHAR(50),
    created_at         TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at         TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_carts_user    UNIQUE (user_id),
    CONSTRAINT fk_cart_user     FOREIGN KEY (user_id)            REFERENCES marketplace.users(id)            ON DELETE CASCADE,
    CONSTRAINT fk_cart_coupon   FOREIGN KEY (coupon_id)          REFERENCES marketplace.coupons(id)          ON DELETE SET NULL,
    CONSTRAINT fk_cart_shipping FOREIGN KEY (shipping_option_id) REFERENCES marketplace.shipping_options(id) ON DELETE SET NULL
);

COMMENT ON TABLE marketplace.carts IS 'Carrito activo del usuario (1:1). Persiste entre sesiones; se vacía al confirmar el pedido.';

CREATE INDEX IF NOT EXISTS idx_carts_user   ON marketplace.carts (user_id);
CREATE INDEX IF NOT EXISTS idx_carts_coupon ON marketplace.carts (coupon_id)
    WHERE coupon_id IS NOT NULL;

CREATE OR REPLACE TRIGGER trg_carts_updated_at
    BEFORE UPDATE ON marketplace.carts
    FOR EACH ROW EXECUTE FUNCTION marketplace.fn_set_updated_at();

-- ────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS marketplace.cart_items (
    id                  UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    cart_id             UUID          NOT NULL,
    product_id          UUID          NOT NULL,
    quantity            INTEGER       NOT NULL DEFAULT 1,
    unit_price_snapshot NUMERIC(12,2) NOT NULL,
    added_at            TIMESTAMPTZ   NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_cart_items           UNIQUE (cart_id, product_id),
    CONSTRAINT fk_ci_cart    FOREIGN KEY (cart_id)    REFERENCES marketplace.carts(id)    ON DELETE CASCADE,
    CONSTRAINT fk_ci_product FOREIGN KEY (product_id) REFERENCES marketplace.products(id) ON DELETE CASCADE,
    CONSTRAINT chk_ci_qty    CHECK (quantity > 0),
    CONSTRAINT chk_ci_price  CHECK (unit_price_snapshot > 0)
);

COMMENT ON TABLE  marketplace.cart_items                     IS 'Ítems del carrito. El precio se congela al momento de añadir.';
COMMENT ON COLUMN marketplace.cart_items.unit_price_snapshot IS 'Precio unitario en el momento de adición. No se actualiza si el producto cambia de precio.';

CREATE INDEX IF NOT EXISTS idx_cart_items_cart    ON marketplace.cart_items (cart_id);
CREATE INDEX IF NOT EXISTS idx_cart_items_product ON marketplace.cart_items (product_id);

-- ────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS marketplace.favorites (
    id         UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id    UUID        NOT NULL,
    product_id UUID        NOT NULL,
    added_at   TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_favorites           UNIQUE (user_id, product_id),
    CONSTRAINT fk_fav_user    FOREIGN KEY (user_id)    REFERENCES marketplace.users(id)    ON DELETE CASCADE,
    CONSTRAINT fk_fav_product FOREIGN KEY (product_id) REFERENCES marketplace.products(id) ON DELETE CASCADE
);

COMMENT ON TABLE marketplace.favorites IS 'Lista de favoritos del comprador. Un usuario no puede duplicar el mismo producto.';

CREATE INDEX IF NOT EXISTS idx_favorites_user    ON marketplace.favorites (user_id);
CREATE INDEX IF NOT EXISTS idx_favorites_product ON marketplace.favorites (product_id);

-- ============================================================
--  MÓDULO 4 — PEDIDOS
-- ============================================================

-- ────────────────────────────────────────────────────────────
--  FUNCIÓN: código secuencial anual WCM-YYYY-NNN
--  CREATE OR REPLACE es idempotente
-- ────────────────────────────────────────────────────────────
CREATE OR REPLACE FUNCTION marketplace.fn_next_order_seq(p_year INTEGER)
RETURNS INTEGER LANGUAGE plpgsql AS $$
DECLARE
    v_seq INTEGER;
BEGIN
    SELECT COALESCE(MAX(yearly_sequence), 0) + 1
    INTO   v_seq
    FROM   marketplace.orders
    WHERE  year = p_year;
    RETURN v_seq;
END;
$$;

-- ────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS marketplace.orders (
    id                       UUID                        PRIMARY KEY DEFAULT gen_random_uuid(),
    buyer_id                 UUID                        NOT NULL,
    address_id               UUID,
    shipping_option_id       VARCHAR(50),
    coupon_id                INTEGER,
    code                     VARCHAR(20)                 NOT NULL,
    yearly_sequence          INTEGER                     NOT NULL,
    year                     INTEGER                     NOT NULL,
    subtotal                 NUMERIC(12,2)               NOT NULL,
    shipping_amount          NUMERIC(12,2)               NOT NULL DEFAULT 0,
    discount_amount          NUMERIC(12,2)               NOT NULL DEFAULT 0,
    total_amount             NUMERIC(12,2)               NOT NULL,
    status                   marketplace.order_status    NOT NULL DEFAULT 'pending_verification',
    shipping_address_snapshot TEXT,
    created_at               TIMESTAMPTZ                 NOT NULL DEFAULT NOW(),
    updated_at               TIMESTAMPTZ                 NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_orders_code       UNIQUE (code),
    CONSTRAINT uq_orders_year_seq   UNIQUE (year, yearly_sequence),
    CONSTRAINT fk_ord_buyer         FOREIGN KEY (buyer_id)           REFERENCES marketplace.users(id)           ON DELETE RESTRICT,
    CONSTRAINT fk_ord_address       FOREIGN KEY (address_id)         REFERENCES marketplace.addresses(id)       ON DELETE SET NULL,
    CONSTRAINT fk_ord_shipping      FOREIGN KEY (shipping_option_id) REFERENCES marketplace.shipping_options(id) ON DELETE RESTRICT,
    CONSTRAINT fk_ord_coupon        FOREIGN KEY (coupon_id)          REFERENCES marketplace.coupons(id)         ON DELETE SET NULL,
    CONSTRAINT chk_ord_subtotal     CHECK (subtotal >= 0),
    CONSTRAINT chk_ord_shipping     CHECK (shipping_amount >= 0),
    CONSTRAINT chk_ord_discount     CHECK (discount_amount >= 0),
    CONSTRAINT chk_ord_total        CHECK (total_amount >= 0),
    CONSTRAINT chk_order_total_calc CHECK (
        total_amount = subtotal + shipping_amount - discount_amount
    )
);

COMMENT ON TABLE  marketplace.orders                           IS 'Pedidos realizados por compradores. Estado inicial: pending_verification.';
COMMENT ON COLUMN marketplace.orders.code                      IS 'Código legible WCM-YYYY-NNN generado con fn_next_order_seq().';
COMMENT ON COLUMN marketplace.orders.yearly_sequence           IS 'Secuencia anual reiniciada cada 1 de enero.';
COMMENT ON COLUMN marketplace.orders.shipping_address_snapshot IS 'JSON del address al momento del pedido para preservar historial.';

CREATE INDEX IF NOT EXISTS idx_orders_buyer   ON marketplace.orders (buyer_id);
CREATE INDEX IF NOT EXISTS idx_orders_status  ON marketplace.orders (status);
CREATE INDEX IF NOT EXISTS idx_orders_code    ON marketplace.orders (code);
CREATE INDEX IF NOT EXISTS idx_orders_created ON marketplace.orders (created_at DESC);
CREATE INDEX IF NOT EXISTS idx_orders_year    ON marketplace.orders (year, yearly_sequence);

CREATE OR REPLACE TRIGGER trg_orders_updated_at
    BEFORE UPDATE ON marketplace.orders
    FOR EACH ROW EXECUTE FUNCTION marketplace.fn_set_updated_at();

-- ────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS marketplace.order_items (
    id                     UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id               UUID          NOT NULL,
    product_id             UUID,
    product_name_snapshot  VARCHAR(255)  NOT NULL,
    product_emoji_snapshot VARCHAR(10),
    quantity               INTEGER       NOT NULL,
    unit_price_snapshot    NUMERIC(12,2) NOT NULL,
    subtotal               NUMERIC(12,2) NOT NULL,

    CONSTRAINT fk_oi_order   FOREIGN KEY (order_id)   REFERENCES marketplace.orders(id)   ON DELETE CASCADE,
    CONSTRAINT fk_oi_product FOREIGN KEY (product_id) REFERENCES marketplace.products(id) ON DELETE SET NULL,
    CONSTRAINT chk_oi_qty      CHECK (quantity > 0),
    CONSTRAINT chk_oi_price    CHECK (unit_price_snapshot > 0),
    CONSTRAINT chk_oi_subtotal CHECK (subtotal > 0),
    CONSTRAINT chk_oi_calc     CHECK (subtotal = quantity * unit_price_snapshot)
);

COMMENT ON TABLE  marketplace.order_items                       IS 'Líneas del pedido con snapshot de nombre y precio.';
COMMENT ON COLUMN marketplace.order_items.product_id            IS 'NULL si el producto fue eliminado; el snapshot preserva el historial.';
COMMENT ON COLUMN marketplace.order_items.product_name_snapshot IS 'Nombre del producto en el momento de la compra.';

CREATE INDEX IF NOT EXISTS idx_order_items_order   ON marketplace.order_items (order_id);
CREATE INDEX IF NOT EXISTS idx_order_items_product ON marketplace.order_items (product_id);

-- ────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS marketplace.order_status_history (
    id         UUID                     PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id   UUID                     NOT NULL,
    status     marketplace.order_status NOT NULL,
    changed_by UUID,
    notes      TEXT,
    changed_at TIMESTAMPTZ              NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_osh_order      FOREIGN KEY (order_id)   REFERENCES marketplace.orders(id) ON DELETE CASCADE,
    CONSTRAINT fk_osh_changed_by FOREIGN KEY (changed_by) REFERENCES marketplace.users(id)  ON DELETE SET NULL
);

COMMENT ON TABLE  marketplace.order_status_history            IS 'Historial de cambios de estado. Inmutable (sin UPDATE/DELETE).';
COMMENT ON COLUMN marketplace.order_status_history.changed_by IS 'NULL = cambio automático del sistema.';

CREATE INDEX IF NOT EXISTS idx_osh_order   ON marketplace.order_status_history (order_id, changed_at DESC);
CREATE INDEX IF NOT EXISTS idx_osh_changed ON marketplace.order_status_history (changed_at DESC);

-- ============================================================
--  MÓDULO 5 — PAGOS
-- ============================================================

-- ────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS marketplace.payment_methods (
    id             UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    code           VARCHAR(50) NOT NULL,
    name           VARCHAR(100) NOT NULL,
    type           VARCHAR(20) NOT NULL,
    account_number VARCHAR(50),
    account_holder VARCHAR(255),
    bank           VARCHAR(100),
    alias          VARCHAR(50),
    nit            VARCHAR(50),
    emoji          VARCHAR(10),
    accent_color   VARCHAR(20),
    is_active      BOOLEAN     NOT NULL DEFAULT TRUE,
    display_order  INTEGER     NOT NULL DEFAULT 0,

    CONSTRAINT uq_payment_methods_code UNIQUE (code),
    CONSTRAINT chk_pm_type CHECK (type IN ('digital_wallet','bank_transfer','bre_b'))
);

COMMENT ON TABLE marketplace.payment_methods IS 'Métodos de pago habilitados (transferencias manuales sin pasarela de pago).';

CREATE INDEX IF NOT EXISTS idx_pm_code   ON marketplace.payment_methods (code);
CREATE INDEX IF NOT EXISTS idx_pm_active ON marketplace.payment_methods (is_active, display_order)
    WHERE is_active = TRUE;

-- ────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS marketplace.order_payments (
    id                  UUID                       PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id            UUID                       NOT NULL,
    payment_method_id   UUID,
    payment_method_code VARCHAR(50)                NOT NULL,
    amount              NUMERIC(12,2)              NOT NULL,
    status              marketplace.payment_status NOT NULL DEFAULT 'submitted',
    reference           VARCHAR(100),
    proof_url           VARCHAR(500),
    submitted_at        TIMESTAMPTZ                NOT NULL DEFAULT NOW(),
    verified_at         TIMESTAMPTZ,
    verified_by         UUID,

    CONSTRAINT uq_order_payments_order  UNIQUE (order_id),
    CONSTRAINT fk_op_order       FOREIGN KEY (order_id)          REFERENCES marketplace.orders(id)          ON DELETE CASCADE,
    CONSTRAINT fk_op_method      FOREIGN KEY (payment_method_id) REFERENCES marketplace.payment_methods(id) ON DELETE SET NULL,
    CONSTRAINT fk_op_verified_by FOREIGN KEY (verified_by)       REFERENCES marketplace.users(id)           ON DELETE SET NULL,
    CONSTRAINT chk_op_amount     CHECK (amount > 0),
    CONSTRAINT chk_op_verification CHECK (
        (status = 'verified' AND verified_at IS NOT NULL AND verified_by IS NOT NULL)
        OR status <> 'verified'
    )
);

COMMENT ON TABLE  marketplace.order_payments           IS 'Pago del pedido (1:1). Flujo manual con comprobante WhatsApp.';
COMMENT ON COLUMN marketplace.order_payments.proof_url IS 'URL del comprobante (imagen/PDF) cargado por el comprador.';

CREATE INDEX IF NOT EXISTS idx_order_payments_order  ON marketplace.order_payments (order_id);
CREATE INDEX IF NOT EXISTS idx_order_payments_status ON marketplace.order_payments (status);

-- ============================================================
--  MÓDULO 6 — SOCIAL
-- ============================================================

-- ────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS marketplace.reviews (
    id                   UUID                     PRIMARY KEY DEFAULT gen_random_uuid(),
    product_id           UUID                     NOT NULL,
    buyer_id             UUID                     NOT NULL,
    order_id             UUID,
    rating               SMALLINT                 NOT NULL,
    title                VARCHAR(255),
    body                 TEXT,
    status               marketplace.review_status NOT NULL DEFAULT 'published',
    is_verified_purchase BOOLEAN                  NOT NULL DEFAULT FALSE,
    helpful_count        INTEGER                  NOT NULL DEFAULT 0,
    created_at           TIMESTAMPTZ              NOT NULL DEFAULT NOW(),
    updated_at           TIMESTAMPTZ              NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_reviews_buyer_product UNIQUE (buyer_id, product_id),
    CONSTRAINT fk_rev_product FOREIGN KEY (product_id) REFERENCES marketplace.products(id) ON DELETE CASCADE,
    CONSTRAINT fk_rev_buyer   FOREIGN KEY (buyer_id)   REFERENCES marketplace.users(id)    ON DELETE CASCADE,
    CONSTRAINT fk_rev_order   FOREIGN KEY (order_id)   REFERENCES marketplace.orders(id)   ON DELETE SET NULL,
    CONSTRAINT chk_rev_rating  CHECK (rating BETWEEN 1 AND 5),
    CONSTRAINT chk_rev_helpful CHECK (helpful_count >= 0)
);

COMMENT ON TABLE  marketplace.reviews                      IS 'Reseñas de productos. Un comprador puede reseñar un producto una sola vez.';
COMMENT ON COLUMN marketplace.reviews.is_verified_purchase IS 'TRUE cuando order_id apunta a un pedido delivered/completed del mismo comprador.';

CREATE INDEX IF NOT EXISTS idx_reviews_product ON marketplace.reviews (product_id);
CREATE INDEX IF NOT EXISTS idx_reviews_buyer   ON marketplace.reviews (buyer_id);
CREATE INDEX IF NOT EXISTS idx_reviews_rating  ON marketplace.reviews (product_id, rating);
CREATE INDEX IF NOT EXISTS idx_reviews_status  ON marketplace.reviews (status)
    WHERE status = 'published';

CREATE OR REPLACE TRIGGER trg_reviews_updated_at
    BEFORE UPDATE ON marketplace.reviews
    FOR EACH ROW EXECUTE FUNCTION marketplace.fn_set_updated_at();

-- ────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS marketplace.review_replies (
    id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    review_id   UUID        NOT NULL,
    producer_id UUID        NOT NULL,
    body        TEXT        NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_review_replies_review UNIQUE (review_id),
    CONSTRAINT fk_rr_review   FOREIGN KEY (review_id)   REFERENCES marketplace.reviews(id) ON DELETE CASCADE,
    CONSTRAINT fk_rr_producer FOREIGN KEY (producer_id) REFERENCES marketplace.users(id)   ON DELETE CASCADE
);

COMMENT ON TABLE  marketplace.review_replies             IS 'Respuesta del productor a una reseña (1:0|1). Solo una respuesta por reseña.';
COMMENT ON COLUMN marketplace.review_replies.producer_id IS 'FK a users.id del usuario con rol PRODUCER que responde.';

CREATE INDEX IF NOT EXISTS idx_review_replies_review   ON marketplace.review_replies (review_id);
CREATE INDEX IF NOT EXISTS idx_review_replies_producer ON marketplace.review_replies (producer_id);

CREATE OR REPLACE TRIGGER trg_review_replies_updated_at
    BEFORE UPDATE ON marketplace.review_replies
    FOR EACH ROW EXECUTE FUNCTION marketplace.fn_set_updated_at();

-- ============================================================
--  MÓDULO 7 — ADMINISTRACIÓN
-- ============================================================

-- ────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS marketplace.producer_approvals (
    id                     UUID                        PRIMARY KEY DEFAULT gen_random_uuid(),
    producer_id            UUID                        NOT NULL,
    producer_name_snapshot VARCHAR(255),
    farm_name_snapshot     VARCHAR(255),
    region                 VARCHAR(100),
    department             VARCHAR(100),
    hectares               NUMERIC(10,2),
    main_variety           VARCHAR(100),
    email                  VARCHAR(255)                NOT NULL,
    phone                  VARCHAR(20),
    status                 marketplace.producer_status NOT NULL DEFAULT 'pending',
    rejection_reason       TEXT,
    reviewed_by            UUID,
    reviewed_at            TIMESTAMPTZ,
    submitted_at           TIMESTAMPTZ                 NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_pa_producer    FOREIGN KEY (producer_id)  REFERENCES marketplace.users(id) ON DELETE CASCADE,
    CONSTRAINT fk_pa_reviewer    FOREIGN KEY (reviewed_by)  REFERENCES marketplace.users(id) ON DELETE SET NULL,
    CONSTRAINT chk_pa_hectares   CHECK (hectares IS NULL OR hectares > 0),
    CONSTRAINT chk_pa_review     CHECK (
        (status IN ('approved','rejected') AND reviewed_by IS NOT NULL AND reviewed_at IS NOT NULL)
        OR status = 'pending'
    )
);

COMMENT ON TABLE  marketplace.producer_approvals             IS 'Solicitudes de aprobación de productores con snapshot de datos al momento del envío.';
COMMENT ON COLUMN marketplace.producer_approvals.producer_id IS 'FK al usuario que solicita ser productor.';

CREATE INDEX IF NOT EXISTS idx_pa_producer ON marketplace.producer_approvals (producer_id);
CREATE INDEX IF NOT EXISTS idx_pa_status   ON marketplace.producer_approvals (status);
CREATE INDEX IF NOT EXISTS idx_pa_date     ON marketplace.producer_approvals (submitted_at DESC);

-- ────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS marketplace.approval_documents (
    id          UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    approval_id UUID         NOT NULL,
    name        VARCHAR(255) NOT NULL,
    type        VARCHAR(50),
    url         VARCHAR(500) NOT NULL,
    uploaded_at TIMESTAMPTZ  NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_ad_approval FOREIGN KEY (approval_id)
        REFERENCES marketplace.producer_approvals(id) ON DELETE CASCADE
);

COMMENT ON TABLE marketplace.approval_documents IS 'Documentos adjuntos a la solicitud de aprobación del productor.';

CREATE INDEX IF NOT EXISTS idx_approval_docs_approval ON marketplace.approval_documents (approval_id);

-- ────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS marketplace.admin_activity_log (
    id                  UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    actor_id            UUID,
    actor_name_snapshot VARCHAR(255),
    type                VARCHAR(50) NOT NULL,
    title               VARCHAR(255) NOT NULL,
    description         TEXT,
    severity            VARCHAR(10)  NOT NULL DEFAULT 'info',
    icon_emoji          VARCHAR(10),
    metadata            JSONB        NOT NULL DEFAULT '{}',
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_aal_actor  FOREIGN KEY (actor_id) REFERENCES marketplace.users(id) ON DELETE SET NULL,
    CONSTRAINT chk_aal_severity CHECK (severity IN ('info','warning','critical'))
);

COMMENT ON TABLE  marketplace.admin_activity_log          IS 'Registro de actividad del panel admin. Solo INSERT (inmutable).';
COMMENT ON COLUMN marketplace.admin_activity_log.type     IS 'Categoría: user_action, product_action, order_action, system.';
COMMENT ON COLUMN marketplace.admin_activity_log.metadata IS 'Payload JSON con datos adicionales de la acción.';

CREATE INDEX IF NOT EXISTS idx_aal_actor    ON marketplace.admin_activity_log (actor_id);
CREATE INDEX IF NOT EXISTS idx_aal_type     ON marketplace.admin_activity_log (type);
CREATE INDEX IF NOT EXISTS idx_aal_severity ON marketplace.admin_activity_log (severity)
    WHERE severity IN ('warning','critical');
CREATE INDEX IF NOT EXISTS idx_aal_created  ON marketplace.admin_activity_log (created_at DESC);
CREATE INDEX IF NOT EXISTS idx_aal_metadata ON marketplace.admin_activity_log USING gin (metadata);

-- ────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS marketplace.notifications (
    id         UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id    UUID        NOT NULL,
    type       VARCHAR(50) NOT NULL,
    message    TEXT        NOT NULL,
    is_read    BOOLEAN     NOT NULL DEFAULT FALSE,
    metadata   JSONB       NOT NULL DEFAULT '{}',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_notif_user FOREIGN KEY (user_id) REFERENCES marketplace.users(id) ON DELETE CASCADE
);

COMMENT ON TABLE  marketplace.notifications          IS 'Notificaciones in-app por usuario.';
COMMENT ON COLUMN marketplace.notifications.type     IS 'order_update | review_reply | approval_result | system.';
COMMENT ON COLUMN marketplace.notifications.metadata IS 'Datos adicionales: { orderId, productId, ... }.';

CREATE INDEX IF NOT EXISTS idx_notifications_user_unread
    ON marketplace.notifications (user_id, created_at DESC)
    WHERE is_read = FALSE;

CREATE INDEX IF NOT EXISTS idx_notifications_user
    ON marketplace.notifications (user_id, created_at DESC);

-- ────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS marketplace.audit_logs (
    id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID,
    action      VARCHAR(50) NOT NULL,
    entity_type VARCHAR(100),
    entity_id   VARCHAR(255),
    ip_address  INET,
    metadata    JSONB       NOT NULL DEFAULT '{}',
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_al_user FOREIGN KEY (user_id) REFERENCES marketplace.users(id) ON DELETE SET NULL
);

COMMENT ON TABLE  marketplace.audit_logs             IS 'Log de auditoría de acciones del sistema. Solo INSERT (inmutable). Retención: 2 años.';
COMMENT ON COLUMN marketplace.audit_logs.action      IS 'LOGIN | LOGOUT | CREATE | UPDATE | DELETE | VIEW.';
COMMENT ON COLUMN marketplace.audit_logs.entity_type IS 'Nombre de la tabla afectada: "products", "orders", etc.';

CREATE INDEX IF NOT EXISTS idx_audit_user    ON marketplace.audit_logs (user_id);
CREATE INDEX IF NOT EXISTS idx_audit_entity  ON marketplace.audit_logs (entity_type, entity_id);
CREATE INDEX IF NOT EXISTS idx_audit_action  ON marketplace.audit_logs (action);
CREATE INDEX IF NOT EXISTS idx_audit_created ON marketplace.audit_logs (created_at DESC);
CREATE INDEX IF NOT EXISTS idx_audit_ip      ON marketplace.audit_logs (ip_address);

-- ============================================================
--  ÍNDICES DE TEXTO LIBRE (pg_trgm)
-- ============================================================
CREATE INDEX IF NOT EXISTS idx_farms_name_trgm      ON marketplace.farms       USING gin (name gin_trgm_ops);
CREATE INDEX IF NOT EXISTS idx_categories_name_trgm ON marketplace.categories  USING gin (name gin_trgm_ops);

-- ============================================================
--  VISTAS
--  CREATE OR REPLACE VIEW es idempotente
-- ============================================================

CREATE OR REPLACE VIEW marketplace.v_products_available AS
SELECT
    p.id,
    p.name,
    p.price,
    p.original_price,
    p.discount_percent,
    p.region,
    p.emoji,
    p.sold_count,
    c.name        AS category_name,
    c.slug        AS category_slug,
    pp.id         AS producer_profile_id,
    u.full_name   AS producer_name,
    f.name        AS farm_name,
    f.department,
    i.quantity    AS stock
FROM      marketplace.products           p
JOIN      marketplace.categories         c   ON c.id  = p.category_id
JOIN      marketplace.producer_profiles  pp  ON pp.id = p.producer_id
JOIN      marketplace.users              u   ON u.id  = pp.user_id
LEFT JOIN marketplace.farms              f   ON f.producer_id = pp.id
LEFT JOIN marketplace.inventory          i   ON i.product_id  = p.id
WHERE p.status  = 'active'
  AND pp.status = 'approved'
  AND (i.quantity IS NULL OR i.quantity > 0);

COMMENT ON VIEW marketplace.v_products_available IS 'Productos activos de productores aprobados con stock > 0.';

-- ────────────────────────────────────────────────────────────
CREATE OR REPLACE VIEW marketplace.v_order_summary AS
SELECT
    o.id,
    o.code,
    o.status,
    o.total_amount,
    o.created_at,
    u.full_name   AS buyer_name,
    u.email       AS buyer_email,
    op.status     AS payment_status,
    op.proof_url  AS payment_proof,
    COUNT(oi.id)  AS item_count
FROM      marketplace.orders         o
JOIN      marketplace.users          u   ON u.id      = o.buyer_id
LEFT JOIN marketplace.order_payments op  ON op.order_id = o.id
LEFT JOIN marketplace.order_items    oi  ON oi.order_id = o.id
GROUP BY o.id, o.code, o.status, o.total_amount, o.created_at,
         u.full_name, u.email, op.status, op.proof_url;

COMMENT ON VIEW marketplace.v_order_summary IS 'Resumen de pedidos con estado de pago e información del comprador.';

-- ============================================================
--  DATOS SEMILLA (idempotentes con ON CONFLICT DO NOTHING)
-- ============================================================

-- Roles
INSERT INTO marketplace.roles (name, description) VALUES
    ('BUYER',    'Comprador — acceso al catálogo, carrito y pedidos'),
    ('PRODUCER', 'Productor — gestión de productos y finca'),
    ('ADMIN',    'Administrador — acceso total al panel de control')
ON CONFLICT (name) DO NOTHING;

-- Certifications
INSERT INTO marketplace.certifications (code, name, issuing_body, description) VALUES
    ('RA',   'Rainforest Alliance',    'Rainforest Alliance',    'Certificación de sostenibilidad ambiental y social'),
    ('FT',   'Fair Trade',             'Fairtrade International', 'Comercio justo con condiciones dignas para productores'),
    ('ORG',  'Orgánico USDA',          'USDA / NOP',             'Producción sin pesticidas ni fertilizantes sintéticos'),
    ('4C',   '4C Verified',            '4C Association',         'Código común para la comunidad del café'),
    ('UTZ',  'UTZ Certified',          'Rainforest Alliance',    'Prácticas agrícolas sostenibles — absorbido por RA'),
    ('BAP',  'Bird Friendly',          'Smithsonian SMBC',       'Café de sombra con hábitat para aves migratorias'),
    ('CAFE', 'C.A.F.E. Practices',     'Starbucks / SCS Global', 'Estándares de adquisición de café Starbucks'),
    ('DOP',  'Denominación de Origen', 'SIC Colombia',           'Reconocimiento geográfico de origen cafetero colombiano')
ON CONFLICT (code) DO NOTHING;

-- Roast levels
INSERT INTO marketplace.roast_levels (code, name, description, icon) VALUES
    ('LIGHT',        'Claro',        'Notas frutales y alta acidez; temp. 196-205 °C',       '☀️'),
    ('MEDIUM_LIGHT', 'Medio Claro',  'Balance entre acidez y cuerpo; temp. 210 °C',           '🌤️'),
    ('MEDIUM',       'Medio',        'Perfil equilibrado; caramelización moderada; 220 °C',   '⚖️'),
    ('MEDIUM_DARK',  'Medio Oscuro', 'Mayor cuerpo, menor acidez; ligero aceite; 225 °C',     '🌙'),
    ('DARK',         'Oscuro',       'Notas achocolatadas/ahumadas; alta amargura; 230+ °C',  '🌑')
ON CONFLICT (code) DO NOTHING;

-- Shipping options
INSERT INTO marketplace.shipping_options (id, name, delivery_window, price, display_order) VALUES
    ('standard', 'Envío Estándar',       '5-7 días hábiles',          9900,  1),
    ('express',  'Envío Express',        '1-2 días hábiles',          19900, 2),
    ('pickup',   'Recogida en Finca',    'Coordinar con productor',   0,     3)
ON CONFLICT (id) DO NOTHING;

-- Payment methods
INSERT INTO marketplace.payment_methods
    (code, name, type, account_number, account_holder, alias, nit, emoji, accent_color, display_order)
VALUES
    ('nequi',       'Nequi',       'digital_wallet', '3148654210',    'World Coffee Marketplace SAS', NULL,       NULL,           '📱', '#6C0E99', 1),
    ('bancolombia', 'Bancolombia', 'bank_transfer',  '421-654321-12', 'World Coffee Marketplace SAS', NULL,       '900.542.310-7','🏦', '#FDBD00', 2),
    ('daviplata',   'Daviplata',   'digital_wallet', '3148654210',    'World Coffee Marketplace SAS', NULL,       NULL,           '💜', '#E11E8E', 3),
    ('breb',        'BRE-B',       'bre_b',          NULL,            'World Coffee Marketplace SAS', 'wcm.pagos','900.542.310-7','⚡', '#0057A8', 4)
ON CONFLICT (code) DO NOTHING;

-- Categories raíz
INSERT INTO marketplace.categories (name, slug, description, icon_emoji) VALUES
    ('Café Especial',     'cafe-especial',   'Cafés con puntaje SCA ≥ 80',            '☕'),
    ('Café Orgánico',     'cafe-organico',   'Producción sin agroquímicos',            '🌿'),
    ('Café de Origen',    'cafe-de-origen',  'Single origin de regiones colombianas',  '🗺️'),
    ('Café Sostenible',   'cafe-sostenible', 'Certificaciones ambientales y sociales', '🌍'),
    ('Kits y Accesorios', 'kits-accesorios', 'Equipos y herramientas de preparación',  '🛠️')
ON CONFLICT (slug) DO NOTHING;

-- ────────────────────────────────────────────────────────────
--  USUARIOS DE PRUEBA
--  ON CONFLICT (email) DO NOTHING garantiza idempotencia
--  NOTA: passwords son bcrypt de "Cafe#2025" (salt 12).
--        En producción, generarlos con la aplicación.
-- ────────────────────────────────────────────────────────────
DO $$
DECLARE
    v_buyer_id         UUID;
    v_producer_id      UUID;
    v_admin_id         UUID;
    v_buyer_role       INTEGER;
    v_prod_role        INTEGER;
    v_admin_role       INTEGER;
    v_prod_profile_id  UUID;
    v_cat_id           UUID;
BEGIN
    -- Recuperar roles
    SELECT id INTO v_buyer_role FROM marketplace.roles WHERE name = 'BUYER';
    SELECT id INTO v_prod_role  FROM marketplace.roles WHERE name = 'PRODUCER';
    SELECT id INTO v_admin_role FROM marketplace.roles WHERE name = 'ADMIN';

    -- ── Comprador ────────────────────────────────────────────
    INSERT INTO marketplace.users (email, password_hash, full_name, phone, privacy_consent)
    VALUES ('buyer@wcm.co', '$2b$12$PLACEHOLDER_BUYER_HASH', 'María García', '3001234567', TRUE)
    ON CONFLICT (email) DO NOTHING;

    SELECT id INTO v_buyer_id FROM marketplace.users WHERE email = 'buyer@wcm.co';

    INSERT INTO marketplace.user_roles (user_id, role_id)
    VALUES (v_buyer_id, v_buyer_role)
    ON CONFLICT (user_id, role_id) DO NOTHING;

    INSERT INTO marketplace.buyer_profiles
        (user_id, city, department, newsletter_opt_in, avatar_initials)
    VALUES (v_buyer_id, 'Bogotá', 'Cundinamarca', TRUE, 'MG')
    ON CONFLICT (user_id) DO NOTHING;

    INSERT INTO marketplace.addresses
        (user_id, label, line1, city, department, is_default)
    VALUES (v_buyer_id, 'Casa', 'Calle 72 # 10-45', 'Bogotá', 'Cundinamarca', TRUE)
    ON CONFLICT DO NOTHING;

    INSERT INTO marketplace.carts (user_id)
    VALUES (v_buyer_id)
    ON CONFLICT (user_id) DO NOTHING;

    -- ── Admin (creado antes del productor para usarlo como aprobador) ──
    INSERT INTO marketplace.users (email, password_hash, full_name, phone, privacy_consent)
    VALUES ('admin@wcm.co', '$2b$12$PLACEHOLDER_ADMIN_HASH', 'Admin WCM', NULL, TRUE)
    ON CONFLICT (email) DO NOTHING;

    SELECT id INTO v_admin_id FROM marketplace.users WHERE email = 'admin@wcm.co';

    INSERT INTO marketplace.user_roles (user_id, role_id)
    VALUES (v_admin_id, v_admin_role)
    ON CONFLICT (user_id, role_id) DO NOTHING;

    -- ── Productor ────────────────────────────────────────────
    INSERT INTO marketplace.users (email, password_hash, full_name, phone, privacy_consent)
    VALUES ('producer@wcm.co', '$2b$12$PLACEHOLDER_PRODUCER_HASH', 'Carlos Ramírez', '3109876543', TRUE)
    ON CONFLICT (email) DO NOTHING;

    SELECT id INTO v_producer_id FROM marketplace.users WHERE email = 'producer@wcm.co';

    INSERT INTO marketplace.user_roles (user_id, role_id)
    VALUES (v_producer_id, v_prod_role)
    ON CONFLICT (user_id, role_id) DO NOTHING;

    INSERT INTO marketplace.producer_profiles
        (user_id, bio, city, department, status, approved_by, approved_at, avatar_initials)
    VALUES (v_producer_id,
            'Caficultor con 20 años de experiencia en el Huila.',
            'Pitalito', 'Huila', 'approved', v_admin_id, NOW(), 'CR')
    ON CONFLICT (user_id) DO NOTHING;

    SELECT id INTO v_prod_profile_id
    FROM marketplace.producer_profiles
    WHERE user_id = v_producer_id;

    -- Finca
    INSERT INTO marketplace.farms
        (producer_id, name, municipality, department,
         altitude_masl, area_hectares, main_variety, process, cupping_score, description)
    VALUES (v_prod_profile_id,
            'Finca La Esperanza', 'Pitalito', 'Huila',
            1750, 12.5, 'Geisha', 'Washed', 87.50,
            'Finca familiar en las montañas del sur del Huila.')
    ON CONFLICT (producer_id) DO NOTHING;

    -- Carrito del productor
    INSERT INTO marketplace.carts (user_id)
    VALUES (v_producer_id)
    ON CONFLICT (user_id) DO NOTHING;

    -- Producto de ejemplo
    SELECT id INTO v_cat_id
    FROM marketplace.categories WHERE slug = 'cafe-especial' LIMIT 1;

    IF v_cat_id IS NOT NULL AND v_prod_profile_id IS NOT NULL THEN
        INSERT INTO marketplace.products
            (producer_id, category_id, name, description,
             price, original_price, discount_percent,
             unit, region, emoji, sold_count, status)
        VALUES (v_prod_profile_id, v_cat_id,
                'Geisha Washed Huila',
                'Café especial de altura con notas florales y cítricas.',
                68000, 75000, 9.33, '250g', 'Huila', '☕', 42, 'active')
        ON CONFLICT DO NOTHING;
    END IF;
END;
$$;

-- ============================================================
--  FIN DEL SCRIPT
-- ============================================================
-- Esquema:   marketplace
-- Tablas:    39
-- Enums:     7  (marketplace.user_status, producer_status, order_status,
--                payment_status, review_status, coupon_discount_type, doc_status)
-- Índices:   56  (todos IF NOT EXISTS, incluyendo GIN pg_trgm y parciales)
-- Vistas:    2   (marketplace.v_products_available, v_order_summary)
-- Triggers:  8   (CREATE OR REPLACE — updated_at en users, buyer_profiles,
--                 producer_profiles, farms, carts, products, reviews,
--                 review_replies)
-- Funciones: 2   (marketplace.fn_set_updated_at, fn_next_order_seq)
-- Seed:      roles (3), certifications (8), roast_levels (5),
--            shipping_options (3), payment_methods (4),
--            categories (5), usuarios de prueba (3)
-- Idempotencia:
--   • SCHEMA:      CREATE SCHEMA IF NOT EXISTS
--   • TYPES:       DO $$ ... EXCEPTION WHEN duplicate_object THEN NULL; END $$
--   • FUNCTIONS:   CREATE OR REPLACE FUNCTION
--   • TABLES:      CREATE TABLE IF NOT EXISTS
--   • INDEXES:     CREATE [UNIQUE] INDEX IF NOT EXISTS
--   • TRIGGERS:    CREATE OR REPLACE TRIGGER  (PostgreSQL 14+)
--   • VIEWS:       CREATE OR REPLACE VIEW
--   • SEED INSERT: ON CONFLICT (...) DO NOTHING
-- ============================================================
