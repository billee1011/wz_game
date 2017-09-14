/*
Navicat MySQL Data Transfer

Source Server         : localhost
Source Server Version : 50716
Source Host           : localhost:3306
Source Database       : wz_game_dev

Target Server Type    : MYSQL
Target Server Version : 50716
File Encoding         : 65001

Date: 2017-09-08 17:56:17
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for char_hero
-- ----------------------------
DROP TABLE IF EXISTS `char_hero`;
CREATE TABLE `char_hero` (
  `player_id` bigint(20) NOT NULL,
  `hero_id` bigint(20) NOT NULL,
  `level` int(11) NOT NULL,
  `exp` int(11) NOT NULL,
  `break_level` int(11) NOT NULL,
  `awake_level` int(11) NOT NULL,
  `awake_info` varchar(50) NOT NULL,
  `tianming_level` int(11) NOT NULL,
  `conf_id` int(11) NOT NULL,
  PRIMARY KEY (`player_id`,`hero_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for player
-- ----------------------------
DROP TABLE IF EXISTS `player`;
CREATE TABLE `player` (
  `player_id` bigint(20) NOT NULL,
  `player_name` varchar(20) NOT NULL,
  `diamond` bigint(20) NOT NULL,
  `silver` bigint(20) NOT NULL,
  `reputation` bigint(20) NOT NULL,
  `user_id` int(11) NOT NULL,
  PRIMARY KEY (`player_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


DROP TABLE IF EXISTS `char_equip`;
CREATE TABLE `char_equip` (
  player_id bigint(20) NOT NULL,
  equip_id  bigint(20) NOT NULL,
  conf_id int(11) not null,
  level int(11) not null,
  jinglian_level int(11) not null,
  jinglian_exp int(11) not null,
  star_level int(11) not null,
  star_exp int(11) not null,
  star_bless int(11) not null,
  gold_level int(11) not null,
  PRIMARY KEY (player_id,equip_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;