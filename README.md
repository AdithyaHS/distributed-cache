# Distributed-Cache

## Steps to run
- mvn clean install
- docker-compose.yml


## Introduction
The aim to this project is to implement distributed cache like redis.  A distributed cache is split across multiple 
systems and a way of providing faster access to the contents of the database. Distributed cache should provide good 
scalability and fault tolerance. 


## Things to consider before design

### REST vs Grpc
Evaluate each of the architectural style and find out which is the most suitable and feasible style for our project.
Some of the things that we are looking for here is support for peer to peer calls and asynchronous calls.

 -<bold> Pros</bold>
 1. 
 2.
 3.
 -<bold> Cons </bold>
 1.
 2.
 3.

### Support for sql and no sql databases

The data below would evaluate our support for sql and no sql databases. Given the amount of time, ho feasible is it to 
support each of the database

 -<bold> Pros</bold>
 1. 
 2.
 3.
 -<bold> Cons </bold>
 1.
 2.
 3.

### Type of caching mechanisms 
- <bold> write through cache </bold>
- <bold> write back cache </bold>
- <bold> write around cache </bold>

### Cache eviction policy
- <bold> LRU </bold>
- <bold> LFU </bold>
- <bold> MRU </bold>
 

#### Naming Conventions

- Package names should include small letters(a - z). 
- Classes should start with capital letters and follow PascalCase
- variables and functions should have camelCase letters

### References
[1] https://www.infoworld.com/article/3262990/how-to-implement-a-distributed-cache-in-aspnet-core.html

[2] https://docs.oracle.com/javase/tutorial/java/package/namingpkgs.html

[3] https://softwareengineering.stackexchange.com/questions/372113/best-way-to-naming-classes-and-packages-in-java

[4] https://www.youtube.com/watch?v=U3RkDLtS7uY

