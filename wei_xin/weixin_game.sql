/*
Navicat MySQL Data Transfer

Source Server         : local
Source Server Version : 50620
Source Host           : 192.168.2.254:3306
Source Database       : weixin_game

Target Server Type    : MYSQL
Target Server Version : 50620
File Encoding         : 65001

Date: 2015-02-03 16:44:06
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for cfg_game
-- ----------------------------
DROP TABLE IF EXISTS `cfg_game`;
CREATE TABLE `cfg_game` (
  `id` int(255) NOT NULL,
  `name` varchar(255) NOT NULL,
  `repute` int(255) NOT NULL,
  `cfg_game_type_id` int(255) NOT NULL,
  `url` varchar(255) NOT NULL,
  `logo` varchar(255) NOT NULL,
  `od` int(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='游戏配置表';

-- ----------------------------
-- Records of cfg_game
-- ----------------------------
INSERT INTO `cfg_game` VALUES ('1', '小鸟试玩版', '1054', '3', 'http://183.136.221.50:99/birdDemo/index.html', 'http://183.136.221.50:99/birdDemo/resource/assets/web/logo.png', '0');
INSERT INTO `cfg_game` VALUES ('2', '消灭星星', '1052', '1', 'http://183.136.221.50:99/xmxx/index.html', 'http://183.136.221.50:99/xmxx/resource/assets/web/icon.png', '0');
INSERT INTO `cfg_game` VALUES ('3', '看图猜成语', '1072', '1', 'http://183.136.221.50:99/ccy/index.html', 'http://183.136.221.50:99/ccy/resource/assets/web/icon.png', '0');
INSERT INTO `cfg_game` VALUES ('4', '打鸟', '1003', '2', 'http://183.136.221.50:99/ShootBird/index.html', 'http://183.136.221.50:99/ShootBird/resource/assets/logo.png', '0');
INSERT INTO `cfg_game` VALUES ('5', '抓小偷', '5', '2', 'http://183.136.221.50:99/clickBird/index.html', 'http://183.136.221.50:99/clickBird/resource/assets/pilcewoman.png', '0');
INSERT INTO `cfg_game` VALUES ('6', '极限躲避', '1', '2', 'http://183.136.221.50:99/Escape/index.html', 'http://183.136.221.50:99/Escape/resource/assets/logo.png', '0');
INSERT INTO `cfg_game` VALUES ('7', '疯狂记忆', '4046', '1', 'http://183.136.221.50:99/crazyBrain/index.html', 'http://183.136.221.50:99/crazyBrain/resource/assets/logo.png', '0');
INSERT INTO `cfg_game` VALUES ('8', '大家都来找妹子', '1', '1', 'http://183.136.221.50:99/FindSister/index.html', 'http://183.136.221.50:99/FindSister/resource/assets/logo.png', '0');
INSERT INTO `cfg_game` VALUES ('9', '舒尔特方格', '0', '1', 'http://183.136.221.50:99/setbg/index.html', 'http://183.136.221.50:99/setbg/resource/assets/web/icon.png', '0');
INSERT INTO `cfg_game` VALUES ('10', '人体奥秘之左右脑协调', '1', '1', 'http://183.136.221.50:99/StrongestBrain/index.html', 'http://183.136.221.50:99/StrongestBrain/resource/assets/logo.png', '0');

-- ----------------------------
-- Table structure for cfg_game_cfg_game_type
-- ----------------------------
DROP TABLE IF EXISTS `cfg_game_cfg_game_type`;
CREATE TABLE `cfg_game_cfg_game_type` (
  `cfg_game_id` int(11) NOT NULL,
  `cfg_game_type_id` int(11) NOT NULL,
  `weight` double NOT NULL COMMENT '权重',
  PRIMARY KEY (`cfg_game_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='游戏类型映射表';

-- ----------------------------
-- Records of cfg_game_cfg_game_type
-- ----------------------------
INSERT INTO `cfg_game_cfg_game_type` VALUES ('1', '1', '1024');

-- ----------------------------
-- Table structure for cfg_game_type
-- ----------------------------
DROP TABLE IF EXISTS `cfg_game_type`;
CREATE TABLE `cfg_game_type` (
  `id` int(11) NOT NULL,
  `name` varchar(255) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='游戏类型';

-- ----------------------------
-- Records of cfg_game_type
-- ----------------------------
INSERT INTO `cfg_game_type` VALUES ('1', '休闲益智');
INSERT INTO `cfg_game_type` VALUES ('2', '动作游戏');
INSERT INTO `cfg_game_type` VALUES ('3', '酷跑游戏');

-- ----------------------------
-- Table structure for user_account
-- ----------------------------
DROP TABLE IF EXISTS `user_account`;
CREATE TABLE `user_account` (
  `id` int(20) NOT NULL AUTO_INCREMENT,
  `openid` varchar(100) NOT NULL,
  `nickname` varchar(255) NOT NULL,
  `sex` varchar(255) NOT NULL,
  `province` varchar(255) NOT NULL,
  `city` varchar(255) NOT NULL,
  `country` varchar(255) NOT NULL,
  `headimgurl` varchar(255) NOT NULL,
  `gold` int(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `appId_unique` (`openid`) USING HASH
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8 COMMENT='用户表';

-- ----------------------------
-- Records of user_account
-- ----------------------------
INSERT INTO `user_account` VALUES ('1', '1', '1', 'sex', 'p', 'c', 'country', 'headimgurl', '0');
INSERT INTO `user_account` VALUES ('2', '2', '2', 'sex', 'p', 'c', 'country', 'headimgurl', '0');
INSERT INTO `user_account` VALUES ('3', '3', '3', 'sex', 'p', 'c', '??', 'headimgurl', '0');
INSERT INTO `user_account` VALUES ('4', '4', '4', 'sex', 'p', 'c', '浙江', 'headimgurl', '0');
INSERT INTO `user_account` VALUES ('5', 'oMAUxtzSDcA7Z68KzTc8HJFHj1ZU', '荫', '1', 'Zhejiang', 'Hangzhou', 'CN', 'http://wx.qlogo.cn/mmopen/9s3XO9eXEkX14I0LIiaLBDibzVmNxHzgnAm8OHUFMDTxDCpaia3g83etjibMStYqVn7lIGBPmvDpmNoAJgwxZ1Q60ZK8sdOpCy0m/0', '0');
INSERT INTO `user_account` VALUES ('6', 'oMAUxtzGbgqRQgh5NFxTdnDP3pXA', '曹长利', '1', 'Zhejiang', 'Hangzhou', 'CN', 'http://wx.qlogo.cn/mmopen/INk4JvWfe8XkG10xaGVLHZDWf0pEic66wSwgzXSqXwrYdibbaG20ZfpksYeO3dJFWnkVVtTibuT7ibC5SWXvjKRUQw/0', '0');
INSERT INTO `user_account` VALUES ('7', '测试_openid', '测试_nickname', '测试_sex', '测试_province', '测试_city', '测试_country', '测试_headimgurl', '0');
INSERT INTO `user_account` VALUES ('8', '', '', '', '', '', '', '', '0');

-- ----------------------------
-- Table structure for user_account_cfg_game
-- ----------------------------
DROP TABLE IF EXISTS `user_account_cfg_game`;
CREATE TABLE `user_account_cfg_game` (
  `user_account_id` int(11) NOT NULL,
  `cfg_game_id` int(11) NOT NULL,
  `score` double(255,0) NOT NULL COMMENT '积分',
  PRIMARY KEY (`user_account_id`,`cfg_game_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='用户用户游戏表';

-- ----------------------------
-- Records of user_account_cfg_game
-- ----------------------------
INSERT INTO `user_account_cfg_game` VALUES ('1', '1', '52');
INSERT INTO `user_account_cfg_game` VALUES ('1', '7', '0');
INSERT INTO `user_account_cfg_game` VALUES ('5', '1', '53');
INSERT INTO `user_account_cfg_game` VALUES ('5', '3', '0');
INSERT INTO `user_account_cfg_game` VALUES ('5', '4', '0');
INSERT INTO `user_account_cfg_game` VALUES ('5', '5', '0');
INSERT INTO `user_account_cfg_game` VALUES ('6', '2', '5505');
INSERT INTO `user_account_cfg_game` VALUES ('6', '7', '0');
