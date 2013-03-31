--- Lab 5 MARATHON dataset

--- query 1
select agegroup, sex, count(*) as Total, min(place) as BEST_PLACE, 
                 max(place) as WORST_PLACE
from marathon
group by agegroup, sex
order by agegroup, sex
;

--- query 2
select count(*)
from marathon m1, marathon m2
where m1.sex = m2.sex and
      m1.agegroup = m2.agegroup and
      m1.groupplace = 1 and 
      m2.groupplace = 2 and
      m1.state = m2.state
;

--- query 3
select to_char(pace, 'MI') as PACE_MINS, count(*)
from marathon
group by to_char(pace, 'MI')
order by PACE_MINS
;


--- query 4
select state, count(*) NUM_TOP10
from marathon
where groupplace < = 10
group by state
order by NUM_TOP10 DESC
;

--- query 5
select town, avg(to_number(substr(to_char(time,'HH:MI:SS'),1,2))*60*60 +
                 to_number(substr(to_char(time,'HH:MI:SS'),4,2))*60 +
                 to_number(substr(to_char(time,'HH:MI:SS'),7,2))) as AVG_TIME_IN_SEC
from marathon
where state = 'CT' 
group by town
having count(*) >= 3
order by AVG_TIME_IN_SEC
;

