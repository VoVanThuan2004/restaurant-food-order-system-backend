CREATE TABLE IF NOT EXISTS role (
    role_id VARCHAR(36) PRIMARY KEY,
    role_name VARCHAR(100) UNIQUE NOT NULL,
    description VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

CREATE TABLE IF NOT EXISTS permission (
    permission_id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(100) UNIQUE NOT NULL,
    resource VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS role_permission (
    role_id VARCHAR(36) NOT NULL,
    permission_id VARCHAR(36) NOT NULL,

    PRIMARY KEY (role_id, permission_id),

    CONSTRAINT fk_role_permission_role
    FOREIGN KEY (role_id)
    REFERENCES role(role_id),

    CONSTRAINT fk_role_permission_permission
    FOREIGN KEY (permission_id)
    REFERENCES permission(permission_id)
);