-- Table images {
--   id SERIAL [pk, increment]
--   created_at TIMESTAMP
--   updated_at TIMESTAMP
--   url VARCHAR(200)
--   label VARCHAR(200)
--   hash VARHCHAR(200)
-- }

-- Table objects {
--   id SERIAL [pk, increment]
--   name VARCHAR(200)
-- }

-- Table tags {
--   id SERIAL [pk, increment]
--   image_id INTEGER [ref: > images.id]
--   object_id INTEGER [ref: > objects.id]
--   confidence DOUBLE
-- }

CREATE TABLE images(
	id SERIAL PRIMARY KEY,
	created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
	updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
	url VARCHAR(200) NOT NULL UNIQUE,
	label VARCHAR(128) NOT NULL,
	object_detection_enabled BOOL NOT NULL,
	hash VARCHAR(64) NOT NULL DEFAULT '',
	objects VARCHAR
);

CREATE TABLE objects (
	id SERIAL PRIMARY KEY,
	name VARCHAR(128) NOT NULL UNIQUE
);

CREATE TABLE tags (
	id SERIAL PRIMARY KEY,
	image_id INTEGER NOT NULL REFERENCES images(id) ON DELETE CASCADE,
	object_id INTEGER NOT NULL REFERENCES objects(id) ON DELETE CASCADE,
	confidence DOUBLE PRECISION,
	UNIQUE(image_id, object_id)
);
