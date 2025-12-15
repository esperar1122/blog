# Monitoring and Observability

## Monitoring Stack

- **Frontend Monitoring:** Sentry for error tracking, Google Analytics for user behavior
- **Backend Monitoring:** Spring Boot Actuator + Micrometer + Prometheus
- **Error Tracking:** Centralized logging with ELK Stack (Elasticsearch, Logstash, Kibana)
- **Performance Monitoring:** APM tools like Alibaba Cloud ARMS

## Key Metrics

**Frontend Metrics:**
- Core Web Vitals (LCP, FID, CLS)
- JavaScript errors by type and frequency
- API response times by endpoint
- User engagement metrics

**Backend Metrics:**
- Request rate and response times by endpoint
- Error rate by HTTP status code
- Database query performance
- JVM metrics (heap usage, GC pauses)
- Redis hit rates and connection counts
