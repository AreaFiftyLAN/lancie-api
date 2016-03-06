# Area FiftyLAN API
This is a Spring based implementation of the LANcie API, used for [Area FiftyLAN](https://areafiftylan.nl/). This API is responsible for everything persistent, from user registration to ordering tickets. 

## Getting Started
Most of the developers (if not all) use IntelliJ, the Java IDE from Jetbrains. It's highly recommended, even when you think Eclipse is the best thing out there. Get it here: https://www.jetbrains.com/idea/. If you already have IntelliJ installed, make sure you have the latest version (15.0 at the time of writing). We assume you have a working version (including it running on Java 8) before you read on. 

There's one plugin you need, which is the Lombok plugin. This can be found in IntelliJ's repositories and should be installed before attempting to run the applcation. Next to that, there's one checkbox you need to enable in IntelliJ's settings. Go to `Settings->Build,Execution,Deployment->Compiler->Annotation Processors` and enable the `Enable Annotation Processors` checkbox. 

### Running the application
IntelliJ has some neat functionality to make running the application very easy. 

#### Set the properties
The API has a number of properties that need to be set before the applicaiton can run. There's a template in `resources\config\application.properties.sample`. Copy the contents from this file into a `application.properties`. This file is gitignored because it contains private information such as possible database credetials and payment keys. In the near future, this process will be easier. The sample proprties assume a working PostgreSQL installation running in the background. 

#### Spring Boot
From the Run/Debug Configuration window, add a new Spring Boot configuration. It needs only two settings to run. The *Main class* should be set to **ch.wisv.areafiftylan.Application** and the *Use classpath of mod...* should be set to your project module, which is the only selectable option in nearly all cases. 

That's it! Select your new Spring Boot configuration and click Run!. This should launch the application on localhost:9000

## Contributing
If you want to contribute, awesome! First, pick an issue and self-assign it. Make your changes in a new branch with 
this naming convention: 

> git checkout -b "fb-master_feature-i-want-to-implement"

* **fb** = *Feature branch*
* **master** = *The branch I want to merge in.*
* **feature-i-want-to-implement** = *A short description of the changes in this branch.*

Once you're satisfied with your changes, create a pull request and give it the label "Ready for merge". You can assign someone in specific or wait for someone to pick it up. 
