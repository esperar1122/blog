# API Specification

## REST API Specification

```yaml
openapi: 3.0.0
info:
  title: 大学生个人博客系统 API
  version: 1.0.0
  description: RESTful API for the university student blog system
servers:
  - url: https://api.blog.example.com/v1
    description: Production server
  - url: https://api-staging.blog.example.com/v1
    description: Staging server

paths:
  # Authentication endpoints
  /auth/register:
    post:
      summary: User registration
      tags:
        - Authentication
      requestBody:
        required: true
        content:
          application/json:
            schema:
              type: object
              required:
                - username
                - email
                - password
              properties:
                username:
                  type: string
                  minLength: 3
                  maxLength: 20
                email:
                  type: string
                  format: email
                password:
                  type: string
                  minLength: 8
      responses:
        '201':
          description: User registered successfully
        '400':
          description: Invalid input data
        '409':
          description: Username or email already exists

  /auth/login:
    post:
      summary: User login
      tags:
        - Authentication
      requestBody:
        required: true
        content:
          application/json:
            schema:
              type: object
              required:
                - username
                - password
              properties:
                username:
                  type: string
                password:
                  type: string
      responses:
        '200':
          description: Login successful
          content:
            application/json:
              schema:
                type: object
                properties:
                  accessToken:
                    type: string
                  refreshToken:
                    type: string
                  user:
                    $ref: '#/components/schemas/User'
        '401':
          description: Invalid credentials

  /auth/refresh:
    post:
      summary: Refresh access token
      tags:
        - Authentication
      requestBody:
        required: true
        content:
          application/json:
            schema:
              type: object
              required:
                - refreshToken
              properties:
                refreshToken:
                  type: string
      responses:
        '200':
          description: Token refreshed successfully
        '401':
          description: Invalid refresh token

  # User endpoints
  /users/profile:
    get:
      summary: Get current user profile
      tags:
        - Users
      security:
        - bearerAuth: []
      responses:
        '200':
          description: User profile retrieved successfully
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/User'
    put:
      summary: Update user profile
      tags:
        - Users
      security:
        - bearerAuth: []
      requestBody:
        required: true
        content:
          application/json:
            schema:
              type: object
              properties:
                nickname:
                  type: string
                bio:
                  type: string
                avatar:
                  type: string
      responses:
        '200':
          description: Profile updated successfully

  # Article endpoints
  /articles:
    get:
      summary: Get articles list with pagination
      tags:
        - Articles
      parameters:
        - name: page
          in: query
          schema:
            type: integer
            default: 0
        - name: size
          in: query
          schema:
            type: integer
            default: 10
        - name: categoryId
          in: query
          schema:
            type: integer
        - name: tagId
          in: query
          schema:
            type: integer
        - name: keyword
          in: query
          schema:
            type: string
      responses:
        '200':
          description: Articles retrieved successfully
          content:
            application/json:
              schema:
                type: object
                properties:
                  content:
                    type: array
                    items:
                      $ref: '#/components/schemas/Article'
                  totalElements:
                    type: integer
                  totalPages:
                    type: integer
    post:
      summary: Create new article
      tags:
        - Articles
      security:
        - bearerAuth: []
      requestBody:
        required: true
        content:
          application/json:
            schema:
              type: object
              required:
                - title
                - content
                - categoryId
              properties:
                title:
                  type: string
                content:
                  type: string
                summary:
                  type: string
                coverImage:
                  type: string
                categoryId:
                  type: integer
                tagIds:
                  type: array
                  items:
                    type: integer
                status:
                  type: string
                  enum: [DRAFT, PUBLISHED]
      responses:
        '201':
          description: Article created successfully
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/Article'

  /articles/{id}:
    get:
      summary: Get article by ID
      tags:
        - Articles
      parameters:
        - name: id
          in: path
          required: true
          schema:
            type: integer
      responses:
        '200':
          description: Article retrieved successfully
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ArticleDetail'
        '404':
          description: Article not found
    put:
      summary: Update article
      tags:
        - Articles
      security:
        - bearerAuth: []
      parameters:
        - name: id
          in: path
          required: true
          schema:
            type: integer
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/UpdateArticleRequest'
      responses:
        '200':
          description: Article updated successfully
        '403':
          description: Not authorized to update this article
        '404':
          description: Article not found
    delete:
      summary: Delete article
      tags:
        - Articles
      security:
        - bearerAuth: []
      parameters:
        - name: id
          in: path
          required: true
          schema:
            type: integer
      responses:
        '204':
          description: Article deleted successfully
        '403':
          description: Not authorized to delete this article
        '404':
          description: Article not found

  # Category endpoints
  /categories:
    get:
      summary: Get all categories
      tags:
        - Categories
      responses:
        '200':
          description: Categories retrieved successfully
          content:
            application/json:
              schema:
                type: array
                items:
                  $ref: '#/components/schemas/Category'

  # Tag endpoints
  /tags:
    get:
      summary: Get all tags
      tags:
        - Tags
      responses:
        '200':
          description: Tags retrieved successfully
          content:
            application/json:
              schema:
                type: array
                items:
                  $ref: '#/components/schemas/Tag'

  # Comment endpoints
  /articles/{articleId}/comments:
    get:
      summary: Get comments for an article
      tags:
        - Comments
      parameters:
        - name: articleId
          in: path
          required: true
          schema:
            type: integer
        - name: page
          in: query
          schema:
            type: integer
            default: 0
        - name: size
          in: query
          schema:
            type: integer
            default: 20
      responses:
        '200':
          description: Comments retrieved successfully
    post:
      summary: Create comment
      tags:
        - Comments
      security:
        - bearerAuth: []
      parameters:
        - name: articleId
          in: path
          required: true
          schema:
            type: integer
      requestBody:
        required: true
        content:
          application/json:
            schema:
              type: object
              required:
                - content
              properties:
                content:
                  type: string
                parentId:
                  type: integer
      responses:
        '201':
          description: Comment created successfully

  # Admin endpoints
  /admin/users:
    get:
      summary: Get all users (Admin only)
      tags:
        - Admin
      security:
        - bearerAuth: []
      parameters:
        - name: page
          in: query
          schema:
            type: integer
            default: 0
        - name: size
          in: query
          schema:
            type: integer
            default: 20
        - name: role
          in: query
          schema:
            type: string
            enum: [USER, ADMIN]
        - name: status
          in: query
          schema:
            type: string
            enum: [ACTIVE, INACTIVE, BANNED]
      responses:
        '200':
          description: Users retrieved successfully
        '403':
          description: Admin access required

components:
  schemas:
    User:
      type: object
      properties:
        id:
          type: integer
        username:
          type: string
        email:
          type: string
        nickname:
          type: string
        avatar:
          type: string
        bio:
          type: string
        role:
          type: string
          enum: [USER, ADMIN]
        status:
          type: string
          enum: [ACTIVE, INACTIVE, BANNED]
        createTime:
          type: string
          format: date-time

    Article:
      type: object
      properties:
        id:
          type: integer
        title:
          type: string
        summary:
          type: string
        coverImage:
          type: string
        status:
          type: string
          enum: [DRAFT, PUBLISHED, DELETED]
        viewCount:
          type: integer
        likeCount:
          type: integer
        commentCount:
          type: integer
        isTop:
          type: boolean
        author:
          $ref: '#/components/schemas/Author'
        category:
          $ref: '#/components/schemas/Category'
        tags:
          type: array
          items:
            $ref: '#/components/schemas/Tag'
        createTime:
          type: string
          format: date-time
        updateTime:
          type: string
          format: date-time
        publishTime:
          type: string
          format: date-time

    ArticleDetail:
      allOf:
        - $ref: '#/components/schemas/Article'
        - type: object
          properties:
            content:
              type: string

    Category:
      type: object
      properties:
        id:
          type: integer
        name:
          type: string
        description:
          type: string
        icon:
          type: string
        parentId:
          type: integer
        sortOrder:
          type: integer
        articleCount:
          type: integer
        createTime:
          type: string
          format: date-time

    Tag:
      type: object
      properties:
        id:
          type: integer
        name:
          type: string
        color:
          type: string
        articleCount:
          type: integer
        createTime:
          type: string
          format: date-time

    Author:
      type: object
      properties:
        id:
          type: integer
        username:
          type: string
        nickname:
          type: string
        avatar:
          type: string

    UpdateArticleRequest:
      type: object
      properties:
        title:
          type: string
        content:
          type: string
        summary:
          type: string
        coverImage:
          type: string
        categoryId:
          type: integer
        tagIds:
          type: array
          items:
            type: integer
        status:
          type: string
          enum: [DRAFT, PUBLISHED]

  securitySchemes:
    bearerAuth:
      type: http
      scheme: bearer
      bearerFormat: JWT
```
