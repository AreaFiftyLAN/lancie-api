# Area FiftyLAN API
This is a Spring based implementation of the LANcie API, used for [Area FiftyLAN](https://areafiftylan.nl/).
This API is responsible for everything persistent, from user registration to ordering tickets.

## Getting Started
Most of the developers (if not all) use IntelliJ, the Java IDE from Jetbrains.
It's highly recommended, even when you think Eclipse is the best thing out there.
For this application, you need the Ultimate version, as we need the Spring Boot support.
Get it here: https://www.jetbrains.com/idea/.
If you already have IntelliJ installed, make sure you have the latest version (2016.1 at the time of writing).
We assume you have a working version (including it running on Java 8) before you read on.

There's one plugin you need, which is the Lombok plugin.
This can be found in IntelliJ's repositories and should be installed before attempting to run the applcation.
Next to that, there's one checkbox you need to enable in IntelliJ's settings.
Go to `Settings->Build,Execution,Deployment->Compiler->Annotation Processors` and enable the `Enable Annotation Processors` checkbox.

### Running the application
IntelliJ has some neat functionality to make running the application very easy. 

#### Set the properties
The API has a number of properties that need to be set before the applicaiton can run.
There's a template in `resources/config/application.properties.sample`.
Copy the contents from this file into a `application-dev.properties`.
This file is gitignored because it contains private information such as possible database credentials and API keys.
The sample proprties assume a working PostgreSQL installation running in the background.

#### Spring Boot
Go to the Run/Debug Configuration window via Run -> Edit Configurations, and add a new Spring Boot configuration.
It needs only two settings to run.
The *Main class* should be set to **ch.wisv.areafiftylan.Application** and the *Use classpath of module* should be set to your project module.
Finally, enable the dev profile by entering `dev` in the `Active Profiles` box.

That's it! Select your new Spring Boot configuration and click Run!. This should launch the application on localhost:9000

## Deploying
If you want to run the API on your server, you probably don't want to run it from the IDE.
### Building
To generate a runnable JAR file, run `./gradlew build`.
This command will run all tests, and create a runnable JAR file in the root folder.
### Configuration
The API has a number of properties that need to be set before the application can run.
There's a template in `resources/config/application.properties.sample`.
Copy this file to a file `application.properties` in the `/config` directory, relative to the location of the JAR.
If you run the JAR, the properties from this file will be used.

## Contributing
If you want to contribute, awesome! First, pick an issue and self-assign it. Make your changes in a new branch, with the following naming convention:

* Fixing a bug? > git checkout -b "**fix-**description-of-bug"
* Implementing a new feature? > git checkout -b "**feature-**description-of-feature"

Once you're satisfied with your changes, create a pull request and give it the label "Ready for merge". 
You can assign someone in specific or wait for someone to pick it up. 
Make sure to include tests and documentation.
If Travis isn't happy, we're not happy. 
