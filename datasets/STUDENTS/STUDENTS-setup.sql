CREATE TABLE List (
   lastName VARCHAR(16),
   firstName VARCHAR(16),
   grade INT,
   classroom INT,
   CONSTRAINT LIST_PK PRIMARY KEY(FirstName, LastName)
);

CREATE TABLE Teachers (
   last VARCHAR(16),
   first VARCHAR(16),
   classroom INT,
   CONSTRAINT TEACHERS_PK PRIMARY KEY(classroom)
);
