# SENG302 Team 500

## Context
This project is called Travel EA is an web application that is like the app of 
all the apps. It acts like an Executive Assistant for organising your travel. 
In this application, you can create your profile, find your own travel partners 
with the similar interests as you, plan your trips or use others’ trips to plan 
your own trip.

## Objective
This README file shows how to deploy the application, test the project and shows 
the overall project structure. We have a master branch that is a Shippable product
and the dev branch that is the development branch for the project.

##### Master Branch
![Master Pipeline](https://eng-git.canterbury.ac.nz/seng302-2019/team-500/badges/master/pipeline.svg?style=flat)
##### Dev Branch
![Dev Pipeline](https://eng-git.canterbury.ac.nz/seng302-2019/team-500/badges/dev/pipeline.svg?style=flat)

The Project split into:
- API backend using Play and Java
- Front end using Vue.js

## Project Structure for Back End
* `backend/app/` The source for the API backend
* `backend/conf/` configuration files required to ensure the backend builds properly

## Project Structure for Front End
* `public` The directory that includes static assets to be served by the front end
* `src` Contains source files

## How to run the product in dev mode
```bash
cd backend
sbt run
```
And open <http://localhost:9000/>  
**Note:** There may be a smmal delay as the front end is automatically started.

## How to run the product in production mode
```bash
# create an environment variable
export APPLICATION_SECRET=[insert a random string as your secret]
# check that the environment variable was successfully created
printenv | grep APPLICATION_SECRET
# run the production server
bash [binary file name, as of right now: "seng202-team-500"]
```

## How to start the Servers on the VM: http port 8443 and 443
 The testing and production server are started automatically by the CI.
 MASTER will update the production server with every tagged commit that passes the CI testing.
 DEV will update the test server with every commit to DEV that passes CI testing.
* [Play Documentation](https://playframework.com/documentation/latest/Home)
* [EBean](https://www.playframework.com/documentation/latest/JavaEbean) is a Java ORM library that uses SQL.The documentation can be found [here](https://ebean-orm.github.io/)
* [Vue.js Documentation](https://vuejs.org/v2/guide/)

