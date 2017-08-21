-- phpMyAdmin SQL Dump
-- version 4.6.6
-- https://www.phpmyadmin.net/

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

CREATE TABLE `timetracking` (
  `record_id` int(11) NOT NULL,
  `date` date NOT NULL,
  `user` char(2) NOT NULL,
  `activity_group` varchar(20) NOT NULL,
  `activity` varchar(100) NOT NULL,
  `start_time` double NOT NULL,
  `end_time` double NOT NULL,
  `duration` double NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

ALTER TABLE `zeiterfassung`
  ADD PRIMARY KEY (`record_id`);

ALTER TABLE `zeiterfassung`
  MODIFY `record_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=16;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
