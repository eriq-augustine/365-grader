---
--- CSC 365.Fall 2012. Lab 5
---

--- Alex Dekhtyar

--- INN Dataset


--- query 1
select rm.roomname, sum(rate* (checkout-checkin)) as REVENUE, 
       sum(rate*(checkout-checkin))/count(*) as AVERAGE
from reservations r, rooms rm
where rm.roomcode = r.room and r.checkin >= '01-SEP-2010' and
      r.checkin <= '30-NOV-2010'
group by rm.roomcode, rm.roomname
order by REVENUE DESC
;


--- query 2
select count(*) as STAYS, sum(rate*(checkout-checkin)) as REVENUE
from reservations
where MOD(checkin - to_date('01-JAN-10','DD-MON-YY'), 7) = 0
;


--- query 3
select MOD(checkin - to_date('28-DEC-09','DD-MON-YY'), 7)+1 as DAY, count(*) as STAYS, sum(rate*(checkout-checkin)) as REVENUE
from reservations
group by MOD(checkin - to_date('28-DEC-09','DD-MON-YY'), 7)+1
order by day
;


--- query 4
select rm.roomname, max(rate-baseprice) AS MARKUP, 
                    min(rate-baseprice) AS DISCOUNT
from rooms rm, reservations r
where rm.roomcode = r.room
group by rm.roomname
order by MARKUP DESC;



--- query 5
select m.roomcode, m.roomname, SUM(checkout - checkin)- (MAX(CHECKOUT)-TO_DATE('31-DEC-10','DD-MON-YY'))*(SIGN(MAX(CHECKOUT)-TO_DATE('31-DEC-10','DD-MON-YY'))+1)/2
       as Days_Occupied
from  reservations r, rooms m
where r.room = m.roomcode
group by m.roomcode, m.roomname
order by Days_Occupied DESC
;


























