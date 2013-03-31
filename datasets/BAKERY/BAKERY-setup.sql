CREATE TABLE Customers (
   cId INT,
   lastName VARCHAR(20),
   firstName VARCHAR(20),
   CONSTRAINT C_PK PRIMARY KEY(cId),
   CONSTRAINT C_UNIQUE UNIQUE(lastName, firstName)
);

CREATE TABLE Goods (
   gId VARCHAR(15),
   flavor VARCHAR(20),
   food VARCHAR(20),
   pRICE FLOAT,
   CONSTRAINT G_PK PRIMARY KEY(gId),
   CONSTRAINT G_UNIQUE UNIQUE(flavor, food)
);

CREATE TABLE Receipts (
   rNumber INT,
   saleDate DATE,
   customer INT,
   CONSTRAINT R_PK PRIMARY KEY(rNumber),
   CONSTRAINT R_FK FOREIGN KEY(customer) REFERENCES Customers(cId)
);

CREATE TABLE Items (
  receipt INT,
  ordinal INT,
  item  VARCHAR(15),
  CONSTRAINT I_FK_G FOREIGN KEY(item) REFERENCES Goods(gId),
  CONSTRAINT I_FK_R FOREIGN KEY(receipt) REFERENCES Receipts(rNumber),
  CONSTRAINT I_PK PRIMARY KEY(receipt, ordinal)
);
