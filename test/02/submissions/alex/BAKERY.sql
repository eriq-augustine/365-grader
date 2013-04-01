
--- Lab 5 BAKERY dataset

-- query 1
select flavor, avg(price) as AVGPRICE, COUNT(*)
from Goods
group by flavor
having count(*) > 3
order by AVGPRICE
;

--- query 2
select sum(price)
from Goods g, Items i, Receipts r
where g.gId = i.item and r.rNumber = i.receipt and
      r.saleDate >= '10-OCT-2007' and r.saleDate<= '15-OCT-2007'
;

--- query 3
select r.rNumber, r.saleDate, count(*), sum(price) as PAID
from Receipts r, Items i, Customers c, Goods g
where r.rNumber = i.receipt and r.customer = c.cid and
      i.item = g.gId and
      c.lastName = 'MESDAQ' and c.firstName = 'CHARLENE'
group by r.rNumber, r.saleDate
order by PAID desc
;


--- query 4
select r.saleDate, count(distinct r.rNumber) as PURCHASES,
       count(*) as ITEMS, sum(price) as REVENUE
from Receipts r, Items i, Goods g
where r.rNumber = i.receipt and g.gId = i.item and
      r.saleDate >= '08-OCT-07' and r.saleDate <= '14-OCT-07'
group by r.saleDate
order by r.saleDate
;

--- query 5
select r.saleDate
from  Receipts r, Items i, Goods g
where r.rNumber = i.receipt and i.item = g.gId and
      g.food = 'Cake'
group by r.saleDate
having count(*) > 5
order by r.saleDate
;










