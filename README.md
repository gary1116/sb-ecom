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



**************************************************************************************************************************************************************

**********************************Socialmedia Backend miniproject learning part for springdata jpa to use db related annotations**********************************
- git link :- https://github.com/gary1116/socialMedia_backend

# ONE TO ONE RELATIONSHIPS

- SocialUser

        package com.social.demo.models;
        import jakarta.persistence.Entity;
        import jakarta.persistence.GeneratedValue;
        import jakarta.persistence.GenerationType;
        import jakarta.persistence.Id;
        
        @Entity
        public class SocialUser {
        
            @Id
            @GeneratedValue(strategy = GenerationType.IDENTITY)
            private Long id;
        
        }


- Profile

        package com.social.demo.models;

        import jakarta.persistence.*;
        
        @Entity
        public class Profile {
        
            @Id
            @GeneratedValue(strategy = GenerationType.IDENTITY)
            private Long id;
        
        
            @OneToOne
            @JoinColumn(name = "social_user")
            private SocialUser socialUser;
        
        }


here
@OneToOne
@JoinColumn(name = "social_user")
private SocialUser socialUser;
🧠 Big picture (VERY IMPORTANT)
You are saying:

Each Profile is linked to exactly ONE SocialUser

That’s it.
This is a One-to-One relationship.

Think of it like:


SocialUser  <---->  Profile
One user → one profile
One profile → one user

🧩 What does @OneToOne actually mean?
1️⃣ @OneToOne (relationship definition)
java
Copy code
@OneToOne
private SocialUser socialUser;
This tells JPA:

“This field is NOT a normal column.
It represents a relationship to another table.”

Without this annotation:

JPA would treat SocialUser as a normal Java object

❌ That would NOT work in DB mapping

So:

@OneToOne =
👉 This entity is related to another entity in a one-to-one manner

🔗 What does @JoinColumn do?
java
Copy code
@JoinColumn(name = "social_user")
This answers WHERE and HOW the relationship is stored.

It tells JPA:
“Create a foreign key column in the profile table
that references social_user.id”

🗄️ Database tables created (VERY IMPORTANT)
🔹 social_user table
id
1
2

🔹 profile table (with @JoinColumn)
id	social_user
10	1
11	2

➡️ social_user is a FOREIGN KEY
➡️ It points to social_user.id

This is the actual link between the tables.

- Why is Profile owning the relationship?

        Because the foreign key is inside profile table.
        
        @JoinColumn(name = "social_user")
        
        
        ➡️ Profile owns the relationship
        ➡️ SocialUser does NOT need to know anything (yet)
        
        This is called the owning side


# BI-DIRECTIONAL MAPPING IN ONE TO ONE RELATIONSHIPS


wo entities that both know about each other:

SocialUser  <------>  SocialProfile


SocialProfile → knows its SocialUser

SocialUser → knows its SocialProfile

This is called a bidirectional relationship.

🧠 The MOST IMPORTANT RULE (memorize this)

Only ONE side owns the relationship

The owning side is the one that has the foreign key

In your code:

@JoinColumn(name = "social_user")
private SocialUser socialUser;


➡️ SocialProfile is the OWNING side
➡️ SocialUser is the INVERSE (non-owning) side

🔍 Let’s analyze each class separately
1️⃣ SocialProfile (OWNING SIDE)
@OneToOne
@JoinColumn(name = "social_user")
private SocialUser socialUser;

What this means:

This side:

✅ Creates the foreign key

✅ Controls the DB relationship

✅ Decides how rows are linked

Database result:

social_profile

id	social_user
1	10

➡️ social_user is a foreign key → social_user.id

🔥 This side writes to the database

2️⃣ SocialUser (INVERSE SIDE)
@OneToOne(mappedBy = "socialUser")
private SocialProfile socialProfile;

What does mappedBy = "socialUser" mean?

It literally means:

“I do NOT own this relationship.
The relationship is already mapped by the field
socialUser inside SocialProfile.”

So JPA understands:

❌ Do NOT create another foreign key

❌ Do NOT create another join table

✅ Just reuse the existing mapping

🧠 Why is mappedBy REQUIRED here?

Without mappedBy:

@OneToOne
private SocialProfile socialProfile;


JPA would think:

“Oh, another relationship!
I’ll create ANOTHER foreign key or join table 😈”

That leads to:

Duplicate mappings

Extra tables

Broken schema

Confusing bugs

mappedBy prevents that.

🗄️ Final database structure (VERY IMPORTANT)

Even after adding @OneToOne in SocialUser…

👉 DATABASE DOES NOT CHANGE

Still only two tables:

social_user

| id |

social_profile

| id | social_user (FK) |

✔ No new column
✔ No new table
✔ No duplicate foreign key

One-line mental model (interview gold)

@JoinColumn = owns the relationship
mappedBy = points to the owner

# ONE TO MANY RELATIONSHIP AND MANY TO ONE

What tables/columns will be created?
social_user table

id (PK)

post table

id (PK)

user_id (FK → social_user.id) ✅ because of:

@ManyToOne
@JoinColumn(name="user_id")
private SocialUser socialUser;


So yes: user_id will be a column in post table and it will store the social_user.id.

🔥 Who “handles” (owns) the relationship?

In JPA:

The side with the foreign key is the owning side.

Here, the FK is in Post, so:

✅ Post.socialUser (@ManyToOne + @JoinColumn) = owning side

❌ SocialUser.post (@OneToMany(mappedBy="socialUser")) = inverse side

So your line:

“one to many relationship which is handled by Post class…”

✅ Correct: Post is the one that actually controls the DB link, because it contains the FK column.

What exactly does mappedBy = "socialUser" mean here?
@OneToMany(mappedBy = "socialUser")
private List<Post> post = new ArrayList<>();


This tells JPA:

“Don’t create another column/table for this.
The relationship is already stored in Post.socialUser.”

So JPA will NOT create:

a social_user_id column in social_user table (doesn’t make sense)

a join table like social_user_posts (that would happen if you didn’t use mappedBy)

✅ What happens at runtime when you save?
Important rule:

Only setting user.getPost().add(post) is not enough to persist the FK.

Because the inverse side (OneToMany) does not write the FK.

You must set the owning side:

post.setSocialUser(user);  // this sets user_id in post table


Best practice: keep both sides in sync using a helper:

public void addPost(Post p) {
post.add(p);
p.setSocialUser(this);
}

Your final question (answer)

“So JPA will see SocialUser class and see one-to-many relationship which is handled by Post class which will have column user_id in post table?”

✅ Yes — the @OneToMany in SocialUser is mainly for navigation (user → posts).
✅ The actual column user_id is created because of the @ManyToOne @JoinColumn in Post.

- Post -> SocialUser (owning side, writes FK)
- SocialUser -> List<Post> (inverse side, for navigation)



# MANY TO MANY RELATIONSHIP

The relationship is stored in a separate join table (user_group) that contains two foreign keys:

user_id → social_user.id

group_id → groups.id

And yes: SocialUser is the owning side in your code (because it defines @JoinTable). Groups is the inverse side (because it uses mappedBy).

- Now I’ll explain only the many-to-many part line by line. (check SocialUser and Groups class for reference)

SocialUser: Many-to-many part (line by line)
@ManyToMany


Says: one user can be in many groups AND one group can have many users.

@JoinTable(
name="user_group",


Tells JPA: create/use a join table named user_group (this table holds the mapping).

    joinColumns=@JoinColumn(name="user_id"),


This is the FK column in user_group that points to THIS entity’s table (social_user).

So user_group.user_id references social_user.id.

    inverseJoinColumns=@JoinColumn(name="group_id")


This is the FK column in user_group that points to the other entity’s table (groups).

So user_group.group_id references groups.id.

private Set<Groups> groups = new HashSet<>();


In Java: a user has a set of Groups (set avoids duplicates like same group added twice).

✅ Because SocialUser declares @JoinTable, this is the OWNING side.

Groups: Many-to-many part (line by line)
@ManyToMany(mappedBy = "groups")


mappedBy = "groups" means:

“The join table mapping is already defined on the other side — in SocialUser.groups.”
So Groups will NOT create another join table.
This side is inverse / non-owning side.
private Set<SocialUser> socialUsers = new HashSet<>();


In Java: a group has a set of users.

- Mental model you should keep

Entity PK → always @Id
FK column name → customizable with @JoinColumn
Linking logic → entity + PK, not column name
Defaults exist → annotations override them
- One-liner you can remember (interview safe)

“JPA links entities using primary keys, not column names —
@JoinColumn only controls how the foreign key column is named.”


- regarding joincolumns, inverseJoinColumns
  @JoinTable(
  name = "user_group",
  joinColumns = @JoinColumn(name = "user_id"),
  inverseJoinColumns = @JoinColumn(name = "group_id")
  )

what problem do these two solve?
A join table has TWO foreign keys.

So JPA must know:
Which FK column points to THIS entity?
Which FK column points to the OTHER entity?
That is exactly what these two attributes answer.

Meaning in plain English

- joinColumns

      “This column belongs to ME (the owning entity).”
          Here:
          Owning entity = SocialUser
          So:
          user_group.user_id → social_user.id

    -  inverseJoinColumns

            “This column belongs to the OTHER entity.”
            Here:
            Other entity = Groups
            So:
            user_group.group_id → groups.id

# DATAINITIALIZER

**EXPLANATION**

- @Configuration — what it does
  @Configuration
  public class DataInitializer {
  Meaning (one line):
  Tells Spring that this class defines beans and should be instantiated and managed by the ApplicationContext.
  Because of this:
  Spring creates one object of DataInitializer
  Constructor injection works
  @Bean methods inside are executed
------------------------------------------------------
- @Bean — what it does
  @Bean
  public CommandLineRunner initializeData() {

Meaning (one line):
Registers the returned CommandLineRunner as a Spring bean that runs automatically after the application context is fully initialized.
Because of this:
Spring calls initializeData()
Stores the returned runner
Executes it at startup

**Code blocks — one line per block (exactly)**
- Block 1: Create users
  SocialUser user1 = new SocialUser();
  SocialUser user2 = new SocialUser();
  SocialUser user3 = new SocialUser();
  ➡️ Creates three user objects in memory.

- Block 2: Save users
  userRepository.save(user1);
  userRepository.save(user2);
  userRepository.save(user3);
  ➡️ Persists users into the social_user table and assigns IDs.

- Block 3: Create groups
  Groups group1 = new Groups();
  Groups group2 = new Groups();
  ➡️ Creates two group objects in memory.

- Block 4: Add users to groups (inverse side)
  group1.getSocialUsers().add(user1);
  group1.getSocialUsers().add(user2);
  group2.getSocialUsers().add(user2);
  group2.getSocialUsers().add(user3);
  ➡️ Updates Java-side group↔user relationship (inverse side, not DB-owning).

- Block 5: Save groups
  groupRepository.save(group1);
  groupRepository.save(group2);
  ➡️ Persists groups into the groups table.

- Block 6: Associate users with groups (owning side)
  user1.getGroups().add(group1);
  user2.getGroups().add(group1);
  user2.getGroups().add(group2);
  user3.getGroups().add(group2);
  ➡️ Sets the owning side of the many-to-many relationship.

- Block 7: Save users again
  userRepository.save(user1);
  userRepository.save(user2);
  userRepository.save(user3);
  ➡️ Writes entries into the user_group join table.

- Block 8: Create posts
  Post post1 = new Post();
  Post post2 = new Post();
  Post post3 = new Post();
  ➡️ Creates three post objects in memory.

- Block 9: Associate posts with users
  post1.setSocialUser(user1);
  post2.setSocialUser(user2);
  post3.setSocialUser(user3);
  ➡️ Sets the owning side of the many-to-one relationship.

- Block 10: Save posts
  postRepository.save(post1);
  postRepository.save(post2);
  postRepository.save(post3);
  ➡️ Persists posts with user_id foreign key in the post table.

- Block 11: Create profiles
  SocialProfile profile1 = new SocialProfile();
  SocialProfile profile2 = new SocialProfile();
  SocialProfile profile3 = new SocialProfile();
  ➡️ Creates profile objects in memory.

- Block 12: Associate profiles with users
  profile1.setUser(user1);
  profile2.setUser(user2);
  profile3.setUser(user3);
  ➡️ Sets the owning side of the one-to-one relationship.

- Block 13: Save profiles
  socialProfileRepository.save(profile1);
  socialProfileRepository.save(profile2);
  socialProfileRepository.save(profile3);
  ➡️ Persists profiles with social_user foreign key.

***One-screen summary (remember this)***

@Configuration → Spring creates & manages this class

@Bean → Spring executes and registers what the method returns

CommandLineRunner → runs after startup

Repositories → write data to DB

Owning side → decides foreign keys / join tables

# regaring spring beans

@Component
→ Generic Spring bean

@Configuration
→ Special component used to define other beans

@Service
→ Component that holds business logic

@Repository
→ Component that talks to the database

@Controller
→ Component that handles web requests


# What is cascading?

Cascading means: “Do the same database operation to related objects automatically.”

That’s it.
Without cascading (what you’re doing now)

    You do this manually:
    
    userRepository.save(user);
    postRepository.save(post);
    profileRepository.save(profile);
    
    
    You are saying:
    
    “Save user, then save everything related one by one.”

With cascading

    You do this:
    userRepository.save(user);
    
    And Spring/JPA automatically:
    
    saves the profile
    saves the posts
    updates join tables
    Because you told it:
    “When I save/delete this object, apply the same operation to its related objects.”

*****TYPES OF CASCADING*****

1️⃣ CascadeType.PERSIST
When you save the parent, the child is also saved.
@OneToMany(cascade = CascadeType.PERSIST)
userRepository.save(user); // saves posts too

Use when:
Child should be created only with parent

2️⃣ CascadeType.MERGE
When you update the parent, the child is also updated.
@OneToMany(cascade = CascadeType.MERGE)
user.setName("New Name");
userRepository.save(user); // updates children


Use when:
You edit child data via parent

3️⃣ CascadeType.REMOVE
When you delete the parent, the child is also deleted.
@OneToMany(cascade = CascadeType.REMOVE)
userRepository.delete(user); // deletes posts too


⚠️ Dangerous if children are shared.

4️⃣ CascadeType.REFRESH
When parent is refreshed from DB, children are refreshed too.
@OneToMany(cascade = CascadeType.REFRESH)


Use when:
You want latest DB state for children

5️⃣ CascadeType.DETACH
When parent is detached from persistence context, children are detached too.
@OneToMany(cascade = CascadeType.DETACH)


Use when:
You manually manage entity states

6️⃣ CascadeType.ALL
Does everything above (PERSIST, MERGE, REMOVE, REFRESH, DETACH).
@OneToMany(cascade = CascadeType.ALL)


Most common choice.

Relationship	Recommended cascade
One-to-One (User → Profile)	ALL
One-to-Many (User → Post)	ALL
Many-to-Many (User ↔ Groups)	❌ Avoid REMOVE



hich cascade runs on which operation
✅ CascadeType.PERSIST

Triggers when you create/save a NEW parent (persist)

✅ child is inserted too

❌ does not delete child

When it happens: entityManager.persist(parent) (and usually repo.save(parent) for new entities)

✅ CascadeType.MERGE

Triggers when you update/attach an existing parent (merge)

✅ child is updated/merged too

❌ does not delete child

When it happens: entityManager.merge(parent) (often repo.save(parent) when parent already has an id)

✅ CascadeType.REMOVE

Triggers when you delete the parent

✅ child is deleted too

❌ does not save/update child

When it happens: entityManager.remove(parent) / repo.delete(parent)

✅ CascadeType.REFRESH

Triggers when you refresh parent from DB

✅ child is refreshed too

❌ no save/update/delete

When it happens: entityManager.refresh(parent)

✅ CascadeType.DETACH

Triggers when you detach parent from persistence context

✅ child becomes detached too

❌ no save/update/delete

When it happens: entityManager.detach(parent) (less common in Spring apps)

✅ CascadeType.ALL

Includes: PERSIST + MERGE + REMOVE + REFRESH + DETACH

So: save + update + delete + refresh + detach all cascade.


**************************************************************************************************************************************************************


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



# UPDATING PRODUCT IMAGE

- Controller
  @PutMapping("/products/{productId}/image")
  public ResponseEntity<ProductDTO> updateProductImage(@PathVariable Long productId,
  @RequestParam("image")MultipartFile image) throws IOException {

       ProductDTO updatedProductDto= productService.updateProductImage(productId,image);

        return new ResponseEntity<>(updatedProductDto,HttpStatus.OK);
  }

        What @RequestParam("image") means here
        @RequestParam is used to extract data from the request, but not from JSON.
        In this case, it means:
        “Get the request parameter named image from the incoming HTTP request.”


        Q. Why @RequestParam and NOT @RequestBody?
        Because file uploads are sent as:
        Content-Type: multipart/form-data
        NOT as JSON.
        So this ❌ would not work:
        @RequestBody MultipartFile image
        Spring uses @RequestParam to bind multipart fields.


        What MultipartFile is
        public ResponseEntity<ProductDTO> updateProductImage(@PathVariable Long productId, @RequestParam("image")MultipartFile image)
        MultipartFile is a Spring abstraction for uploaded files. 
        It represents:
        the uploaded file
        in memory or temporary disk storage
        BEFORE you save it permanently
        It provides methods like:
        image.getOriginalFilename()
        image.getInputStream()
        image.getSize()
        image.getContentType()

        ***So when Postman sends:*** 
        Key: image
        Type: File
        Value: robot.jpg

        ***Spring:***
        Parses the multipart request
        Finds the image field
        Wraps it in a MultipartFile
        Injects it into your method


- private String uploadImage(String path, MultipartFile file) throws IOException Method

        private String uploadImage(String path, MultipartFile file) throws IOException {

        // get File names of current/original file
        String originalFileName= file.getOriginalFilename();

        //generate a unique file name
        String randomId= UUID.randomUUID().toString();
        //        if file name-> Gary.jpg -> random id -> 1234-> it will be saved as 1234.jpg
        // this will give the extension originalFileName.substring(originalFileName.lastIndexOf('.')
        // like .jpg, .jpeg and concat with randomId created above
        String fileName= randomId.concat(originalFileName.substring(originalFileName.lastIndexOf('.')));
        //pathSeparator is nothing but a forward slash "/"
        String filePath=path+ File.separator+fileName;

        //check if path exist and create
        File folder=new File(path);
        if(!folder.exists())
            folder.mkdir();

        // upload to server
        Files.copy(file.getInputStream(), Paths.get(filePath));
        return fileName;

        }

***private String uploadImage(String path, MultipartFile file) throws IOException***
- Meaning:
        takes the upload directory path
        takes the uploaded file
        returns the final stored filename

***Step 1: Get original file name***
        java
        String originalFileName = file.getOriginalFilename();
        If user uploads:
        robot.jpg
        Then:
        originalFileName = "robot.jpg"
        This is the client-side filename.

***Step 2: Generate a unique ID***
        java
        String randomId = UUID.randomUUID().toString();
        Why?
        Prevent filename clashes
        Two users uploading image.jpg won’t overwrite each other
        Example:
        randomId = "a3f9-2c91-4e2d-b8f1"
***Step 3: Preserve file extension***
        originalFileName.substring(originalFileName.lastIndexOf('.'))
        For:
        robot.jpg
        This extracts:
        .jpg
        So final filename becomes:
        String fileName = randomId.concat(".jpg");
        Example:
        a3f9-2c91-4e2d-b8f1.jpg
***Step 4: Build file path***
        String filePath = path + File.separator + fileName;
        If:
        path = "images"
        Then:
        images/a3f9-2c91-4e2d-b8f1.jpg
        This is where the file will be stored.
***Step 5: Ensure directory exists***
        File folder = new File(path);
        if (!folder.exists())
        folder.mkdir();

        What new File(path) really means
        File folder = new File("images");
        This does NOT create a folder

        It only creates a Java object that represents:
        “a directory (or file) named images on disk”

        Think of it like:
        “a reference to a possible folder”
        
        At this point:
        No folder is created
        Java is just pointing to a location

        What folder.exists() checks
        folder.exists()
        
        This asks the OS:
        “Does a file or directory named images already exist on disk?”
        ✅ true → folder already exists
        ❌ false → folder does NOT exist

        What folder.mkdir() actually does
        folder.mkdir();
        
        This tells the OS:
        “Create a directory named images at this path.”
        So if your project is running from:
        C:/projects/sb-ecom/
        Then after mkdir():
        C:/projects/sb-ecom/images/
        is created on disk.


***Step 6: Save the file to disk***
        Files.copy(file.getInputStream(), Paths.get(filePath));

A) file.getInputStream()
What is an InputStream (simple meaning)

An InputStream is:
      A stream of bytes that Java can read sequentially
      Think of it like:
      “a pipe from which Java can read file data bit by bit”
      What MultipartFile is holding

When a file is uploaded:
      It’s temporarily stored in memory or temp disk
      It is not yet saved permanently

So:
file.getInputStream()
means:
      “Give me a stream to read the uploaded file’s raw bytes”

At this point:
        File is still temporary
        Java hasn’t written it anywhere permanent yet
        B) Paths.get(filePath)
        Paths.get("images/abc123.jpg")


This creates a Path object representing:

“the destination where the file should be written”
It’s the target location on disk.

C) What Files.copy() does
Files.copy(inputStream, destinationPath);

This literally means:
        “Read bytes from this stream
        and write them into this file path”
        Step-by-step:
        Open input stream (uploaded file)
        Open output stream (new file on disk)
        Copy bytes chunk by chunk
        Close streams automatically
        
After this line:
        File exists physically on disk
        Upload is complete


***Step 7: Return filename***
        return fileName;
        You store only the filename in DB, not the whole path.



**************************************************************************************************************************************************************

**********************************Spring security miniproject learning part for spring security related code**********************************
- git link :- https://github.com/gary1116/SpringSecurity


***code***

@EnableWebSecurity
@EnableMethodSecurity
@Bean
SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception{
http.authorizeHttpRequests((requests)->requests.
requestMatchers("/h2-console/**").permitAll()
.anyRequest().authenticated());
http.sessionManagement(session->session.sessionCreationPolicy(SessionCreationPolicy.STATELESS   ));
http.formLogin(withDefaults());
http.httpBasic(withDefaults());
http.headers(headers->
headers.frameOptions(frameOptionsConfig ->
frameOptionsConfig.sameOrigin()));
http.csrf(csrf->csrf.disable());
return http.build();
}

**********

@Configuration
@EnableWebSecurity
public class SecurityConfig {
- @Configuration:
  this class contributes beans.

    - @EnableWebSecurity:
      Registers SecurityFilterChain
      Hooks it into the Servlet filter pipeline
      Ensures every HTTP request passes through security filters
      SecurityConfig is your configuration class.


@Bean
SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception{
- @Bean:
  registers the returned SecurityFilterChain in the ApplicationContext.

Method name defaultSecurityFilterChain is just the bean name by default.

- HttpSecurity http: Spring injects/provides the HttpSecurity builder into this method so you can configure it.

- throws Exception: many HttpSecurity configuration calls can throw checked exceptions.


- http.authorizeHttpRequests((requests)->requests.requestMatchers("/h2-console/**").permitAll().anyRequest().authenticated());
  This sets authorization rules (who can access what).
  authorizeHttpRequests(...): start defining access rules for HTTP endpoints.
  requestMatchers("/h2-console/**").permitAll():
  any URL like /h2-console, /h2-console/login.do, etc. is allowed without login.
  /** means “anything under this path”.
  .anyRequest().authenticated():
  every other endpoint must be logged in.
  ✅ So: H2 console public, everything else protected.


- http.sessionManagement(session->session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
  Configures session behavior.

-> SessionCreationPolicy.STATELESS
It means:
Spring Security will not create or use an HTTP session
So no JSESSIONID cookie is used to remember the user
➡️ In practice: no session cookie-based auth

STATELESS means:

Spring Security will not create or use HTTP sessions to store authentication.
This is typical for REST APIs with JWT or token-based auth.
Important: if you are using formLogin() (session-based) this doesn’t match the usual approach, because form login typically relies on a session to remember the logged-in user.
So here you’ve mixed two styles:

STATELESS (API/token style)
formLogin() (session/browser style)
It can still compile, but behavior might be confusing.

- http.formLogin(withDefaults());
  Enables form-based login with default settings.
  Default behavior:
  Spring Security auto-generates a login page if you didn’t create one.
  When you submit credentials, it authenticates and (normally) stores auth in the session.
  Again: this normally expects stateful session behavior.

- http.httpBasic(withDefaults());
  Enables HTTP Basic authentication (username/password sent in the Authorization: Basic ... header).
  This is common for quick testing with Postman/curl, internal services, etc.
  With stateless APIs, Basic can work (each request sends credentials).


- http.headers(headers->headers.frameOptions(frameOptionsConfig ->frameOptionsConfig.sameOrigin()));
  This is specifically for H2 console (and other pages that use HTML frames/iframes).
  Modern browsers + Spring Security block pages from being shown in an <iframe> to prevent clickjacking.
  H2 console UI is served in a way that needs frames/iframes.
  frameOptions().sameOrigin() means:
  allow framing only if the page is from the same origin (same domain/host).
  safer than disabling frame options entirely.
  ✅ Without this, you often get a blank page / refused to display H2 console.


- http.csrf(csrf->csrf.disable());
  Disables CSRF protection.
  CSRF protection mainly matters for browser session-based apps using cookies.
  H2 console does POST requests and often breaks when CSRF is enabled (you’ll see 403 errors).
  ✅ Disabling CSRF makes H2 console usable easily during dev.

- return http.build();
  Finalizes the configuration and builds the SecurityFilterChain object that Spring Security will use to secure requests.


***code***

@Bean
public UserDetailsService userDetailsService(){
UserDetails user1= User.withUsername("user1")
.password("{noop}password1")
.roles("USER")
.build();
UserDetails admin= User.withUsername("admin")
.password("{noop}adminPass")
.roles("ADMIN")
.build();
JdbcUserDetailsManager userDetailsManager=
new JdbcUserDetailsManager(dataSource);
userDetailsManager.createUser(user1);
userDetailsManager.createUser(admin);
return userDetailsManager;
}

***********


What is DataSource?

In simple words:
DataSource is a factory that gives database connections to your application.
Instead of writing:

DriverManager.getConnection(...)
Spring gives you:

DataSource
which:
knows DB URL
knows username / password
knows driver
manages connections properly

Where did this DataSource come from?

You did NOT create it manually, right?
Spring Boot created it automatically because you added:

- spring.datasource.url=jdbc:h2:mem:test
- spring.h2.console=true
  When Spring Boot sees:
  spring.datasource.url
  H2 on the classpath

➡️ It auto-configures a DataSource bean.

So this works:

- @Autowired
- DataSource dataSource;
  because Spring already has a DataSource bean ready.

What database is this DataSource pointing to?
- spring.datasource.url=jdbc:h2:mem:test
  Means:
  H2 database
  in-memory
  database name = test
  lives only while app is running
  So:
  restart app → DB gone


Why JdbcUserDetailsManager needs DataSource
Earlier (in-memory)
new InMemoryUserDetailsManager(...)
Users stored in Java memory (RAM)
→ lost on restart
→ no DB
Now (JDBC)
new JdbcUserDetailsManager(dataSource)
“Store users in a database, using JDBC, using this DataSource.”

So:
users go into DB tables
Spring Security reads users from DB
authentication is DB-backed

Now your method — line by line
- @Bean
- public UserDetailsService userDetailsService(){


You’re defining a UserDetailsService bean

Spring Security will use this to:
load users
verify passwords
check roles
- UserDetails user1 = User.withUsername("user1")
  Start building a user with username user1
- .password("{noop}password1")
  Password = password1
  {noop} = no encoding (plain text)
  Needed because Spring Security expects encoded passwords
- .roles("USER")
  Assigns role USER
  Internally stored as ROLE_USER
- .build();
  Creates the UserDetails object


- JdbcUserDetailsManager userDetailsManager = new JdbcUserDetailsManager(dataSource);
  🔥 THIS IS THE KEY LINE

What happens here:
You create a JdbcUserDetailsManager
You pass the DataSource

This tells Spring Security:
“Use this database to store & fetch users”

Internally:
Uses JDBC
Uses SQL queries

Uses tables like:
users
authorities
***(Spring Security expects a default schema for this)***

- userDetailsManager.createUser(user1);
  Inserts user1 into DB

Writes into:
users table
authorities table

Equivalent SQL (conceptually):
INSERT INTO users ...
INSERT INTO authorities ...

- return userDetailsManager;
  You return a database-backed UserDetailsService

Spring Security now:
authenticates users from H2 DB
not from memory
***code***

@PreAuthorize("hasRole('ADMIN')")
@GetMapping("/admin")
public String adminEndpoint(){
return "Hello Admin";
}

**********

@PreAuthorize("hasRole('ADMIN')")
@GetMapping("/admin")
public String adminEndpoint(){
return "Hello Admin";
}
What @PreAuthorize("hasRole('ADMIN')") means
Only an authenticated user who has the role ADMIN is allowed to execute this method.

If the condition fails, the method is never called.


- @EnableMethodSecurity
  turns ON security checks at the method level.

- URL security vs Method security (quick clarity)
  Type	Example	Purpose
  URL-level	http.authorizeHttpRequests()	Protects endpoints by path
  Method-level	@PreAuthorize	Protects business logic


- What is JWT?
  A self-contained proof of authentication that the client sends with every request.
  Instead of:server remembering you in a session

    - without JWT :-
      ***Traditional login (session-based)***
      How your app works right now:
      User logs in (form login / basic auth)
      Server authenticates user
      Server remembers user (or rechecks credentials)
      Client sends request
      Server checks authentication again
      Even with STATELESS, you’re still sending username + password every time (Basic Auth).

    -   ***JWT-based login (token-based)***
        JWT flow:
        User logs in with username + password
        Server verifies credentials
        Server generates a JWT
        Server sends JWT to client
        Client stores JWT (localStorage / memory)
        Client sends JWT in every request:
        Authorization: Bearer <jwt>
        Server verifies token (signature + expiry)
        Server allows/denies request
        🚫 No session
        🚫 No password sent again
        ✅ Fully stateless

    - What is inside a JWT?

      A JWT has 3 parts:
      header.payload.signature
      Example (decoded)
      {
      "sub": "admin",
      "roles": ["ROLE_ADMIN"],
      "iat": 1700000000,
      "exp": 1700003600
      }
      sub → username
      roles → authorities
      iat → issued at
      exp → expiry time
      The token is signed, so it can’t be tampered with.

***FILES THAT WE WOULD NEED TO IMPLEMENT JWT IN OUR PROJECT***
JwtUtils
AuthTokenFilter
AuthEntryPointJwt

- JwtUtils
  Contains utility methods for generating, parsing and validating jwts
  Include generating a token from a username, validating a JWT and extracting the username from a token
- AuthTokenFilter
  Filters incoming requests to check for a valid JWT in the header,
  setting the authentication context if the token is valid
  Extracts JWT from request header, validates it and
  configures the Spring Security context with user details if the token is valid
- AuthEntryPointJwt
  Provides Custom handling for unauthorized requests,
  typically when authentication is required but not supplied or valid
  when an unauthorized request is detected, it logs the error and returns a JSON
  response with an error message, status code, and the path attempted


***code***
@Component
public class JwtUtils {
private static final Logger logger= LoggerFactory.getLogger(JwtUtils.class);

    @Value("${spring.app.jwtExpirationMs}")
    private int jwtExpirationMs;
    @Value("${spring.app.jwtSecret}")
    private String JwtSecret;
    //getting JWT from Header
    public String getJwtFromHeader(HttpServletRequest request){
        String bearerToken= request.getHeader("Authorization");
        logger.debug("Authorization header: {}",bearerToken);
        if(bearerToken!=null && bearerToken.startsWith("Bearer ")){
            return bearerToken.substring(7); //Remove Bearer prefix
        }
        return null;
    }

    //Generating Token from Username
    public String generateTokenFromUsername(UserDetails userDetails){
        String username= userDetails.getUsername();
        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date((new Date().getTime()+jwtExpirationMs)))
                .signWith(key())
                .compact();
    }

    //Getting Username from JWT Token
    public String getUsernameFromJWTToken(String token){
        return Jwts.parser()
                .verifyWith((SecretKey) key())
                .build().parseSignedClaims(token)
                .getPayload().getSubject();
    }
    //Generate Signing Key
    public Key key(){
        return Keys.hmacShaKeyFor(
                Decoders.BASE64.decode(JwtSecret)
        );
    }

    // Validate JWT Token
    public boolean validateJwtToken(String authToken){

        try{
            System.out.println("validate");
            Jwts.parser()
                    .verifyWith((SecretKey) key())
                    .build()
                    .parseSignedClaims(authToken);
            return true;

        }catch(MalformedJwtException exception){
                logger.error("Invalid JWT token:{}", exception.getMessage());
        }catch(ExpiredJwtException exception){
            logger.error("JWT token is expired:{}", exception.getMessage());
        }catch(UnsupportedJwtException exception){
            logger.error("JWT token is not supported:{}", exception.getMessage());
        }catch(IllegalArgumentException exception){
            logger.error("JWT claims string is empty:{}", exception.getMessage());
        }
        return false;
    }

}
**********

# EXPLANATION OF ABOVE CODE


- @Component
    - public class JwtUtils {
      @Component → Spring will:
      create this class as a Spring bean
      allow it to be injected using @Autowired
      This makes JwtUtils available to filters, services, etc.


- private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);
  Creates a logger for this class
  Used to log debug and error messages
  Better than System.out.println() for production code

Reading values from application.properties

- @Value("${spring.app.jwtExpirationMs}")
    - private int jwtExpirationMs;
      Injects value from:
      properties
      spring.app.jwtExpirationMs=600000000000
      This value represents token validity duration (milliseconds)




- @Value("${spring.app.jwtSecret}")
    - private String JwtSecret;
      Injects your secret key from:
      spring.app.jwtSecret=mySecretKey!#912738
      This secret is used to:
      sign the JWT
      verify its integrity


- public String getJwtFromHeader(HttpServletRequest request){
  This method extracts the JWT from the HTTP request
  Called typically inside a JWT authentication filter


- String bearerToken = request.getHeader("Authorization");
  Reads the Authorization HTTP header
  Example header:
  Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...

- logger.debug("Authorization header: {}", bearerToken);
  Logs the header value (only visible if debug logging enabled)
  Useful for debugging missing or malformed tokens


- if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
  Checks:
  header exists
  uses Bearer scheme

- return bearerToken.substring(7);
  "Bearer " = 7 characters
  Removes "Bearer " prefix
  Returns only the JWT


- return null;
  If header missing or malformed → no token present
  Generating JWT from user details

- public String generateTokenFromUsername(UserDetails userDetails){
  Generates a JWT after successful authentication
  UserDetails contains:
  username
  roles
  account status


- String username = userDetails.getUsername();
  Extracts username
  This will be stored inside JWT as the subject


- return Jwts.builder()
  Starts building a JWT using jjwt library


.subject(username)
Sets sub (subject) claim
This identifies who the token belongs to


- .issuedAt(new Date())
  Sets iat claim
  Token creation time


- .expiration(new Date((new Date().getTime() + jwtExpirationMs)))
  Sets exp claim
  Token expiry time = current time + configured duration


- .signWith(key())
  Signs the JWT using HMAC key
  Ensures:
  token cannot be modified
  token authenticity can be verified


- .compact();
  Builds and serializes JWT into a string
  This string is what you send to the client
  Reading username from JWT

    - public String getUsernameFromJWTToken(String token){
      Extracts username from JWT
      Used during request authentication


- return Jwts.parser()
  Starts JWT parser


- .verifyWith((SecretKey) key())
  Verifies JWT signature using the same secret key
  If signature invalid → exception thrown


- .build()
- .parseSignedClaims(token)
  Parses and validates token
  Ensures:
  token not tampered
  signature valid
  not expired (unless caught elsewhere)


.getPayload().getSubject();
Extracts sub claim
Returns the username

- public Key key(){
  Creates the cryptographic key used to:
  sign JWT
  verify JWT


- return Keys.hmacShaKeyFor(Decoders.BASE64.decode(JwtSecret));
  What this is trying to do
  Decode secret from Base64
  Create HMAC-SHA key

🚨 PROBLEM HERE (important)
Your secret:
properties
spring.app.jwtSecret=mySecretKey!#912738
This is NOT Base64-encoded, so:
Decoders.BASE64.decode() will throw errors
OR produce invalid key
✅ Correct options
Option 1 (recommended): remove Base64 decoding
return Keys.hmacShaKeyFor(JwtSecret.getBytes());
Option 2: Base64-encode secret in properties
properties
spring.app.jwtSecret=bXlTZWNyZXRLZXkhIzkxMjczOA==

- public boolean validateJwtToken(String authToken){
  Checks whether JWT is:
  well-formed
  signed correctly
  not expired
  supported


- try {
- System.out.println("validate");
  Debug print (can be removed later)


- Jwts.parser()
- .verifyWith((SecretKey) key())
- .build()
- .parseSignedClaims(authToken);
  Parses and validates JWT
  If any problem occurs → exception thrown
  return true;
  Token is valid

- Exception handling (very important)

  catch (MalformedJwtException exception) {
  Token structure is invalid
  catch (ExpiredJwtException exception) {
  Token is expired (exp exceeded)
  catch (UnsupportedJwtException exception) {
  Token type/algorithm not supported
  catch (IllegalArgumentException exception) {
  Token is empty or null
  return false;
  Token validation failed





*****AuthTokenFilter code*****

@Component
public class AuthTokenFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserDetailsService userDetailsService;

    private static final Logger logger= LoggerFactory.getLogger(AuthTokenFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        logger.debug("AuthTokenFilter called for URI:{}", request.getRequestURI());
        try{
                String jwt=parseJwt(request);
                if(jwt != null && jwtUtils.validateJwtToken(jwt)){
                    String username=jwtUtils.getUsernameFromJWTToken(jwt);
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                    UsernamePasswordAuthenticationToken authentication=new UsernamePasswordAuthenticationToken(
                            userDetails,null,userDetails.getAuthorities()
                    );
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    logger.debug("Roles from jwt : {}",userDetails.getAuthorities());
                }
        }catch(Exception e){
            logger.error("cannot set user authentication:{}", e);
        }
        filterChain.doFilter(request,response);

    }

    private String parseJwt(HttpServletRequest request) {

        String jwt=jwtUtils.getJwtFromHeader(request);

        logger.debug("AuthTokenFilter.java: {}",jwt);
        return jwt;
    }
}

****************************


- @Component
- public class AuthTokenFilter extends OncePerRequestFilter {
  @Component → Spring creates this filter as a bean, so it can be injected/used in the security configuration.
  extends OncePerRequestFilter → guarantees this filter runs once per HTTP request (prevents double execution in the same request).

- Dependencies injected
  @Autowired
  private JwtUtils jwtUtils;
  Injects your JwtUtils bean.
  @Autowired
  private UserDetailsService userDetailsService;
  Injects the UserDetailsService bean.
  Used to load user info (roles/authorities/password flags) from DB or memory using:

    - private static final Logger logger = LoggerFactory.getLogger(AuthTokenFilter.class);
      Standard logger for debug + error messages.
      Main filter method

- @Override
- protected void doFilterInternal(HttpServletRequest request,
- HttpServletResponse response,
- FilterChain filterChain)
- throws ServletException, IOException {
  This is the method Spring calls for every request.
  Parameters:
  request: incoming HTTP request
  response: outgoing HTTP response
  filterChain: lets you pass control to the next filter/controller


- logger.debug("AuthTokenFilter called for URI:{}", request.getRequestURI());
  Logs which endpoint is being called.
  Useful to confirm this filter is running.


- try{
- String jwt = parseJwt(request);
  Wrap everything inside try-catch to avoid breaking request flow.
  parseJwt(request) fetches the JWT from the Authorization header using JwtUtils.


- if(jwt != null && jwtUtils.validateJwtToken(jwt)){
  Checks two things:
  JWT exists in header
  JWT is valid (signature ok, not expired, properly formed)
  If either fails → no authentication is set, request continues as anonymous.


- String username = jwtUtils.getUsernameFromJWTToken(jwt);
  Extracts username from JWT sub claim.
  Now you know “who is this request claiming to be?”


- UserDetails userDetails = userDetailsService.loadUserByUsername(username);
  Loads full user details from DB (or in-memory) by username.
  Why needed even though JWT has username?
  because Spring needs authorities (roles)
  and account flags (enabled, locked, etc.)
  and optionally you can re-check user still exists


- UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
  This creates a Spring Security Authentication object.
  Inside it:
  principal = userDetails (the logged-in user object)
  credentials = null (because we don’t store password here; JWT already proved identity)
  authorities = roles/permissions (like ROLE_ADMIN)
  This object tells Spring:
  “This request is authenticated as this user with these roles.”


- authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
  Adds request-specific metadata into the Authentication object.
  Example info stored:
  client IP address
  session id (if any)
  Mainly useful for auditing/logging.


- SecurityContextHolder.getContext().setAuthentication(authentication);
  🔥 Most important line in the entire filter.
  Spring Security stores the current user in a thread-local “security context”.
  After this line, Spring treats the request as logged in.
  Then:
  @PreAuthorize works
  hasRole('ADMIN') works
  SecurityContextHolder.getContext().getAuthentication() returns this user
  Without this line → even with a valid JWT, Spring thinks user is anonymous.


logger.debug("Roles from jwt : {}", userDetails.getAuthorities());
Logs the authorities loaded for the user.

Slight wording: roles are not “from jwt” here — you loaded them via UserDetailsService, not directly from token.
But debug-wise, it helps confirm the correct roles are applied.


- }catch(Exception e){
- logger.error("cannot set user authentication:{}", e);
- }
  If anything goes wrong (bad token, DB issue, casting, etc.):
  logs the error
  does NOT kill the request pipeline
  Result: request continues as unauthenticated.

Continue the chain
- filterChain.doFilter(request,response);
  Passes control to the next filter.
  Eventually reaches controller.
  This must be called, otherwise requests will hang.


- private String parseJwt(HttpServletRequest request) {
  Private helper to extract JWT from request.


- String jwt = jwtUtils.getJwtFromHeader(request);
  Calls your earlier method:
  reads Authorization header
  checks Bearer
  returns token string or null


- logger.debug("AuthTokenFilter.java: {}", jwt);
- return jwt;

*******AuthEntryPoint code*******

@Component
public class AuthEntryPointJwt implements AuthenticationEntryPoint {

    private static final Logger logger= LoggerFactory.getLogger(AuthEntryPointJwt.class);
    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException)
            throws IOException, ServletException {

        logger.error("Unauthorized error: {}", authException.getMessage());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        final Map<String, Object> body= new HashMap<>();
        body.put("status",HttpServletResponse.SC_UNAUTHORIZED);
        body.put("error","unauthorized");
        body.put("message",authException.getMessage());
        body.put("path",request.getServletPath());

        final ObjectMapper mapper= new ObjectMapper();
        mapper.writeValue(response.getOutputStream(), body);
    }
}

***************************************


# EXPLANATION FOR ABOVE CODE

- @Component
    - public class AuthEntryPointJwt implements AuthenticationEntryPoint {
      @Component
      Registers this class as a Spring bean
      Allows Spring Security to inject and use it in SecurityConfig
      implements AuthenticationEntryPoint
      This interface defines what happens when an unauthenticated user tries to access a protected resource
      In JWT apps, this replaces:
      redirect to login page ❌
      with JSON error response ✅


- private static final Logger logger = LoggerFactory.getLogger(AuthEntryPointJwt.class);
  Used to log authentication errors
  Helpful for debugging invalid/missing JWTs


- @Override
- public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)throws IOException, ServletException {
  This method is automatically called by Spring Security when:
  User is not authenticated
  AND tries to access a secured endpoint
  OR JWT is missing / invalid / expired
  You never call this method manually.


- logger.error("Unauthorized error: {}", authException.getMessage());
  Logs the reason authentication failed
  Examples:
  “JWT expired”
  “Full authentication is required”
  “Bad credentials”
  Set response type

    - response.setContentType(MediaType.APPLICATION_JSON_VALUE);
      Sets response Content-Type to:
      application/json
      Important for REST APIs (clients expect JSON, not HTML)


- response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
  Sets HTTP status code to 401
  Meaning:
  “You are not authenticated”
  ⚠️ Difference worth knowing:
  401 Unauthorized → not authenticated
  403 Forbidden → authenticated but not allowed
  Build response body

- final Map<String, Object> body = new HashMap<>();
  Creates a map to hold JSON response data


- body.put("status", HttpServletResponse.SC_UNAUTHORIZED);
  Adds status code: 401


- body.put("error", "unauthorized");
  Human-readable error type


- body.put("message", authException.getMessage());
  Actual reason authentication failed

Example:
“Full authentication is required to access this resource”


- body.put("path", request.getServletPath());
  Adds the endpoint path that was accessed
  Helps client understand which API failed

- final ObjectMapper mapper = new ObjectMapper();
  Jackson ObjectMapper converts Java objects to JSON


- mapper.writeValue(response.getOutputStream(), body);
  Writes the body map as JSON directly to HTTP response

        Output example:
        {
        "status": 401,
        "error": "unauthorized",
        "message": "Full authentication is required to access this resource",
        "path": "/api/admin"
        }


**************************************************************************************************************************************************************


# Added User, Address, Role Model and explanation of relationships below

User ↔ Role = Many-to-Many
User ↔ Address = Many-to-Many
User → Product and Product → User = One-to-Many / Many-to-One (seller relationship)

I’ll explain each relationship + the new annotations you used.

1) User ↔ Role — Many-to-Many
   In User
   @ManyToMany(cascade = {PERSIST, MERGE}, fetch = EAGER)
   @JoinTable(
   name="user_role",
   joinColumns = @JoinColumn(name = "user_id"),
   inverseJoinColumns = @JoinColumn(name = "role_id")
   )
   private Set<Role> roles = new HashSet<>();

Meaning
One user can have many roles (ADMIN, USER, etc.)
One role can belong to many users
How DB stores it
Because many-to-many can’t be stored in a single column, JPA creates a join table:
user_role
user_id (FK → users.user_id)
role_id (FK → roles.role_id)

So if Gary has ADMIN + USER, you get 2 rows in user_role.
Why Set<Role>
Avoids duplicate roles for a user automatically.
- fetch = FetchType.EAGER
When you load a User, it also loads roles immediately.
Useful for auth/login flows, but can become heavy if used everywhere.
cascade = PERSIST, MERGE
If you save a new User, it can also persist/merge the linked Role entities.
Important: In real apps, Roles are usually pre-created and you typically avoid cascading to roles to prevent accidental role creation.

2) User ↔ Address — Many-to-Many (bidirectional)
   In User (owning side)
   @ManyToMany(cascade = {PERSIST, MERGE})
   @JoinTable(
   name="user_addresses",
   joinColumns = @JoinColumn(name="user_id"),
   inverseJoinColumns = @JoinColumn(name = "address_id")
   )
   private List<Address> addresses = new ArrayList<>();

In Address (inverse side)
@ManyToMany(mappedBy = "addresses")
private List<User> users = new ArrayList<>();

Meaning
A user can have multiple addresses
The same address can be linked to multiple users
(example: family members share one home address)
Owning vs inverse side
The side with @JoinTable is the owning side → User
The side with mappedBy is the inverse side → Address
Only the owning side updates the join table.
Join table created

user_addresses
user_id
address_id

Why mappedBy = "addresses"
It means:
“Don’t create another join table from Address side. The relationship is already defined by the addresses field in User.”

3) User ↔ Product — One-to-Many / Many-to-One (seller owns products)
   In Product
   @ManyToOne
   @JoinColumn(name="seller_id")
   private User user;

In User
@OneToMany(
mappedBy = "user",
cascade = {PERSIST, MERGE},
orphanRemoval = true
)
private Set<Product> products;

Meaning
One user (seller) can sell many products
Each product has exactly one seller

    How DB stores it
    This is NOT a join table.
    Instead, the products table will have a column:
    seller_id (FK → users.user_id)
    So many products can point to the same user via seller_id.
    mappedBy = "user"

This means:

“User is NOT the owner of the relationship in DB. Product is the owner because it has the foreign key column (seller_id).”

So Product.user is the owning side.

- orphanRemoval = true (important)
This means:
    If a product is removed from user.products, JPA will delete that product row from the DB.
    Example:
    user.getProducts().remove(p1);
    → JPA can delete p1 from DB (if the entity is managed and transaction is correct).
    ⚠️ In many ecommerce apps, you may not want to physically delete products (you might “soft delete” instead).

New annotations you used
- @Table(uniqueConstraints = …)
- @Table(name = "users",
- uniqueConstraints = {
- @UniqueConstraint(columnNames = "username"),
- @UniqueConstraint(columnNames = "email")
- })
    Adds DB-level uniqueness:
    two users cannot share same username
    two users cannot share same email
    Even if validation is bypassed, DB enforces it.

- @Enumerated(EnumType.STRING)
- private AppRole roleName;
    Stores enum as text:
    ✅ ADMIN, USER (safe)
    instead of
    ❌ 0, 1 (dangerous if you reorder enum values)

- Lombok: @ToString.Exclude
You used it to prevent infinite recursion / huge logs.
Example:
User → addresses → users → addresses → users … (loop)
So you exclude the back-reference fields from toString().

- Quick relationship map (easy mental model)
    User * ↔ * Role via user_role
    User * ↔ * Address via user_addresses
    User 1 → * Product (seller) via products.seller_id
    Category 1 → * Product via products.category_id (from your earlier code)

# SECURITY

check this section ***Spring security miniproject learning part for spring security related code***
for explanation of below files  
JwtUtils
AuthTokenFilter
AuthEntryPointJwt

- UserDetailsImpl
**********CODE**********
  @NoArgsConstructor
  @Data
  public class UserDetailsImpl implements UserDetails {

  private static final long serialVersionUID=1L;

  private Long id;
  private String username;
  private String email;

  @JsonIgnore
  private String password;

  private Collection<? extends GrantedAuthority> authorities;

  public static UserDetailsImpl build(User user){

        List<GrantedAuthority> authorities= user.getRoles().stream()
                .map(role-> new SimpleGrantedAuthority(role.getRoleName().name()))
                .collect(Collectors.toList());

        return new UserDetailsImpl(
                user.getUserId(),
                user.getUsername(),
                user.getEmail(),
                user.getPassword(),
                authorities

        );
  }


    public UserDetailsImpl(Long id, String username, String email, String password, Collection<? extends GrantedAuthority> authority) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.authorities = authority;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public @Nullable String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return UserDetails.super.isEnabled();
    }

    @Override
    public boolean equals(Object o){
        if(this==o)
            return true;
        if(o==null || getClass() != o.getClass())
            return false;
        UserDetailsImpl user = (UserDetailsImpl)  o;
        return Objects.equals(id, user.id);
    }
}
************************

- EXPLANATION

- @NoArgsConstructor
Lombok generates a no-argument constructor.
This is useful for frameworks, serialization, and general object creation.

- @Data
Lombok generates:
getters and setters
toString()
equals() and hashCode()
(We still override equals() manually.)

- implements UserDetails
This makes the class compatible with Spring Security.
Spring Security will call methods like:
getUsername()
getPassword()
getAuthorities()
account-status methods (isAccountNonLocked(), etc.)

- Serializable Identifier
private static final long serialVersionUID = 1L;
Used for Java serialization compatibility.
Commonly added in UserDetails implementations.

- Core User Fields
private Long id;
private String username;
private String email;
These fields store authenticated user information:
id → useful for JWT claims, auditing, and identifying the user internally
username → used by Spring Security for authentication
email → extra user information

- @JsonIgnore
Prevents the password from being included in JSON responses.
This avoids accidental password leakage in APIs.

- Authorities (Roles / Permissions)
- private Collection<? extends GrantedAuthority> authorities;
Spring Security uses authorities to perform authorization checks.
GrantedAuthority is used in expressions like:
hasRole("ADMIN")
hasAuthority("ADMIN")

Static Factory Method: build(User user)
- public static UserDetailsImpl build(User user)
This method converts our database User entity into a UserDetailsImpl.

- List<GrantedAuthority> authorities = user.getRoles().stream()
- .map(role -> new SimpleGrantedAuthority(role.getRoleName().name()))
- .collect(Collectors.toList());
What this does:
Retrieves roles from the database (e.g. ADMIN, USER)
Converts each role into a GrantedAuthority
Collects them into a list

Example:
Role enum ADMIN → authority "ADMIN"
(If using hasRole("ADMIN"), you may need "ROLE_ADMIN" depending on configuration.)

- return new UserDetailsImpl(
- user.getUserId(),
- user.getUsername(),
- user.getEmail(),
- user.getPassword(),
- authorities
- );


Creates and returns a fully populated UserDetailsImpl object using DB values.
Constructor
- public UserDetailsImpl(Long id, String username, String email,
- String password,
- Collection<? extends GrantedAuthority> authority)
Initializes all fields required by Spring Security.

Required UserDetails Methods
@Override
public Collection<? extends GrantedAuthority> getAuthorities() {
return authorities;
}
Spring Security uses this to determine the user’s roles/permissions.
the rest are getters/setters


- UserDetailsServiceImpl
  **********CODE**********

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    UserRepository userRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(()->
                        new UsernameNotFoundException("Username not found with username:- "+username));
        return UserDetailsImpl.build(user);
    }
}

  ************************

- @Service
Marks this class as a Spring-managed bean.
Spring automatically detects and registers it in the application context.
Spring Security requires a UserDetailsService bean to perform authentication, so this annotation is mandatory.

- implements UserDetailsService
This interface is used by Spring Security during login.
Spring Security will automatically call:
loadUserByUsername(String username)
when a user attempts to authenticate.

Repository Injection
- @Autowired
- UserRepository userRepository;

Spring injects UserRepository so we can:
query the database
fetch user information using JPA
This repository is responsible for interacting with the users table.

Core Authentication Method
- @Override
- @Transactional
- public UserDetails loadUserByUsername(String username)
- throws UsernameNotFoundException
This is the most important method in the class.
loadUserByUsername(String username)
Spring Security calls this method automatically during authentication.
What Spring passes:

username → value entered by the user during login

What Spring expects back:
a UserDetails object if user exists
an exception if user does not exist

@Transactional
Ensures that the database session remains open while the user is being loaded.
This is important because:
User has relationships (e.g. roles)
Hibernate may lazily fetch roles
without a transaction, a LazyInitializationException could occur
So this annotation guarantees safe data loading.

Fetching User from Database
- User user = userRepository.findByUsername(username)
Queries the database for a user with the given username.
The method returns:
Optional<User>
Handling User Not Found
.orElseThrow(() ->
new UsernameNotFoundException(
"Username not found with username:- " + username
)
);

If no user is found:
throws UsernameNotFoundException
Spring Security catches this exception
authentication fails automatically
This is the correct and expected behavior for Spring Security.
Converting User → UserDetails
return UserDetailsImpl.build(user);
Converts the JPA User entity into a UserDetailsImpl
Adds roles as GrantedAuthority
Returns the object Spring Security understands
From this point onward:
Spring Security compares passwords
checks authorities
decides whether authentication succeeds