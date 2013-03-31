CREATE TABLE Appellations (
   id INT,
   appellation VARCHAR(50) UNIQUE,
   county VARCHAR(16),
   state VARCHAR(16),
   area VARCHAR(24),
   isAVA CHAR(3),
   PRIMARY KEY(id)
);

CREATE TABLE Grapes (
   id INT PRIMARY KEY,
   grape VARCHAR(24) UNIQUE,
   color VARCHAR(10)
);

CREATE TABLE Wine (
  wineId INT PRIMARY KEY,
  grape VARCHAR(24),
  winery VARCHAR(48),
  appellation VARCHAR(50),
  name VARCHAR(50),
  vintage INT,
  price FLOAT,
  score INT,
  cases INT,
  CONSTRAINT WINE_GRAPE FOREIGN KEY(grape) REFERENCES Grapes(grape),
  CONSTRAINT WINE_AVA FOREIGN KEY(appellation) REFERENCES Appellations(appellation)
);
