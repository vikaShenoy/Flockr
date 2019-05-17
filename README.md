# SENG302 Team 500

##### Master pipeline
![Master Pipeline](https://eng-git.canterbury.ac.nz/seng302-2019/team-500/badges/master/pipeline.svg?style=flat)

##### Dev pipeline
![Dev Pipeline](https://eng-git.canterbury.ac.nz/seng302-2019/team-500/badges/dev/pipeline.svg?style=flat)

Project split into:
- API backend using Play and Java
- Front end using Vue.js

## Project Structure for Back End
* `backend/app/` The source for the API backend
* `backend/doc/` User and design documentation for the whole project
* `backend/doc/examples/` Demo example files for use with the backend
* `backend/conf/` configuration files required to ensure the backend builds properly

## Project Structure for Front End
* `public` The directory that includes static assets to be served by the front end
* `src` Contains source files

## How to run the back end in dev mode
```bash
cd backend
sbt run
```
And open <http://localhost:9000/>

## How to run the back end in production mode
```bash
# create an environment variable
export APPLICATION_SECRET=[insert a random string as your secret]
# check that the environment variable was successfully created
printenv | grep APPLICATION_SECRET
# run the production server
bash [binary file name, as of right now: "seng202-team-500"]
```

## How to run the front end
```bash
cd frontend
npm install # only initially to install dependencies
npm run serve # starts the development server (with hot reload)
```
And open <http://localhost:8080/>

## How to start the Servers on the VM: http port 8443 and 443
1. Open terminal and tpye ssh sengstudent@csse-s302g6.canterbury.ac.nz
2. Enter the password
3. Start the server by typing `sudo startserver -d | -p <start>`
    **Note: -p means production server and -d means development server.**
    eg. sudo startserver -d start -p start // To start both servers
4. Enter password if requested
5. To stop the server, type `sudo startserver -d | -p <stop>`
    eg. sudo startserver -d stop -p stop

### Reference
* [Play Documentation](https://playframework.com/documentation/latest/Home)
* [EBean](https://www.playframework.com/documentation/latest/JavaEbean) is a Java ORM library that uses SQL.The documentation can be found [here](https://ebean-orm.github.io/)
* [Vue.js Documentation](https://vuejs.org/v2/guide/)

