CREATE TABLE Rooms (
   roomCode CHAR(5) PRIMARY KEY,
   roomName VARCHAR(30) UNIQUE,
   beds INT,  -- number of beds
   bedType VARCHAR(8),
   maxOcc INT,  -- max occupancy
   basePrice FLOAT,
   decor VARCHAR(20)
);

CREATE TABLE Reservations (
  code INT,
  room CHAR(5),
  checkIn DATE,
  checkOut DATE,
  rate FLOAT,
  lastName VARCHAR(15),
  firstName VARCHAR(15),
  adults INT,
  kids INT,
  CONSTRAINT RES_PK PRIMARY KEY(code),
  CONSTRAINT RES_UNIQUE UNIQUE(room, checkIn),
  CONSTRAINT RES_ROOM FOREIGN KEY(room) REFERENCES Rooms(roomCode)
);
