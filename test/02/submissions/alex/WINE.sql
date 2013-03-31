---
--- CSC 365. Spring 2011. Lab 5
---

--- Alex Dekhtyar

--- INN Dataset


--- query 1
select score, avg(price) as AVERAGE_PRICE, MIN(PRICE) as CHEAPEST,
       MAX(PRICE) as MOST_EXPENSIVE, count(*) as NUM_WINES, sum(CASES) as CASES
from wine
where score >=88
group by score
order by score
;

--- query 2
select vintage, count(*) as GOOD_SB
from  wine w, appellations a, grapes g
where a.county = 'Sonoma' and a.appellation = w.appellation
      and w.score >= 90 and g.color = 'Red' and g.grape = w.grape
group by w.vintage
order by w.vintage
;


--- query 3
select w.appellation, a.county, count(*) as NUM_WINES, avg(Price) as AVG_PRICE,
       sum(cases*12) as BOTTLES
from wine w, appellations a
where a.appellation = w.appellation and w.vintage = 2007 and
      w.grape = 'Cabernet Sauvingnon'
group by w.appellation, a.county
having count(*) > 2
order by NUM_WINES DESC
;





--- query 4
select a.appellation, sum(price*12*cases) as sales
from wine w, appellations a
where w.appellation = a.appellation and a.area = 'Central Coast'
      and w.vintage = 2008
group by a.appellation
order by sales desc
;


--- query 5
select a.county, MAX(w.score) as BEST_SCORE
from wine w, grapes g, appellations a
where w.appellation = a.appellation and w.grape = g.grape and
      g.color = 'Red' and w.vintage = 2009
group by a.county
having a.county <> 'N/A'
order by BEST_SCORE DESC
;





