CREATE TABLE policy (
    id                  UUID           NOT NULL,
    policy_number       VARCHAR(30)    NOT NULL,
    policyholder_name   VARCHAR(150)   NOT NULL,
    line_of_business    VARCHAR(20)    NOT NULL,
    status              VARCHAR(20)    NOT NULL,
    premium_amount      DECIMAL(15, 2) NOT NULL,
    currency            CHAR(3)        NOT NULL,
    effective_date      DATE           NOT NULL,
    expiry_date         DATE           NOT NULL,
    region              VARCHAR(50)    NOT NULL,
    underwriter         VARCHAR(100)   NOT NULL,
    flagged_for_review  BOOLEAN        DEFAULT FALSE NOT NULL,
    created_at          TIMESTAMP      NOT NULL,
    updated_at          TIMESTAMP      NOT NULL,

    CONSTRAINT pk_policy PRIMARY KEY (id),
    CONSTRAINT uk_policy_policy_number UNIQUE (policy_number),
    CONSTRAINT ck_policy_line_of_business CHECK (line_of_business IN
        ('PROPERTY', 'MOTOR', 'HEALTH', 'LIFE', 'TRAVEL', 'MARINE', 'LIABILITY', 'ACCIDENT')),
    CONSTRAINT ck_policy_status CHECK (status IN
        ('ACTIVE', 'PENDING', 'EXPIRED', 'CANCELLED', 'SUSPENDED')),
    CONSTRAINT ck_policy_premium_amount CHECK (premium_amount >= 0),
    CONSTRAINT ck_policy_dates CHECK (expiry_date >= effective_date)
);

CREATE INDEX idx_policy_policy_number  ON policy (policy_number);
CREATE INDEX idx_policy_status         ON policy (status);
CREATE INDEX idx_policy_region         ON policy (region);
CREATE INDEX idx_policy_effective_date ON policy (effective_date);
