# Sailing Course Planner

Sailing Course Planner is a simple application to help organizing Sailing compotitions. You can use it to visually design your course using a desktop/tablet UI, share the course with other organisers and use a mobile UI when actually setting it up (dropping buoys to right location).

## Running the application

To run the application locally, you'll need PostGIS enable PostgreSQL server and a database for the app. The database url can be seen/edited in application.properties file. Note, that you'll need to enable PostGIS manually in your database, with command:

    CREATE EXTENSION postgis;

Hibernate will on first launch create a schema for you automatilly.

After you have prepared a database, you can just launch the main method of SailingCoursePlannerApplication from your IDE or run the project with "mvn spring-boot:run".

By default the application is in debug/test/development mode, where user is hardcoded. By configuring app keys to application.properties you can put the app into SaaS mode, where users are required to login with their Google account.

## Features, design view

 * share courses with other
 * secured using Google OAuth2 login, so that the track is not published outside compitition organisation
 * any number of main buoys
 * show the distance of course
 * reordering buoys
 * rotating the course
 * scaling the course
 * helper buoys, which are not calculated in the distance. Can be positioned relatively to main buoys.
 
Planned: course templates

## Features, mobile view

 * mobile web app that works with all modern smartphones
 * share the secret track link using email, SMS, whatsup directly to organizers setting up the course
 * Shows course on a map
 * Shows users own position
 * Clicking on buoy opens a mode where app reports distanse, direction (azimuth aka compass direction) and time to buoy with current speed

## Technology

 * PostgreSQL + PostGIS
 * JPA, Hibernate and it's spatial extensions
 * Spring Boot, Spring Data JPA
 * Vaadin
 * LeafletJS
 
