# sb-ecom
a springboot ecommerce application with all  backend functionalities

# H2 Database
it is an open source relational database 
it is written in java
it is fast for java applications
it is light-weight
it is not meant to be used for applications which will be going for production
it is embedded and server mode
embedded mode:- the db runs within the same jvm as our applications run
server mode:- it runs on its own which means it has  a separate process on its own
              it has a server 
              it can accept connections from clients over the network

in application.properties:-spring.h2.console.enabled=true
if we add this line we can see the h2 console a url will be provided in terminal once we run the application 

spring.datasource.url=jdbc:h2:mem:a41f9110-2dea-4d22-8e21-32bc8dd9c46b

the above line will make that h2 connectivity database url permanent because everytime we run the application the url changes adding the above line in application.properties file will make that url permanent

@Entity:-
now by default whichever annotation has this class it will map that very class to a table in the database and that table will share the same name as the class name

this annotation
@GeneratedValue(strategy = GenerationType.IDENTITY)

It tells JPA/Hibernate that the database will automatically generate the primary key value when a new row is inserted.

| Strategy   | Who generates ID  | Notes                           |
| ---------- | ----------------- | ------------------------------- |
| `IDENTITY` | Database          | Most common, simple             |
| `SEQUENCE` | Database sequence | Preferred for PostgreSQL/Oracle |
| `TABLE`    | Table-based       | Rare, slow                      |
| `AUTO`     | Provider decides  | Depends on DB                   |


here in below part
public interface CategoryRepository extends JpaRepository<Category,Long> {
}

Category tells Spring Data which entity this repository manages, and Long tells it the type of that entity’s primary key (@Id).
