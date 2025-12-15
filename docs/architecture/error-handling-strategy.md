# Error Handling Strategy

## Error Response Format

```typescript
interface ApiError {
  error: {
    code: string;
    message: string;
    details?: Record<string, any>;
    timestamp: string;
    requestId: string;
  };
}
```

## Frontend Error Handling

```typescript
// src/utils/errorHandler.ts
export class ErrorHandler {
  static handle(error: any): void {
    console.error('API Error:', error)

    if (error.response?.data) {
      const apiError = error.response.data

      switch (apiError.error?.code) {
        case 'VALIDATION_ERROR':
          // Handle field validation errors
          this.handleValidationErrors(apiError.error.details)
          break
        case 'UNAUTHORIZED':
          // Redirect to login
          router.push('/auth/login')
          break
        case 'FORBIDDEN':
          ElMessage.error('没有权限执行此操作')
          break
        default:
          ElMessage.error(apiError.error.message || '操作失败')
      }
    } else {
      ElMessage.error('网络错误，请稍后重试')
    }
  }

  private static handleValidationErrors(details: Record<string, string[]>): void {
    Object.entries(details).forEach(([field, messages]) => {
      messages.forEach(message => {
        ElMessage.error(`${field}: ${message}`)
      })
    })
  }
}
```

## Backend Error Handling

```java
// src/main/java/com/example/blog/exception/GlobalExceptionHandler.java
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex) {

        Map<String, String[]> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> {
            String fieldName = error.getField();
            String errorMessage = error.getDefaultMessage();
            errors.merge(fieldName, new String[]{errorMessage},
                (old, new) -> Stream.concat(Arrays.stream(old), Arrays.stream(new)).toArray(String[]::new));
        });

        ErrorResponse response = ErrorResponse.builder()
                .code("VALIDATION_ERROR")
                .message("请求数据验证失败")
                .details(errors)
                .timestamp(LocalDateTime.now())
                .requestId(MDC.get("requestId"))
                .build();

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException ex) {
        ErrorResponse response = ErrorResponse.builder()
                .code(ex.getCode())
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .requestId(MDC.get("requestId"))
                .build();

        return ResponseEntity.status(ex.getHttpStatus()).body(response);
    }
}
```
