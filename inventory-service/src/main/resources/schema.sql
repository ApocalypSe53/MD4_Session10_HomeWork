CREATE TABLE IF NOT EXISTS medicines (
    id         VARCHAR(50)    PRIMARY KEY,
    name       VARCHAR(255)   NOT NULL,
    stock      INT            NOT NULL CHECK (stock >= 0),
    price      NUMERIC(12, 2) NOT NULL
);

-- order_id là khóa chính -> dùng để chống xử lý trùng khi Kafka gửi lại cùng một sự kiện
CREATE TABLE IF NOT EXISTS orders (
    order_id        VARCHAR(64)  PRIMARY KEY,
    medicine_id     VARCHAR(50)  NOT NULL,
    quantity        INT          NOT NULL,
    status          VARCHAR(40)  NOT NULL,
    order_time      TIMESTAMPTZ  NOT NULL,
    processed_at    TIMESTAMPTZ  NOT NULL,
    kafka_partition INT          NOT NULL,
    kafka_offset    BIGINT       NOT NULL
);
