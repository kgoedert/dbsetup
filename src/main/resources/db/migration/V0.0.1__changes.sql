create table book
(
	id bigint primary key not null auto_increment,
	name varchar(200) not null,
	publication_date timestamp
);

create table author
(
	id bigint primary key not null,
	name varchar(200) not null,
);