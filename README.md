# landev
Spring based implementation of the LANcie API

## Getting Started
Most of the developers (if not all) use IntelliJ, the Java IDE from Jetbrains. It's highly recommended, even when you think Eclipse is the best thing out there. Get it here: https://www.jetbrains.com/idea/. If you already have IntelliJ installed, make sure you have the latest version (14.1.4 at the time of writing).

For the Spring back-end part of the application, that's all you need. For the Polymer front-end, you'll need some more. From the Polymer Starter Kit page:

#### Install dependencies (Polymer)

With Node.js installed, run the following one liner from the root of your Polymer Starter Kit download (which is the /static directory in this repository:

```sh
npm install -g gulp bower && npm install && bower install
```

### Running the application
IntelliJ has some neat functionality to make running the application very easy. 

#### Spring Boot (Back-end API)
From the Run/Debug Configuration window, add a new Spring Boot configuration. It needs only two settings to run. The *Main class* should be set to **ch.wisv.areafiftylan.Application** and the *Use classpath of mod...* should be set to your project module, which is the only selectable option in nearly all cases. 

That's it! Select your new Spring Boot configuration and click Run!. This should launch the application on localhost:8080

#### Front-end Polymer
In the /static/ folder, you will find all the files from the Polymer Starter Kit, including the Gulpfile.js. Install the plugin to deal with this in IntelliJ, right click the Gulpfile.js and select "Show Gulp Tasks" to make sure IntelliJ recognizes everything correctly. 

In the Run/Debug Configuration screen, create a new Gulp task. For development, it's useful to create a Run Configuration for the 'serve' task and the 'default' task. 

You can run both configurations simultaneously, so you can test the back-end connection from the Polymer fron-end.

## Contributing
If you want to contribute, awesome! First, pick an issue and self-assign it. Make your changes in a new branch with 
this naming convention: 

> git checkout -b "fb-master_feature-i-want-to-implement"

* **fb** = *Feature branch*
* **master** = *The branch I want to merge in.*
* **feature-i-want-to-implement** = *A short description of the changes in this branch.*

Once you're satisfied with your changes, create a pull request and give it the label "Ready for merge". You can assign someone in specific or wait for someone to pick it up. 
