create database if not exists clinica_spring;
use clinica_spring;

DROP TABLE IF EXISTS `usuarios`;
CREATE TABLE `usuarios` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `ativo` tinyint(1) NOT NULL,
  `email` varchar(255) NOT NULL,
  `senha` varchar(255) NOT NULL,
  `codigo_verificador` varchar(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_USUARIO_EMAIL` (`email`),
  KEY `IDX_USUARIO_EMAIL` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `especialidades`;
CREATE TABLE `especialidades` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `descricao` text,
  `titulo` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_TITULO` (`titulo`),
  KEY `IDX_ESPECIALIDADE_TITULO` (`titulo`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `horas`;
CREATE TABLE `horas` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `hora_minuto` time NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_HORA_MINUTO` (`hora_minuto`),
  KEY `IDX_HORA_MINUTO` (`hora_minuto`)
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `medicos`;
CREATE TABLE `medicos` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `crm` int(11) NOT NULL,
  `data_inscricao` date NOT NULL,
  `nome` varchar(255) NOT NULL,
  `id_usuario` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_CRM` (`crm`),
  UNIQUE KEY `UK_NOME` (`nome`),
  UNIQUE KEY `UK_USUARIO_ID` (`id_usuario`),
  CONSTRAINT `FK_USUARIO_ID` FOREIGN KEY (`id_usuario`) REFERENCES `usuarios` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `medicos_tem_especialidades`;
CREATE TABLE `medicos_tem_especialidades` (
  `id_especialidade` bigint(20) NOT NULL,
  `id_medico` bigint(20) NOT NULL,
  UNIQUE KEY `MEDICO_UNIQUE_ESPECIALIZACAO` (`id_especialidade`,`id_medico`),
  KEY `FK_ESPECIALIDADE_MEDICO_ID` (`id_medico`),
  CONSTRAINT `FK_ESPECIALIDADE_MEDICO_ID` FOREIGN KEY (`id_medico`) REFERENCES `medicos` (`id`),
  CONSTRAINT `FK_MEDICO_ESPECIALIDADE_ID` FOREIGN KEY (`id_especialidade`) REFERENCES `especialidades` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `pacientes`;
CREATE TABLE `pacientes` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `data_nascimento` date NOT NULL,
  `nome` varchar(255) NOT NULL,
  `id_usuario` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_PACIENTE_NOME` (`nome`),
  KEY `FK_PACIENTE_USUARIO_ID` (`id_usuario`),
  CONSTRAINT `FK_PACIENTE_USUARIO_ID` FOREIGN KEY (`id_usuario`) REFERENCES `usuarios` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `perfis`;
CREATE TABLE `perfis` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `descricao` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_PERFIL_DESCRICAO` (`descricao`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `agendamentos`;
CREATE TABLE `agendamentos` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `data_consulta` date DEFAULT NULL,
  `id_especialidade` bigint(20) DEFAULT NULL,
  `id_horario` bigint(20) DEFAULT NULL,
  `id_medico` bigint(20) DEFAULT NULL,
  `id_paciente` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_ESPECIALIDADE_ID` (`id_especialidade`),
  KEY `FK_HORA_ID` (`id_horario`),
  KEY `FK_MEDICO_ID` (`id_medico`),
  KEY `FK_PACIENTE_ID` (`id_paciente`),
  CONSTRAINT `FK_ESPECIALIDADE_ID` FOREIGN KEY (`id_especialidade`) REFERENCES `especialidades` (`id`),
  CONSTRAINT `FK_HORA_ID` FOREIGN KEY (`id_horario`) REFERENCES `horas` (`id`),
  CONSTRAINT `FK_MEDICO_ID` FOREIGN KEY (`id_medico`) REFERENCES `medicos` (`id`),
  CONSTRAINT `FK_PACIENTE_ID` FOREIGN KEY (`id_paciente`) REFERENCES `pacientes` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `usuarios_tem_perfis`;
CREATE TABLE `usuarios_tem_perfis` (
  `usuario_id` bigint(20) NOT NULL,
  `perfil_id` bigint(20) NOT NULL,
  PRIMARY KEY (`usuario_id`,`perfil_id`),
  KEY `FK_USUARIO_TEM_PERFIL_ID` (`perfil_id`),
  KEY `FK_PERFIL_TEM_USUARIO_ID` (`usuario_id`),
  CONSTRAINT `FK_PERFIL_TEM_USUARIO_ID` FOREIGN KEY (`usuario_id`) REFERENCES `usuarios` (`id`),
  CONSTRAINT `FK_USUARIO_TEM_PERFIL_ID` FOREIGN KEY (`perfil_id`) REFERENCES `perfis` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

INSERT INTO `horas` VALUES (1,'07:00:00'),(2,'07:30:00'),(3,'08:00:00'),(4,'08:30:00'),(5,'09:00:00'),(6,'09:30:00'),(7,'10:00:00'),(8,'10:30:00'),(9,'11:00:00'),(10,'11:30:00'),(11,'13:00:00'),(12,'13:30:00'),(13,'14:00:00'),(14,'14:30:00'),(15,'15:00:00'),(16,'15:30:00'),(17,'16:00:00'),(18,'16:30:00'),(19,'17:00:00'),(20,'17:30:00');
