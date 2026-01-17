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

# how H2 gets connected WITHOUT you writing code
- You added Spring Data JPA dependency

In pom.xml you have something like:

    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>


This pulls in:
Hibernate (JPA implementation)
Spring ORM
Transaction manager
Auto-configuration logic

    You added H2 dependency
    <dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>runtime</scope>
    </dependency>


This tells Spring:
=> “An H2 database is available on the classpath.”

Spring Boot sees H2 → auto-configures DataSource
Spring Boot has this rule:
    If H2 is on the classpath and no DB is explicitly configured → auto-configure H2.
    So internally Spring Boot does something like:
    DataSource dataSource = new HikariDataSource(
    url = "jdbc:h2:mem:testdb",
    username = "sa",
    password = ""
    );


You didn’t write this — Spring Boot did.

Spring Boot creates EntityManager
Once DataSource exists:
Hibernate is initialized
EntityManagerFactory is created
Entities annotated with @Entity are scanned
Now Hibernate knows:

which tables exist
how to map entities to tables
Spring creates JpaRepository implementations
When you write:
public interface CategoryRepository extends JpaRepository<Category, Long>

    Spring:

    sees JpaRepository

    generates a proxy implementation at runtime

    injects EntityManager into it

    So findAll() internally becomes:

    entityManager.createQuery("select c from Category c")

    Important clarification (this answers your doubt directly)
    ❌ This is NOT happening:
    
    “DB got connected because I extended JpaRepository”
    
    ✅ This IS happening:
    
    “DB got connected because Spring Boot auto-configured a DataSource using H2, and JpaRepository is using that connection.”
    
    How Spring knows to use H2 specifically
    
    Because of:
    
    H2 dependency present
    
    No other DB configured
    
    Boot’s default config kicks in
    
    If you add MySQL tomorrow, H2 stops being used automatically.
    
    Where is the H2 config actually coming from?
    
    Even if you didn’t write this:
    
    spring.datasource.url=jdbc:h2:mem:testdb
    spring.datasource.username=sa
    spring.datasource.password=
    
    
    Spring Boot assumes it by default.
    
    You can see it in logs at startup:
    
    HikariPool-1 - Start completed.
    H2 console available at '/h2-console'
    
    Why you can access H2 console without config
    
    Because Spring Boot auto-enables it when H2 is detected:
    
    http://localhost:8080/h2-console


Again — auto-configuration.
What happens if you remove JPA but keep H2?
DB will still connect
But you won’t have repositories
You’d need JDBC manually
So JPA ≠ DB connection

    Mental model to remember forever 🧠
    H2 dependency
    ↓
    Spring Boot auto-configures DataSource
    ↓
    Hibernate uses DataSource
    ↓
    JpaRepository uses Hibernate
    ↓
    Your service calls repository



# VALIDATIONS

- @NotBlank
    below code with annotation @NotBlank checks if the value given by user is an empty string or not like:- ""
        @NotBlank
        private String categoryName;


- @Valid
  Valid tells Spring to validate the incoming request body against the validation rules defined on the Category class before executing the controller method.
  so since we have added notBlank in categoryName it will validate over that and send appropriate response back from controller

        @PostMapping("/public/categories")
        public ResponseEntity<String> createCategory(@Valid @RequestBody Category category){
        categoryService.createCategory(category);
        return new ResponseEntity<>("Category added successfully",HttpStatus.CREATED);
        }

- MyGlobalExceptionHandler
  this is  a custom exception handler where any exceptions will occur this class will handle it

- @RestControllerAdvice
  this is a specialized version of ControllerAdvice 
  this is geared towards rest Api so if are using apis we should be using this @RestControllerAdvice
  if we add this annotation to our class :-MyGlobalExceptionHandler, this will intercept any exception that are thrown by any controller in the application

- @ExceptionHandler
  this annotation is used to define methods in your exceptionhandler to handle a specific type of exception


        @ExceptionHandler(MethodArgumentNotValidException.class)
        public Map<String, String> myMethodArgumentNotValidException(MethodArgumentNotValidException e){
        Map<String,String> response= new HashMap<>();

            e.getBindingResult().getAllErrors().forEach(error->{
                String fieldName=((FieldError)error).getField();
                String message= error.getDefaultMessage();
                response.put(fieldName,message);
            });
    
            return response;
        }

  in above code we are basically telling that we have a exception handler for exception MethodArgumentNotValidException 
  you need to intercept and you need to execute above method
  we are accepting the argument which is of the exception object which is of type MethodArgumentNotValidException

- in below line :-
  e.getBindingResult()
  Returns a BindingResult object, which contains:

    all validation errors
    
    field-level errors
    
    global errors

- This line:
  e.getBindingResult().getAllErrors()

    Returns a list of errors like:

    [
    FieldError(categoryName, "must not be blank"),
    FieldError(categoryName, "size must be between 3 and 50")
    ]

    Looping over validation errors
    e.getBindingResult().getAllErrors().forEach(error -> {

    Each error here is:

    either a FieldError
    or an ObjectError
    You cast it:
    String fieldName = ((FieldError) error).getField();

    So you extract:
    categoryName
    
    And then:
    String message = error.getDefaultMessage();

    Which comes from annotations like:
    @NotBlank(message = "Category name cannot be empty")
    Building the response map
    response.put(fieldName, message);
    So final response looks like:
    
    {
    "categoryName": "Category name cannot be empty"
    }

- ResourceNotFoundException
  created a new custom exception which will be used in CategoryServiceImpl

# PAGINATION

- what is a DTO?
 Data transfer Object(DTO)
 they are like a custom object that we have to send as a response to people who are consuming our apis
 they will help us to tailor our data, we can hide any details any parameter any sensitive information

 DTO are a simple java object that represent the subset of our domain model meaning the model 
 that we have defined in our application (Category for now)

 currently our model is tightly coupled to our service implementation which is not right approach so
 dto can be used where we dont need to use model in our serviceImpl class
 
- what is a DTO pattern?
   Design pattern used to transfer data between software application subsytems.
 

- CategoryDTO, CategoryResponse
  CategoryDTO is the request object and CategoryResponse is the Response Object
  in CategoryService for getAllCategories method that was of type List<Category>
  which has been changed to CategoryResponse because 
  CategoryResponse itself is acting as a model with List<CategoryDto> field
  where CategoryDto is a replica of Category model 

  we have mapped all Category object to CategoryDto and did changes in categoryServiceImpl which can help us in only using catgeoryDto
  and did changes in controller class for the same

- @ReuqestParam
  @GetMapping("/echo")
  public ResponseEntity<String> echoMessage(@RequestParam(name="message") String message){
  return new ResponseEntity<>("Echoed message:- "+message,HttpStatus.OK);
  }
  above request param annotation is being used so that we can take value from url now in controller this method is present so
  in url we can pass value as
  http://localhost:8080/api/echo?message=Hi
  so here we are passing Hi value with variable as message so thats why we are taking parameter string with name message


 we have updated getAllCategories from CategoryServiceImpl
  these are the new lines we have added to get page detail below is the explanation
 Pageable pageDetails= PageRequest.of(pageNumber,pageSize);
 Page<Category> categoryPage= categoryRepository.findAll(pageDetails);

1-> Pageable pageDetails = PageRequest.of(pageNumber, pageSize);
    What is Pageable?
    Pageable is an interface that represents:
    “Which slice of data do you want?”

    It answers:
    which page number
    how many records per page
    (optionally) sorting info


    What is PageRequest?
    PageRequest is a concrete implementation of Pageable.

    PageRequest.of(pageNumber, pageSize)

    means:
    “Give me page <pageNumber> with <pageSize> records per page.”
    Example:
    PageRequest.of(0, 10)
    = first 10 records
    PageRequest.of(1, 10)

    = next 10 records

    Important rule
    Pagination in Spring Data is 0-based:
    
    page 0 → first page
    
    page 1 → second page

2-> Page<Category> categoryPage = categoryRepository.findAll(pageDetails);
    What is Page<T>?
    Page<T> is a wrapper object that contains:
    the actual data (List<T>)
    pagination metadata

    So it’s not just data — it’s data + info about data.

    What does findAll(pageDetails) do?
    Instead of fetching all categories, it:
    Applies pagination rules from Pageable
    
    Generates SQL like:
    
    SELECT *
    FROM category
    LIMIT 10 OFFSET 10;
    (assuming pageNumber=1, pageSize=10)

    Wraps the result in a Page<Category>
    
    What does Page<Category> contain?
    1️⃣ The data (what you are using)
    List<Category> list = categoryPage.getContent();
    This is the current page’s data only.


# MANAGING PRODUCTS SECTION

added new product model, repository, controller,service 
in product model added Category object with many to one mapping

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    Big-picture meaning (one sentence)
    Many products belong to one category, and this relationship is stored in the database using a foreign key column called category_id in the product table.


    What @ManyToOne means conceptually
    @ManyToOne
    private Category category;

    This tells JPA: 
    Many Product rows → One Category row
    Each product has exactly one category
    A category can have many products