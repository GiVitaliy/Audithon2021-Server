-- справочники
-- ЗАЛИПУХА. юнит тест бы сделан на тестовых данных, в миграционных скриптах есть реальные данные
-- что с этим делать пока не понятно
update institution set address_id = null;
delete from addr_delivery;
delete from address;
delete from addr_street;
delete from addr_city;
delete from addr_street_type;
delete from addr_city_type;
delete from addr_region;
delete from postal_office;

INSERT INTO public.addr_city_type (id, caption, short_caption) VALUES (3, 'деревня', 'д');
INSERT INTO public.addr_city_type (id, caption, short_caption) VALUES (4, 'поселок городского типа', 'пгт');
INSERT INTO public.addr_city_type (id, caption, short_caption) VALUES (6, 'поселок', 'п');
INSERT INTO public.addr_city_type (id, caption, short_caption) VALUES (26, 'район', 'р-н');

INSERT INTO public.addr_street_type (id, caption, short_caption) VALUES (5, 'микрорайон', 'мкр');
INSERT INTO public.addr_street_type (id, caption, short_caption) VALUES (3, 'переулок', 'пер');
INSERT INTO public.addr_street_type (id, caption, short_caption) VALUES (30, 'проезд', 'пр-д');
INSERT INTO public.addr_street_type (id, caption, short_caption) VALUES (31, 'бульвар', 'б-р');
INSERT INTO public.addr_street_type (id, caption, short_caption) VALUES (49, 'шоссе', 'ш');

insert into addr_region (id, caption, pfr62_code, is_city)
values (1, 'region', 'region', true);
insert into addr_region (id, caption, pfr62_code, is_city)
values (2, 'region 2', 'region 2', true);

insert into addr_region (id, caption, pfr62_code, is_city)
values (20, 'region 20', 'region 20', true);
insert into addr_region (id, caption, pfr62_code, is_city)
values (21, 'region 21', 'region 21', true);
insert into addr_region (id, caption, pfr62_code, is_city)
values (15, 'region 15', 'region 15', true);
insert into addr_region (id, caption, pfr62_code, is_city)
values (16, 'region 16', 'region 16', true);

insert into addr_city_type(id, caption, short_caption)
values (1, 'type', 't');

insert into addr_street_type (id, caption, short_caption)
values (1, 'тип 1', 'тип 1');

-- города
insert into addr_city (region_id, id, caption, type, is_default, rk, pfr62_code)
values
  (1, 1, 'Ханты-Мансийск', 1, true, 1.23, 'Ханты-Мансийск'),
  (1, 2, 'Город 2', 1, false, 2.34, 'Город 2'),
  (2, 331, '2', 1, false, 2.34, '2'),
  (15, 28, 'САРАНПАУЛЬ', 1, false, 2.34, 'САРАНПАУЛЬ'),
  (16, 35, 'Мулымья', 1, false, 2.34, 'Мулымья'),
  (20, 1, 'Абан', 1, false, 2.34, 'Абан'),
  (20, 3, 'Сургут', 1, false, 2.34, 'Сургут'),
  (20, 20, 'Дорожный', 1, false, 2.34, 'Абан'),
  (21, 1, 'Большая Салырь', 1, false, 2.34, 'Большая Салырь');


INSERT INTO public.fias (aoguid, aoid, aolevel, areacode, autocode, centstatus, citycode, code, currstatus, enddate,
                         formalname, ifnsfl, ifnsul, nextid, offname, okato, oktmo, operstatus, parentguid, placecode,
                         plaincode, postalcode, previd, regioncode, shortname, startdate, streetcode, terrifnsfl, terrifnsul,
                         updatedate, ctarcode, extrcode, sextcode, livestatus, normdoc, plancode, cadnum, divtype)
VALUES ('76b2b7d0-529f-4a37-99cd-c90a5c893412', '511b5b9e-09e6-4f9e-b069-e89e0743297c', 4, 9, 0, 0, 3, '8600900300000', 0,
 '2079-06-06', 'Солнечный', '8617', '8617', null, 'Солнечный', '71126000014', '71826407101', 21, '28bca73b-31f9-45c3-acb6-c4c11f038a6d',
  0, '86009003000', null, 'b1aeb41e-12cb-4436-8bde-92e523f8f0a6', 86, 'с/п', '2014-06-18', 0, null, null, '2018-02-02', 0, 0, 0, 1,
  null, 0, null, 0) on CONFLICT do NOTHING;

INSERT INTO public.fias (aoguid, aoid, aolevel, areacode, autocode, centstatus, citycode, code, currstatus, enddate,
                         formalname, ifnsfl, ifnsul, nextid, offname, okato, oktmo, operstatus, parentguid, placecode,
                         plaincode, postalcode, previd, regioncode, shortname, startdate, streetcode, terrifnsfl, terrifnsul,
                         updatedate, ctarcode, extrcode, sextcode, livestatus, normdoc, plancode, cadnum, divtype)
VALUES ('53666660-529f-4a37-99cd-c90a5c893413', '511b5b9e-09e6-4f9e-b069-e89e0743297c', 4, 9, 0, 0, 3, '8600900300000', 0,
                                                '2079-06-06', 'Солнечный', '8617', '8617', null, 'Солнечный', '71126000014', '71826407101', 21, '28bca73b-31f9-45c3-acb6-c4c11f038a6d',
                                                                           0, '86009003000', null, 'b1aeb41e-12cb-4436-8bde-92e523f8f0a6', 86, 'с/п', '2014-06-18', 0, null, null, '2018-02-02', 0, 0, 0, 1,
        null, 0, null, 0) on CONFLICT do NOTHING;

INSERT INTO public.fias_h (houseguid, aoguid, buildnum, enddate, eststatus, houseid, housenum, statstatus, ifnsfl, ifnsul, okato,
                           oktmo, postalcode, startdate, strucnum, strstatus, terrifnsfl, terrifnsul, updatedate, normdoc, counter,
                           cadnum, divtype, signature)
VALUES ('76b2b7d0-529f-4a37-99cd-c90a5c893413', '53666660-529f-4a37-99cd-c90a5c893413', null, '2079-06-06', 2, '9dfab531-0d5a-4062-93a8-01075d9a5ef5', '8', 0, '8619', '8619', '71185000000', '71885000001', '628380', '2018-12-12', null, 0, '8612', '8612', '2018-12-17', '6fb75641-4291-44b1-8741-2bfff6e5971e', 6, null, 1, '8') on CONFLICT do NOTHING;

-- улицы
insert into addr_street (region_id, city_id, id, caption, type, pfr62_code, aoguid)
values
  (1, 1, 0, '-' , 1, null, null),
  (1, 1, 1, 'улица 11' , 1, 'улица 1.1', null),
  (1, 1, 2, 'улица 22' , 1, 'улица 2.1', null),
  (1, 1, 3, '3' , 1, '3', null),
  (1, 1, 4, '4-я' , 1, '4-я', null),
  (1, 1, 284, 'СОТ Наука 3 км автодороги Х-М-Т' , 1, 'СОТ Наука 3 км автодороги Х-М-Т', null),
  (1, 1, 311, 'СОТ Учитель-3 17 км автодороги Х-М-Т' , 1, 'СОТ Учитель-3 17 км автодороги Х-М-Т', null),
  (1, 1, 113, 'Снежная' , 1, 'Снежная', '76b2b7d0-529f-4a37-99cd-c90a5c893412'),
  (1, 1, 112, 'Энгельса' , 1, 'Энгельса', null),
  (1, 2, 3, 'улица 21' , 1, 'улица 2.1', null),
  (1, 2, 4, 'улица 22' , 1, 'улица 2.2', null),
  (2, 331, 0, '-' , 1, '-', null),
  (15, 28, 5, 'Е.АРТЕЕВОЙ' , 1, 'Е.АРТЕЕВОЙ', '53666660-529f-4a37-99cd-c90a5c893413'),
  (20, 1, 1, 'Кустарная' , 1, 'Кустарная', null),
  (20, 20, 0, '-' , 1, 'Кустарная', null),
  (21, 1, 1, 'Клубничная' , 1, 'Клубничная', null);

-- адреса
insert into address (id, region_id, city_id, street_id, house, building, room, other, address_short_text, address_normalized)
values
  (1, 1, 1, 1, '11', null, '1111', null, 'short text 1', 'normalized 1'),
  (2, 1, 1, 2, '22', 'aa', '2222', null, 'short text 2', 'normalized 2'),
  (3, 1, 2, 3, '33', 'bb', null, null, 'short text 3', 'normalized 3'),
  (4, 1, 2, 4, '44', null, '4444', 'abcd', 'short text 4', 'normalized 4');

--откат последовательности не происходит при откате транзакции.
-- Предзаполненные адреса в итоге конфликтуют и не дают добавлять в ходе тестов новые записи.
--select setval('address_id_seq', (select max(id) from address));

-- офисы почты
insert into postal_office(id, caption)
values
  (1, 'post 1'),
  (2, 'post 2'),
  (3, 'post 3'),
  (4, 'post 4'),
  (5, 'post 5'),
  (6, 'post 6'),
  (7, 'post 7'),
  (8, 'post 8'),
  (9, 'post 9'),
  (10, 'post 10'),
  (11, 'post 11');

--графики доставки
insert into addr_delivery(delivery_schema_id, region_id, city_id, street_id,
                          house_from, house_to, house_concrete, house_parity,
                          building_from, building_to, building_concrete,
                          room_from, room_to,
                          payreq_post_id, payreq_deliveryday, payreq_deliverybranch)
values
  --весь город  по умолчанию в отделении 1
  (1, 1, 1, null, null, null, null, null, null, null, null, null, null, 1, 1, 1),
  --вся улица по умолчанию в отделении 2
  (1, 1, 1, 1, null, null, null, null, null, null, null, null, null, 2, 2, 2),
  --для домов с 1 по 10 на улице 1 - отделение 3
  (1, 1, 1, 1, 1, 10, null, null, null, null, null, null, null, 3, 3, 3),
  --для четных домов с 1 по 10 на улице 2 - отделение 4
  (1, 1, 1, 2, 1, 10, null, 0, null, null, null, null, null, 4, 4, 4),
  --для нечетных домов с 1 по 10 на улице 2 - отделение 5
  (1, 1, 1, 2, 1, 10, null, 1, null, null, null, null, null, 5, 5, 5),
  --для дома 7б на улице 1 - отделение 6
  (1, 1, 1, 1, null, null, '7б', null, null, null, null, null, null, 6, 6, 6),
  --для дома 7б, строений с 1 по 5 на улице 1 - отделение 7
  (1, 1, 1, 1, null, null, '7б', null, 1, 5, null, null, null, 7, 7, 7),
  --для дома 7б, строения 3б на улице 1 - отделение 8
  (1, 1, 1, 1, null, null, '7б', null, null, null, '3б', null, null, 8, 8, 8),
  --для дома 7б, квартиры с 1 по 100 на улице 1 - отделение 9
  (1, 1, 1, 1, null, null, '7б', null, null, null, null, 1, 100, 9, 9, 9),
  --для дома 7, строения 3б, квартиры с 1 по 100 на улице 1 - отделение 10
  (1, 1, 1, 1, null, null, '7', null, null, null, '3б', 1, 100, 10, 10, 10),
  --для домов с 11 по 20, строения 3б, квартиры с 1 по 100 на улице 1 - отделение 11
  (1, 1, 1, 1, 11, 20, null, null, null, null, '3б', 1, 100, 11, 11, 11);


INSERT INTO public.addr_region (id, caption, pfr62_code, is_city, aoguid) VALUES (18, 'Советский район', null, false, null);
INSERT INTO public.addr_city (region_id, id, caption, type, is_default, rk, pfr62_code, aoguid) VALUES (18, 14, 'Советский', 1, false, 1.5000, null, null);
INSERT INTO public.addr_street (region_id, city_id, id, caption, type, pfr62_code, aoguid) VALUES (18, 14, 0, '-', 1, null, null);
INSERT INTO public.addr_city (region_id, id, caption, type, is_default, rk, pfr62_code, aoguid) VALUES (18, 32, 'Пионерский', 4, false, 1.5000, null, null);
INSERT INTO public.addr_street (region_id, city_id, id, caption, type, pfr62_code, aoguid) VALUES (18, 32, 0, '-', 1, null, null);
INSERT INTO public.addr_street (region_id, city_id, id, caption, type, pfr62_code, aoguid) VALUES (18, 14, 315, 'снт Дружба, Пушкина', 1, null, null);
INSERT INTO public.addr_street (region_id, city_id, id, caption, type, pfr62_code, aoguid) VALUES (18, 14, 75, 'Пушкина', 1, null, null);

INSERT INTO public.addr_city (region_id, id, caption, type, is_default, rk, pfr62_code, aoguid) VALUES (15, 36, 'Хулимсунт', 3, false, 1.5000, null, null);
INSERT INTO public.addr_street (region_id, city_id, id, caption, type, pfr62_code, aoguid) VALUES (15, 36, 0, '-', 1, null, null);
INSERT INTO public.addr_street (region_id, city_id, id, caption, type, pfr62_code, aoguid) VALUES (15, 36, 2, '2', 5, null, null);
INSERT INTO public.addr_street (region_id, city_id, id, caption, type, pfr62_code, aoguid) VALUES (15, 36, 567, '45-й', 5, null, null);


INSERT INTO public.addr_region (id, caption, pfr62_code, is_city, aoguid) VALUES (11, 'Нягань', null, true, null);
INSERT INTO public.addr_city (region_id, id, caption, type, is_default, rk, pfr62_code, aoguid) VALUES (11, 7, 'Нягань', 1, true, 1.5000, null, null);
INSERT INTO public.addr_street (region_id, city_id, id, caption, type, pfr62_code, aoguid) VALUES (11, 7, 328, '1-й', 5, null, null);
INSERT INTO public.addr_street (region_id, city_id, id, caption, type, pfr62_code, aoguid) VALUES (11, 7, 1, '1-й', 30, null, null);
INSERT INTO public.addr_street (region_id, city_id, id, caption, type, pfr62_code, aoguid) VALUES (11, 7, 320, '10-й', 5, null, null);
INSERT INTO public.addr_street (region_id, city_id, id, caption, type, pfr62_code, aoguid) VALUES (11, 7, 136, '10-й', 30, null, null);
INSERT INTO public.addr_street (region_id, city_id, id, caption, type, pfr62_code, aoguid) VALUES (11, 7, 10, 'Речная', 30, null, null);

INSERT INTO public.addr_region (id, caption, pfr62_code, is_city, aoguid) VALUES (7, 'Нижневартовск', null, true, null);
INSERT INTO public.addr_city (region_id, id, caption, type, is_default, rk, pfr62_code, aoguid) VALUES (7, 12, 'Нижневартовск', 1, true, 1.5000, null, null);
INSERT INTO public.addr_street (region_id, city_id, id, caption, type, pfr62_code, aoguid) VALUES (7, 12, 101, 'Рябиновый', 3, null, null);
INSERT INTO public.addr_street (region_id, city_id, id, caption, type, pfr62_code, aoguid) VALUES (7, 12, 152, 'Рябиновый', 31, null, null);
INSERT INTO public.addr_street (region_id, city_id, id, caption, type, pfr62_code, aoguid) VALUES (7, 12, 36, 'Заозерный', 30, null, null);
INSERT INTO public.addr_street (region_id, city_id, id, caption, type, pfr62_code, aoguid) VALUES (7, 12, -36, 'Заозёварный', 30, null, null);

INSERT INTO public.addr_city (region_id, id, caption, type, is_default, rk, pfr62_code, aoguid) VALUES (7, 38, 'МЖК', 6, false, 1.5000, null, null);
INSERT INTO public.addr_street (region_id, city_id, id, caption, type, pfr62_code, aoguid) VALUES (7, 38, 0, '-', 1, null, null);
INSERT INTO public.addr_street (region_id, city_id, id, caption, type, pfr62_code, aoguid) VALUES (7, 12, 0, '-', 1, null, null);

INSERT INTO public.addr_region (id, caption, pfr62_code, is_city, aoguid) VALUES (13, 'Когалым', null, true, null);
INSERT INTO public.addr_city (region_id, id, caption, type, is_default, rk, pfr62_code, aoguid) VALUES (13, 4, 'Когалым', 1, true, 1.5000, null, null);
INSERT INTO public.addr_street (region_id, city_id, id, caption, type, pfr62_code, aoguid) VALUES (13, 4, 0, '-', 1, null, null);
INSERT INTO public.addr_street (region_id, city_id, id, caption, type, pfr62_code, aoguid) VALUES (13, 4, 42, 'Сургутское', 49, null, null);

INSERT INTO public.addr_region (id, caption, pfr62_code, is_city, aoguid) VALUES (10, 'Урай', null, true, null);
INSERT INTO public.addr_city (region_id, id, caption, type, is_default, rk, pfr62_code, aoguid) VALUES (10, 9, 'Урай', 1, true, 1.5000, null, null);

INSERT INTO public.addr_street (region_id, city_id, id, caption, type, pfr62_code, aoguid) VALUES (10, 9, 0, '-', 1, null, null);
INSERT INTO public.addr_street (region_id, city_id, id, caption, type, pfr62_code, aoguid) VALUES (10, 9, 14, '1-й', 5, null, null);
INSERT INTO public.addr_street (region_id, city_id, id, caption, type, pfr62_code, aoguid) VALUES (10, 9, 17, '1Д', 5, null, null);

INSERT INTO public.addr_city (region_id, id, caption, type, is_default, rk, pfr62_code, aoguid)
VALUES (2, 93, '1', 26, false, 1.5000, null, NULL);
INSERT INTO public.addr_street (region_id, city_id, id, caption, type, pfr62_code, aoguid)
VALUES (2, 93, 0, '-', 1, null, null);
INSERT INTO public.addr_street (region_id, city_id, id, caption, type, pfr62_code, aoguid)
VALUES (16, 35, 0, '-', 1, null, null);
INSERT INTO public.addr_street (region_id, city_id, id, caption, type, pfr62_code, aoguid)
VALUES (20, 3, 345, 'ПСОК N 6 Витамин', 1, null, null);

INSERT INTO public.addr_region (id, caption, pfr62_code, is_city, aoguid) VALUES (4, 'Мегион', null, true, null);
INSERT INTO public.addr_city (region_id, id, caption, type, is_default, rk, pfr62_code, aoguid) VALUES (4, 47, 'ПГК Волга', 1, FALSE , 1.5000, null, null);
INSERT INTO public.addr_city (region_id, id, caption, type, is_default, rk, pfr62_code, aoguid) VALUES (4, 48, 'Мегион', 1, FALSE , 1.5000, null, null);
INSERT INTO public.addr_street (region_id, city_id, id, caption, type, pfr62_code, aoguid) VALUES (4, 47, 0, '-', 1, null, null);
INSERT INTO public.addr_street (region_id, city_id, id, caption, type, pfr62_code, aoguid) VALUES (4, 47, -1, '000', 1, null, null);

INSERT INTO public.addr_region (id, caption, pfr62_code, is_city, aoguid) VALUES (6, 'Нефтеюганский район', null, true, null);
INSERT INTO public.addr_city (region_id, id, caption, type, is_default, rk, pfr62_code, aoguid) VALUES (6, 71, 'СНТ Белые ночи', 1, FALSE , 1.5000, null, null);
INSERT INTO public.addr_street (region_id, city_id, id, caption, type, pfr62_code, aoguid) VALUES (6, 71, 0, '-', 1, null, null);