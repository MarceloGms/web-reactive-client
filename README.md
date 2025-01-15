# Reactive Media Management System  

This repository contains the implementation for **Project #2** of the "Enterprise Application Integration" course at the University of Coimbra. The project uses **Reactive Streams** and **Spring WebFlux** to build a scalable, asynchronous system for managing media content and user interactions.  

## Project Overview  

The system comprises two key components:  

1. **Reactive Server**:  
   - Implements reactive CRUD operations for media (movies and TV shows) and user data.  
   - Handles many-to-many relationships between users and media.  
   - Built using **Reactive Streams** for non-blocking data processing.  

2. **Reactive Client**:  
   - Consumes data from the server reactively to generate reports.  
   - Demonstrates fault tolerance with retry mechanisms for network failures.  
   - Processes data efficiently, leveraging the power of **Reactive Streams**.  

## Objectives  

1. Master declarative reactive programming with **Reactor Core** and **Spring WebFlux**.  
2. Build scalable, non-blocking systems using **Reactive Streams** principles.  
3. Implement client-side fault tolerance and retry mechanisms.  

## Features  

### Reactive Server  

The server exposes the following endpoints:  
- **Media Management**: Reactive CRUD operations for media entities.  
- **User Management**: Reactive CRUD operations for user entities.  
- **Relationships**: Manage many-to-many relationships between users and media.  

### Reactive Client  

The client generates the following reports by consuming server data:  
1. Titles and release dates of media items.  
2. Total and categorized media counts (e.g., highly-rated content).  
3. Media from specific decades, sorted by average rating.  
4. Comprehensive user statistics, including media subscriptions.  

The client leverages **Reactive Streams** to efficiently fetch, transform, and display the data.  

# How to run

## `make run`: run all the reqs

## `make cli ARGS="req1.txt req2.txt ..."`: run specific reqs
