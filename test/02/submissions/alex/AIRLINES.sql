--- Lab 5 Airlines dataset

--- query 1
select a.code, a.name
from Airports a, Flights f
where f.source= a.code
group by a.code, a.name
having count(*) = 19
order by a.code
;


--- query 2
select count(distinct f1.source)
from  Flights f1, Flights f2
where f2.destination = 'ASY' and
      f1.destination = f2.source and
      f1.source != 'ASY'
;

--- query 3
select f2.destination, count(distinct f1.source) as REACHABLE
from Flights f1, Flights f2
where f1.destination = f2.source and
      f1.source <> f2.destination 
 ---     and f2.destination = 'ASY'
group by f2.destination
having count(distinct f2.airline*f2.flightno) = 19
order by REACHABLE DESC
;



--- query 4
select count(distinct f1.source)
from  Flights f1, Flights f2
where (f2.destination = 'ATE' and
      f1.destination = f2.source and
      f1.source != 'ATE') OR
      (f1.destination = 'ATE' and
       f1.airline = f2.airline and f1.flightno=f2.flightno)
;


--- query 5
select a.name, count(distinct f.source) AS "Operational Airports"
from Airlines a, Airports p, Flights f
where f.airline = a.id and f.source = p.code
group by a.id, a.name, a.abbreviation
order by "Operational Airports" DESC
;






