## The thought process (development, approach)

### The process
1. Get familiar with Kotlin and his developing ecosystem
2. Understand well the requirements, explore the project and made a couple of requests to the existing resources
3. Think about the design of the solution
4. Code a very basic solution (just to have a working solution)
5. Learn about the coroutines (I want to make the app fast, process invoices in parallel)
6. Find a reasonable library for dependency injection
7. Find a reasonable library for the scheduler (I don't want to reinvent the wheel)
8. Implement dependency injection (Dagger2)
9. Implement the scheduler (In-App)
10. Designing a pipeline that process invoices in parallel (channels, agents, commands) using coroutines
11. Do some refactorings
12. Implement an event service to keep track of all the events in the system (for traceability)
13. Write some tests
14. Refactor the code

### General principle
For the scope of this exercices I wanted to create a system based on passing messages between components (I want a solution that is microservice fiendly).
The idea is that the app is small and is focused on doing one thing well (UNIX like philosophy). I created a pipeline (using coroutines) that uses channels 
to pass messages (commands) between components and to distribute the main flow to different agents for parallel processing.
The system should be scalable and it should be relatively easy to add new features, that's why I introduced a couple of abstractions.
The pipeline can be used for other computations as we wrap the action in a command object. The pipeline expects a stream of commands and as long as these
commands respect the contract (Interfaces: Command and CommandResult) the pipeline will work (I added an image at the root project for the general principle).

### The scheduler
I choosed to use a library and to make the scheduler in-app (we can also implement the scheduler as a cron job outside of the main application). The scheduling pattern is configurable in properties file. I started to implement
some methods so we can change the scheduler pattern without closing the app. Didn't finished that just to save time and to work more on 
the parallel processing of invoices but the implementation of this mechanism is pretty straightforward. We can expose a new
resource that can change the scheduling pattern or use another method.

### Dependency Injection
I used Dagger2 for this purpose. It does a lot of magic and it was relatively difficult to bootstrap with dagger. A lot of difficulties when I started 
write some tests. Once you have a setup that works, dagger is very powerful and useful. Adding new services is very easy. It took me time to get confortable
with but it is an investment that pays off really fast after that and the code becomes more clean and elegant. 

### The tests

### Possible new features

### Production grade 

### Time Estimation
It took me a couple of days to learn the basics about Kotlin, the coroutines and some useful libraries that could be helpful for beat Antaeus.
For writing the code  I made 4-5 sessions of coding 3 hours each so in total it was roughly 12-15 hours.

### Libraries
- kotlin coroutines
- dagger2
- www.sauronsoftware.it/projects/cron4j

### Notes

### Final thoughts

## Antaeus

Antaeus (/ænˈtiːəs/), in Greek mythology, a giant of Libya, the son of the sea god Poseidon and the Earth goddess Gaia. He compelled all strangers who were passing through the country to wrestle with him. Whenever Antaeus touched the Earth (his mother), his strength was renewed, so that even if thrown to the ground, he was invincible. Heracles, in combat with him, discovered the source of his strength and, lifting him up from Earth, crushed him to death.

Welcome to our challenge.

## The challenge

As most "Software as a Service" (SaaS) companies, Pleo needs to charge a subscription fee every month. Our database contains a few invoices for the different markets in which we operate. Your task is to build the logic that will schedule payment of those invoices on the first of the month. While this may seem simple, there is space for some decisions to be taken and you will be expected to justify them.

## Instructions

Fork this repo with your solution. Ideally, we'd like to see your progression through commits, and don't forget to update the README.md to explain your thought process.

Please let us know how long the challenge takes you. We're not looking for how speedy or lengthy you are. It's just really to give us a clearer idea of what you've produced in the time you decided to take. Feel free to go as big or as small as you want.

## Developing

Requirements:
- \>= Java 11 environment

### Building

```
./gradlew build
```

### Running

There are 2 options for running Anteus. You either need libsqlite3 or docker. Docker is easier but requires some docker knowledge. We do recommend docker though.


*Running through docker*

Install docker for your platform

```
make docker-run
```

*Running Natively*

Native java with sqlite (requires libsqlite3):

If you use homebrew on MacOS `brew install sqlite`.

```
./gradlew run
```


### App Structure
The code given is structured as follows. Feel free however to modify the structure to fit your needs.
```
├── pleo-antaeus-app
|       main() & initialization
|
├── pleo-antaeus-core
|       This is probably where you will introduce most of your new code.
|       Pay attention to the PaymentProvider and BillingService class.
|
├── pleo-antaeus-data
|       Module interfacing with the database. Contains the database models, mappings and access layer.
|
├── pleo-antaeus-models
|       Definition of the "rest api" models used throughout the application.
|
├── pleo-antaeus-rest
|        Entry point for REST API. This is where the routes are defined.
└──
```

### Main Libraries and dependencies
* [Exposed](https://github.com/JetBrains/Exposed) - DSL for type-safe SQL
* [Javalin](https://javalin.io/) - Simple web framework (for REST)
* [kotlin-logging](https://github.com/MicroUtils/kotlin-logging) - Simple logging framework for Kotlin
* [JUnit 5](https://junit.org/junit5/) - Testing framework
* [Mockk](https://mockk.io/) - Mocking library
* [Sqlite3](https://sqlite.org/index.html) - Database storage engine

Happy hacking 😁!
