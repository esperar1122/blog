# Testing Strategy

## Testing Pyramid

```
E2E Tests
/        \
Integration Tests
/            \
Frontend Unit  Backend Unit
```

## Test Organization

**Frontend Tests:**
```
tests/
├── unit/              # Component unit tests
│   ├── components/
│   └── composables/
├── integration/       # Service integration tests
└── e2e/              # End-to-end tests
    └── user-flows/
```

**Backend Tests:**
```
src/test/java/
├── unit/              # Service unit tests
├── integration/       # Repository and API integration tests
└── e2e/              # Full application tests
```

## Test Examples

**Frontend Component Test:**
```typescript
// tests/unit/components/ArticleCard.spec.ts
import { mount } from '@vue/test-utils'
import { describe, it, expect } from 'vitest'
import ArticleCard from '@/components/article/ArticleCard.vue'

describe('ArticleCard', () => {
  const mockArticle = {
    id: 1,
    title: 'Test Article',
    summary: 'Test summary',
    author: { id: 1, nickname: 'Test Author' },
    viewCount: 100,
    commentCount: 10,
    likeCount: 5
  }

  it('renders article information correctly', () => {
    const wrapper = mount(ArticleCard, {
      props: { article: mockArticle }
    })

    expect(wrapper.find('.article-title').text()).toBe('Test Article')
    expect(wrapper.find('.article-summary').text()).toBe('Test summary')
    expect(wrapper.text()).toContain('100')
    expect(wrapper.text()).toContain('10')
    expect(wrapper.text()).toContain('5')
  })

  it('emits click event when clicked', async () => {
    const wrapper = mount(ArticleCard, {
      props: { article: mockArticle }
    })

    await wrapper.trigger('click')
    expect(wrapper.emitted('click')).toBeTruthy()
    expect(wrapper.emitted('click')[0]).toEqual([mockArticle])
  })
})
```

**Backend API Test:**
```java
// src/test/java/com/example/blog/controller/ArticleControllerTest.java
@WebMvcTest(ArticleController.class)
@Import(SecurityConfig.class)
class ArticleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ArticleService articleService;

    @Test
    @WithMockUser(roles = "USER")
    void shouldCreateArticle() throws Exception {
        CreateArticleRequest request = new CreateArticleRequest();
        request.setTitle("Test Article");
        request.setContent("Test content");
        request.setCategoryId(1L);

        when(articleService.createArticle(any(), any()))
            .thenReturn(new ArticleResponse());

        mockMvc.perform(post("/api/v1/articles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }
}
```
