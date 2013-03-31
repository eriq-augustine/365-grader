
--- Lab 5 BAKERY dataset

-- query 1
select flavor, avg(price) as AVGPRICE, COUNT(*)
from goods
group by flavor
having count(*) > 3
order by AVGPRICE
;

--- query 2
select sum(price)
from goods g, items i, receipts r
where g.gid = i.item and r.rnumber = i.reciept and
      r.saledate >= '10-OCT-2007' and r.saledate<= '15-OCT-2007'
;

--- query 3
select r.rnumber, r.saledate, count(*), sum(price) as PAID
from receipts r, items i, customers c, goods g
where r.rnumber = i.reciept and r.customer = c.cid and
      i.item = g.gid and
      c.lastname = 'MESDAQ' and c.firstname = 'CHARLENE'
group by r.rnumber, r.saledate
order by PAID desc
;


--- query 4
select r.saledate, count(distinct r.rnumber) as PURCHASES,
       count(*) as ITEMS, sum(price) as REVENUE
from receipts r, items i, goods g
where r.rnumber = i.reciept and g.gid = i.item and
      r.saledate >= '08-OCT-07' and r.saledate <= '14-OCT-07'
group by r.saledate
order by r.saledate
;

--- query 5
select r.saledate
from  reciepts r, items i, goods g
where r.rnumber = i.reciept and i.item = g.gid and
      g.food = 'Cake'
group by r.saledate
having count(*) > 5
order by r.saledate
;










