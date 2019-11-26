insert into demo.CUSTOMERS
(`id`, `first_name`, `last_name`, `email`, `gender`, `club_status`, `comments`)
values
 (121214, 'Silvia', 'Urbon', 'arookj@europa.eu', 'Female', 'gold', 'Cross-group 24/7 application');

ALTER TABLE demo.CUSTOMERS
ADD country varchar(100) NOT NULL
AFTER gender;

insert into demo.CUSTOMERS
(`id`, `first_name`, `last_name`, `email`, `gender`, `country`, `club_status`, `comments`)
values
 (121219, 'Pere', 'Urbon', 'pere.urbon@europa.eu', 'Male', 'Katalonia', 'gold', 'Cross-group 24/7 application');

 ALTER TABLE demo.CUSTOMERS DROP COLUMN country;
 ALTER TABLE demo.CUSTOMERS DROP COLUMN club_status;

 insert into demo.CUSTOMERS
(`id`, `first_name`, `last_name`, `email`, `gender`,  `comments`)
values
 (121221, 'Lluc', 'Urbon', 'lluc.urbon@europa.eu', 'Male', 'Cross-group 24/7 application');
