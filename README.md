# SENG302 Team 500

## Context
Flockr is a travel application targeted towards users that are interested in travelling with other
people, and collaboratively planning these trips. Flockr has been designed with this in mind, and
has some key features that empower users to plan trips with their friends, family or business
partners.  
With Flockr, you can create a trip with other users and specify permissions for each of them.
This allows other members of the trip to adjust the destinations in the trip, as well as the arrival
and departure dates of each location.  
Users can also create sub trips (legs) allowing them to easily manage a larger trip. All of this can
be done collaboratively in real time. When managing these trips in real time, we thought our
users would like some way of communicating with each other, in case they are not in the same
room. This is why we have included text and voice chatting within our application, these are
available from anywhere in the app, there is also a shortcut to create a group chat from a trip.

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
* `app/` The source for the API backend
* `conf/` Configuration files required for builing and running the project
* `frontend/` Vue.js frontend for application
* `target/universal` Distributable Zip files are created in this directory after building with `sbt dist` 

```aidl
├── LICENSE
├── NOTICE
├── README.md
├── app
│   ├── actions
│   │   ├── ActionState.java
│   │   ├── Admin.java
│   │   ├── LoggedIn.java
│   │   ├── ProfileCompleted.java
│   │   └── SuperAdmin.java
│   ├── controllers
│   │   ├── AuthController.java
│   │   ├── ChatController.java
│   │   ├── DestinationController.java
│   │   ├── FrontendController.scala
│   │   ├── PhotoController.java
│   │   ├── RoleController.java
│   │   ├── TreasureHuntController.java
│   │   ├── TripController.java
│   │   ├── UserController.java
│   │   └── WebSocketController.java
│   ├── exceptions
│   │   ├── BadRequestException.java
│   │   ├── ConflictingRequestException.java
│   │   ├── DestinationNotFound.java
│   │   ├── FailedToLoginException.java
│   │   ├── FailedToSignUpException.java
│   │   ├── ForbiddenRequestException.java
│   │   ├── NotFoundException.java
│   │   ├── ServerErrorException.java
│   │   └── UnauthorizedException.java
│   ├── filters
│   │   └── ExampleFilter.java
│   ├── models
│   │   ├── BaseModel.java
│   │   ├── ChatGroup.java
│   │   ├── Country.java
│   │   ├── Destination.java
│   │   ├── DestinationPhoto.java
│   │   ├── DestinationProposal.java
│   │   ├── DestinationType.java
│   │   ├── Message.java
│   │   ├── Nationality.java
│   │   ├── Passport.java
│   │   ├── PersonalPhoto.java
│   │   ├── Role.java
│   │   ├── RoleType.java
│   │   ├── TravellerType.java
│   │   ├── TreasureHunt.java
│   │   ├── TripComposite.java
│   │   ├── TripDestinationLeaf.java
│   │   ├── TripNode.java
│   │   ├── User.java
│   │   └── UserRole.java
│   ├── modules
│   │   ├── voice
│   │   └── websocket
│   ├── repository
│   │   ├── AuthRepository.java
│   │   ├── ChatRepository.java
│   │   ├── DatabaseExecutionContext.java
│   │   ├── DestinationRepository.java
│   │   ├── PhotoRepository.java
│   │   ├── RoleRepository.java
│   │   ├── TreasureHuntRepository.java
│   │   ├── TripRepository.java
│   │   └── UserRepository.java
│   ├── tasks
│   │   ├── CountrySyncTask.java
│   │   ├── DeleteExpiredDestinationPhotos.java
│   │   ├── DeleteExpiredDestinationProposal.java
│   │   ├── DeleteExpiredDestinationsTask.java
│   │   ├── DeleteExpiredPhotosTask.java
│   │   ├── DeleteExpiredTreasureHunts.java
│   │   ├── DeleteExpiredTripsTask.java
│   │   ├── DeleteExpiredUsersTask.java
│   │   ├── ExampleDestinationDataTask.java
│   │   ├── ExampleTripsDataTask.java
│   │   ├── ExampleUserData.java
│   │   ├── ExampleUserPhotoData.java
│   │   ├── PopulateTask.java
│   │   ├── SampleData
│   │   ├── SetupTask.java
│   │   └── TasksController.java
│   └── util
│       ├── AuthUtil.java
│       ├── ChatUtil.java
│       ├── CountrySchedulerUtil.java
│       ├── DestinationUtil.java
│       ├── Responses.java
│       ├── Security.java
│       ├── TreasureHuntUtil.java
│       └── TripUtil.java
├── build.sbt
├── conf
│   ├── application.conf
│   ├── application.prod.conf
│   ├── application.test.conf
│   ├── evolutions
│   │   └── default
│   ├── logback.xml
│   ├── messages
│   └── routes
├── frontend
│   ├── README.md
│   ├── babel.config.js
│   ├── dist
│   │   ├── css
│   │   ├── flockr-logo.ico
│   │   ├── img
│   │   ├── index.html
│   │   ├── js
│   │   └── media
│   ├── jest.config.js
│   ├── node_modules
│   ├── package-lock.json
│   ├── package.json
│   ├── project
│   │   └── build.properties
│   ├── public
│   │   ├── flockr-logo.ico
│   │   └── index.html
│   ├── src
│   │   ├── assets
│   │   ├── components
│   │   ├── config.js
│   │   ├── containers
│   │   ├── main.js
│   │   ├── plugins
│   │   ├── routes.js
│   │   ├── stores
│   │   ├── styles
│   │   └── utils
│   └── tests
│       └── unit
├── jacoco.sbt
├── logs
│   └── application.log
├── package-lock.json
├── project
│   ├── VuePlayHook.scala
│   ├── build.properties
│   ├── plugins.sbt
│   ├── project
│   │   └── target
│   └── target
│       ├── config-classes
│       ├── scala-2.12
│       └── streams
├── public
│   └── vue-front-end-goes-here
│       ├── css
│       ├── flockr-logo.ico
│       ├── img
│       ├── index.html
│       ├── js
│       └── media
├── sbt
├── sbt-dist
│   └── conf
│       ├── sbtconfig.txt
│       └── sbtopts
├── sbt.bat
├── scripts
│   ├── script-helper
│   └── test-sbt
├── sonar-project.properties
├── src
│   └── main
│       └── resources
├── storage
│   ├── defaults
│   │   └── defaultCoverPhoto.jpg
│   └── photos
│       
├── tag_checker.py
└── test
    ├── ApplyDbMigrations.java
    ├── CucumberTestRunner.java
    ├── controllers
    │   ├── ChatControllerTest.java
    │   ├── DestinationControllerTest.java
    │   ├── PhotoControllerTest.java
    │   ├── TreasureHuntControllerTest.java
    │   ├── TripControllerTests
    │   └── UserControllerTest.java
    ├── models
    │   ├── DestinationTest.java
    │   └── TripNodeTest.java
    ├── resources
    │   ├── features
    │   └── fileStorageForTests
    ├── steps
    │   ├── AdminSteps.java
    │   ├── CommonTestSteps.java
    │   ├── DestinationProposalTestSteps.java
    │   ├── DestinationTestingSteps.java
    │   ├── UserPhotoSteps.java
    │   ├── UserRoleSteps.java
    │   ├── auth
    │   └── users
    ├── testingUtilities
    │   ├── FakeClient.java
    │   ├── FakePlayClient.java
    │   ├── PlayResultToJson.java
    │   ├── TestAuthenticationHelper.java
    │   └── TestState.java
    └── util
        ├── AuthUtilTest.java
        ├── ConnectedUsersTest.java
        ├── CountrySchedulerTest.java
        ├── NationalitySchedulerTest.java
        ├── PassportSchedulerTest.java
        └── TripUtilTest.java
```

## Deployment Procedure
### How to run the product in development mode
Open a terminal and navigate to the project root directory.
Start the application with the command:
```bash
sbt run
```
And open <http://localhost:8080/>  
**Note:** There may be a small delay as the front end is automatically started.

### How to build the product for distribution
Open a terminal and navigate to the project root directory.
Start the application with the command:
```bash
sbt dist
```

### How to run the distributable product
Navigate to the target/universal directory from the project root then use the following commands to run the application on http://localhost:8080
```bash
unzip seng302-team-500-0.0.1-SNAPSHOT.zip
cd seng302-team-500-0.0.1-SNAPSHOT/bin
bash seng302-team-500
```

### How to start the Servers on the VM: http port 8443 and 443
 The testing and production server are started automatically by the CI.
 MASTER will update the production server with every tagged commit that passes the CI testing.
 DEV will update the test server with every commit to DEV that passes CI testing.
* [Play Documentation](https://playframework.com/documentation/latest/Home)
* [EBean](https://www.playframework.com/documentation/latest/JavaEbean) is a Java ORM library that uses SQL.The documentation can be found [here](https://ebean-orm.github.io/)
* [Vue.js Documentation](https://vuejs.org/v2/guide/)

### How to run the tests
```bash
sbt test
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
* SBT-Jacoco v3.1.0
* Open Data Soft - [World Cities Population Data-Set](https://public.opendatasoft.com/explore/dataset/worldcitiespop/table/?disjunctive.country&sort=population)
* WebRTC
## Copyright
Copyright of Canterbury University

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

