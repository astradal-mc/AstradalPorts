-- Create table for Portstones
CREATE TABLE IF NOT EXISTS portstones (
    id TEXT PRIMARY KEY,
    type TEXT NOT NULL,
    world TEXT NOT NULL,
    x REAL NOT NULL,
    y REAL NOT NULL,
    z REAL NOT NULL,
    town TEXT,
    nation TEXT,
    name TEXT NOT NULL,
    fee REAL DEFAULT 0,
    icon TEXT,
    enabled BOOLEAN NOT NULL DEFAULT 1
);

-- Create table for Cooldowns
CREATE TABLE IF NOT EXISTS cooldowns (
    player_uuid TEXT NOT NULL,
    type TEXT NOT NULL,
    last_use_ms INTEGER NOT NULL,
    PRIMARY KEY(player_uuid, type)
);

-- Create table for Holograms
CREATE TABLE IF NOT EXISTS holograms (
    portstone_id TEXT NOT NULL,
    entity_uuid TEXT NOT NULL,
    PRIMARY KEY(portstone_id)
);
