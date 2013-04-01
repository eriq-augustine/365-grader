--- Lab 5 Marathon dataset

--- query 1
select agegroup, sex, count(*) as Total, min(place) as BEST_PLACE,
                 max(place) as WORST_PLACE
from Marathon
group by agegroup, sex
order by agegroup, sex
;

--- query 2
select count(*)
from Marathon m1, Marathon m2
where m1.sex = m2.sex and
      m1.agegroup = m2.agegroup and
      m1.groupplace = 1 and
      m2.groupplace = 2 and
      m1.state = m2.state
;
