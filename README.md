# BouncingBallAkkaFX
Distributed Bouncing Ball written with Akka and JavaFX
## Demo
![Demo](https://github.com/mhrimaz/BouncingBallAkkaFX/raw/master/bouncingball.GIF)
## TODO
- Use Akka Cluster inorder to have no single-point of failure.
- Make communication more efficient
- In some cases the ball enter into more than one application
## Running
The Project is maven based, you can easily open it with your IDE and maven will handle dependencies.
If you are familiar with Maven you can execute it without any problem.
Also you can use the jar files. First run the server then run clients to form a grid. by sendeing a message which specify x position, y position, degree of movement and the radius of the circle you can create a ball in the first client.
