CREATE TABLE Continents (
   id INT PRIMARY KEY,
   name VARCHAR(15)
);

CREATE TABLE Countries (
   id INT PRIMARY KEY,
   name VARCHAR(20),
   continent INT REFERENCES Continents
);

CREATE TABLE CarMakers (
   id INT PRIMARY KEY,
   maker VARCHAR(15),
   fullName VARCHAR(25),
   country INT REFERENCES Countries
);

CREATE TABLE Models (
   id INT PRIMARY KEY,
   maker INT REFERENCES CarMakers,
   model VARCHAR(15)
);

CREATE TABLE Makes (
   id INT PRIMARY KEY,
   model VARCHAR(15),
   make VARCHAR(60)
);

CREATE TABLE CarData (
   id INT PRIMARY KEY REFERENCES Makes,
   mpg FLOAT,
   cylinders INT,
   eDispl INT,
   horsepower INT,
   weight INT,
   accelerate FLOAT,
   year INT
);
