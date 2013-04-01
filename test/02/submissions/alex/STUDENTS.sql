--- Fall 2012 Lab 5 STUDENTS dataset

--- query 1
select t.first, t.last
from  List l, Teachers t
where l.classroom = t.classroom
group by t.first, t.last
having count(*) >=3 and count(*)<= 5
order by t.last
;

--- query 2
select grade, count(distinct classroom) AS NCLASSES, count(*)
from List
group by grade
order by NCLASSES DESC
;




--- query 3
select classroom, count(*) as CN
from List
where grade = 4
group by classroom
order by CN DESC
;


--- query 4
select classroom, min(lastname)
from List
where grade =0
group by classroom
order by classroom
;
