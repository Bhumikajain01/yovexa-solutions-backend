package com.yovexa.solutions.cache;

import com.yovexa.solutions.config.RedisConfig;
import com.yovexa.solutions.dto.hero.HeroResponse;
import com.yovexa.solutions.dto.service.ServiceResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.cache.RedisCacheManager;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class RedisCacheTest {

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private RedisConfig redisConfig;

    @Test
    void testCacheManagerConfiguration() {
        assertNotNull(cacheManager, "CacheManager should not be null");
        assertTrue(cacheManager instanceof RedisCacheManager, "CacheManager should be an instance of RedisCacheManager");
        assertNotNull(redisConfig.errorHandler(), "CacheErrorHandler should be configured");

        assertNotNull(cacheManager.getCache(RedisConfig.HERO_CACHE));
        assertNotNull(cacheManager.getCache(RedisConfig.ABOUT_CACHE));
        assertNotNull(cacheManager.getCache(RedisConfig.SERVICES_CACHE));
        assertNotNull(cacheManager.getCache(RedisConfig.PROJECTS_CACHE));
        assertNotNull(cacheManager.getCache(RedisConfig.CASE_STUDIES_CACHE));
        assertNotNull(cacheManager.getCache(RedisConfig.BLOGS_CACHE));
        assertNotNull(cacheManager.getCache(RedisConfig.SETTINGS_CACHE));
        assertNotNull(cacheManager.getCache(RedisConfig.DASHBOARD_CACHE));
        assertNotNull(cacheManager.getCache(RedisConfig.ADMINS_CACHE));
    }

    @Test
    void testDirectSerialization() throws Exception {
        HeroResponse hero = HeroResponse.builder()
                .id("test-id-1")
                .headline("Test Headline")
                .isActive(true)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        List<HeroResponse> list = List.of(hero);

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        System.out.println("PLAIN_JSON: " + mapper.writeValueAsString(list));

        ObjectMapper pureMapper = new ObjectMapper();
        pureMapper.registerModule(new JavaTimeModule());
        pureMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        pureMapper.activateDefaultTyping(
                com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL,
                com.fasterxml.jackson.annotation.JsonTypeInfo.As.WRAPPER_ARRAY
        );

        Object toSerialize = list;
        if (toSerialize instanceof java.util.Collection<?> col && !(col instanceof java.util.ArrayList)) {
            toSerialize = new java.util.ArrayList<>(col);
        }

        byte[] pureBytes = pureMapper.writeValueAsBytes(toSerialize);
        System.out.println("PURE_SER_JSON: " + new String(pureBytes));
        Object pureRes = pureMapper.readValue(pureBytes, Object.class);
        System.out.println("PURE_DESER_RESULT: " + pureRes);
        System.out.println("PURE_DESER_CLASS: " + pureRes.getClass().getName());
        if (pureRes instanceof List<?> l && !l.isEmpty()) {
            System.out.println("PURE_ELEMENT_CLASS: " + l.get(0).getClass().getName());
            assertEquals(HeroResponse.class, l.get(0).getClass());
        }

        // Test single DTO
        byte[] heroBytes = pureMapper.writeValueAsBytes(hero);
        System.out.println("HERO_SER_JSON: " + new String(heroBytes));
        Object heroRes = pureMapper.readValue(heroBytes, Object.class);
        System.out.println("HERO_DESER_CLASS: " + heroRes.getClass().getName());
        assertEquals(HeroResponse.class, heroRes.getClass());
    }

    @Test
    void testProjectCaseStudyAndBlogSerialization() {
        RedisConfig.RedisJsonSerializer serializer = new RedisConfig.RedisJsonSerializer();

        // 1. ProjectResponse list
        com.yovexa.solutions.dto.project.ProjectResponse proj = com.yovexa.solutions.dto.project.ProjectResponse.builder()
                .id("proj-1")
                .name("Test Project")
                .slug("test-project")
                .features(List.of("Feature 1"))
                .technologies(List.of("Tech 1"))
                .createdAt(Instant.now())
                .build();
        byte[] projBytes = serializer.serialize(List.of(proj));
        Object projDeser = serializer.deserialize(projBytes);
        assertNotNull(projDeser);
        assertTrue(projDeser instanceof List);

        // 2. CaseStudyResponse list
        com.yovexa.solutions.dto.casestudy.CaseStudyResponse cs = com.yovexa.solutions.dto.casestudy.CaseStudyResponse.builder()
                .id("cs-1")
                .title("Test CS")
                .slug("test-cs")
                .createdAt(Instant.now())
                .build();
        byte[] csBytes = serializer.serialize(List.of(cs));
        Object csDeser = serializer.deserialize(csBytes);
        assertNotNull(csDeser);
        assertTrue(csDeser instanceof List);

        // 3. BlogResponse list
        com.yovexa.solutions.dto.blog.BlogResponse blog = com.yovexa.solutions.dto.blog.BlogResponse.builder()
                .id("blog-1")
                .title("Test Blog")
                .slug("test-blog")
                .createdAt(Instant.now())
                .build();
        byte[] blogBytes = serializer.serialize(List.of(blog));
        Object blogDeser = serializer.deserialize(blogBytes);
        assertNotNull(blogDeser);
        assertTrue(blogDeser instanceof List);

        // 4. Categories list (List<Map<String, String>>)
        List<java.util.Map<String, String>> categories = List.of(
                java.util.Map.of("id", "all", "label", "All Projects"),
                java.util.Map.of("id", "WEB_APPLICATIONS", "label", "Web Applications")
        );
        byte[] catBytes = serializer.serialize(categories);
        Object catDeser = serializer.deserialize(catBytes);
        assertNotNull(catDeser);
        assertTrue(catDeser instanceof List);
    }

    @Test
    void testCachePutAndGetWithList() {
        Cache heroCache = cacheManager.getCache(RedisConfig.HERO_CACHE);
        assertNotNull(heroCache);

        HeroResponse hero = HeroResponse.builder()
                .id("test-id-1")
                .headline("Test Headline")
                .isActive(true)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        List<HeroResponse> list = List.of(hero);

        // Put list into cache (skipped gracefully if Redis server is not running locally)
        try {
            heroCache.put("test-all", list);

            // Retrieve from cache
            Cache.ValueWrapper wrapper = heroCache.get("test-all");
            assertNotNull(wrapper, "Wrapper should not be null");
            Object cachedValue = wrapper.get();
            assertNotNull(cachedValue, "Cached value should not be null");
            assertTrue(cachedValue instanceof List, "Cached value should be a List");

            List<?> retrievedList = (List<?>) cachedValue;
            assertEquals(1, retrievedList.size());
            assertTrue(retrievedList.get(0) instanceof HeroResponse, "Element should be HeroResponse");
            HeroResponse retrievedHero = (HeroResponse) retrievedList.get(0);
            assertEquals("test-id-1", retrievedHero.getId());
            assertEquals("Test Headline", retrievedHero.getHeadline());

            // Evict
            heroCache.evict("test-all");
            assertNull(heroCache.get("test-all"));
        } catch (org.springframework.data.redis.RedisConnectionFailureException e) {
            System.out.println("Redis server offline during testCachePutAndGetWithList; handled gracefully: " + e.getMessage());
        }
    }

    @Test
    void testCachePutAndGetWithSingleDto() {
        Cache serviceCache = cacheManager.getCache(RedisConfig.SERVICES_CACHE);
        assertNotNull(serviceCache);

        ServiceResponse service = ServiceResponse.builder()
                .id("srv-123")
                .title("Web Development")
                .slug("web-development")
                .features(List.of("React", "Spring Boot"))
                .isActive(true)
                .build();

        try {
            serviceCache.put("slug-web-development", service);

            Cache.ValueWrapper wrapper = serviceCache.get("slug-web-development");
            assertNotNull(wrapper);
            Object cached = wrapper.get();
            assertNotNull(cached);

            serviceCache.evict("slug-web-development");
            assertNull(serviceCache.get("slug-web-development"));
        } catch (org.springframework.data.redis.RedisConnectionFailureException e) {
            System.out.println("Redis server offline during testCachePutAndGetWithSingleDto; handled gracefully: " + e.getMessage());
        }
    }
}
