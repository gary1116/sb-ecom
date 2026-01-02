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

