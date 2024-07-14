CREATE DATABASE lobbyRoblox;
USE lobbyRoblox;
CREATE TABLE sala (id_sala INT PRIMARY KEY AUTO_INCREMENT NOT NULL,
							sala VARCHAR(200));
CREATE TABLE jugador (id_jugador INT PRIMARY KEY AUTO_INCREMENT NOT NULL,
						jugador VARCHAR(200));
CREATE TABLE sala_lugares (id_sala INT NOT NULL,
							id_lugar INT NOT NULL,
                            id_jugador INT,
CONSTRAINT PK1_sala_lugares PRIMARY KEY (id_sala, id_lugar),
CONSTRAINT FK1_lugar_jugador FOREIGN KEY (id_jugador) REFERENCES jugador (id_jugador),
CONSTRAINT FK2_lugar_sala FOREIGN KEY (id_sala) REFERENCES sala (id_sala));

CREATE USER 'adminjuegos'@'localhost' IDENTIFIED BY '123';
ALTER USER 'adminjuegos'@'localhost' IDENTIFIED WITH mysql_native_password BY '123';
GRANT ALL PRIVILEGES ON lobbyRoblox.* TO 'adminjuegos'@'localhost';
FLUSH PRIVILEGES;

INSERT INTO sala (sala) VALUES ('Roblox');
INSERT INTO jugador (jugador) VALUES ('Rodri Martínez'),('Guadalupe Trejo'),('Laura Bárcenas'),
									('Mati Méndez'),('Juan Gálvez'),('Norma Santillán'),
									('Pablo Rivera'),('Gustavo García'),('Carlos Páez'),
                                    ('Federico León'),('Sandra Álvarez'),('Diana Prieto'),
                                    ('Samuel Estrada'),('Verónica Zárate'),('Rodolfo Camargo'),
                                    ('Abigail Monroy'),('Luis Gaytán'),('Rafael Jiménez'),
                                    ('María Ortiz'),('Isabel Salgado'),('Saúl Hernández'),
                                    ('Esteban Duarte'),('Isidro Ramírez'),('Leti Rodríguez');
INSERT INTO sala_lugares (id_sala, id_lugar) VALUES (1,1),(1,2),(1,3),(1,4),(1,5),(1,6),(1,7),(1,8);