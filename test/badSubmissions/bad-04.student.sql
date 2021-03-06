-- Lab 7

-- Q1
SELECT A100.AirportCode, A100.AirportName
FROM Flights F, Airports100 A100
WHERE F.SourceAirport = A100.AirportCode
GROUP BY A100.AirportCode, A100.AirportName
HAVING COUNT(*) = 19
ORDER BY 1;

-- Q2
SELECT COUNT(DISTINCT F.SourceAirport) NumAirports
FROM Flights F, Flights F2
WHERE F2.DestAirport = 'ASY' AND
      F.DestAirport = F2.SourceAirport AND
      F.SourceAirport != 'ASY';

-- Q3
SELECT AirCode AirportCode, COUNT(DISTINCT F.SourceAirport) NumAirports
FROM Flights F, Flights F2,(SELECT A100.AirportCode AirCode
                            FROM Flights F3, Airports100 A100
                            WHERE F3.SourceAirport = A100.AirportCode
                            GROUP BY A100.AirportCode
                            HAVING COUNT(*) = 19)
WHERE F2.DestAirport = AirCode AND
      F.DestAirport = F2.SourceAirport AND
      F.SourceAirport != AirCode
GROUP BY AirCode
ORDER BY 2 DESC;

-- Q4
SELECT COUNT(*) NumAirports
FROM
(SELECT F.SourceAirport
FROM Flights F, Flights F2
WHERE F2.DestAirport = 'ATE' AND
      F.DestAirport = F2.SourceAirport AND
      F.SourceAirport != 'ATE'
UNION
SELECT F3.SourceAirport
FROM Flights F3
WHERE F3.DestAirport = 'ATE');

-- ERROR: Query does not end with a semicolon.
-- Q5
SELECT A.Airline, COUNT(DISTINCT A100.AirportCode) TotalAirports
FROM Airlines A, Airports100 A100, Flights F
WHERE F.Airline = A.Id AND
      F.DestAirport = A100.AirportCode
GROUP BY A.Airline
ORDER BY 2 DESC
