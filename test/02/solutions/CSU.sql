--- Lab 5 CSU dataset

--- query 1
select c.campus, sum(f.Fee) as SUM_FEE
from Campuses c, Fees f
where id = campusid and f.year >=2000 and f.year <= 2005
group by c.id, c.campus
having AVG(f.fee) > 2500
order by SUM_FEE
;


--- query 2
select Campus, MIN(enrolled) as MINIMUM, AVG(enrolled) as AVERAGE, MAX(enrolled) as MAXIMUM
from Campuses c, Enrollments e
where c.id = e.campusid
group by id, campus
having count(*) >60
order by AVERAGE;


--- query 3
select c.campus, sum(Degrees) as "Total Degrees"
from Campuses c, Degrees d
where c.id = d.campusid and d.year >= 1998 and d.year<=2002
 and county in ('Los Angeles', 'Orange')
group by c.id, c.campus
order by "Total Degrees" DESC
;

--- query 4
select c.campus, count(*)
from Campuses c, DisciplineEnrollments d, Enrollments e
where c.id = d.campusid and d.graduate > 0 and e.campusid = c.id and
      e.enrolled > 20000 and e.year = 2004
group by c.campus
order by c.campus
;



--- query 5
select e.year, min(e.fte/f.fte) as BEST,
       AVG(e.fte/f.fte) as AVERAGE,
       MAX(e.fte/f.fte) as WORST
from Campuses c, Enrollments e, Faculty f
where f.campusid = c.id and e.campusid = c.id and
      e.year = f.year
group by e.year
order by e.year
;












