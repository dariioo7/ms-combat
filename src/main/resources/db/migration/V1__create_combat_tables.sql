CREATE TABLE combats (
                         id BIGINT AUTO_INCREMENT PRIMARY KEY,
                         enemy VARCHAR(255),
                         max_participants INT,
                         user_id BIGINT,
                         result VARCHAR(50),
                         experience_gained INT,
                         coins_gained INT,
                         combat_date DATETIME
);

CREATE TABLE combat_participants (
                                     combat_id BIGINT NOT NULL,
                                     character_id BIGINT NOT NULL,
                                     CONSTRAINT fk_combat FOREIGN KEY (combat_id) REFERENCES combats(id)
);