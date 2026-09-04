-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: Sep 04, 2026 at 11:15 PM
-- Server version: 10.4.32-MariaDB
-- PHP Version: 8.0.30

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `dental_clinic_db`
--

DELIMITER $$
--
-- Procedures
--
CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_generate_bill` (IN `p_appointment_id` INT)   BEGIN
    DECLARE v_fee DECIMAL(10,2);

    SELECT t.consultation_fee INTO v_fee
    FROM appointments a
    JOIN treatments t ON a.treatment_id = t.treatment_id
    WHERE a.appointment_id = p_appointment_id;

    INSERT INTO bills (appointment_id, total_amount, generated_date)
    VALUES (p_appointment_id, v_fee, NOW());
END$$

--
-- Functions
--
CREATE DEFINER=`root`@`localhost` FUNCTION `fn_dentist_appointment_count` (`p_dentist_id` INT, `p_date` DATE) RETURNS INT(11) DETERMINISTIC BEGIN
    DECLARE v_count INT;
    SELECT COUNT(*) INTO v_count
    FROM appointments
    WHERE dentist_id = p_dentist_id AND appointment_date = p_date;
    RETURN v_count;
END$$

DELIMITER ;

-- --------------------------------------------------------

--
-- Table structure for table `appointments`
--

CREATE TABLE `appointments` (
  `appointment_id` int(11) NOT NULL,
  `appointment_no` varchar(20) DEFAULT NULL,
  `patient_id` int(11) NOT NULL,
  `dentist_id` int(11) NOT NULL,
  `treatment_id` int(11) NOT NULL,
  `appointment_date` date NOT NULL,
  `appointment_time` time NOT NULL,
  `status` enum('PENDING','COMPLETED','CANCELLED') DEFAULT 'PENDING'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `appointments`
--

INSERT INTO `appointments` (`appointment_id`, `appointment_no`, `patient_id`, `dentist_id`, `treatment_id`, `appointment_date`, `appointment_time`, `status`) VALUES
(2, 'APT00002', 2, 2, 2, '2026-09-24', '22:31:00', 'PENDING'),
(3, 'APT00003', 3, 1, 4, '2026-10-01', '13:06:00', 'PENDING'),
(4, 'APT00004', 4, 2, 3, '2026-09-03', '23:09:00', 'PENDING'),
(5, 'APT00005', 5, 1, 3, '2026-10-01', '13:07:00', 'CANCELLED'),
(6, 'APT00006', 6, 1, 1, '2026-09-23', '23:20:00', 'PENDING'),
(7, 'APT00007', 7, 2, 2, '2026-09-24', '12:20:00', 'PENDING'),
(8, 'APT00008', 10, 2, 3, '2026-09-03', '23:10:00', 'PENDING'),
(9, 'APT00009', 11, 4, 1, '2026-09-10', '09:00:00', 'PENDING'),
(10, 'APT00010', 12, 5, 2, '2026-09-11', '10:30:00', 'PENDING'),
(11, 'APT00011', 13, 6, 3, '2026-09-12', '14:00:00', 'PENDING'),
(12, 'APT00012', 14, 6, 1, '2026-09-04', '02:41:00', 'PENDING'),
(13, 'APT00013', 15, 2, 5, '2026-09-04', '01:25:00', 'PENDING'),
(14, 'APT00014', 16, 2, 2, '2026-09-04', '01:24:00', 'PENDING'),
(15, 'APT00015', 17, 5, 1, '2026-09-05', '00:56:00', 'PENDING'),
(16, 'APT00016', 18, 1, 1, '2026-09-05', '01:02:00', 'PENDING');

--
-- Triggers `appointments`
--
DELIMITER $$
CREATE TRIGGER `trg_prevent_double_booking_insert` BEFORE INSERT ON `appointments` FOR EACH ROW BEGIN
    DECLARE conflict_count INT;

    SELECT COUNT(*) INTO conflict_count
    FROM appointments
    WHERE dentist_id = NEW.dentist_id
      AND appointment_date = NEW.appointment_date
      AND appointment_time = NEW.appointment_time
      AND status != 'CANCELLED';

    IF conflict_count > 0 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Double booking: this dentist already has an appointment at this date and time.';
    END IF;
END
$$
DELIMITER ;
DELIMITER $$
CREATE TRIGGER `trg_prevent_double_booking_update` BEFORE UPDATE ON `appointments` FOR EACH ROW BEGIN
    DECLARE conflict_count INT;

    IF NEW.status != 'CANCELLED' THEN
        SELECT COUNT(*) INTO conflict_count
        FROM appointments
        WHERE dentist_id = NEW.dentist_id
          AND appointment_date = NEW.appointment_date
          AND appointment_time = NEW.appointment_time
          AND status != 'CANCELLED'
          AND appointment_id != NEW.appointment_id;

        IF conflict_count > 0 THEN
            SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Double booking: this dentist already has an appointment at this date and time.';
        END IF;
    END IF;
END
$$
DELIMITER ;

-- --------------------------------------------------------

--
-- Table structure for table `bills`
--

CREATE TABLE `bills` (
  `bill_id` int(11) NOT NULL,
  `appointment_id` int(11) NOT NULL,
  `total_amount` decimal(10,2) NOT NULL,
  `generated_date` datetime DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `bills`
--

INSERT INTO `bills` (`bill_id`, `appointment_id`, `total_amount`, `generated_date`) VALUES
(1, 7, 3500.00, '2026-09-04 00:09:39'),
(2, 2, 3500.00, '2026-09-04 00:09:51'),
(3, 8, 12000.00, '2026-09-04 00:10:02'),
(4, 6, 1000.00, '2026-09-04 00:10:05'),
(5, 13, 100000.00, '2026-09-04 01:24:12'),
(6, 14, 3500.00, '2026-09-04 01:24:57'),
(7, 12, 1000.00, '2026-09-04 12:52:48'),
(8, 10, 3500.00, '2026-09-04 21:30:00'),
(9, 15, 1000.00, '2026-09-05 00:58:50');

-- --------------------------------------------------------

--
-- Table structure for table `dentists`
--

CREATE TABLE `dentists` (
  `dentist_id` int(11) NOT NULL,
  `name` varchar(100) NOT NULL,
  `specialization` varchar(100) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `dentists`
--

INSERT INTO `dentists` (`dentist_id`, `name`, `specialization`) VALUES
(1, 'Dr. Silva', 'General Dentistry specialist'),
(2, 'Dr. Perera', 'Orthodontics'),
(3, 'test1', 'test 1'),
(4, 'Dr. Fernando', 'Orthodontics'),
(5, 'Dr. Jayasinghe', 'General Dentistry'),
(6, 'Dr. Wickramasinghe', 'Oral Surgery');

-- --------------------------------------------------------

--
-- Table structure for table `notifications`
--

CREATE TABLE `notifications` (
  `notification_id` int(11) NOT NULL,
  `appointment_id` int(11) NOT NULL,
  `message` varchar(255) NOT NULL,
  `channel` enum('EMAIL','SMS') NOT NULL,
  `sent_at` datetime DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `notifications`
--

INSERT INTO `notifications` (`notification_id`, `appointment_id`, `message`, `channel`, `sent_at`) VALUES
(1, 10, 'Reminder: Your dental appointment is scheduled for 2026-09-11 at 10:30.', 'EMAIL', '2026-09-04 00:45:32'),
(2, 9, 'Reminder: Your dental appointment is scheduled for 2026-09-10 at 09:00.', 'EMAIL', '2026-09-04 00:45:50'),
(3, 13, 'Reminder: Your dental appointment is scheduled for 2026-09-04 at 01:25.', 'EMAIL', '2026-09-04 01:25:15'),
(4, 16, 'Reminder: Your dental appointment is scheduled for 2026-09-05 at 01:02.', 'EMAIL', '2026-09-05 01:48:36');

-- --------------------------------------------------------

--
-- Table structure for table `patients`
--

CREATE TABLE `patients` (
  `patient_id` int(11) NOT NULL,
  `name` varchar(100) NOT NULL,
  `address` varchar(255) DEFAULT NULL,
  `contact_number` varchar(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `patients`
--

INSERT INTO `patients` (`patient_id`, `name`, `address`, `contact_number`) VALUES
(2, 'new', 'new', '07122222222'),
(3, 'bb', 'b', '111111111111111111'),
(4, 'aa', 'aa', '111111111111111111'),
(5, 'zz', 'zz', '07122222222'),
(6, 'ss', 'ss', '11111111'),
(7, 'aaaa', 'aaa', '11111111'),
(10, 'sssssss', 'ssssssss', '11111111111111'),
(11, 'Kasun Perera', 'Colombo 05', '0771234567'),
(12, 'Nimali Silva', 'Nugegoda', '0712345678'),
(13, 'Sahan Fernando', 'Dehiwala', '0763456789'),
(14, 'new', 'new', '111111111111111111'),
(15, 'qq', '11', '11111111111111111111'),
(16, 'aaaaaaaaa', '1aaaaaa', '11111111111111111111'),
(17, 'DOC', 'DOC', '07112345678'),
(18, 'abc', 'abc', '07123456789');

-- --------------------------------------------------------

--
-- Table structure for table `treatments`
--

CREATE TABLE `treatments` (
  `treatment_id` int(11) NOT NULL,
  `treatment_name` varchar(100) NOT NULL,
  `consultation_fee` decimal(10,2) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `treatments`
--

INSERT INTO `treatments` (`treatment_id`, `treatment_name`, `consultation_fee`) VALUES
(1, 'Consultation', 1000.00),
(2, 'Tooth Filling', 3500.00),
(3, 'Root Canal', 12000.00),
(4, 'Teeth Cleaning', 2500.00),
(5, 'test', 100000.00);

-- --------------------------------------------------------

--
-- Table structure for table `users`
--

CREATE TABLE `users` (
  `staff_id` int(11) NOT NULL,
  `username` varchar(50) NOT NULL,
  `password` varchar(255) NOT NULL,
  `role` enum('ADMIN','RECEPTIONIST') NOT NULL DEFAULT 'RECEPTIONIST',
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `is_active` tinyint(1) NOT NULL DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `users`
--

INSERT INTO `users` (`staff_id`, `username`, `password`, `role`, `created_at`, `is_active`) VALUES
(1, 'admin', 're367e5J5p7JIHDKEkkxag==:yWY1R2gwPAxc+xs5T4nVRQ3e7Cs9RqjfN0Dl/gUXkFE=', 'ADMIN', '2026-09-02 16:00:03', 1),
(2, 'reception1', 'iMuCvrBAekEUODDRS4WWxw==:Kva4H4GCsf1EowYyP0m8S8Of9l8gbszGlYAmeWz106o=', 'RECEPTIONIST', '2026-09-02 16:00:03', 1),
(3, '2', 'iMuCvrBAekEUODDRS4WWxw==:Kva4H4GCsf1EowYyP0m8S8Of9l8gbszGlYAmeWz106o=', 'RECEPTIONIST', '2026-09-03 10:53:28', 1),
(4, 'malindu', '84PqR9XtMsynEUvJl5YeEQ==:W5X8AovmKe411ZuWdiBOjFZYhQaD+Dilfv6FSIv8eq4=', 'ADMIN', '2026-09-03 11:08:33', 1),
(5, 'staff', 'Rx0dBS/3wFfg//O2cgb1Fg==:IH50xeO20AaGYzKnn/TUx8LlBsjJYNIUzni2jq0Z87Y=', 'RECEPTIONIST', '2026-09-03 20:25:15', 1),
(6, 'newtest', 'jIUQ51mOhUHQ9IemgNqm1w==:98Y5Ey8t1FPrPj4Wnp5YfjN8kdH91UHbsNwQMh+sGQ8=', 'RECEPTIONIST', '2026-09-04 06:50:55', 1),
(7, 'aaaaaaaa', '+iXOCcSfZCqf+jCs89Plwg==:oYUpg22/0Kgvf+PY2h9ZQcJ7YgzAp9CaXxykD6dOYgQ=', 'RECEPTIONIST', '2026-09-04 06:58:54', 1);

--
-- Indexes for dumped tables
--

--
-- Indexes for table `appointments`
--
ALTER TABLE `appointments`
  ADD PRIMARY KEY (`appointment_id`),
  ADD UNIQUE KEY `appointment_no` (`appointment_no`),
  ADD KEY `patient_id` (`patient_id`),
  ADD KEY `dentist_id` (`dentist_id`),
  ADD KEY `treatment_id` (`treatment_id`);

--
-- Indexes for table `bills`
--
ALTER TABLE `bills`
  ADD PRIMARY KEY (`bill_id`),
  ADD KEY `appointment_id` (`appointment_id`);

--
-- Indexes for table `dentists`
--
ALTER TABLE `dentists`
  ADD PRIMARY KEY (`dentist_id`);

--
-- Indexes for table `notifications`
--
ALTER TABLE `notifications`
  ADD PRIMARY KEY (`notification_id`),
  ADD KEY `appointment_id` (`appointment_id`);

--
-- Indexes for table `patients`
--
ALTER TABLE `patients`
  ADD PRIMARY KEY (`patient_id`);

--
-- Indexes for table `treatments`
--
ALTER TABLE `treatments`
  ADD PRIMARY KEY (`treatment_id`);

--
-- Indexes for table `users`
--
ALTER TABLE `users`
  ADD PRIMARY KEY (`staff_id`),
  ADD UNIQUE KEY `username` (`username`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `appointments`
--
ALTER TABLE `appointments`
  MODIFY `appointment_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=17;

--
-- AUTO_INCREMENT for table `bills`
--
ALTER TABLE `bills`
  MODIFY `bill_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=10;

--
-- AUTO_INCREMENT for table `dentists`
--
ALTER TABLE `dentists`
  MODIFY `dentist_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=7;

--
-- AUTO_INCREMENT for table `notifications`
--
ALTER TABLE `notifications`
  MODIFY `notification_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=5;

--
-- AUTO_INCREMENT for table `patients`
--
ALTER TABLE `patients`
  MODIFY `patient_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=20;

--
-- AUTO_INCREMENT for table `treatments`
--
ALTER TABLE `treatments`
  MODIFY `treatment_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=6;

--
-- AUTO_INCREMENT for table `users`
--
ALTER TABLE `users`
  MODIFY `staff_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=8;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `appointments`
--
ALTER TABLE `appointments`
  ADD CONSTRAINT `appointments_ibfk_1` FOREIGN KEY (`patient_id`) REFERENCES `patients` (`patient_id`),
  ADD CONSTRAINT `appointments_ibfk_2` FOREIGN KEY (`dentist_id`) REFERENCES `dentists` (`dentist_id`),
  ADD CONSTRAINT `appointments_ibfk_3` FOREIGN KEY (`treatment_id`) REFERENCES `treatments` (`treatment_id`);

--
-- Constraints for table `bills`
--
ALTER TABLE `bills`
  ADD CONSTRAINT `bills_ibfk_1` FOREIGN KEY (`appointment_id`) REFERENCES `appointments` (`appointment_id`);

--
-- Constraints for table `notifications`
--
ALTER TABLE `notifications`
  ADD CONSTRAINT `notifications_ibfk_1` FOREIGN KEY (`appointment_id`) REFERENCES `appointments` (`appointment_id`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
