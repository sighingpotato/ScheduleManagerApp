# 📅 일정 관리 애플리케이션 (Schedule Manager App)

## 1. API Specification
RESTful API 명세서입니다. 보안을 위해 모든 응답(Response)에서 password는 제외되며, 인증/인가가 필요한 API는 Session(Cookie)을 통해 검증합니다.

### 1.1. 유저 회원가입 (Sign Up)
- **URL**: `/api/users/signup`
- **Method**: `POST`
- **Description**: 새로운 유저를 생성합니다. (비밀번호 8자 이상, 유저명 4자 이하 조건 적용)

**Request Header**
```Plaintext
Content-Type: application/json
```

**Request Body**
```
{
  "username": "홍길동",
  "email": "test@example.com",
  "password": "12345678"
}
```

| 이름       | 데이터타입  | 설명   |
|----------|--------|------|
| username | String | 유저명  |
| email    | String | 이메일  |
| password | String | 비밀번호 |

**Response Body (✅ 201 Created)**
```
{
  "id": 1,
  "username": "홍길동",
  "email": "test@example.com",
  "createdAt": "2026-04-16T10:00:00",
  "updatedAt": "2026-04-16T10:00:00"
}
```

### 1.2. 유저 로그인 (Login)

- **URL**: `/api/users/login`

- **Method**: `POST`

- **Description**: 이메일과 비밀번호로 로그인하여 세션을 생성합니다.

**Request Body**
```
{
  "email": "test@example.com",
  "password": "12345678"
}
```

**Resoponse Header**

- Set-Cookie: JSESSIONID=ABCDEF123456; Path=/; HttpOnly

**Response Body (✅ 200 OK)**

- (Body 없음)

### 1.3 일정 생성 (Create Schedule)

- **URL**: `/api/schedules`

- **Method**: `POST`

- **Description**: 새로운 일정을 생성합니다. (로그인 세션 필요, 제목 10자 이내)

**Request Body**
```
{
  "title": "스프링 스터디",
  "content": "JPA 연관관계 매핑 학습"
}
```

| 이름      | 데이터타입  | 설명             |
|---------|--------|----------------|
| title   | String | 일정 제목 (최대 10자) |
| content | String | 일정 내용          |

**Response Body (✅ 201 Created)**
```
{
  "id": 1,
  "title": "스프링 스터디",
  "content": "JPA 연관관계 매핑 학습",
  "userId": 1,
  "createdAt": "2026-04-16T14:00:00",
  "updatedAt": "2026-04-16T14:00:00"
}
```

### 1.4. 일정 페이징 조회 (Paging List)

- **URL**: `/api/schedules`

- **Method**: `GET`

- **Description**: 등록된 전체 일정을 페이징하여 조회합니다. (수정일 기준 내림차순 정렬)

**Query Parameter**

| 이름   | 데이터타입   | 설명                       |
|------|---------|--------------------------|
| page | Integer | 조회할 페이지 번호 (기본값: 0)      |
| size | Integer | 한 페이지당 데이터 개수 (기본값 : 10) |

**Response Body (✅ 200 OK)**
```
{
  "content": [
    {
      "id": 1,
      "title": "스프링 스터디",
      "content": "JPA 연관관계 매핑 학습",
      "commentCount": 3,
      "username": "홍길동",
      "createdAt": "2026-04-16T14:00:00",
      "updatedAt": "2026-04-16T15:30:00"
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 10
  },
  "totalElements": 1,
  "totalPages": 1
}
```

### 1.5. 댓글 생성 (Create Comment)

- **URL**: `/api/schedules/{scheduleId}/comments`

- **Method**: `POST`

- **Description**: 특정 일정에 댓글을 생성합니다. (로그인 세션 필요)

**Request Body**
```
{
  "content": "좋은 스터디네요! 참여하고 싶습니다."
}
```

**Response Body (✅ 201 Created)**
```
{
  "id": 1,
  "scheduleId": 1,
  "userId": 2,
  "content": "좋은 스터디네요! 참여하고 싶습니다.",
  "createdAt": "2026-04-16T16:00:00",
  "updatedAt": "2026-04-16T16:00:00"
}
```

## 2. ERD (Entity Relationship Diagram)
**Table**: user

| Column    | Type         | Key | Constraint     | Description       |
|-----------|--------------|-----|----------------|-------------------|
| id        | BIGINT       | PK  | AUTO_INCREMENT | 유저 고유 식별자         |
| username  | VARCHAR(4)   |     | NOT NULL       | 유저명 (최대 4자)       |
| email     | VARCHAR(100) |     | NOT NULL       | 이메일 (로그인 ID)      |
| password  | VARCHAR(255) |     | NOT NULL       | 암호화된 비밀번호 (8자 이상) |
| createdAt | DATETIME     |     | NOT NULL       | 생성일               |
| updatedAt | DATETIME     |     | NOT NULL       | 수정일               |

**Table**: schedule

| Column    | Type         | Key | Constraint     | Description    |
|-----------|--------------|-----|----------------|----------------|
| id        | BIGINT       | PK  | AUTO_INCREMENT | 일정 고유 식별자      |
| user_id   | VARCHAR      |     | NOT NULL       | 작성자 유저 ID      |
| title     | VARCHAR(10)  |     | NOT NULL       | 일정 제목 (최대 10자) |
| content   | VARCHAR(255) |     | NOT NULL       | 일정 내용          |
| createdAt | DATETIME     |     | NOT NULL       | 생성일            |
| updatedAt | DATETIME     |     | NOT NULL       | 수정일            |

**Table**: comment

| Column      | Type         | Key | Constraint     | Description |
|-------------|--------------|-----|----------------|-------------|
| id          | BIGINT       | PK  | AUTO_INCREMENT | 댓글 고유 식별자   |
| user_id     | BIGINT       |     | NOT NULL       | 작성자 유저 ID   |
| schedule_id | BIGINT       |     | NOT NULL       | 연관된 일정 ID   |
| content     | VARCHAR(255) |     | NOT NULL       | 댓글 내용       |
| createdAt   | DATETIME     |     | NOT NULL       | 생성일         |
| updatedAt   | DATETIME     |     | NOT NULL       | 수정일         |