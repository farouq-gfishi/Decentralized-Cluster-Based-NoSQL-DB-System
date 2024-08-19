# Decentralized Cluster-Based NoSQL Database System

## Overview

The Decentralized Cluster-Based NoSQL Database System is designed to facilitate scalable, distributed data management without relying on a central manager node. Built using Java, this system ensures data consistency and load balancing across nodes within a cluster. Key features include multithreading optimization, robust security measures, hash indexing for fast data retrieval, and seamless communication via HTTP(S) protocol.

## Key Features

- **Cluster Interaction**: Distributed data management with seamless node interactions.
- **Data Consistency and Load Balance**: Ensures consistency and balanced workload without a central manager node.
- **Multithreading Optimization**: Efficient resource utilization and enhanced performance through concurrent task execution.
- **Security Measures**: Protects data integrity with strong security protocols and access controls.
- **Hash Indexing for Speed**: Optimizes data retrieval and query processing with hash indexing.
- **Communication Protocol**: Utilizes HTTP(S) for secure and efficient node communication.
- **DevOps Integration**: Includes CI/CD pipelines and Docker for automated testing, building, and deployment.
- **Comprehensive Testing Suite**: Validates functionality and performance with extensive testing.

## System Requirements

- **Java Development Kit (JDK)**: Version 11 or higher.
- **Docker**: For containerized deployment.
- **Maven or Gradle**: For project management and build automation.
- **CI/CD Tools**: Tools such as GitHub Actions for continuous integration and deployment.
- **Testing Frameworks**: JUnit for unit testing, and other relevant tools for performance testing.

## Installation and Setup

### Clone the Repository

```bash
git clone https://github.com/farouq-gfishi/Decentralized-Cluster-Based-NoSQL-DB-System.git
```

### Build the Project

Use Maven to build and package the application:

```bash
mvn clean install
```

### Setup Docker Environment

Ensure Docker and Docker Compose are installed. Start the application and database containers:

```bash
docker-compose up
```

### Run Unit Tests

Execute unit tests to verify the functionality of the application:

```bash
mvn test
```

## Configuration

Configuration settings for the application are managed through environment variables and configuration files. Refer to the docker-compose.yml and application.properties files for detailed configuration options.
