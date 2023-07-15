insert into member(CREATED_TIME, UPDATED_TIME, EMAIL, NAME, ROLE) values (now(), now(), 'gjwjdghk123@gmail.com', 'Jeonghwa Heo', 'ADMIN');
insert into member(CREATED_TIME, UPDATED_TIME, EMAIL, NAME, ROLE)values (now(), now(), 'wjdghkwhdl@jr.naver.com', '허정화', 'HOST');
insert into member(CREATED_TIME, UPDATED_TIME, EMAIL, NAME, ROLE)values (now(), now(), 'yh.kim@jr.naver.com', '김영화', 'VISITOR');

insert into category(CREATED_TIME, UPDATED_TIME, NAME)values (now(), now(), '스터디 공간');
insert into category(CREATED_TIME, UPDATED_TIME, NAME)values (now(), now(), '오피스 공간');
insert into category(CREATED_TIME, UPDATED_TIME, NAME)values (now(), now(), '파티 공간');
insert into category(CREATED_TIME, UPDATED_TIME, NAME)values (now(), now(), '연습 공간');
insert into category(CREATED_TIME, UPDATED_TIME, NAME)values (now(), now(), '스튜디오 공간');

insert into space(CREATED_TIME, UPDATED_TIME, DEPTH_FIRST, DEPTH_SECOND, DEPTH_THIRD, DETAIL_ADDRESS, NAME, DESCRIPTION, CATEGORY_ID, HOST_ID)values (now(), now(), '서울', '강남구', '역삼동', '817-36 5층', '에프랩', '상위1%어쩌고', 1, 2);
insert into space(CREATED_TIME, UPDATED_TIME, DEPTH_FIRST, DEPTH_SECOND, DEPTH_THIRD,DETAIL_ADDRESS, NAME, DESCRIPTION, CATEGORY_ID, HOST_ID)values (now(), now(), '서울', '강남구', '역삼동', '827-3 1~6층', '토즈 강남역토즈타워점', '쾌적상쾌스터디카페', 1, 2);

insert into facility(facility_id, created_time, updated_time, description, facility_type, name, reservation_enable, space_id)values (default, now(), now(), '스터디 룸', 'ROOM', 'facilityRoom', true, 2);
insert into facility (facility_id, created_time, updated_time, description, facility_type, name, reservation_enable, space_id)values (default, now(), now(), '스터디카페 좌석', 'SEAT', '좌석', true, 1);

insert into facility_schedule (facility_schedule_id, end_date_time, facility_id, start_date_time) values (default, '2023-07-01T23:59:59', 1, '2023-07-01T00:00');
insert into facility_schedule (facility_schedule_id, end_date_time, facility_id, start_date_time) values (default, '2023-07-01T23:59:59', 2, '2023-07-01T00:00');
insert into facility_schedule (facility_schedule_id, end_date_time, facility_id, start_date_time) values (default, '2023-07-02T23:59:59', 2, '2023-07-02T00:00');
insert into facility_schedule (facility_schedule_id, end_date_time, facility_id, start_date_time) values (default, '2023-07-03T23:59:59', 2, '2023-07-03T00:00');


insert into reservation (reservation_id, created_time, updated_time, facility_id, reservation_start, reservation_end, status, visitor_id) values (default, now(), now(), 2, '2023-07-14T10:00:00', '2023-07-14T12:59:59','COMPLETED', 1);
insert into reservation (reservation_id, created_time, updated_time, facility_id, reservation_start, reservation_end, status, visitor_id) values (default, now(), now(), 2, '2023-07-01T16:00:00', '2023-07-01T17:59:59','COMPLETED', 3);
insert into reservation (reservation_id, created_time, updated_time, facility_id, reservation_start, reservation_end, status, visitor_id) values (default, now(), now(), 2, '2023-07-02T16:00:00', '2023-07-02T17:59:59','COMPLETED', 3);
