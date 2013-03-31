CREATE TABLE Campuses (
   id INT,
   campus VARCHAR(60),
   location VARCHAR(30),
   county VARCHAR(20),
   year INT,
   PRIMARY KEY(Id)
);

CREATE TABLE Fees (
   campusId INT,
   year INT,
   fee INT,
   PRIMARY KEY(year, campusId),
   FOREIGN KEY(campusId) REFERENCES Campuses(id)
);

CREATE TABLE Degrees (
   year INT,
   campusId INT,
   degrees INT,
   PRIMARY KEY(year, campusId),
   FOREIGN KEY(campusId) REFERENCES Campuses(id)
);

CREATE TABLE Disciplines (
   id INT PRIMARY KEY,
   name VARCHAR(40)
);

CREATE TABLE DisciplineEnrollments (
   campusId INT REFERENCES Campuses(id),
   discipline INT REFERENCES Disciplines(id),
   year INT,
   undergrad INT,
   graduate INT,
   PRIMARY KEY(campusId, discipline, year)
);

CREATE TABLE Enrollments (
   campusId INT REFERENCES Campuses(id),
   year INT ,
   enrolled INT,
   fte FLOAT,
   PRIMARY KEY(campusId, year)
);

CREATE TABLE Faculty (
   campusId INT REFERENCES Campuses(id),
   year INT,
   fte FLOAT,
   PRIMARY KEY(campusId, year)
);
