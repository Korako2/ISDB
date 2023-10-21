-- Создание перечисления "СТАТУС ВОДИТЕЛЯ"
CREATE TYPE СТАТУС_ВОДИТЕЛЯ AS ENUM (
  'ВЫХОДНОЙ',
  'В_ПУТИ',
  'РАЗГРУЖАЕТ',
  'ЗАГРУЖАЕТ',
  'ОЖИДАЕТ_ПОЛУЧЕНИЯ_ЗАКАЗА',
  'ОЖИДАЕТ_ПОГРУЗКИ',
  'ОЖИДАЕТ_РАЗГРУЗКИ',
  'ПРИБЫЛ_НА_МЕСТО_ЗАГРУЗКИ'
);

-- Создание перечисления "ТИП КОРПУСА"
CREATE TYPE ТИП_КОРПУСА AS ENUM (
  'ОТКРЫТЫЙ',
  'ЗАКРЫТЫЙ'
);

CREATE TYPE ТИП_ГРУЗА AS ENUM (
  'НАСЫПНОЙ',
  'НАВАЛОЧНЫЙ',
  'ТАРНЫЙ'
);

-- Создание перечисления "СТАТУС ЗАКАЗА"
CREATE TYPE СТАТУС_ЗАКАЗА AS ENUM (
  'ПРИНЯТ',
  'В РАБОТЕ',
  'ПРИБЫЛ НА МЕСТО ЗАГРУЗКИ',
  'В ЗАГРУЗКЕ',
  'ПРИБЫЛ НА МЕСТО РАЗГРУЗКИ',
  'В ДОРОГЕ',
  'ДОСТВЛЕН',
  'ВЫПОЛНЕН'
);


-- Создание перечисления "ТИП_КОНТАКТНЫХ_ДАННЫХ"
CREATE TYPE ТИП_КОНТАКТНЫХ_ДАННЫХ AS ENUM (
  'НОМЕР_ТЕЛЕФОНА',
  'TELEGRAM',
  'ПОЧТА'
);

-- Создание таблицы "ЧЕЛОВЕК"
CREATE TABLE IF NOT EXISTS ЧЕЛОВЕК (
  ИД_ЧЕЛОВЕКА serial PRIMARY KEY,
  ИМЯ VARCHAR(20) NOT NULL,
  ФАМИЛИЯ VARCHAR(20) NOT NULL,
  ОТЧЕСТВО VARCHAR(20),
  ПОЛ CHAR(1) NOT NULL,
  ПАСПОРТ VARCHAR(10)
);

-- Создание табицы "КОНТАКТНЫЕ_ДАННЫЕ"
CREATE TABLE IF NOT EXISTS КОНТАКТНЫЕ_ДАННЫЕ (
  ИД_ЧЕЛОВЕКА int REFERENCES ЧЕЛОВЕК (ИД_ЧЕЛОВЕКА),
  ТИП_КОНТАКТНЫХ_ДАННЫХ ТИП_КОНТАКТНЫХ_ДАННЫХ,
  ЗНАЧЕНИЕ text,
  PRIMARY KEY (ИД_ЧЕЛОВЕКА, ТИП_КОНТАКТНЫХ_ДАННЫХ)
);

-- Создание таблицы "ВОДИТЕЛЬ"
CREATE TABLE IF NOT EXISTS ВОДИТЕЛЬ (
  ИД_ВОДИТЕЛЯ serial PRIMARY KEY,
  КОНТАКТНЫЕ_ДАННЫЕ VARCHAR(11) NOT NULL,
  ПАСПОРТ VARCHAR(10) NOT NULL,
  НОМЕР_БАНКОВСКОЙ_КАРТЫ text NOT NULL
);

-- Создание таблицы "ЗАКАЗЧИК"
CREATE TABLE IF NOT EXISTS ЗАКАЗЧИК (
  ИД_ЗАКАЗЧИКА serial PRIMARY KEY,
  ИД_ЧЕЛОВЕКА int REFERENCES ЧЕЛОВЕК (ИД_ЧЕЛОВЕКА),
  ОРГАНИЗАЦИЯ VARCHAR(50)
);

-- Создание таблицы "ИСТОРИЯ_СТАТУСОВ_ВОДИТЕЛЕЙ"
CREATE TABLE IF NOT EXISTS ИСТОРИЯ_СТАТУСОВ_ВОДИТЕЛЕЙ (
  ИД_ВОДИТЕЛЯ int REFERENCES ВОДИТЕЛЬ (ИД_ВОДИТЕЛЯ),
  ДАТА date,
  СТАТУС СТАТУС_ВОДИТЕЛЯ,
  -- ИД_ВОДИТЕЛЯ и ДАТА - PK
  PRIMARY KEY (ИД_ВОДИТЕЛЯ, ДАТА)
);

-- Создание таблицы "ТАРИФНАЯ СТАВКА"
CREATE TABLE IF NOT EXISTS ТАРИФНАЯ_СТАВКА (
  ИД_ВОДИТЕЛЯ int REFERENCES ВОДИТЕЛЬ (ИД_ВОДИТЕЛЯ) PRIMARY KEY,
  СУТОЧНАЯ_СТАВКА int NOT NULL,
  СТАВКА_ЗА_КМ int NOT NULL
);

-- Создание таблицы "ВОДИТЕЛЬСКОЕ_УДОСТОВЕРЕНИЕ"
CREATE TABLE IF NOT EXISTS ВОДИТЕЛЬСКОЕ_УДОСТОВЕРЕНИЕ (
  ИД_ВОДИТЕЛЯ int REFERENCES ВОДИТЕЛЬ(ИД_ВОДИТЕЛЯ) PRIMARY KEY,
  ДАТА_ПОЛУЧЕНИЯ date NOT NULL,
  ДАТА_ОКОНЧАНИЯ date NOT NULL,
  НОМЕР_ВУ int
);

-- Создание таблицы "АВТОМОБИЛЬ"
CREATE TABLE IF NOT EXISTS АВТОМОБИЛЬ (
  ИД_АВТОМОБИЛЯ serial PRIMARY KEY,
  НОМЕР varchar(9) NOT NULL,
  МОДЕЛЬ varchar(50) NOT NULL,
  ГОД_ВЫПУСКА date NOT NULL,
  ДЛИНА float NOT NULL,
  ШИРИНА float NOT NULL,
  ВЫСОТА float NOT NULL,
  ГРУЗОПОДЪЕМНОСТЬ float NOT NULL,
  ТИП_КОРПУСА ТИП_КОРПУСА
);

-- Создание таблицы "ВЛАДЕНИЕ_АВТО"
CREATE TABLE IF NOT EXISTS ВЛАДЕНИЕ_АВТО (
  ИД_АВТОМОБИЛЯ int REFERENCES АВТОМОБИЛЬ (ИД_АВТОМОБИЛЯ),
  ИД_ВОДИТЕЛЯ int REFERENCES ВОДИТЕЛЬ (ИД_ВОДИТЕЛЯ),
  ДАТА_НАЧАЛА_ВЛАДЕНИЯ date,
  ДАТА_ОКОНЧАНИЯ_ВЛАДЕНИЯ date,
  PRIMARY KEY (ИД_АВТОМОБИЛЯ, ИД_ВОДИТЕЛЯ)
);

-- Создание таблицы "ИСТОРИЯ_ПЕРЕМЕЩЕНИЯ_АВТО"
CREATE TABLE IF NOT EXISTS ИСТОРИЯ_ПЕРЕМЕЩЕНИЯ_АВТО (
  ИД_АВТОМОБИЛЯ int REFERENCES АВТОМОБИЛЬ (ИД_АВТОМОБИЛЯ),
  ДАТА timestamp,
  ШИРОТА float NOT NULL,
  ДОЛГОТА float NOT NULL,
  ПРОБЕГ float NOT NULL,
  PRIMARY KEY (ИД_АВТОМОБИЛЯ, ДАТА)
);


-- Создание таблицы "ЗАКАЗ"
CREATE TABLE IF NOT EXISTS ЗАКАЗ (
  ИД_ЗАКАЗА serial PRIMARY KEY,
  ИД_ЗАКАЗЧИКА int NOT NULL REFERENCES ЗАКАЗЧИК (ИД_ЗАКАЗЧИКА),
  РАССТОЯНИЕ float NOT NULL,
  ЦЕНА float NOT NULL,
  ДАТА_ОФОРМЛЕНИЯ date NOT NULL,
  ИД_АВТОМОБИЛЯ int REFERENCES АВТОМОБИЛЬ (ИД_АВТОМОБИЛЯ)
);

-- Создание таблицы "СТАТУСЫ_ЗАКАЗОВ"
CREATE TABLE IF NOT EXISTS СТАТУСЫ_ЗАКАЗОВ (
  ИД_ЗАКАЗА int REFERENCES ЗАКАЗ (ИД_ЗАКАЗА),
  ВРЕМЯ timestamp,
  СТАТУС СТАТУС_ЗАКАЗА,
  PRIMARY KEY (ИД_ЗАКАЗА, ВРЕМЯ)
);

-- Создание таблицы "ГРУЗ"
CREATE TABLE IF NOT EXISTS ГРУЗ (
  ИД_ГРУЗА serial PRIMARY KEY,
  ВЕС float NOT NULL,
  ШИРИНА float NOT NULL,
  ВЫСОТА float NOT NULL,
  ДЛИНА float NOT NULL,
  ИД_ЗАКАЗА int NOT NULL REFERENCES ЗАКАЗ (ИД_ЗАКАЗА),
  ТИП_ГРУЗА ТИП_ГРУЗА
);



-- Создание таблицы "АДРЕС"
CREATE TABLE IF NOT EXISTS АДРЕС (
  ИД_АДРЕСА serial PRIMARY KEY,
  СТРАНА text NOT NULL,
  ГОРОД text NOT NULL,
  УЛИЦА text NOT NULL,
  ЗДАНИЕ int NOT NULL,
  КОРПУС int
);

-- Создание таблицы "ПУНКТ_ХРАНЕНИЯ"
CREATE TABLE IF NOT EXISTS ПУНКТ_ХРАНЕНИЯ (
  ИД_АДРЕСА int REFERENCES АДРЕС(ИД_АДРЕСА) PRIMARY KEY,
  ДОЛГОТА float NOT NULL,
  ШИРОТА float NOT NULL
);

-- Создание таблицы "РАЗГРУЗ_ПОГРУЗ_СОГЛАШЕНИЕ"
CREATE TABLE IF NOT EXISTS РАЗГРУЗ_ПОГРУЗ_СОГЛАШЕНИЕ (
  ИД_ЗАКАЗА int REFERENCES ЗАКАЗ (ИД_ЗАКАЗА) PRIMARY KEY,
  ИД_ВОДИТЕЛЯ int NOT NULL REFERENCES ВОДИТЕЛЬ (ИД_ВОДИТЕЛЯ),
  ПУНКТ_ОТПРАВЛЕНИЯ int NOT NULL REFERENCES ПУНКТ_ХРАНЕНИЯ (ИД_АДРЕСА),
  ПУНКТ_ПОЛУЧЕНИЯ int NOT NULL REFERENCES ПУНКТ_ХРАНЕНИЯ (ИД_АДРЕСА),
  ИД_ОТПРАВИТЕЛЯ int NOT NULL REFERENCES ЧЕЛОВЕК (ИД_ЧЕЛОВЕКА),
  ИД_ПОЛУЧАТЕЛЯ int NOT NULL REFERENCES ЧЕЛОВЕК (ИД_ЧЕЛОВЕКА),
  ВРЕМЯ_НА_РАЗГРУЗКУ time NOT NULL,
  ВРЕМЯ_НА_ПОГРУЗКУ time NOT NULL
);

-- Создание таблицы "ТОПЛИВНЫЕ_КАРТЫ_ВОДИТЕЛЕЙ"
CREATE TABLE IF NOT EXISTS ТОПЛИВНЫЕ_КАРТЫ_ВОДИТЕЛЕЙ (
  ИД_ВОДИТЕЛЯ int REFERENCES ВОДИТЕЛЬ(ИД_ВОДИТЕЛЯ),
  НОМЕР_ТОПЛИВНОЙ_КАРТЫ VARCHAR(40) NOT NULL,
  НАЗВАНИЕ_ЗАПРАВОЧНОЙ_СТАРНЦИИ VARCHAR(50),
  PRIMARY KEY (ИД_ВОДИТЕЛЯ, НОМЕР_ТОПЛИВНОЙ_КАРТЫ),
  UNIQUE (НОМЕР_ТОПЛИВНОЙ_КАРТЫ)
);
-- Создание таблицы "РАСХОДЫ_ТП"
CREATE TABLE IF NOT EXISTS РАСХОДЫ_ТП (
  НОМЕР_ТОПЛИВНОЙ_КАРТЫ VARCHAR(40) REFERENCES ТОПЛИВНЫЕ_КАРТЫ_ВОДИТЕЛЕЙ(НОМЕР_ТОПЛИВНОЙ_КАРТЫ) PRIMARY KEY,
  ДАТА date,
  СУММА double precision NOT NULL
);

