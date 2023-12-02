SELECT setval('person_id_seq', MAX(id)) FROM person;
SELECT setval('customer_id_seq', MAX(id)) FROM customer;
SELECT setval('driver_id_seq', MAX(id)) FROM driver;
SELECT setval('vehicle_id_seq', MAX(id)) FROM vehicle;
SELECT setval('orders_id_seq', MAX(id)) FROM orders;
SELECT setval('cargo_id_seq', MAX(id)) FROM cargo;
SELECT setval('address_id_seq', MAX(id)) FROM address;
