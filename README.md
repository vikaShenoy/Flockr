# SENG302 Team 500

## Context
Travel EA is the app of all the apps. It acts like an Executive Assistant for
organising your travel. 
In this application, you can create your profile, find travel partners 
with similar interests to you, plan your trips or use others’ trips to plan 
your own.

## Objective
This README file shows how to deploy the application, test the project and shows 
the overall project structure. We have a master branch that is a Shippable product
and the dev branch that is the development branch for the project.

##### Master Branch
![Master Pipeline](https://eng-git.canterbury.ac.nz/seng302-2019/team-500/badges/master/pipeline.svg?style=flat)
##### Dev Branch
![Dev Pipeline](https://eng-git.canterbury.ac.nz/seng302-2019/team-500/badges/dev/pipeline.svg?style=flat)

TravelEA is split into:
- Backend using Play and Java to implement the TravelEA API
- Front end using Vue.js

## Project Structure
### Project Structure for Back End
* `app/` The source for the API backend
* `conf/` configuration files required to ensure the backend builds properly

### Project Structure for Front End
* `public` The directory that includes static assets to be served by the front end
* `src` Contains source files

## Deployment Procedure
### How to run the product in dev mode
```bash
sbt run
```
And open <http://localhost:8080/>  
**Note:** There may be a small delay as the front end is automatically started.

### How to run the product in production mode
```bash
# create an environment variable
export APPLICATION_SECRET=[insert a random string as your secret]
# check that the environment variable was successfully created
printenv | grep APPLICATION_SECRET
# run the production server
bash [binary file name, as of right now: "seng202-team-500"]
```

### How to start the Servers on the VM: http port 8443 and 443
 The testing and production server are started automatically by the CI.
 MASTER will update the production server with every tagged commit that passes the CI testing.
 DEV will update the test server with every commit to DEV that passes CI testing.
* [Play Documentation](https://playframework.com/documentation/latest/Home)
* [EBean](https://www.playframework.com/documentation/latest/JavaEbean) is a Java ORM library that uses SQL.The documentation can be found [here](https://ebean-orm.github.io/)
* [Vue.js Documentation](https://vuejs.org/v2/guide/)

### How to run the tests
```
Simply right-click on test directory and click 'Run All Tests'
```
**OR**
```bash
sbt test
```

### Examples of Admin in the Application
```
Email: luis@gmail.com
Password: so-secure

```

## Dependencies
* H2 Database 1.4.197
* MySql 5.1.24
* Jaxb-Core 2.3.0.1
* Jaxb-Runtime 2.3.2
* JbCrypt 0.3m
* Cucumber-Java 4.2.0
* Awaitility 2.0.0
* Cucumber-Core 4.2.0
* Cucumber-JVM 4.2.0
* Cucumber-Junit 4.2.0
* Junit-Interface 0.8
* Axios 0.18.0
* Bootstrap 4.3.1
* Email-Validator 2.0.4
* Jest 24.8.0
* I 0.3.6
* Moment 2.24.0
* Sortablejs 1.8.4
* Superagent 4.1.0
* Vue 2.6.6
* Vue-Cropperjs 3.0.0
* Vue-Router 3.0.2
* Vue2-Dropzone 3.5.9
* Vuejs-Datepicker 1.5.4
* Vuetify 1.5.5
* Vue/cli-Plugin-Babel 3.5.0
* Vue/cli-Plugin-Eslint 3.5.0
* Vue/cli-Plugin-Unit-Jest 3.7.0
* Vue/cli-Service 3.6.0
* Vue/Test-Utils 1.0.0-beta.29
* Babel-Core 7.0.0-bridge.0
* Babel-Eslint 10.0.1
* Babel-Jest 23.6.0
* Eslint 5.8.0
* Eslint-Plugin-Vue 5.0.0
* Node-Sass 4.11.0
* Sass-Loader 7.1.0
* Stylus 0.54.5
* Stylus-Loader 3.0.1
* Vue-cli-Plugin-Vuetify 0.5.0
* Vue-Template-Compiler 2.5.21
* Vuetify-Loader 1.0.5

## Copyright
Copyyright of Canterbury University

## Licensing
Refer to the root directory for the [project license](/LICENSE)

## Author and Contributors
* Andy Holden
* Angelica Dela Cruz
* Exequiel Bahamonde Carcamo
* Finn Greig
* Isaac Foster
* Rafael Goesmann
* Sam Annand
* Vikas Shenoy

