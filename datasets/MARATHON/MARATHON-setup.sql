CREATE TABLE Marathon (
   place INT PRIMARY KEY,
   time TIME,
   pace TIME,
   groupPlace INT,
   ageGroup CHAR(8),
   age INT,
   -- The CHECK constraint is parsed, but ignored in most MySQL engines.
   sex CHAR(1) CHECK(sex = 'M' OR sex = 'F'),
   bibNumber INT,
   firstName VARCHAR(20),
   lastName VARCHAR(20),
   town VARCHAR(20),
   state CHAR(2)
);  
