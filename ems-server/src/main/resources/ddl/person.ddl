create table person (
  id              varchar(255) not null,
  revision        integer not null,
  tags            long varchar,
  name            varchar (255),
  description     long varchar,
  notes           long varchar,
  gender          varchar(6),
  birthdate       date,
  language        varchar(8),
  countryCode     varchar(2),
  zipCode         varchar(10),
  addresses       long varchar,
  photo           long varchar, -- URI of the photo
  primary key(id)
)
