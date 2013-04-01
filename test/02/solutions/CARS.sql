
--- Lab 5 CARS dataset

-- query 1
select cm.maker, max(mpg) AS BEST_MPG, avg(Accelerate) as AVG_ACC
from CarData c, Models m, Makes mk, CarMakers cm, Countries cn   
where c.id = mk.id and mk.model = m.model and 
      m.maker = cm.id and cm.country=cn.id and cn.name = 'japan'
group by cm.maker
order by BEST_MPG
;


--- query 2
select cm.maker, count(*) as FAST
from CarData c, Models m, Makes mk, CarMakers cm, Countries cn
where c.id = mk.id and mk.model = m.model and 
      m.maker = cm.id and cm.country=cn.id and cn.name = 'usa'
      and c.cylinders=4 and accelerate < 14 and weight < 4000
group by cm.maker
order by FAST DESC
;

--- query 3
select c.year, min(c.mpg) AS BEST, avg(mpg) as AVERAGE,
       max(mpg) as WORST
from CarData c, Models m, Makes mk, CarMakers cm
where c.id = mk.id and mk.model= m.model and m.maker = cm.id
      and cm.maker = 'honda'
group by c.year
having count(*) > 2
order by c.year
;


--- query 4
select c.year, min(EDISPL) as Smallest, max(EDISPL) as Largest
from CarData c, Models m, Makes mk, CarMakers cm, Countries cn
where c.id = mk.id and mk.model=m.model and m.maker = cm.id
     and cm.country = cn.id and cn.name = 'usa'
group by year
having avg(horsepower) < 100
order by c.year
;
