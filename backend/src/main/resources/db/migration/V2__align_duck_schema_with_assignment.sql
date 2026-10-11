-- V1 was created for an early prototype (name/breed). Those fields do not map
-- reliably to the assignment's color/size/price model. Preserve legacy rows for audit,
-- but soft-delete them so invented placeholder attributes are never sold as real stock.
ALTER TABLE ducks
    ADD COLUMN color VARCHAR(10) NOT NULL DEFAULT 'RED',
    ADD COLUMN size VARCHAR(10) NOT NULL DEFAULT 'MEDIUM',
    ADD COLUMN price NUMERIC(12, 2) NOT NULL DEFAULT 0.01,
    ADD COLUMN deleted BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN version BIGINT NOT NULL DEFAULT 0;

UPDATE ducks SET deleted = TRUE, quantity = GREATEST(quantity, 1);

ALTER TABLE ducks
    DROP COLUMN name,
    DROP COLUMN breed;

ALTER TABLE ducks
    ALTER COLUMN color DROP DEFAULT,
    ALTER COLUMN size DROP DEFAULT,
    ALTER COLUMN price DROP DEFAULT,
    ALTER COLUMN deleted DROP DEFAULT,
    ALTER COLUMN version DROP DEFAULT;

ALTER TABLE ducks
    ADD CONSTRAINT ck_ducks_color
        CHECK (color IN ('RED', 'GREEN', 'YELLOW', 'BLACK')),
    ADD CONSTRAINT ck_ducks_size
        CHECK (size IN ('XLARGE', 'LARGE', 'MEDIUM', 'SMALL', 'XSMALL')),
    ADD CONSTRAINT ck_ducks_price_positive CHECK (price > 0),
    ADD CONSTRAINT ck_ducks_quantity_positive CHECK (quantity > 0);

-- The partial unique index lets a soft-deleted item be re-created while ensuring
-- there is only one active inventory row for a given color/size/price tuple.
CREATE UNIQUE INDEX ux_ducks_active_color_size_price
    ON ducks (color, size, price)
    WHERE deleted = FALSE;

CREATE INDEX ix_ducks_active_quantity
    ON ducks (quantity, id)
    WHERE deleted = FALSE;
