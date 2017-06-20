-- MySQL dump 10.13  Distrib 5.7.16, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: yc_game
-- ------------------------------------------------------
-- Server version	5.5.32

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `accounts`
--

DROP TABLE IF EXISTS `accounts`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `accounts` (
  `machine_id` varchar(64) NOT NULL,
  `user_id` int(11) NOT NULL AUTO_INCREMENT,
  `phone_num` varchar(20) DEFAULT '',
  `password` varchar(20) DEFAULT '',
  `alipay_account` varchar(20) DEFAULT '',
  `alipay_name` varchar(20) DEFAULT '',
  `create_time` int(11) NOT NULL,
  `register_time` int(11) NOT NULL,
  `channel` varchar(20) NOT NULL,
  `login_time` int(11) NOT NULL,
  `create_ip` varchar(30) DEFAULT NULL,
  `last_login_ip` varchar(30) DEFAULT NULL,
  `device` varchar(10) NOT NULL,
  `last_login_machine` varchar(64) DEFAULT NULL,
  `last_login_device` varchar(10) DEFAULT NULL,
  `last_login_channel` varchar(10) DEFAULT NULL,
  `login` tinyint(1) NOT NULL,
  PRIMARY KEY (`user_id`),
  KEY `phone_num` (`phone_num`,`machine_id`)
) ENGINE=MyISAM AUTO_INCREMENT=1003621 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `back_accounts`
--

DROP TABLE IF EXISTS `back_accounts`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `back_accounts` (
  `user_id` int(11) NOT NULL AUTO_INCREMENT,
  `user_name` varchar(20) NOT NULL,
  `password` varchar(20) NOT NULL,
  `level` int(11) NOT NULL,
  PRIMARY KEY (`user_id`)
) ENGINE=MyISAM AUTO_INCREMENT=2 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `banner_gain`
--

DROP TABLE IF EXISTS `banner_gain`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `banner_gain` (
  `time` int(11) NOT NULL,
  `coin_value` bigint(20) NOT NULL,
  PRIMARY KEY (`time`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `block`
--

DROP TABLE IF EXISTS `block`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `block` (
  `type` int(11) NOT NULL,
  `info` varchar(64) NOT NULL DEFAULT '',
  PRIMARY KEY (`info`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `exchange`
--

DROP TABLE IF EXISTS `exchange`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `exchange` (
  `order_id` int(11) NOT NULL AUTO_INCREMENT,
  `phone_num` varchar(20) NOT NULL,
  `ali_account` varchar(20) NOT NULL,
  `exchange_count` int(11) NOT NULL,
  `exchange_time` int(11) NOT NULL,
  `status` int(11) DEFAULT '1',
  `player_id` int(11) DEFAULT '10000',
  `operation_time` int(11) NOT NULL,
  `tax_count` int(11) NOT NULL,
  `need_pay` int(11) NOT NULL,
  `ali_name` varchar(20) NOT NULL,
  `lock_user` int(11) DEFAULT '0',
  PRIMARY KEY (`order_id`)
) ENGINE=MyISAM AUTO_INCREMENT=100119 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `game_detail`
--

DROP TABLE IF EXISTS `game_detail`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `game_detail` (
  `game_no` bigint(20) NOT NULL,
  `player_id` int(11) NOT NULL,
  `player_account` varchar(20) NOT NULL,
  `position` int(11) NOT NULL,
  `final_cost_gain` int(11) NOT NULL,
  `detail` tinytext NOT NULL,
  PRIMARY KEY (`game_no`,`player_id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `game_record`
--

DROP TABLE IF EXISTS `game_record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `game_record` (
  `game_id` int(11) NOT NULL AUTO_INCREMENT,
  `game_text` text NOT NULL,
  `player_1` int(11) NOT NULL,
  `player_2` int(11) NOT NULL,
  `player_3` int(11) DEFAULT NULL,
  `player_4` int(11) DEFAULT NULL,
  `time` int(11) NOT NULL,
  PRIMARY KEY (`game_id`)
) ENGINE=MyISAM AUTO_INCREMENT=100438 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `history_info`
--

DROP TABLE IF EXISTS `history_info`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `history_info` (
  `time` varchar(30) NOT NULL,
  `info` text NOT NULL,
  PRIMARY KEY (`time`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `login_record`
--

DROP TABLE IF EXISTS `login_record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `login_record` (
  `user_id` int(11) NOT NULL,
  `login_time` int(11) NOT NULL
) ENGINE=MyISAM DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `mail`
--

DROP TABLE IF EXISTS `mail`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `mail` (
  `player_id` int(11) NOT NULL,
  `id` int(11) NOT NULL,
  `sender` varchar(20) NOT NULL,
  `send_time` int(11) NOT NULL,
  `mail_id` int(11) NOT NULL,
  `param` varchar(255) NOT NULL,
  `money` int(11) NOT NULL,
  `gain` int(11) NOT NULL,
  `readed` int(11) NOT NULL,
  PRIMARY KEY (`player_id`,`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `message_info`
--

DROP TABLE IF EXISTS `message_info`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `message_info` (
  `player_id` int(11) NOT NULL,
  `content` text NOT NULL,
  `contact_time` int(11) NOT NULL,
  `reply_time` int(11) DEFAULT NULL,
  `reply_content` text
) ENGINE=MyISAM DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `order_info`
--

DROP TABLE IF EXISTS `order_info`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `order_info` (
  `order_id` varchar(30) NOT NULL,
  `player_id` int(11) NOT NULL,
  `charge_num` int(11) NOT NULL,
  `time` int(11) NOT NULL,
  `status` int(11) NOT NULL,
  `ok_time` int(11) NOT NULL,
  `player_account` varchar(20) NOT NULL,
  `platform` varchar(10) NOT NULL,
  `acutal_money` int(11) DEFAULT NULL,
  `ip` varchar(20) DEFAULT NULL,
  `machine_id` varchar(64) DEFAULT NULL,
  `device` varchar(10) NOT NULL,
  `province` varchar(20) DEFAULT NULL,
  PRIMARY KEY (`order_id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `player`
--

DROP TABLE IF EXISTS `player`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `player` (
  `player_id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) NOT NULL,
  `coin` bigint(20) NOT NULL,
  `headIcon` varchar(30) NOT NULL,
  `nickname` varchar(20) NOT NULL,
  `bank_password` varchar(20) NOT NULL,
  `bank_coin` bigint(20) NOT NULL,
  `score` int(11) NOT NULL,
  PRIMARY KEY (`player_id`),
  KEY `user_id` (`user_id`)
) ENGINE=MyISAM AUTO_INCREMENT=11932 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `player_score_list`
--

DROP TABLE IF EXISTS `player_score_list`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `player_score_list` (
  `player_id` int(11) NOT NULL,
  `game_no` bigint(20) NOT NULL,
  `record_data` blob NOT NULL,
  PRIMARY KEY (`player_id`,`game_no`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tax_info`
--

DROP TABLE IF EXISTS `tax_info`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tax_info` (
  `game_no` bigint(20) NOT NULL,
  `game_type` varchar(10) NOT NULL,
  `room_id` int(11) NOT NULL,
  `banner_id` int(11) NOT NULL,
  `flow_count` int(11) NOT NULL,
  `tax_count` int(11) NOT NULL,
  `time` int(11) NOT NULL,
  PRIMARY KEY (`game_no`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2017-02-23 11:37:31
