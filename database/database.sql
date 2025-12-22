-- Database setup
DROP DATABASE IF EXISTS worklink_db;
CREATE DATABASE IF NOT EXISTS worklink_db;
USE worklink_db;

-- Stores user accounts (institutes and freelancers)
CREATE TABLE IF NOT EXISTS Users (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role ENUM('INSTITUTE', 'JOB_SEEKER') NOT NULL
);

-- Job listings posted by institutes
CREATE TABLE IF NOT EXISTS Jobs (
    id INT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(100) NOT NULL,
    content TEXT NOT NULL,
    budget VARCHAR(50),
    institute_id INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (institute_id) REFERENCES Users(id)
);

-- Tracks which freelancers applied to which jobs
CREATE TABLE IF NOT EXISTS JobApplications (
    id INT PRIMARY KEY AUTO_INCREMENT,
    job_id INT,
    jobseeker_id INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (job_id) REFERENCES Jobs(id),
    FOREIGN KEY (jobseeker_id) REFERENCES Users(id)
);

