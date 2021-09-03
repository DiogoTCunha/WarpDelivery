--STORE
INSERT INTO STORE (storeid,name, postalcode, address, latitude,longitude) VALUES (1,'STORE ONE', '2725-032', 'ADDRESS EXAMPLE 1',123.231,123.312);
INSERT INTO STORE (storeid,name, postalcode, address, latitude,longitude) VALUES (2,'STORE TWO', '7359-081', 'ADDRESS EXAMPLE 2',13.231,12.312);

--USERS
INSERT INTO WARPER VALUES ('user1', 'user', 'one', '968488765', 'password1', 'user1@email.com');
INSERT INTO WARPER VALUES ('user2', 'user', 'two', '923947365', 'password2', 'user2@email.com');
INSERT INTO WARPER VALUES ('user3', 'user', 'three', '917385375', 'password3', 'user3@email.com');


--WARPER VEHICLES
INSERT INTO VEHICLE VALUES ('user3', 'small', 'AA-01-ZZ');
INSERT INTO VEHICLE VALUES ('user3', 'medium', 'BB-32-AS');

--DELIVERIES
INSERT INTO DELIVERY (deliveryid, warperusername, storeid, state, clientphone, purchasedate, deliverdate
                        , deliverLatitude, deliverLongitude, deliverAddress, rating, reward, type)
					  VALUES (1,'user3', 1, 'Delivered', '923947365', '2020-06-22 18:10:35', null,
							             12.343421, 13.31231, 'Rua teste 123', null, null, 'small');


INSERT INTO DELIVERY (deliveryid , warperusername, storeid, state, clientphone, purchasedate, deliverdate,
                            deliverLatitude, deliverLongitude, deliverAddress, rating, reward, type)
					  VALUES (2,'user3', 1, 'Cancelled', '923947365', '2020-06-22 18:10:35', null,
							         13.343421, 13.31231, 'Rua teste 123', null, null, 'small');


--DELIVERY STATE TRANSITIONS
INSERT INTO STATE_TRANSITIONS VALUES (1, '2020-06-22 18:20:33', 'Delivering', 'Delivered');
INSERT INTO STATE_TRANSITIONS VALUES (1, '2020-06-22 19:00:25', 'Delivering', 'Cancelled');

