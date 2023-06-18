insert into member(CREATED_TIME, UPDATED_TIME, EMAIL, NAME, ROLE) values (now(), now(), 'gjwjdghk123@gmail.com', 'Jeonghwa Heo', 'ADMIN');
insert into member(CREATED_TIME, UPDATED_TIME, EMAIL, NAME, ROLE) values (now(), now(), 'wjdghkwhdl@jr.naver.com', '허정화', 'HOST');

insert into category(CREATED_TIME, UPDATED_TIME, NAME) values (now(), now(), '스터디 공간');
insert into category(CREATED_TIME, UPDATED_TIME, NAME) values (now(), now(), '오피스 공간');
insert into category(CREATED_TIME, UPDATED_TIME, NAME) values (now(), now(), '파티 공간');
insert into category(CREATED_TIME, UPDATED_TIME, NAME) values (now(), now(), '연습 공간');
insert into category(CREATED_TIME, UPDATED_TIME, NAME) values (now(), now(), '스튜디오 공간');

insert into space(CREATED_TIME, UPDATED_TIME, DEPTH_FIRST, DEPTH_SECOND, DEPTH_THIRD, DETAIL_ADDRESS, NAME, CATEGORY_ID, HOST_ID) values (now(), now(), '서울', '강남구', '역삼동', '817-36 5층', '에프랩', 1, 2);
insert into space(CREATED_TIME, UPDATED_TIME, DEPTH_FIRST, DEPTH_SECOND, DEPTH_THIRD, DETAIL_ADDRESS, NAME, CATEGORY_ID, HOST_ID) values (now(), now(), '서울', '강남구', '역삼동', '827-3 1~6층', '토즈 강남역토즈타워점', 1, 2);
