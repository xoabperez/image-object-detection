CREATE OR REPLACE FUNCTION get_object_id(_object varchar(128), OUT _object_id int)
  LANGUAGE plpgsql AS
$func$
BEGIN
   LOOP
      SELECT id
      FROM   objects
      WHERE  name = _object
      INTO   _object_id;

      EXIT WHEN FOUND;

      INSERT INTO objects AS o (name)
      VALUES (_object)
      ON     CONFLICT (name) DO NOTHING
      RETURNING o.id
      INTO   _object_id;

      EXIT WHEN FOUND;
   END LOOP;
END
$func$;