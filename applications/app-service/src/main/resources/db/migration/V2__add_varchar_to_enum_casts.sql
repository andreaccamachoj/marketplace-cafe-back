-- Allow R2DBC String binding for PostgreSQL custom enum columns.
-- Without these, parameterized queries bind varchar values and PostgreSQL
-- rejects them because there is no implicit cast from varchar to user-defined enums.
CREATE CAST (character varying AS marketplace.user_status)         WITH INOUT AS IMPLICIT;
CREATE CAST (character varying AS marketplace.producer_status)     WITH INOUT AS IMPLICIT;
CREATE CAST (character varying AS marketplace.order_status)        WITH INOUT AS IMPLICIT;
CREATE CAST (character varying AS marketplace.payment_status)      WITH INOUT AS IMPLICIT;
CREATE CAST (character varying AS marketplace.review_status)       WITH INOUT AS IMPLICIT;
CREATE CAST (character varying AS marketplace.coupon_discount_type) WITH INOUT AS IMPLICIT;
CREATE CAST (character varying AS marketplace.doc_status)          WITH INOUT AS IMPLICIT;
