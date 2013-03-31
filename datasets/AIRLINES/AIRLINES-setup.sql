CREATE TABLE Airlines (
   id INT PRIMARY KEY,
   name VARCHAR(30),
   abbreviation VARCHAR(15),
   country VARCHAR(30)
);

CREATE TABLE Airports (
  city VARCHAR(50),
  code Char(3) PRIMARY KEY,
  name VARCHAR(60),
  country VARCHAR(30),
  countryAbbreviation VARCHAR(15)
);

CREATE TABLE Flights (
  airline INT REFERENCES Airlines,
  flightNo INT,
  source CHAR(3) REFERENCES Airports,
  destination CHAR(3) REFERENCES Airports,
  Primary Key(Airline, FlightNo)
);
