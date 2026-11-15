-- =====================================================================
-- PixelMeet · Script de creación e inserción de datos (MySQL 8.4)
-- ISY2202 · Experiencia 1 · Semana 3 · Grupo 20
--
-- Crea la base pixelmeet, sus 7 tablas y los datos iniciales:
--   roles (USER, ADMIN), 3 usuarios de prueba, 12 eventos con sus
--   fotografías y 3 banners comerciales.
--
-- El esquema es el mismo que genera la aplicación (JPA/Hibernate) al
-- arrancar contra MySQL 8.4, y los datos son los de DataInitializer.
--
-- Usuarios de prueba (contraseñas guardadas con BCrypt, costo 12):
--   admin  / Admin123!  -> roles ADMIN y USER
--   gamer1 / Gamer123!  -> rol USER
--   gamer2 / Gamer123!  -> rol USER
--
-- Uso con el despliegue Docker (borra y recrea las tablas):
--   docker compose exec -T mysql mysql -uroot -p<clave_root> < sql/pixelmeet.sql
-- Con datos ya cargados, la aplicación no vuelve a insertar los iniciales.
-- =====================================================================

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `pixelmeet` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;

USE `pixelmeet`;
DROP TABLE IF EXISTS `banners`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `banners` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `activo` bit(1) NOT NULL,
  `imagen` varchar(80) NOT NULL,
  `llamado` varchar(40) NOT NULL,
  `marca` varchar(60) NOT NULL,
  `mensaje` varchar(200) NOT NULL,
  `orden` int NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

INSERT INTO `banners` (`id`, `activo`, `imagen`, `llamado`, `marca`, `mensaje`, `orden`) VALUES (1,_binary '','banner-neokeys.svg','Ver teclados','NeoKeys','Teclados mecánicos con 15% de descuento para la comunidad PixelMeet.',1);
INSERT INTO `banners` (`id`, `activo`, `imagen`, `llamado`, `marca`, `mensaje`, `orden`) VALUES (2,_binary '','banner-bytenet.svg','Cotizar plan','ByteNet Fibra','Plan gamer 1 Gbps con ping bajo a servidores de Santiago.',2);
INSERT INTO `banners` (`id`, `activo`, `imagen`, `llamado`, `marca`, `mensaje`, `orden`) VALUES (3,_binary '','banner-respawn.svg','Cómo llegar','Respawn Café','Café, snacks y consolas libres. Muestra tu entrada y obtén 2x1.',3);
DROP TABLE IF EXISTS `evento_fotos`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `evento_fotos` (
  `evento_id` bigint NOT NULL,
  `foto` varchar(120) DEFAULT NULL,
  `orden` int NOT NULL,
  PRIMARY KEY (`evento_id`,`orden`),
  CONSTRAINT `FKpbfl69261qaqabuf4uow2ajcy` FOREIGN KEY (`evento_id`) REFERENCES `eventos` (`id`),
  CONSTRAINT `evento_fotos_chk_1` CHECK ((`orden` >= 0))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

INSERT INTO `evento_fotos` (`evento_id`, `foto`, `orden`) VALUES (1,'valorant-1.svg',0);
INSERT INTO `evento_fotos` (`evento_id`, `foto`, `orden`) VALUES (1,'valorant-2.svg',1);
INSERT INTO `evento_fotos` (`evento_id`, `foto`, `orden`) VALUES (1,'estadio-1.svg',2);
INSERT INTO `evento_fotos` (`evento_id`, `foto`, `orden`) VALUES (2,'lan-1.svg',0);
INSERT INTO `evento_fotos` (`evento_id`, `foto`, `orden`) VALUES (2,'lan-2.svg',1);
INSERT INTO `evento_fotos` (`evento_id`, `foto`, `orden`) VALUES (2,'cs2-1.svg',2);
INSERT INTO `evento_fotos` (`evento_id`, `foto`, `orden`) VALUES (3,'lol-1.svg',0);
INSERT INTO `evento_fotos` (`evento_id`, `foto`, `orden`) VALUES (3,'lol-2.svg',1);
INSERT INTO `evento_fotos` (`evento_id`, `foto`, `orden`) VALUES (4,'smash-1.svg',0);
INSERT INTO `evento_fotos` (`evento_id`, `foto`, `orden`) VALUES (4,'torneo-1.svg',1);
INSERT INTO `evento_fotos` (`evento_id`, `foto`, `orden`) VALUES (5,'minecraft-1.svg',0);
INSERT INTO `evento_fotos` (`evento_id`, `foto`, `orden`) VALUES (5,'minecraft-2.svg',1);
INSERT INTO `evento_fotos` (`evento_id`, `foto`, `orden`) VALUES (6,'sf6-1.svg',0);
INSERT INTO `evento_fotos` (`evento_id`, `foto`, `orden`) VALUES (6,'torneo-1.svg',1);
INSERT INTO `evento_fotos` (`evento_id`, `foto`, `orden`) VALUES (7,'convencion-1.svg',0);
INSERT INTO `evento_fotos` (`evento_id`, `foto`, `orden`) VALUES (7,'cosplay-1.svg',1);
INSERT INTO `evento_fotos` (`evento_id`, `foto`, `orden`) VALUES (7,'estadio-1.svg',2);
INSERT INTO `evento_fotos` (`evento_id`, `foto`, `orden`) VALUES (8,'rocket-1.svg',0);
INSERT INTO `evento_fotos` (`evento_id`, `foto`, `orden`) VALUES (8,'torneo-1.svg',1);
INSERT INTO `evento_fotos` (`evento_id`, `foto`, `orden`) VALUES (9,'fortnite-1.svg',0);
INSERT INTO `evento_fotos` (`evento_id`, `foto`, `orden`) VALUES (9,'lan-2.svg',1);
INSERT INTO `evento_fotos` (`evento_id`, `foto`, `orden`) VALUES (10,'pokemon-1.svg',0);
INSERT INTO `evento_fotos` (`evento_id`, `foto`, `orden`) VALUES (10,'torneo-1.svg',1);
INSERT INTO `evento_fotos` (`evento_id`, `foto`, `orden`) VALUES (11,'fc-1.svg',0);
INSERT INTO `evento_fotos` (`evento_id`, `foto`, `orden`) VALUES (11,'estadio-1.svg',1);
INSERT INTO `evento_fotos` (`evento_id`, `foto`, `orden`) VALUES (12,'tekken-1.svg',0);
INSERT INTO `evento_fotos` (`evento_id`, `foto`, `orden`) VALUES (12,'torneo-1.svg',1);
INSERT INTO `evento_fotos` (`evento_id`, `foto`, `orden`) VALUES (12,'estadio-1.svg',2);
DROP TABLE IF EXISTS `eventos`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `eventos` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `ciudad` varchar(60) NOT NULL,
  `cupos` int DEFAULT NULL,
  `descripcion` varchar(3000) NOT NULL,
  `destacado` bit(1) NOT NULL,
  `enlace_acceso` varchar(300) DEFAULT NULL,
  `fecha_hora` datetime(6) NOT NULL,
  `inscripcion` varchar(1000) NOT NULL,
  `interesados` int NOT NULL,
  `modalidad` enum('ONLINE','PRESENCIAL') NOT NULL,
  `nombre` varchar(120) NOT NULL,
  `organizador` varchar(120) NOT NULL,
  `pais` varchar(60) NOT NULL,
  `precio` varchar(40) NOT NULL,
  `publicado_en` datetime(6) NOT NULL,
  `requisitos` varchar(1000) NOT NULL,
  `resumen` varchar(200) NOT NULL,
  `tipo` varchar(40) NOT NULL,
  `ubicacion` varchar(200) DEFAULT NULL,
  `videojuego` varchar(60) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

INSERT INTO `eventos` (`id`, `ciudad`, `cupos`, `descripcion`, `destacado`, `enlace_acceso`, `fecha_hora`, `inscripcion`, `interesados`, `modalidad`, `nombre`, `organizador`, `pais`, `precio`, `publicado_en`, `requisitos`, `resumen`, `tipo`, `ubicacion`, `videojuego`) VALUES (1,'Santiago',32,'La Copa Andina reúne a los mejores equipos amateur del país en un formato de doble eliminación. Habrá casteo en vivo, zona de streaming y firma de autógrafos con jugadores profesionales.',_binary '',NULL,'2026-11-21 10:00:00.000000','Inscripción por equipo hasta el 15 de noviembre en el sitio de Andes Esports. Cupos limitados a 32 equipos.',1840,'PRESENCIAL','Copa Andina de Valorant','Andes Esports','Chile','$20.000 por equipo','2026-11-06 02:36:26.097058','Equipos de 5 jugadores mayores de 16 años. Cuenta Riot con rango mínimo Oro. Llevar periféricos propios; los PC los pone la organización.','Torneo 5v5 con equipos de todo Chile y premio de $3.000.000.','Torneo','Movistar Arena, Av. Beaucheff 1204, Santiago','Valorant');
INSERT INTO `eventos` (`id`, `ciudad`, `cupos`, `descripcion`, `destacado`, `enlace_acceso`, `fecha_hora`, `inscripcion`, `interesados`, `modalidad`, `nombre`, `organizador`, `pais`, `precio`, `publicado_en`, `requisitos`, `resumen`, `tipo`, `ubicacion`, `videojuego`) VALUES (2,'Valparaíso',300,'La LAN más grande de la Quinta Región vuelve con 300 puestos, red de 10 Gbps, zona de descanso y torneos con premios en hardware.',_binary '\0',NULL,'2026-11-28 09:00:00.000000','Venta de entradas en línea; el puesto se asigna por orden de llegada al confirmar el pago.',960,'PRESENCIAL','LAN Party Valparaíso 2026','Club Gamer Porteño','Chile','$15.000','2026-11-03 02:36:26.097118','Traer PC o notebook, cable de red y audífonos. Mayores de 14 años (menores de 18 con autorización firmada).','48 horas de LAN con torneos de CS2, Rocket League y retro.','LAN Party','Centro de Eventos Duoc UC, Av. Brasil 2021, Valparaíso','Counter-Strike 2');
INSERT INTO `eventos` (`id`, `ciudad`, `cupos`, `descripcion`, `destacado`, `enlace_acceso`, `fecha_hora`, `inscripcion`, `interesados`, `modalidad`, `nombre`, `organizador`, `pais`, `precio`, `publicado_en`, `requisitos`, `resumen`, `tipo`, `ubicacion`, `videojuego`) VALUES (3,'Concepción',128,'Fase de grupos online los fines de semana de diciembre. Los 4 mejores equipos clasifican a la final presencial de enero en Concepción.',_binary '','https://discord.gg/ligasur-ejemplo','2026-12-05 18:00:00.000000','Registro del equipo en el formulario de Liga Sur Gaming; se envía el enlace de Discord al confirmar.',2210,'ONLINE','Clasificatorio online League of Legends Sur','Liga Sur Gaming','Chile','Gratis','2026-11-07 02:36:26.097141','Equipos de 5 titulares y 1 suplente, servidor LAS, nivel de invocador 30 o superior.','Clasificatorio abierto para la liga regional LoL Sur.','Clasificatorio',NULL,'League of Legends');
INSERT INTO `eventos` (`id`, `ciudad`, `cupos`, `descripcion`, `destacado`, `enlace_acceso`, `fecha_hora`, `inscripcion`, `interesados`, `modalidad`, `nombre`, `organizador`, `pais`, `precio`, `publicado_en`, `requisitos`, `resumen`, `tipo`, `ubicacion`, `videojuego`) VALUES (4,'Santiago',256,'Formato doble eliminación al mejor de 3 (top 8 al mejor de 5). Habrá zona libre para jugar casual y venta de comida.',_binary '\0',NULL,'2026-12-12 11:00:00.000000','Inscripción en start.gg hasta el 10 de diciembre o el mismo día si quedan cupos.',640,'PRESENCIAL','Smash Fest Santiago','Smash Chile Comunidad','Chile','$8.000','2026-10-31 02:36:26.097161','Traer control propio. Reglamento competitivo oficial de la comunidad.','Torneo 1v1 de Smash Ultimate con side events de dobles.','Torneo','Centro Cultural GAM, Av. Libertador B. O\'Higgins 227, Santiago','Super Smash Bros.');
INSERT INTO `eventos` (`id`, `ciudad`, `cupos`, `descripcion`, `destacado`, `enlace_acceso`, `fecha_hora`, `inscripcion`, `interesados`, `modalidad`, `nombre`, `organizador`, `pais`, `precio`, `publicado_en`, `requisitos`, `resumen`, `tipo`, `ubicacion`, `videojuego`) VALUES (5,'Buenos Aires',NULL,'Charlas cortas de constructores latinoamericanos, concurso de construcción por equipos y sorteo de cuentas premium.',_binary '\0','https://twitch.tv/builderslatam-ejemplo','2026-12-13 17:00:00.000000','Inscripción gratuita en el formulario del evento; la IP del servidor se envía por correo.',410,'ONLINE','Meetup Minecraft Builders','Builders LATAM','Argentina','Gratis','2026-11-05 02:36:26.097182','Minecraft Java 1.21 o superior. Micrófono opcional.','Encuentro online para mostrar construcciones y aprender redstone.','Meetup',NULL,'Minecraft');
INSERT INTO `eventos` (`id`, `ciudad`, `cupos`, `descripcion`, `destacado`, `enlace_acceso`, `fecha_hora`, `inscripcion`, `interesados`, `modalidad`, `nombre`, `organizador`, `pais`, `precio`, `publicado_en`, `requisitos`, `resumen`, `tipo`, `ubicacion`, `videojuego`) VALUES (6,'Viña del Mar',64,'Noche de juegos de pelea con torneo principal de Street Fighter 6 y side de Tekken 8. Transmisión en vivo con comentaristas.',_binary '\0',NULL,'2026-12-19 19:00:00.000000','Inscripción presencial hasta las 18:30 hrs o por adelantado en el Instagram del organizador.',380,'PRESENCIAL','Fighting Games Night','FGC Quinta','Chile','$5.000','2026-10-27 02:36:26.097201','Traer control o arcade stick. Mayores de 15 años.','Torneo de SF6 y Tekken 8 con premios en dinero.','Torneo','Respawn Café, 5 Norte 650, Viña del Mar','Street Fighter 6');
INSERT INTO `eventos` (`id`, `ciudad`, `cupos`, `descripcion`, `destacado`, `enlace_acceso`, `fecha_hora`, `inscripcion`, `interesados`, `modalidad`, `nombre`, `organizador`, `pais`, `precio`, `publicado_en`, `requisitos`, `resumen`, `tipo`, `ubicacion`, `videojuego`) VALUES (7,'Lima',NULL,'Dos días de actividades: stands de marcas, zona de juegos indie latinoamericanos, concurso de cosplay y paneles con desarrolladores.',_binary '',NULL,'2027-01-16 10:00:00.000000','Entradas en la boletería oficial del evento; descuento para grupos de 4 o más.',3150,'PRESENCIAL','GamerFest Lima','GamerFest Perú','Perú','S/ 45','2026-10-19 02:36:26.097250','Entrada general para mayores de 12 años; menores acompañados por un adulto.','Convención gamer con lanzamientos, cosplay y zona indie.','Convención','Centro de Exposiciones Jockey, Lima','Varios');
INSERT INTO `eventos` (`id`, `ciudad`, `cupos`, `descripcion`, `destacado`, `enlace_acceso`, `fecha_hora`, `inscripcion`, `interesados`, `modalidad`, `nombre`, `organizador`, `pais`, `precio`, `publicado_en`, `requisitos`, `resumen`, `tipo`, `ubicacion`, `videojuego`) VALUES (8,'Ciudad de México',256,'Formato suizo seguido de playoffs. Las finales se transmiten con casteo en español.',_binary '\0','https://youtube.com/@rllatam-ejemplo','2027-01-23 16:00:00.000000','Registro del equipo en el bracket online hasta 24 horas antes del inicio.',720,'ONLINE','Rocket League 2v2 Online Cup','RL Latam League','México','Gratis','2026-11-04 02:36:26.097305','Equipos de 2 jugadores, servidores SAM o NA Este, cuenta Epic vinculada.','Copa online 2v2 abierta a toda Latinoamérica.','Torneo',NULL,'Rocket League');
INSERT INTO `eventos` (`id`, `ciudad`, `cupos`, `descripcion`, `destacado`, `enlace_acceso`, `fecha_hora`, `inscripcion`, `interesados`, `modalidad`, `nombre`, `organizador`, `pais`, `precio`, `publicado_en`, `requisitos`, `resumen`, `tipo`, `ubicacion`, `videojuego`) VALUES (9,'Santiago',NULL,'Game jam para creadores de mapas. Un jurado evalúa jugabilidad, originalidad y tema; los mapas ganadores se destacan en la comunidad.',_binary '\0','https://discord.gg/creadoreschile-ejemplo','2027-02-06 10:00:00.000000','Inscripción en el formulario de Creadores Chile; el tema se revela al iniciar.',290,'ONLINE','Fortnite Creative Jam','Creadores Chile','Chile','Gratis','2026-11-08 02:36:26.097329','Equipos de 1 a 3 personas, cuenta Epic y UEFN instalado.','48 horas para crear un mapa en Fortnite Creative / UEFN.','Game Jam',NULL,'Fortnite');
INSERT INTO `eventos` (`id`, `ciudad`, `cupos`, `descripcion`, `destacado`, `enlace_acceso`, `fecha_hora`, `inscripcion`, `interesados`, `modalidad`, `nombre`, `organizador`, `pais`, `precio`, `publicado_en`, `requisitos`, `resumen`, `tipo`, `ubicacion`, `videojuego`) VALUES (10,'Concepción',128,'Torneo por suizo con corte a top 8. Incluye categorías Junior, Senior y Master.',_binary '\0',NULL,'2027-02-20 10:00:00.000000','Inscripción previa obligatoria en la plataforma de la liga.',530,'PRESENCIAL','Pokémon VGC Regional Concepción','Liga Pokémon Biobío','Chile','$10.000','2026-11-02 02:36:26.097348','Traer consola Nintendo Switch con el equipo registrado y Player ID.','Regional de Pokémon Escarlata y Púrpura formato VGC.','Torneo','Mall Plaza Trébol, Av. Jorge Alessandri 3177, Talcahuano','Pokémon');
INSERT INTO `eventos` (`id`, `ciudad`, `cupos`, `descripcion`, `destacado`, `enlace_acceso`, `fecha_hora`, `inscripcion`, `interesados`, `modalidad`, `nombre`, `organizador`, `pais`, `precio`, `publicado_en`, `requisitos`, `resumen`, `tipo`, `ubicacion`, `videojuego`) VALUES (11,'Santiago',40,'Representantes de distintas casas de estudio compiten en formato liga. Esta fecha incluye la presentación de los equipos.',_binary '\0',NULL,'2027-03-13 15:00:00.000000','Inscripción a través de la dirección de deportes de cada institución.',860,'PRESENCIAL','EA FC Liga Universitaria','Deporte Electrónico Universitario','Chile','Gratis','2026-10-30 02:36:26.097363','Ser alumno regular con credencial vigente. Jugar en PlayStation 5.','Fecha inaugural de la liga universitaria de EA Sports FC.','Liga','Duoc UC Sede San Joaquín, Av. Vicuña Mackenna 4917, Santiago','EA Sports FC');
INSERT INTO `eventos` (`id`, `ciudad`, `cupos`, `descripcion`, `destacado`, `enlace_acceso`, `fecha_hora`, `inscripcion`, `interesados`, `modalidad`, `nombre`, `organizador`, `pais`, `precio`, `publicado_en`, `requisitos`, `resumen`, `tipo`, `ubicacion`, `videojuego`) VALUES (12,'Buenos Aires',512,'Torneo abierto con invitados de Chile, Perú y Brasil. Incluye exhibiciones y side events.',_binary '\0',NULL,'2027-04-10 12:00:00.000000','Inscripción en start.gg; cierre una semana antes del evento.',1210,'PRESENCIAL','Tekken 8 Buenos Aires Showdown','Tekken Argentina','Argentina','ARS 25.000','2026-10-24 02:36:26.097383','Traer control. Inscripción individual. Mayores de 16 años.','Torneo internacional de Tekken 8 con invitados de la región.','Torneo','Centro Costa Salguero, Buenos Aires','Tekken 8');
DROP TABLE IF EXISTS `roles`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `roles` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `nombre` varchar(20) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKldv0v52e0udsh2h1rs0r0gw1n` (`nombre`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

INSERT INTO `roles` (`id`, `nombre`) VALUES (2,'ADMIN');
INSERT INTO `roles` (`id`, `nombre`) VALUES (1,'USER');
DROP TABLE IF EXISTS `usuario_juegos`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `usuario_juegos` (
  `usuario_id` bigint NOT NULL,
  `juego` varchar(40) DEFAULT NULL,
  KEY `FK3mbvxg9lodf9kks8qvt3xvuw9` (`usuario_id`),
  CONSTRAINT `FK3mbvxg9lodf9kks8qvt3xvuw9` FOREIGN KEY (`usuario_id`) REFERENCES `usuarios` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

INSERT INTO `usuario_juegos` (`usuario_id`, `juego`) VALUES (2,'Valorant');
INSERT INTO `usuario_juegos` (`usuario_id`, `juego`) VALUES (2,'League of Legends');
INSERT INTO `usuario_juegos` (`usuario_id`, `juego`) VALUES (3,'Street Fighter 6');
INSERT INTO `usuario_juegos` (`usuario_id`, `juego`) VALUES (3,'Tekken 8');
DROP TABLE IF EXISTS `usuario_roles`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `usuario_roles` (
  `usuario_id` bigint NOT NULL,
  `rol_id` bigint NOT NULL,
  PRIMARY KEY (`usuario_id`,`rol_id`),
  KEY `FKbt9i9yrb9ug88xnh82n9m60pr` (`rol_id`),
  CONSTRAINT `FKbt9i9yrb9ug88xnh82n9m60pr` FOREIGN KEY (`rol_id`) REFERENCES `roles` (`id`),
  CONSTRAINT `FKuu9tea04xb29m2km5lwe46ua` FOREIGN KEY (`usuario_id`) REFERENCES `usuarios` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

INSERT INTO `usuario_roles` (`usuario_id`, `rol_id`) VALUES (1,1);
INSERT INTO `usuario_roles` (`usuario_id`, `rol_id`) VALUES (2,1);
INSERT INTO `usuario_roles` (`usuario_id`, `rol_id`) VALUES (3,1);
INSERT INTO `usuario_roles` (`usuario_id`, `rol_id`) VALUES (1,2);
DROP TABLE IF EXISTS `usuarios`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `usuarios` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `activo` bit(1) NOT NULL,
  `avatar` varchar(20) NOT NULL,
  `creado_en` datetime(6) NOT NULL,
  `email` varchar(100) NOT NULL,
  `nombre` varchar(60) NOT NULL,
  `nombre_usuario` varchar(20) NOT NULL,
  `notif_correo` bit(1) NOT NULL,
  `notif_nuevos_eventos` bit(1) NOT NULL,
  `notif_push` bit(1) NOT NULL,
  `password` varchar(100) NOT NULL,
  `username` varchar(100) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKkfsp0s1tflm1cwlj8idhqsad0` (`email`),
  UNIQUE KEY `UKm2dvbwfge291euvmk6vkkocao` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

INSERT INTO `usuarios` (`id`, `activo`, `avatar`, `creado_en`, `email`, `nombre`, `nombre_usuario`, `notif_correo`, `notif_nuevos_eventos`, `notif_push`, `password`, `username`) VALUES (1,_binary '','avatar-4','2026-11-08 02:36:25.396926','admin@pixelmeet.cl','Administrador PixelMeet','admin',_binary '',_binary '',_binary '\0','$2a$12$XOhoo8eNG4t8TpcAmjy5JuusoCVQybjQ4peDjlfr..1PREAkZAVY.','admin');
INSERT INTO `usuarios` (`id`, `activo`, `avatar`, `creado_en`, `email`, `nombre`, `nombre_usuario`, `notif_correo`, `notif_nuevos_eventos`, `notif_push`, `password`, `username`) VALUES (2,_binary '','avatar-2','2026-11-08 02:36:25.736732','gamer1@pixelmeet.cl','Camila Rojas','camirojas',_binary '',_binary '',_binary '\0','$2a$12$SY02lF/RPkWJZB/7qkrvBOYNi0X86qH3f.IoKq55x8O9oE9LqKNXG','gamer1');
INSERT INTO `usuarios` (`id`, `activo`, `avatar`, `creado_en`, `email`, `nombre`, `nombre_usuario`, `notif_correo`, `notif_nuevos_eventos`, `notif_push`, `password`, `username`) VALUES (3,_binary '','avatar-3','2026-11-08 02:36:26.071054','gamer2@pixelmeet.cl','Diego Muñoz','diegomz',_binary '',_binary '',_binary '\0','$2a$12$7hLz2PKLBMHo.h81tEeCs.IV5Vro.EqZGFaC4RaoqKpJ/Uege7tIu','gamer2');
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
