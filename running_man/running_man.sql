/*
Navicat MySQL Data Transfer

Source Server         : local
Source Server Version : 50620
Source Host           : 192.168.2.254:3306
Source Database       : running_man

Target Server Type    : MYSQL
Target Server Version : 50620
File Encoding         : 65001

Date: 2015-02-02 16:32:03
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for rm_base_camp
-- ----------------------------
DROP TABLE IF EXISTS `rm_base_camp`;
CREATE TABLE `rm_base_camp` (
  `id` int(11) NOT NULL,
  `name` varchar(255) NOT NULL,
  `count` int(11) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of rm_base_camp
-- ----------------------------
INSERT INTO `rm_base_camp` VALUES ('1', 'ZTV1-浙江卫视【新闻综合频道】', '4');
INSERT INTO `rm_base_camp` VALUES ('2', 'ZTV2-钱江都市频道', '0');
INSERT INTO `rm_base_camp` VALUES ('3', 'ZTV3-浙江经济生活', '0');
INSERT INTO `rm_base_camp` VALUES ('4', 'ZTV4-教育科技频道', '0');
INSERT INTO `rm_base_camp` VALUES ('5', 'ZTV5-快乐5台影视娱乐', '0');
INSERT INTO `rm_base_camp` VALUES ('6', 'ZTV6-6频道民生休闲', '0');
INSERT INTO `rm_base_camp` VALUES ('7', 'ZTV7-公共新农村频道', '0');
INSERT INTO `rm_base_camp` VALUES ('8', 'ZTV8-少儿频道', '0');
INSERT INTO `rm_base_camp` VALUES ('9', 'ZTV9-留学世界频道，为数字电视频道', '0');
INSERT INTO `rm_base_camp` VALUES ('10', 'ZTV10-国际频道(台湾中华电信MOD 317频道)', '0');
INSERT INTO `rm_base_camp` VALUES ('11', 'ZTV11-好易购家庭购物频道', '0');

-- ----------------------------
-- Table structure for rm_base_match
-- ----------------------------
DROP TABLE IF EXISTS `rm_base_match`;
CREATE TABLE `rm_base_match` (
  `id` int(11) NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of rm_base_match
-- ----------------------------
INSERT INTO `rm_base_match` VALUES ('1', '第一场');
INSERT INTO `rm_base_match` VALUES ('2', '第二场');

-- ----------------------------
-- Table structure for rm_base_round
-- ----------------------------
DROP TABLE IF EXISTS `rm_base_round`;
CREATE TABLE `rm_base_round` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=103 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of rm_base_round
-- ----------------------------
INSERT INTO `rm_base_round` VALUES ('17', '第%d轮');
INSERT INTO `rm_base_round` VALUES ('18', '第%d轮');
INSERT INTO `rm_base_round` VALUES ('19', '第%d轮');
INSERT INTO `rm_base_round` VALUES ('20', '第%d轮');
INSERT INTO `rm_base_round` VALUES ('21', '第%d轮');
INSERT INTO `rm_base_round` VALUES ('22', '第%d轮');
INSERT INTO `rm_base_round` VALUES ('23', '第%d轮');
INSERT INTO `rm_base_round` VALUES ('24', '第%d轮');
INSERT INTO `rm_base_round` VALUES ('25', '第%d轮');
INSERT INTO `rm_base_round` VALUES ('26', '第%d轮');
INSERT INTO `rm_base_round` VALUES ('27', '第%d轮');
INSERT INTO `rm_base_round` VALUES ('28', '第%d轮');
INSERT INTO `rm_base_round` VALUES ('29', '第%d轮');
INSERT INTO `rm_base_round` VALUES ('30', '第%d轮');
INSERT INTO `rm_base_round` VALUES ('31', '第%d轮');
INSERT INTO `rm_base_round` VALUES ('32', '第%d轮');
INSERT INTO `rm_base_round` VALUES ('33', '第%d轮');
INSERT INTO `rm_base_round` VALUES ('34', '第%d轮');
INSERT INTO `rm_base_round` VALUES ('35', '第%d轮');
INSERT INTO `rm_base_round` VALUES ('36', '第%d轮');
INSERT INTO `rm_base_round` VALUES ('37', '第%d轮');
INSERT INTO `rm_base_round` VALUES ('38', '第%d轮');
INSERT INTO `rm_base_round` VALUES ('39', '第%d轮');
INSERT INTO `rm_base_round` VALUES ('40', '第%d轮');
INSERT INTO `rm_base_round` VALUES ('41', '第%d轮');
INSERT INTO `rm_base_round` VALUES ('42', '第%d轮');
INSERT INTO `rm_base_round` VALUES ('43', '第%d轮');
INSERT INTO `rm_base_round` VALUES ('44', '第%d轮');
INSERT INTO `rm_base_round` VALUES ('45', '第%d轮');
INSERT INTO `rm_base_round` VALUES ('46', '第%d轮');
INSERT INTO `rm_base_round` VALUES ('47', '第%d轮');
INSERT INTO `rm_base_round` VALUES ('48', '第%d轮');
INSERT INTO `rm_base_round` VALUES ('49', '第%d轮');
INSERT INTO `rm_base_round` VALUES ('50', '第%d轮');
INSERT INTO `rm_base_round` VALUES ('51', '第%d轮');
INSERT INTO `rm_base_round` VALUES ('52', '第%d轮');
INSERT INTO `rm_base_round` VALUES ('53', '第%d轮');
INSERT INTO `rm_base_round` VALUES ('54', '第%d轮');
INSERT INTO `rm_base_round` VALUES ('55', '第%d轮');
INSERT INTO `rm_base_round` VALUES ('56', '第%d轮');
INSERT INTO `rm_base_round` VALUES ('57', '第%d轮');
INSERT INTO `rm_base_round` VALUES ('58', '第%d轮');
INSERT INTO `rm_base_round` VALUES ('59', '第%d轮');
INSERT INTO `rm_base_round` VALUES ('60', '第%d轮');
INSERT INTO `rm_base_round` VALUES ('61', '第%d轮');
INSERT INTO `rm_base_round` VALUES ('62', '第%d轮');
INSERT INTO `rm_base_round` VALUES ('63', '第%d轮');
INSERT INTO `rm_base_round` VALUES ('64', '第%d轮');
INSERT INTO `rm_base_round` VALUES ('65', '第%d轮');
INSERT INTO `rm_base_round` VALUES ('66', '第%d轮');
INSERT INTO `rm_base_round` VALUES ('67', '第%d轮');
INSERT INTO `rm_base_round` VALUES ('68', '第%d轮');
INSERT INTO `rm_base_round` VALUES ('69', '第%d轮');
INSERT INTO `rm_base_round` VALUES ('70', '第%d轮');
INSERT INTO `rm_base_round` VALUES ('71', '第%d轮');
INSERT INTO `rm_base_round` VALUES ('72', '第%d轮');
INSERT INTO `rm_base_round` VALUES ('73', '第%d轮');
INSERT INTO `rm_base_round` VALUES ('74', '第%d轮');
INSERT INTO `rm_base_round` VALUES ('75', '第%d轮');
INSERT INTO `rm_base_round` VALUES ('76', '第%d轮');
INSERT INTO `rm_base_round` VALUES ('77', '第%d轮');
INSERT INTO `rm_base_round` VALUES ('78', '第%d轮');
INSERT INTO `rm_base_round` VALUES ('79', '第%d轮');
INSERT INTO `rm_base_round` VALUES ('80', '第%d轮');
INSERT INTO `rm_base_round` VALUES ('81', '第%d轮');
INSERT INTO `rm_base_round` VALUES ('82', '第%d轮');
INSERT INTO `rm_base_round` VALUES ('83', '第%d轮');
INSERT INTO `rm_base_round` VALUES ('84', '第%d轮');
INSERT INTO `rm_base_round` VALUES ('85', '第%d轮');
INSERT INTO `rm_base_round` VALUES ('86', '第%d轮');
INSERT INTO `rm_base_round` VALUES ('87', '第%d轮');
INSERT INTO `rm_base_round` VALUES ('88', '第%d轮');
INSERT INTO `rm_base_round` VALUES ('89', '第%d轮');
INSERT INTO `rm_base_round` VALUES ('90', '第%d轮');
INSERT INTO `rm_base_round` VALUES ('91', '第%d轮');
INSERT INTO `rm_base_round` VALUES ('92', '第%d轮');
INSERT INTO `rm_base_round` VALUES ('93', '第%d轮');
INSERT INTO `rm_base_round` VALUES ('94', '第%d轮');
INSERT INTO `rm_base_round` VALUES ('95', '第%d轮');
INSERT INTO `rm_base_round` VALUES ('96', '第%d轮');
INSERT INTO `rm_base_round` VALUES ('97', '第%d轮');
INSERT INTO `rm_base_round` VALUES ('98', '第%d轮');
INSERT INTO `rm_base_round` VALUES ('99', '第%d轮');
INSERT INTO `rm_base_round` VALUES ('100', '第%d轮');
INSERT INTO `rm_base_round` VALUES ('101', '第%d轮');
INSERT INTO `rm_base_round` VALUES ('102', '第%d轮');

-- ----------------------------
-- Table structure for rm_data
-- ----------------------------
DROP TABLE IF EXISTS `rm_data`;
CREATE TABLE `rm_data` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `data` text,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of rm_data
-- ----------------------------
INSERT INTO `rm_data` VALUES ('1', 'hello');
INSERT INTO `rm_data` VALUES ('2', 'hello');
INSERT INTO `rm_data` VALUES ('3', 'hello');

-- ----------------------------
-- Table structure for rm_match
-- ----------------------------
DROP TABLE IF EXISTS `rm_match`;
CREATE TABLE `rm_match` (
  `rm_camp_id` int(11) NOT NULL,
  `rm_base_match_id` int(11) NOT NULL,
  `rm_base_round_id` int(11) NOT NULL,
  `score` int(255) DEFAULT NULL,
  `start_time` datetime DEFAULT NULL,
  `end_time` datetime DEFAULT NULL,
  `count` int(11) NOT NULL,
  `status` int(11) NOT NULL,
  PRIMARY KEY (`rm_camp_id`,`rm_base_match_id`,`rm_base_round_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of rm_match
-- ----------------------------
INSERT INTO `rm_match` VALUES ('1', '1', '95', '1341', '2015-01-26 20:00:39', '2015-01-26 20:01:12', '11', '2');
INSERT INTO `rm_match` VALUES ('1', '1', '96', '1068', '2015-01-26 20:03:06', '2015-01-26 20:03:23', '9', '2');
INSERT INTO `rm_match` VALUES ('1', '1', '97', '1121', '2015-01-26 20:08:13', '2015-01-26 20:08:55', '9', '2');
INSERT INTO `rm_match` VALUES ('1', '1', '98', '1080', '2015-01-26 20:14:21', '2015-01-26 20:14:41', '9', '2');
INSERT INTO `rm_match` VALUES ('1', '1', '99', '1067', '2015-01-26 20:27:17', '2015-01-26 20:28:00', '9', '2');
INSERT INTO `rm_match` VALUES ('1', '1', '100', '0', null, null, '1', '0');
INSERT INTO `rm_match` VALUES ('1', '1', '101', '0', null, null, '1', '0');
INSERT INTO `rm_match` VALUES ('1', '1', '102', '1081', '2015-01-26 20:37:19', '2015-01-26 20:37:47', '9', '2');
INSERT INTO `rm_match` VALUES ('1', '2', '95', '0', null, null, '1', '0');
INSERT INTO `rm_match` VALUES ('1', '2', '96', '0', null, null, '1', '0');
INSERT INTO `rm_match` VALUES ('1', '2', '97', '0', null, null, '1', '0');
INSERT INTO `rm_match` VALUES ('1', '2', '98', '0', null, null, '1', '0');
INSERT INTO `rm_match` VALUES ('1', '2', '99', '0', null, null, '1', '0');
INSERT INTO `rm_match` VALUES ('1', '2', '100', '0', null, null, '1', '0');
INSERT INTO `rm_match` VALUES ('1', '2', '101', '0', null, null, '1', '0');
INSERT INTO `rm_match` VALUES ('1', '2', '102', '0', null, null, '1', '0');
INSERT INTO `rm_match` VALUES ('2', '1', '95', '1487', '2015-01-26 20:00:39', '2015-01-26 20:01:55', '12', '2');
INSERT INTO `rm_match` VALUES ('2', '1', '96', '1013', '2015-01-26 20:03:06', '2015-01-26 20:03:23', '9', '2');
INSERT INTO `rm_match` VALUES ('2', '1', '97', '1040', '2015-01-26 20:08:13', '2015-01-26 20:08:55', '9', '2');
INSERT INTO `rm_match` VALUES ('2', '1', '98', '1054', '2015-01-26 20:14:21', '2015-01-26 20:14:41', '9', '2');
INSERT INTO `rm_match` VALUES ('2', '1', '99', '1082', '2015-01-26 20:27:17', '2015-01-26 20:28:00', '9', '1');
INSERT INTO `rm_match` VALUES ('2', '1', '100', '0', null, null, '1', '0');
INSERT INTO `rm_match` VALUES ('2', '1', '101', '0', null, null, '1', '0');
INSERT INTO `rm_match` VALUES ('2', '1', '102', '1048', '2015-01-26 20:37:19', '2015-01-26 20:37:47', '9', '2');
INSERT INTO `rm_match` VALUES ('2', '2', '95', '0', null, null, '1', '0');
INSERT INTO `rm_match` VALUES ('2', '2', '96', '0', null, null, '1', '0');
INSERT INTO `rm_match` VALUES ('2', '2', '97', '0', null, null, '1', '0');
INSERT INTO `rm_match` VALUES ('2', '2', '98', '0', null, null, '1', '0');
INSERT INTO `rm_match` VALUES ('2', '2', '99', '0', null, null, '1', '0');
INSERT INTO `rm_match` VALUES ('2', '2', '100', '0', null, null, '1', '0');
INSERT INTO `rm_match` VALUES ('2', '2', '101', '0', null, null, '1', '0');
INSERT INTO `rm_match` VALUES ('2', '2', '102', '0', null, null, '1', '0');
INSERT INTO `rm_match` VALUES ('3', '1', '99', '1081', '2015-01-26 20:27:17', '2015-01-26 20:28:00', '9', '1');
INSERT INTO `rm_match` VALUES ('3', '1', '100', '0', null, null, '1', '0');
INSERT INTO `rm_match` VALUES ('3', '1', '101', '0', null, null, '1', '0');
INSERT INTO `rm_match` VALUES ('3', '1', '102', '11707', '2015-01-26 20:37:19', '2015-01-26 20:40:26', '88', '1');
INSERT INTO `rm_match` VALUES ('3', '2', '99', '0', null, null, '1', '0');
INSERT INTO `rm_match` VALUES ('3', '2', '100', '0', null, null, '1', '0');
INSERT INTO `rm_match` VALUES ('3', '2', '101', '0', null, null, '1', '0');
INSERT INTO `rm_match` VALUES ('3', '2', '102', '0', null, null, '1', '0');
INSERT INTO `rm_match` VALUES ('4', '1', '99', '1070', '2015-01-26 20:27:17', '2015-01-26 20:28:00', '9', '1');
INSERT INTO `rm_match` VALUES ('4', '1', '100', '0', null, null, '1', '0');
INSERT INTO `rm_match` VALUES ('4', '1', '101', '0', null, null, '1', '0');
INSERT INTO `rm_match` VALUES ('4', '1', '102', '11608', '2015-01-26 20:37:19', '2015-01-26 20:40:26', '88', '1');
INSERT INTO `rm_match` VALUES ('4', '2', '99', '0', null, null, '1', '0');
INSERT INTO `rm_match` VALUES ('4', '2', '100', '0', null, null, '1', '0');
INSERT INTO `rm_match` VALUES ('4', '2', '101', '0', null, null, '1', '0');
INSERT INTO `rm_match` VALUES ('4', '2', '102', '0', null, null, '1', '0');
INSERT INTO `rm_match` VALUES ('5', '1', '99', '1082', '2015-01-26 20:27:17', '2015-01-26 20:28:00', '9', '2');
INSERT INTO `rm_match` VALUES ('5', '1', '100', '0', null, null, '1', '0');
INSERT INTO `rm_match` VALUES ('5', '1', '101', '0', null, null, '1', '0');
INSERT INTO `rm_match` VALUES ('5', '1', '102', '11649', '2015-01-26 20:37:19', '2015-01-26 20:40:26', '88', '1');
INSERT INTO `rm_match` VALUES ('5', '2', '99', '0', null, null, '1', '0');
INSERT INTO `rm_match` VALUES ('5', '2', '100', '0', null, null, '1', '0');
INSERT INTO `rm_match` VALUES ('5', '2', '101', '0', null, null, '1', '0');
INSERT INTO `rm_match` VALUES ('5', '2', '102', '0', null, null, '1', '0');
INSERT INTO `rm_match` VALUES ('6', '1', '99', '1031', '2015-01-26 20:27:17', '2015-01-26 20:28:00', '9', '1');
INSERT INTO `rm_match` VALUES ('6', '1', '100', '0', null, null, '1', '0');
INSERT INTO `rm_match` VALUES ('6', '1', '101', '0', null, null, '1', '0');
INSERT INTO `rm_match` VALUES ('6', '1', '102', '11575', '2015-01-26 20:37:19', '2015-01-26 20:40:26', '88', '1');
INSERT INTO `rm_match` VALUES ('6', '2', '99', '0', null, null, '1', '0');
INSERT INTO `rm_match` VALUES ('6', '2', '100', '0', null, null, '1', '0');
INSERT INTO `rm_match` VALUES ('6', '2', '101', '0', null, null, '1', '0');
INSERT INTO `rm_match` VALUES ('6', '2', '102', '0', null, null, '1', '0');
INSERT INTO `rm_match` VALUES ('7', '1', '99', '1063', '2015-01-26 20:27:17', '2015-01-26 20:28:00', '9', '1');
INSERT INTO `rm_match` VALUES ('7', '1', '100', '0', null, null, '1', '0');
INSERT INTO `rm_match` VALUES ('7', '1', '101', '0', null, null, '1', '0');
INSERT INTO `rm_match` VALUES ('7', '1', '102', '11692', '2015-01-26 20:37:19', '2015-01-26 20:40:26', '88', '1');
INSERT INTO `rm_match` VALUES ('7', '2', '99', '0', null, null, '1', '0');
INSERT INTO `rm_match` VALUES ('7', '2', '100', '0', null, null, '1', '0');
INSERT INTO `rm_match` VALUES ('7', '2', '101', '0', null, null, '1', '0');
INSERT INTO `rm_match` VALUES ('7', '2', '102', '0', null, null, '1', '0');
INSERT INTO `rm_match` VALUES ('8', '1', '99', '945', '2015-01-26 20:27:17', '2015-01-26 20:27:58', '8', '1');
INSERT INTO `rm_match` VALUES ('8', '1', '100', '0', null, null, '1', '0');
INSERT INTO `rm_match` VALUES ('8', '1', '101', '0', null, null, '1', '0');
INSERT INTO `rm_match` VALUES ('8', '1', '102', '11658', '2015-01-26 20:37:19', '2015-01-26 20:40:26', '88', '1');
INSERT INTO `rm_match` VALUES ('8', '2', '99', '0', null, null, '1', '0');
INSERT INTO `rm_match` VALUES ('8', '2', '100', '0', null, null, '1', '0');
INSERT INTO `rm_match` VALUES ('8', '2', '101', '0', null, null, '1', '0');
INSERT INTO `rm_match` VALUES ('8', '2', '102', '0', null, null, '1', '0');
INSERT INTO `rm_match` VALUES ('9', '1', '99', '954', '2015-01-26 20:27:17', '2015-01-26 20:27:58', '8', '1');
INSERT INTO `rm_match` VALUES ('9', '1', '100', '0', null, null, '1', '0');
INSERT INTO `rm_match` VALUES ('9', '1', '101', '0', null, null, '1', '0');
INSERT INTO `rm_match` VALUES ('9', '1', '102', '11616', '2015-01-26 20:37:19', '2015-01-26 20:40:27', '88', '1');
INSERT INTO `rm_match` VALUES ('9', '2', '99', '0', null, null, '1', '0');
INSERT INTO `rm_match` VALUES ('9', '2', '100', '0', null, null, '1', '0');
INSERT INTO `rm_match` VALUES ('9', '2', '101', '0', null, null, '1', '0');
INSERT INTO `rm_match` VALUES ('9', '2', '102', '0', null, null, '1', '0');
INSERT INTO `rm_match` VALUES ('10', '1', '99', '899', '2015-01-26 20:27:17', '2015-01-26 20:27:58', '8', '1');
INSERT INTO `rm_match` VALUES ('10', '1', '100', '0', null, null, '1', '0');
INSERT INTO `rm_match` VALUES ('10', '1', '101', '0', null, null, '1', '0');
INSERT INTO `rm_match` VALUES ('10', '1', '102', '11656', '2015-01-26 20:37:19', '2015-01-26 20:40:27', '88', '1');
INSERT INTO `rm_match` VALUES ('10', '2', '99', '0', null, null, '1', '0');
INSERT INTO `rm_match` VALUES ('10', '2', '100', '0', null, null, '1', '0');
INSERT INTO `rm_match` VALUES ('10', '2', '101', '0', null, null, '1', '0');
INSERT INTO `rm_match` VALUES ('10', '2', '102', '0', null, null, '1', '0');
INSERT INTO `rm_match` VALUES ('11', '1', '99', '936', '2015-01-26 20:27:17', '2015-01-26 20:27:58', '8', '1');
INSERT INTO `rm_match` VALUES ('11', '1', '100', '0', null, null, '1', '0');
INSERT INTO `rm_match` VALUES ('11', '1', '101', '0', null, null, '1', '0');
INSERT INTO `rm_match` VALUES ('11', '1', '102', '11617', '2015-01-26 20:37:19', '2015-01-26 20:40:27', '88', '1');
INSERT INTO `rm_match` VALUES ('11', '2', '99', '0', null, null, '1', '0');
INSERT INTO `rm_match` VALUES ('11', '2', '100', '0', null, null, '1', '0');
INSERT INTO `rm_match` VALUES ('11', '2', '101', '0', null, null, '1', '0');
INSERT INTO `rm_match` VALUES ('11', '2', '102', '0', null, null, '1', '0');

-- ----------------------------
-- Table structure for rm_user
-- ----------------------------
DROP TABLE IF EXISTS `rm_user`;
CREATE TABLE `rm_user` (
  `id` int(11) NOT NULL,
  `rm_camp_id` int(11) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of rm_user
-- ----------------------------
INSERT INTO `rm_user` VALUES ('1', '1', '张三');
INSERT INTO `rm_user` VALUES ('2', '2', '李四');
