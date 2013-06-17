-- This script will create the tables for keeping
-- the jobs characteristics and the results
-- (designed for mySQL)
--
-- $Id: soaplabCreate.sql,v 1.1.1.1 2006/11/03 09:14:53 marsenger Exp $
-- -------------------------------------------------------------
create database soaplab;
use soaplab;

drop table jobs;
drop table results;
drop table other_results;

create table jobs
 (id varchar(255) not null primary key
 ,localized char(1) default 'n'
 ,instance_ior text
 ,jcontrol_ior text
 ,service_ior text
 ,analysis_name varchar(255)
 ,last_accessed timestamp
 ,status varchar(64)
 ,created int default -1
 ,started int default -1
 ,ended int default -1
 ,elapsed int default -1
 ,last_event text
 ,owner varchar(64)
 ,credentials text
 )
;

create table results
 (id int not null primary key auto_increment default 0
 ,job_id varchar(255) not null 
 ,name varchar(64) not null
 ,type varchar(64)
 ,result longblob
 ,other_results char(1) default 'n'
,unique (job_id, name)
)
;

create table other_results
 (result_id int not null 
 ,order_num int
 ,result longblob
 )
;

# some testing:
#insert into jobs (id) values ('324');
#insert into results (job_id, name) values (1,'test');
#insert into other_results (result_id, order_num) values (last_insert_id(), 5);
#insert into other_results (result_id, order_num) values (last_insert_id(), 6);
#select id,last_accessed from jobs;
#select id,job_id,name from results;
#select result_id, order_num from other_results;
