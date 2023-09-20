create table transaktion_entity
(
    id serial primary key,
    transaktionen_id int,
    sender     varchar(50),
    empfaenger text[],
    betrag     real,
    grund      varchar(20)
);

create table gruppe_entity
(
    id serial primary key,
    gruppen_id   int ,
    gruppen_name varchar(50),
    geschlossen  boolean,
    mitglieder   text[],
    ausgabe boolean
);