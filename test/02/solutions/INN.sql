---
--- CSC 365.Fall 2012. Lab 5
---

--- Alex Dekhtyar

--- INN Dataset


--- query 1
select rm.roomname, sum(rate* (checkout-checkin)) as REVENUE, 
       sum(rate*(checkout-checkin))/count(*) as AVERAGE
from Reservations r, Rooms rm
where rm.roomcode = r.room and r.checkin >= '01-SEP-2010' and
      r.checkin <= '30-NOV-2010'
group by rm.roomcode, rm.roomname
order by REVENUE DESC
;

--- query 2
select rm.roomname, max(rate-baseprice) AS MARKUP, 
                    min(rate-baseprice) AS DISCOUNT
from Rooms rm, Reservations r
where rm.roomcode = r.room
group by rm.roomname
order by MARKUP DESC;
