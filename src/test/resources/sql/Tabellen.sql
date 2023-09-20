create table gruppe_entity
(
    id int auto_increment primary key,
    gruppen_id   int ,
    gruppen_name varchar(50),
    geschlossen  boolean,
    mitglieder   varchar(75),
    ausgabe boolean
);

create table transaktion_entity
(
    id int auto_increment primary key,
    transaktionen_id int,
    sender     varchar(50),
    empfaenger varchar(75),
    betrag     real,
    grund      varchar(20)
);