# Data Models

## User

**Purpose:** Represents system users including students and administrators, managing authentication and profile information.

**Key Attributes:**
- id: Long - Unique identifier for the user
- username: String - Unique username for login (3-20 characters)
- email: String - User's email address for login and notifications
- password: String - Encrypted password hash
- nickname: String - Display name for the user (optional)
- avatar: String - Profile picture URL stored in OSS
- bio: String - User biography or introduction
- role: UserRole - User role (USER or ADMIN)
- status: UserStatus - Account status (ACTIVE, INACTIVE, BANNED)
- createTime: LocalDateTime - Account creation timestamp
- updateTime: LocalDateTime - Last update timestamp
- lastLoginTime: LocalDateTime - Last successful login time

### TypeScript Interface

```typescript
export interface User {
  id: number;
  username: string;
  email: string;
  nickname?: string;
  avatar?: string;
  bio?: string;
  role: 'USER' | 'ADMIN';
  status: 'ACTIVE' | 'INACTIVE' | 'BANNED';
  createTime: string;
  updateTime: string;
  lastLoginTime?: string;
}
```

### Relationships
- One-to-many with Article (user can have many articles)
- One-to-many with Comment (user can have many comments)
- Many-to-many with Article through ArticleLike (user can like many articles)

## Article

**Purpose:** Represents blog posts created by users, managing content and metadata.

**Key Attributes:**
- id: Long - Unique identifier for the article
- title: String - Article title (required, max 200 characters)
- content: String - Article content in Markdown format
- summary: String - Article summary for list display
- coverImage: String - Cover image URL stored in OSS
- status: ArticleStatus - Article status (DRAFT, PUBLISHED, DELETED)
- viewCount: Integer - Number of times the article has been viewed
- likeCount: Integer - Number of likes the article has received
- commentCount: Integer - Number of comments on the article
- isTop: Boolean - Whether the article is pinned to top
- authorId: Long - Foreign key to User table
- categoryId: Long - Foreign key to Category table
- createTime: LocalDateTime - Article creation timestamp
- updateTime: LocalDateTime - Last update timestamp
- publishTime: LocalDateTime - Publication timestamp

### TypeScript Interface

```typescript
export interface Article {
  id: number;
  title: string;
  content: string;
  summary?: string;
  coverImage?: string;
  status: 'DRAFT' | 'PUBLISHED' | 'DELETED';
  viewCount: number;
  likeCount: number;
  commentCount: number;
  isTop: boolean;
  authorId: number;
  categoryId: number;
  createTime: string;
  updateTime: string;
  publishTime?: string;
}
```

### Relationships
- Many-to-one with User (article belongs to one author)
- Many-to-one with Category (article belongs to one category)
- Many-to-many with Tag through ArticleTag (article can have many tags)
- One-to-many with Comment (article can have many comments)
- Many-to-many with User through ArticleLike (article can be liked by many users)

## Category

**Purpose:** Represents article categories for organizing content.

**Key Attributes:**
- id: Long - Unique identifier for the category
- name: String - Category name (required, unique)
- description: String - Category description
- icon: String - Category icon identifier
- parentId: Long - Parent category ID for hierarchical structure
- sortOrder: Integer - Display order
- articleCount: Integer - Number of articles in this category
- createTime: LocalDateTime - Category creation timestamp
- updateTime: LocalDateTime - Last update timestamp

### TypeScript Interface

```typescript
export interface Category {
  id: number;
  name: string;
  description?: string;
  icon?: string;
  parentId?: number;
  sortOrder: number;
  articleCount: number;
  createTime: string;
  updateTime: string;
}
```

### Relationships
- One-to-many with Article (category can have many articles)
- Self-referential one-to-many for hierarchical structure

## Tag

**Purpose:** Represents tags for flexible article categorization.

**Key Attributes:**
- id: Long - Unique identifier for the tag
- name: String - Tag name (required, unique)
- color: String - Tag color in hex format
- articleCount: Integer - Number of articles with this tag
- createTime: LocalDateTime - Tag creation timestamp
- updateTime: LocalDateTime - Last update timestamp

### TypeScript Interface

```typescript
export interface Tag {
  id: number;
  name: string;
  color: string;
  articleCount: number;
  createTime: string;
  updateTime: string;
}
```

### Relationships
- Many-to-many with Article through ArticleTag (tag can be applied to many articles)

## Comment

**Purpose:** Represents user comments on articles, supporting nested replies.

**Key Attributes:**
- id: Long - Unique identifier for the comment
- content: String - Comment content
- articleId: Long - Foreign key to Article table
- userId: Long - Foreign key to User table
- parentId: Long - Parent comment ID for nested structure
- level: Integer - Comment nesting level
- likeCount: Integer - Number of likes for the comment
- status: CommentStatus - Comment status (NORMAL, DELETED)
- createTime: LocalDateTime - Comment creation timestamp
- updateTime: LocalDateTime - Last update timestamp

### TypeScript Interface

```typescript
export interface Comment {
  id: number;
  content: string;
  articleId: number;
  userId: number;
  parentId?: number;
  level: number;
  likeCount: number;
  status: 'NORMAL' | 'DELETED';
  createTime: string;
  updateTime: string;
}
```

### Relationships
- Many-to-one with Article (comment belongs to one article)
- Many-to-one with User (comment belongs to one user)
- Self-referential one-to-many for nested comment structure

## ArticleTag

**Purpose:** Junction table for many-to-many relationship between articles and tags.

**Key Attributes:**
- articleId: Long - Foreign key to Article table
- tagId: Long - Foreign key to Tag table
- createTime: LocalDateTime - Association creation timestamp

### TypeScript Interface

```typescript
export interface ArticleTag {
  articleId: number;
  tagId: number;
  createTime: string;
}
```

## ArticleLike

**Purpose:** Tracks user likes on articles.

**Key Attributes:**
- id: Long - Unique identifier for the like record
- articleId: Long - Foreign key to Article table
- userId: Long - Foreign key to User table
- createTime: LocalDateTime - Like creation timestamp

### TypeScript Interface

```typescript
export interface ArticleLike {
  id: number;
  articleId: number;
  userId: number;
  createTime: string;
}
```
