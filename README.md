# 📅 일정 관리 앱 Develop

> 사용자의 일정을 등록하고 관리하며, 댓글을 통해 소통할 수 있는 서비스


## 🛠️ Tech Stack (기술 스택)
- **Language**: Java 17
- **Framework**: Spring Boot 3.5.9
- **Database**: MySQL 9.5.0, Spring Data JPA
- **Security**: BCrypt Password
- **Tool**: IntelliJ IDEA, Postman, Git/GitHub


## ✨ Key Features (핵심 기능)

### 1. 사용자 (User)
- **회원가입**: 이름, 이메일, 비밀번호를 입력하여 회원을 생성합니다.
- **보안**: BCrypt 알고리즘을 사용하여 비밀번호를 안전하게 암호화하여 저장합니다.
- **로그인**: 이메일과 비밀번호(matches 검증)를 통해 로그인합니다.
- **CRUD**: 유저를 생성, 조회, 수정, 삭제할 수 있습니다.

### 2. 일정 (Schedule)
- **CRUD**: 일정을 생성, 단건 조회, 수정, 삭제할 수 있습니다.
- **페이징 조회 (Paging)**: 전체 일정을 한 번에 불러오지 않고, 페이지 번호와 크기(size)를 지정해 페이징 처리된 데이터를 반환합니다. (최신 수정일 기준 내림차순 정렬)
- **권한 검증**: 일정 수정 및 삭제 시, 작성자의 비밀번호가 일치해야만 처리가 가능합니다.

### 3. 댓글 (Comment)
- **CRUD**: 특정 일정에 댓글을 생성, 조회, 수정, 삭제할 수 있습니다.
- **영속성 전이 (Cascade)**: 부모 엔티티인 일정이 삭제될 경우, 해당 일정에 달린 모든 댓글도 함께 삭제(CascadeType.REMOVE)되어 고아 데이터를 방지합니다.

### 4. ERD, API 명세서

- [README.md](src/main/README.md)
-----------
## 작업 과정

API 명세서, ERD를 작성해준 후 JPA Auditing(생성일, 수정일 자동화)을 우선하여 해줬다.
Application 클래스에 @EnableJpaAuditing 어노테이션을 넣어준 후 BaseEntity 클래스를 생성하여 먼저 작업을 해줬다.

**ScheduleManagerAppApplication**
```
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {

		@Getter
    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
```

### Entity

**User**

id는 pk로 지정해주고 나머지 요소에는 null 값이 될 수 없도록 해준 후 유저명과 이메일에는 길이 제한을 뒀다. 그리고 이메일은 중복 값을 가질 수 없도록 unique = true로 해줬다.
```
@Getter
@Entity
@Table(name = "users")
public class User extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 유저 고유 식별자

    @Column(nullable = false, length = 4)
    private String username; // 유저명

    @Column(nullable = false, length = 100, unique = true)
    private String email; // 이메일

    @Getter
    @Column(nullable = false)
    private String password; // 비밀번호

    protected User() {}

    // 생성자
    public User(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
    }
    
    // 유저 정보 수정
    public void updateUsername(String username) {
        this.username = username;
    }

}
```

**Schedule**

@ManyToOne을 사용하는 것이 핵심이었는데 여러 개의 일정은 한 명의 유저가 만들 수 있는 것이라고 생각하니까 조금 쉽게 이해가 가서 사용해줬다.
````
@Getter
@Entity
@Table(name = "schedules")
public class Schedule extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 일정 고유 식별자

    @Column(nullable = false, length = 10)
    private String title; // 일정 제목

    @Column(nullable = false)
    private String content; // 일정 내용

    @ManyToOne(fetch = FetchType.LAZY) // 다대일(N:1) 단방향 연관관계 설정(한 명의 유저가 여러 개의 일정 생성 가능)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // 작성자 유저 정보

    protected  Schedule() {}

    public Schedule(String title, String content, User user) {
        this.title = title;
        this.content = content;
        this.user = user;
    }
}
````

### Repository

**UserRepository**
Service에서 findByEmail 메서드를 사용하기 위해 Optional 구문을 Service 작업 중 추가해줬다.
Optional의 경우는 "데이터가 없을 수도 있음(null 방지)"을 나타내는 안전 상자라 사용해줬다.
```
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
```

**ScheduleRepository**
```
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
}
```

### Dto

**SignupRequestDto**
도전 과제 LV 5를 고려하여 @NotBlank와 @Size를 이용하여 미리 처리해줬다.
```
@Getter
public class SignupRequestDto {

    @NotBlank
    @Size(max = 4, message = "유저명은 4자까지 입니다.")
    private String username;

    @Email
    @NotBlank
    private String email;

    @NotBlank
    @Size(min = 8, message = "비밀번호는 8자 이상입니다.")
    private String password;
}
```
**LoginRequestDto**
@NotBlank를 이용하여 미리 처리해줬다.
```
@Getter
public class LoginRequestDto {

    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String password;
}
```
**UserRequestDto**
```
@Getter
public class UserRequestDto {
    private String username;
}
```
**UserResponseDto**
@RequiredArgsConstructor을 통하여 변수를 final로 선언해줬다.
```
@Getter
@RequiredArgsConstructor
public class UserResponseDto {
    private final Long id;
    private final String username;
    private String email;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 생성자
    public UserResponseDto(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.createdAt = user.getCreatedAt();
        this.updatedAt = user.getUpdatedAt();
    }
}
```
**ScheduleRequestDto**
@NotBlank와 @Size를 이용하여 미리 처리해줬다.
```
@Getter
public class ScheduleRequestDto {

		@NotBlank
		@Size(max = 10)
    private String title;
    
    @NotBlank
    private String content;
}
```
**ScheduleResponseDto**
```
@Getter
@RequiredArgsConstructor
public class ScheduleResponseDto {
    private final Long id;
    private final String title;
    private final String content;
    private final Long userId;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public ScheduleResponseDto(Schedule schedule) {
        this.id = schedule.getId();
        this.title = schedule.getTitle();
        this.content = schedule.getContent();
        this.userId = schedule.getUser().getId();
        this.createdAt = schedule.getCreatedAt();
        this.updatedAt = schedule.getUpdatedAt();
    }
}
```

### Service

**UserService**
서비스 로직에 회원가입과 로그인, CRUD를 구현해줬다. 중복 검사를 통해 이메일이 존재하는지와 가입이 되었는지, 비밀번호가 맞는지 확인하는 구문도 구현해줬다.

.orElseThrow()에 대한 이해도 조금 부족한거 같아 아래에 정리했다.
```
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    // 회원 가입
    @Transactional
    public UserResponseDto signup(SignupRequestDto requestDto) {
        // 비밀번호를 그대로 저장
        User user = new User(requestDto.getUsername(), requestDto.getEmail(), requestDto.getPassword());
        return new UserResponseDto(userRepository.save(user));
    }

    // 로그인
    @Transactional(readOnly = true)
    public User login(LoginRequestDto requestDto) {
        User user = userRepository.findByEmail(requestDto.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("이메일이 존재하지 않습니다."));

        if (!user.getPassword().equals(requestDto.getPassword()))
            throw new IllegalArgumentException("비밀번호가 틀렸습니다.");

        return user;
    }

    // 유저 전체 조회
    @Transactional(readOnly = true)
    public List<UserResponseDto> getAllUsers() {
        List<User> userList = userRepository.findAll();
        List<UserResponseDto> responseDtoList = new ArrayList<>();

        // for문을 돌면서 유저를 하나씩 꺼낸다.
        for (User user : userList) {
            UserResponseDto dto = new UserResponseDto(user);

            responseDtoList.add(dto);
        }
        return responseDtoList;
    }

    // 유저 단건 조회
    @Transactional(readOnly = true)
    public UserResponseDto getUser(Long id) {
        return new UserResponseDto(userRepository.findById(id).orElseThrow());
    }

    // 유저 수정
    @Transactional
    public UserResponseDto updateUser(Long id, UserRequestDto requestDto,Long sessionUserId) {
        if (!id.equals(sessionUserId)) throw new IllegalArgumentException("권한이 없습니다.");
        User user = userRepository.findById(id).orElseThrow();
        user.updateUsername(requestDto.getUsername());
        return new UserResponseDto(user);
    }

    // 유저 삭제
    @Transactional
    public void deleteUser(Long id, Long sessionUserId) {
        if (!id.equals(sessionUserId)) throw new IllegalArgumentException("권한이 없습니다.");
        userRepository.deleteById(id);
    }
}
```

**ScheduleService**
```
@Service
@RequiredArgsConstructor
public class ScheduleService {
    private final ScheduleRepository scheduleRepository;
    private final UserRepository userRepository;

    // 일정 생성
    @Transactional
    public ScheduleResponseDto createSchedule(ScheduleRequestDto requestDto, Long userId) {
        User user = userRepository.findById(userId).orElseThrow();
        Schedule schedule = new Schedule(requestDto.getTitle(), requestDto.getContent(), user);
        return new ScheduleResponseDto(scheduleRepository.save(schedule));
    }

    // 일정 전체 조회
    @Transactional(readOnly = true)
    public List<ScheduleResponseDto> getAllSchedules() {
        List<Schedule> scheduleList = scheduleRepository.findAll();
        List<ScheduleResponseDto> responseDtoList = new ArrayList<>();

        for (Schedule schedule : scheduleList) {
            ScheduleResponseDto dto = new ScheduleResponseDto(schedule);

            responseDtoList.add(dto);
        }
        return responseDtoList;
    }

    // 일정 단건 조회
    @Transactional(readOnly = true)
    public ScheduleResponseDto getSchedule(Long id) {
        return new ScheduleResponseDto(scheduleRepository.findById(id).orElseThrow());
    }

    // 일정 수정
    @Transactional
    public ScheduleResponseDto updateSchedule(Long id,ScheduleRequestDto requestDto, Long userId) {
        Schedule schedule = scheduleRepository.findById(id).orElseThrow();
        if(!schedule.getUser().getId().equals(userId))
            throw new IllegalArgumentException("수정 권한이 없습니다.");
        schedule.update(requestDto.getTitle(), requestDto.getContent());
        return new ScheduleResponseDto(schedule);
    }

    // 일정 삭제
    @Transactional
    public void deleteSchedule(Long id, Long userId) {
        Schedule schedule = scheduleRepository.findById(id).orElseThrow();
        if(!schedule.getUser().getId().equals(userId))
            throw new IllegalArgumentException("삭제 권한이 없습니다.");
        scheduleRepository.delete(schedule);
    }
}
```

### Controller

**UserController**
서비스와 연동하여 회원가입과 로그인의 API를 구현해주고 데이터를 검증할 수 있도록 해줬다.
```
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

		// 인증, 인가 세션 검증
    private Long getUserIdFromSession(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("LOGIN_USER_ID") == null)
            throw new IllegalArgumentException("로그인이 필요합니다.");
        return (Long) session.getAttribute("LOGIN_USER_ID");
    }

    // 회원가입
    @PostMapping("/signup")
    public ResponseEntity<UserResponseDto> signup(@RequestBody SignupRequestDto requestDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.signup(requestDto));
    }

    // 로그인
    @PostMapping("/login")
    public ResponseEntity<Void> login(@RequestBody LoginRequestDto requestDto, HttpServletRequest request) {

        // 서비스에 로그인 검증 요청
        User user = userService.login(requestDto);

        // 로그인 시, 세션 생성 및 유저 id 저장
        HttpSession session = request.getSession(true);
        session.setAttribute("LOGIN_USER_ID", user.getId());
        return ResponseEntity.ok().build();
    }

    // 유저 전체 조회
    @GetMapping
    public ResponseEntity<List<UserResponseDto>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    // 유저 단건 조회
    @GetMapping("/{userId}")
    public ResponseEntity<UserResponseDto> getUser(@PathVariable Long userId) {
        return ResponseEntity.ok(userService.getUser(userId));
    }

    // 유저 수정
    @PatchMapping("/{userId}")
    public ResponseEntity<UserResponseDto> updateUser(@PathVariable Long userId, @RequestBody UserRequestDto requestDto, HttpServletRequest request) {
        return ResponseEntity.ok(userService.updateUser(userId, requestDto, getUserIdFromSession(request)));
    }

    // 유저 삭제
    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId, HttpServletRequest request) {
        userService.deleteUser(userId, getUserIdFromSession(request));
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
```

**ScheduleController**
```
@RestController
@RequestMapping("/api/schedules")
@RequiredArgsConstructor
public class ScheduleController {
    private final ScheduleService scheduleService;

    public Long getUserIdFromSession(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        // 인증, 인가 세션 검증
        if (session == null || session.getAttribute("LOGIN_USER_ID") == null) throw new IllegalArgumentException("로그인이 필요합니다.");
        return (Long) session.getAttribute("LOGIN_USER_ID");
    }

    // 일정 생성
    @PostMapping
    public ResponseEntity<ScheduleResponseDto> createSchedule(@RequestBody ScheduleRequestDto requestDto, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(scheduleService.createSchedule(requestDto, getUserIdFromSession(request)));
    }

    // 일정 전체 조회
    @GetMapping
    public ResponseEntity<List<ScheduleResponseDto>> getAllSchedules() {
        return ResponseEntity.ok(scheduleService.getAllSchedules());
    }

    // 일정 단건 조회
    @GetMapping("/{scheduleId}")
    public ResponseEntity<ScheduleResponseDto> getSchedule(@PathVariable Long scheduleId) {
        return ResponseEntity.ok(scheduleService.getSchedule(scheduleId));
    }

    // 일정 수정
    @PutMapping("/{scheduleId}")
    public ResponseEntity<ScheduleResponseDto> updateSchedule(@PathVariable Long scheduleId, @RequestBody ScheduleRequestDto requestDto, HttpServletRequest request) {
        return ResponseEntity.ok(scheduleService.updateSchedule(scheduleId, requestDto, getUserIdFromSession(request)));
    }

    // 일정 삭제
    @DeleteMapping("/{scheduleId}")
    public ResponseEntity<Void> deleteSchedule(@PathVariable Long scheduleId, HttpServletRequest request) {
        scheduleService.deleteSchedule(scheduleId, getUserIdFromSession(request));
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
```
### LV 5 예외 처리

**ErrorResponseDto**
GlobalExceptionHandler에서 사용할 Dto를 만들어줬다.

처음에는 @RequiredArgsConstructor를 사용해줬다가 해결할 수 없다는 오류가 나타나 당황했지만 다른 ResponseDto와 다르게 생성자가 괄호 안 재료를 그대로 받아오기 때문이라는 것을 알고 지워줬다.
````
import lombok.Getter;

@Getter
public class ErrorResponseDto {
    private final String error;
    private final String message;

    // 생성자
    public ErrorResponseDto(String error, String message) {
        this.error = error;
        this.message = message;
    }
}
````

**GlobalExceptionHandler**
예외 처리를 위해 excepion 패키지를 만들어준 후 전체적으로 예외 처리를 관리하기 위해서 따로 클래스를 만들어줬다.
````
@RestControllerAdvice
public class GlobalExceptionHandler {

    // @Valid 검증 실패 시
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDto> handleValidationExceptions(IllegalArgumentException exception) {
        ErrorResponseDto errorDto = new ErrorResponseDto("Bad request", exception.getMessage());
        return new ResponseEntity<>(errorDto, HttpStatus.BAD_REQUEST);
    }

    // 직접 만든 에러
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException exception) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(exception.getMessage());
    }
}
````
Validation 적용을 위한 dto 수정은 필수 과제 작업 때 미리 해뒀으므로 Controller 파일들의 Signup, Login, Schedule 생성/수정 메서드의 @RequestBody 앞에 @Valid 어노테이션을 붙이는 것으로 마무리할 수 있었다.

### LV 6 비밀번호 암호화

**PasswordEncoder**
build.gradle에 조건에 나와 있는 의존성을 추가한 후에 config 패키지를 생성 후 조건에서 주어진 PasswordEncoder를 그대로 넣어줬다.

또한, 적용을 위해 UserService 클래스에 선언하여 암호화 도구를 주입시켜줬다.
````
@Component
public class PasswordEncoder {

    public String encode(String rawPassword) {
        return BCrypt.withDefaults().hashToString(BCrypt.MIN_COST, rawPassword.toCharArray());
    }

    public boolean matches(String rawPassword, String encodedPassword) {
        BCrypt.Result result = BCrypt.verifyer().verify(rawPassword.toCharArray(), encodedPassword);
        return result.verified;
    }
}

public class UserService {
		private final PasswordEncoder passwordEncoder; // 암호화 도구 주입
````

**UserService 수정**
PasswordEncoder를 적용시 login 내부의 .equals는 사용할 수 없게 된다고 한다. 단방향 암호화 기술이라 DB에는 복잡한 해시값으로 변환되어 저장되기 때문이다.

그래서 .matches()로 수정해줘야 한다.
````
// signup 메서드 내부 수정 (저장 전 암호화)
String encodedPassword = passwordEncoder.encode(requestDto.getPassword()); 
User user = new User(requestDto.getUsername(), requestDto.getEmail(), encodedPassword);

// login 메서드 내부 수정 (matches 사용)
if (!passwordEncoder.matches(requestDto.getPassword(), user.getPassword())) throw new IllegalArgumentException("비밀번호 불일치");
````

### LV 7 댓글 CRUD

**Comment**
````
@Entity
@Getter
@NoArgsConstructor
@Table(name = "comment")
public class Comment extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id", nullable = false)
    private Schedule schedule;

    public Comment(String content, User user, Schedule schedule) {
        this.content = content;
        this.user = user;
        this.schedule = schedule;
    }

    public void update(String content) {
        this.content = content;
    }
}
````

**CommentRepository**
````
public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findAllByScheduleId(Long scheduleId);
}
````

**CommentRequestDto**
````
@Getter
public class CommentRequestDto {

    @NotBlank
    private String content;
}
````

**CommentResponseDto**
````
@Getter
@RequiredArgsConstructor
public class CommentResponseDto {
    private final Long id;
    private final Long scheduleId;
    private final Long userId;
    private final String username;
    private final String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public CommentResponseDto(Comment comment) {
        this.id = comment.getId();
        this.scheduleId = comment.getSchedule().getId();
        this.userId = comment.getUser().getId();
        this.username = comment.getUser().getUsername();
        this.content = comment.getContent();
        this.createdAt = comment.getCreatedAt();
        this.updatedAt = comment.getUpdatedAt();
    }
}
````

**CommentService**
````
@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final ScheduleRepository scheduleRepository;
    private final UserRepository userRepository;

    // 댓글 생성
    @Transactional
    public CommentResponseDto createComment(Long scheduleId, CommentRequestDto requestDto, Long userId) {
        User user = userRepository.findById(userId).orElseThrow();
        Schedule schedule = scheduleRepository.findById(scheduleId).orElseThrow();
        Comment comment = new Comment(requestDto.getContent(), user, schedule);
        return new CommentResponseDto(commentRepository.save(comment));
    }

    // 댓글 전체 조회
    @Transactional(readOnly = true)
    public List<CommentResponseDto> getAllComments(Long scheduleId) {
        List<Comment> commentList = commentRepository.findAllByScheduleId(scheduleId);
        List<CommentResponseDto> responseList = new ArrayList<>();
        for (Comment comment : commentList) {
            responseList.add(new CommentResponseDto(comment));
        }
        return responseList;
    }

    // 댓글 단건 조회
    @Transactional(readOnly = true)
    public CommentResponseDto getComment(Long commentId) {
        Comment comment = commentRepository.findById(commentId).orElseThrow();
        return new CommentResponseDto(comment);
        }

    // 댓글 수정
    @Transactional
    public CommentResponseDto updateComment(Long commentId, CommentRequestDto requestDto, Long userId) {
        Comment comment = commentRepository.findById(commentId).orElseThrow();
        if (!comment.getUser().getId().equals(userId))
            throw new IllegalArgumentException("권한 없음");
        comment.update(requestDto.getContent());
        return new CommentResponseDto(comment);
    }

    // 댓글 삭제
    @Transactional
    public void deleteComment(Long commentId, Long userId) {
        Comment comment = commentRepository.findById(commentId).orElseThrow();
        if (!comment.getUser().getId().equals(userId))
            throw new IllegalArgumentException("권한 없음");
        commentRepository.delete(comment);
    }
}
````

**CommentController**
````
@RestController
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;

    private Long getUserIdFromSession(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("LOGIN_USER_ID") == null)
            throw new IllegalArgumentException("로그인 필요");
        return (Long) session.getAttribute("LOGIN_USER_ID");
    }

    // 댓글 생성
    @PostMapping("/api/schedules/{scheduleId}/comments")
    public ResponseEntity<CommentResponseDto> createComment(
            @PathVariable Long scheduleId,
            @Valid @RequestBody CommentRequestDto requestDto,
            HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(commentService.createComment(scheduleId, requestDto, getUserIdFromSession(request)));
    }

    // 댓글 전체 조회
    @GetMapping("/api/schedules/{scheduleId}/comments")
    public ResponseEntity<List<CommentResponseDto>> getAllComments(@PathVariable Long scheduleId) {
        return ResponseEntity.ok(commentService.getAllComments(scheduleId));
    }

    // 댓글 단건 조회
    @GetMapping("/api/comments/{commentId}")
    public ResponseEntity<CommentResponseDto> getComment(@PathVariable Long commentId) {
        return ResponseEntity.ok(commentService.getComment(commentId));
    }

    // 댓글 수정
    @PutMapping("/api/comments/{commentId}")
    public ResponseEntity<CommentResponseDto> updateComment(
            @PathVariable Long commentId,
            @Valid @RequestBody CommentRequestDto requestDto,
            HttpServletRequest request) {
        return ResponseEntity.ok(commentService.updateComment(commentId, requestDto, getUserIdFromSession(request)));
    }

    // 댓글 삭제
    @DeleteMapping("/api/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long commentId, HttpServletRequest request) {
        commentService.deleteComment(commentId, getUserIdFromSession(request));
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
````

**Schedule 수정**
페이징으로 넘어가기 전에 1개의 일정에는 여러개의 댓글이 달릴 수 있으므로 @OneToMany 어노테이션을 사용해줬다. 그 다음 댓글들을 담아둘 수 있는 상자가 필요하므로 리스트를 선언해줬고 cascade의 경우는 일정 데이터 삭제 시에 남아있는 댓글 데이터도 지우기 위해 사용해줬다.
````
// 기존 코드 아래에 추가
@OneToMany(mappedBy = "schedule", cascade = CascadeType.All)
private List<Comment> comments = new ArrayList<>();
````

### LV 8 일정 페이징 조회

페이징은 배우지 않은 개념이라 익숙하지 않아 검색하여 보면서 따라가는 것에 초점을 뒀다.

**SchedulePageResponseDto**
이번에는 댓글 개수와 작성자 유저명까지도 선언해줬다.
````
@Getter
@RequiredArgsConstructor
public class SchedulePageResponseDto {
    private final Long id;
    private final String title;
    private final String content;
    private final int commentCount; // 댓글 개수
    private final String username; // 작성자 유저명
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public SchedulePageResponseDto(Schedule schedule) {
        this.id = schedule.getId();
        this.title = schedule.getTitle();
        this.content = schedule.getContent();
        this.commentCount = schedule.getComments().size();
        this.username = schedule.getUser().getUsername();
        this.createdAt = schedule.getCreatedAt();
        this.updatedAt = schedule.getUpdatedAt();
    }
}
````

**ScheduleService 수정**
이번에도 전체 조회와 동일한 방식으로 작업해줬으나 처음에는 리스트를 그대로 dto에게 돌려줬다가 페이징 정보가 다 날아가는 오류가 나타났다.

무엇이 문제인가 했더니 List는 단순한 바구니고 Page는 송장이 붙어있는 택배 상자로 비유할 수 있었다. 이 내용을 학습한 후에 PageImpl로 감싸줘 오류를 해결할 수 있었다.
````
// 기존 클래스 내부에 추가
@Transactional(readOnly = true)
public Page<SchedulePageResponseDto> getSchedules(Pageable pageable) {
    Page<Schedule> schedulePage = scheduleRepository.findAll(pageable);
    List<SchedulePageResponseDto> responseDtoList = new ArrayList<>();

    for (Schedule schedule : schedulePage.getContent()) {
        SchedulePageResponseDto dto = new SchedulePageResponseDto(schedule);

        responseDtoList.add(dto);
     }
    return new PageImpl<>(responseDtoList, pageable, schedulePage.getTotalElements());
````

**ScheduleController 수정**
````
 // 전체 조회 코드 getAllScheules() 메서드 삭제
 // 기존 코드 아래에 추가
 // 일정 페이징 조회
 @GetMapping
 public ResponseEntity<Page<SchedulePageResponseDto>> getSchedules(
         // 수정일 내림차순 정렬
         @PageableDefault(size = 10, sort = "updatedAt", direction = Sort.Direction.DESC) Pageable pageable) {
     return ResponseEntity.ok(scheduleService.getSchedules(pageable));
````


## 트러블 슈팅

- Application 클래스에서 @MappedSuperclass와 @EntityListeners를 잘 몰라 사용하지 않고 넘어갔던 경우가 있어 간단하게 정리 해봤다.
  - **1️⃣ `@MappedSuperclass`**: 이 클래스를 상속받는 다른 엔티티들(User, Schedule)이 이 클래스의 필드(시간)를 자신의 컬럼처럼 쓰게 해준다.
  - **2️⃣ `@EntityListeners`**: 엔티티에 변화가 생길 때마다 특정 로직(시간 자동 저장)을 실행하도록 감시자를 붙인다.

- Optional의 경우 Service를 작업해주면서 Repository에 자동으로 생성은 되었지만 어떻게 사용되는지 잘 알지 못해 어떠한 상황에 쓰이는지 알아봤다.
    - Optional은 "데이터가 없을 수도 있음(null 방지)"을 나타내는 안전 상자다.

- Dto의 의미를 다시 정리해봤다.
    - Dto는 데이터를 운반하는 객체다.

- `.orElseTrow()`의 경우는 자주 사용하고 있어 어떨 때 사용하면 되는지 감은 잡고 있지만 명확히 알고 사용하는 것은 아닌 것 같아 간단하게 정리해봤다. 
    - `.orElseThrow()`의 이해
        - `.orElseThrow(...)`: 배달 상자에게 내리는 단호한 명령이다. "상자 열어서 유저 있으면 내놔! 만약 비어있으면 내가 적어둔 에러(IllegalArgumentException)를 냅다 던져버려!"

- 인증 / 인가 세션 검증의 경우는 강의와 자료를 통하여 어떻게든 구현은 했지만 아직도 감이 잡히지 않았다.

    그리하여 세션 코드를 한 줄씩 따로 검색하며 일상 생활의 예를 통해 해석해봤다. 아직도 어렵지만 전 보다는 이해하는데 조금 나아진 것 같다.
  - 인증 / 인가 코드가 왜 필요할까?
    - 인터넷(HTTP)은 기본적으로 '단기 기억 상실증'에 걸려 있다. 그래서 서버가 사용자를 기억하게 만들기 위해 세션(Session)이라는 시스템을 사용한다.
      - **로그인 성공 시:** 서버는 놀이공원 입구에서 자유이용권 팔찌(Session ID)를 채워주고, 서버 내부의 사물함(Session)에 "이 팔찌 번호는 1번 유저(홍길동) 거임" 이라고 적어둔다.
      - **그 이후 요청 시:** 브라우저가 알아서 이 팔찌를 서버에 보여주기 때문에, 서버는 "아~ 사물함을 보니 1번 유저시군요. 통과!" 하고 처리한다.
  >   `private Long getUserIdFromSession(HttpServletRequest request) {`
   >   - 👨‍✈️ **경비원:** "지금부터 들어온 요청(request)을 검사해서, 이 사람의 유저 번호(UserId)가 뭔지 알아내 볼게."

    >   `HttpSession session = request.getSession(false);`
     >   - 🎫 **팔찌 확인:** `getSession(false)`는 "이 손님 팔찌 찼는지 확인해 봐!"라는 뜻이다.
     >   - 여기서 `false`가 아주 중요하다. "팔찌가 없으면 **새로 만들어 주지 말고(false)**, 그냥 없다고 나한테 보고해!"라는 의미다.

     >   `if (session == null || session.getAttribute("LOGIN_USER_ID") == null) throw new IllegalArgumentException("로그인이 필요합니다.");`
    >   - 🚫 **입장 거부:** * `session == null`: "팔찌를 안 차고 왔네?" (아예 로그인한 적이 없음)

     >   `session.getAttribute("LOGIN_USER_ID") == null`: "팔찌는 있는데, 우리 사물함을 열어보니 '누구인지' 적혀있지 않네?" (세션이 만료됐거나 비정상적임)
    >   - 둘 중 하나라도 걸리면 예외(에러)를 던져서 쫓아낸다. "로그인하고 와!"

     >   `return (Long) session.getAttribute("LOGIN_USER_ID");
  }`
    >   - ✅ **입장 허가:** 무사히 검사를 통과했다면, 사물함(세션)에 고이 적어두었던 `LOGIN_USER_ID`(예: 1번, 2번 등)를 꺼내서 반환해 준다. 


- `.matches()`가 내부적으로 하는 일: "DB에 저장된 해시값(`$2a...`)에서 암호화 공식을 역추적해서, 방금 유저가 입력한 `1234`를 똑같은 공식으로 돌렸을 때 저 해시값이 나오는 게 맞는지 수학적으로 검증해 줄게! 맞으면 `true`, 틀리면 `false`를 줄게!"

- Cascade의 경우 머리에 잘 들어오지 않아 처음에는 어떤 상황에서 써야하는지 전혀 감을 잡지 못했다.

    하지만 Comment 작업 후 일정을 삭제할 경우 따로 작업해주지 않으면 댓글은 고스란히 남아있게 된다는 것을 알았고 강의를 통해 무엇을 사용해야 하는지 다시 깨닫고 정리해봤다.
    - **Cascade의 종류 (참고)**
        - **`CascadeType.ALL`**: 모든 작업(저장, 삭제, 수정 등)을 자식에게 폭포수처럼 다 적용한다. (가장 많이 쓰임)
        - **`CascadeType.PERSIST`**: 부모를 DB에 저장할 때, 자식들도 묶어서 한 번에 같이 저장해 준다.
        - **`CascadeType.REMOVE`**: 부모를 삭제할 때, 자식들도 같이 삭제한다.

> **페이지네이션**
>
> Spring Data JPA에서는 **Pageable과 Page 인터페이스를 사용하여** 데이터를 **페이지 단위로 조회**할 수 있다.
>
> 이를 통해 **대량의 데이터를 한 번에 불러오는 것이 아니라, 필요한 만큼 가져올 수 있도록** 해준다.
>
> ✅ **페이지네이션 핵심 개념**
>
> **📌 기본 개념**
>
> 1. Pageable : **페이지 정보를 담는 인터페이스**
    - 요청한 페이지 번호(page), 페이지 크기(size), 정렬 방식(sort)을 포함
    - PageRequest.of(page, size)를 사용하여 객체 생성
> 2. Page<T> : **페이지 결과를 담는 객체**
    - getContent() : 현재 페이지의 데이터 리스트
    - getTotalPages() : 전체 페이지 수
    - getTotalElements() : 전체 데이터 개수
    - hasNext(), hasPrevious() : 이전/다음 페이지 여부 확인

- List와 Page가 조회로 동일하여 차이점을 잘 몰라 ScheduleService 수정을 할 때, 전체 조회를 참고하여 작성하다가 페이징 정보가 날아가는 오류가 나타났다. 그 이유는 다음과 같이 간략하게 정리할 수 있었다.
  - 전체 조회(= 단순한 바구니)와 페이징(= 송장이 붙은 택배 상자) 조회
    - 전체 조회(`List`)는 단순한 데이터의 나열이므로, 알맹이만 바꿔서 새 리스트로 반환하면 된다.
    - 페이징 조회(`Page`)는 프론트엔드가 페이징 처리를 할 수 있도록 '페이지 부가 정보'를 반드시 함께 전달해야 한다. `for`문으로 알맹이만 조작하게 되면 이 부가 정보가 담긴 껍데기가 사라지기 때문에, 위의 경우에는 직접 `PageImpl`이라는 새 껍데기를 만들어 부가 정보를 다시 세팅(재조립)해 주어야 하는 것이다.

- 처음에 MySQL에서 테이블이 제대로 생성되지 않고 테이블 이름을 실수로 명세서와 다르게 설정해줘 수정하려 할 때 반영이 안되는 것이 문제였다. 검색을 통해 이유가 무엇인지 확실하게 알 수 있었는데 이유는 application.properties에 있었다.

    spring.jpa.hibernate.ddl-auto를 설정해주지 않은 것이 문제였는데 update로 설정해주자 코드 실행 시 만들어지지 않았던 테이블들이 만들어지는 것을 확인할 수 있었다. update로 해놓으면 자동화 기능이 활성화 되어서 그런 것이었다.

    잘못 생성된 테이블의 경우는 일반적인 방법으로는 지워지지 않았는데 spring.jpa.hibernate.ddl-auto를 create로 잠시 바꿔주자 삭제할 수 있었다.