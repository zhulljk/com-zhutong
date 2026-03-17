package com.zhut.user;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhut.user.util.SnowflakeIdGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 用户服务测试
 */
@SpringBootTest
@TestPropertySource(properties = {
    "spring.main.allow-circular-references=true"
})
class UserApplicationTests {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        if (mockMvc == null) {
            mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        }
        if (objectMapper == null) {
            objectMapper = new ObjectMapper();
        }
    }

    /**
     * 测试上下文加载
     */
    @Test
    void contextLoads() {
        assertNotNull(webApplicationContext);
    }

    /**
     * 测试雪花算法 ID 生成器
     */
    @Test
    void testSnowflakeIdGenerator() {
        // 测试生成 ID 的唯一性
        long id1 = SnowflakeIdGenerator.generate();
        long id2 = SnowflakeIdGenerator.generate();
        
        System.out.println("生成的 ID1: " + id1);
        System.out.println("生成的 ID2: " + id2);
        
        // ID 应该是唯一的
        assertNotEquals(id1, id2, "雪花算法生成的 ID 应该是唯一的");
        
        // ID 应该是正数
        assertTrue(id1 > 0, "生成的 ID 应该是正数");
        assertTrue(id2 > 0, "生成的 ID 应该是正数");
        
        // 测试批量生成 ID 的唯一性
        java.util.Set<Long> ids = new java.util.HashSet<>();
        for (int i = 0; i < 100; i++) {
            ids.add(SnowflakeIdGenerator.generate());
        }
        assertEquals(100, ids.size(), "批量生成的 100 个 ID 应该都是唯一的");
    }

    /**
     * 测试用户注册接口
     */
    @Test
    void testRegister() throws Exception {
        long timestamp = System.currentTimeMillis();
        Map<String, String> registerData = new HashMap<>();
        String uniqueUsername = "testuser_" + timestamp;
        registerData.put("username", uniqueUsername);
        registerData.put("password", "123456");
        registerData.put("email", "test_" + timestamp + "@example.com");
        registerData.put("nickname", "Test User");

        String requestBody = objectMapper.writeValueAsString(registerData);

        MvcResult result = getMockMvc().perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        String responseContent = result.getResponse().getContentAsString();
        System.out.println("注册响应：" + responseContent);
        
        // 解析响应
        JsonNode jsonNode = objectMapper.readTree(responseContent);
        assertEquals(200, jsonNode.get("code").asInt(), "注册应该成功");
        
        // 验证返回数据中包含 ID
        JsonNode dataNode = jsonNode.get("data");
        assertNotNull(dataNode);
        assertTrue(dataNode.has("id"), "返回数据应该包含用户 ID");
        long userId = dataNode.get("id").asLong();
        assertTrue(userId > 0, "用户 ID 应该是正数");
        
        System.out.println("注册用户 ID: " + userId);
    }

    /**
     * 测试用户登录接口
     */
    @Test
    void testLogin() throws Exception {
        // 先注册一个用户
        long timestamp = System.currentTimeMillis();
        String uniqueUsername = "logintest_" + timestamp;
        registerTestUser(uniqueUsername, "123456", "logintest_" + timestamp + "@example.com");
        
        // 然后测试登录
        Map<String, String> loginData = new HashMap<>();
        loginData.put("account", uniqueUsername);
        loginData.put("password", "123456");

        String requestBody = objectMapper.writeValueAsString(loginData);

        MvcResult result = getMockMvc().perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        String responseContent = result.getResponse().getContentAsString();
        System.out.println("登录响应：" + responseContent);
        
        // 解析响应
        JsonNode jsonNode = objectMapper.readTree(responseContent);
        assertEquals(200, jsonNode.get("code").asInt(), "登录应该成功");
        
        // 验证返回数据中包含 Token
        JsonNode dataNode = jsonNode.get("data");
        assertNotNull(dataNode);
        assertTrue(dataNode.has("token"), "返回数据应该包含 Token");
        String token = dataNode.get("token").asText();
        assertNotNull(token);
        assertFalse(token.isEmpty(), "Token 不应该为空");
        
        System.out.println("获取的 Token: " + token);
    }

    /**
     * 测试注册后登录的完整流程
     */
    @Test
    void testRegisterAndLogin() throws Exception {
        long timestamp = System.currentTimeMillis();
        String uniqueUsername = "flowtest_" + timestamp;
        String password = "test123456";
        String email = "flowtest_" + timestamp + "@example.com";
        
        // 1. 注册用户
        registerTestUser(uniqueUsername, password, email);
        
        // 2. 使用注册的账号登录
        Map<String, String> loginData = new HashMap<>();
        loginData.put("account", uniqueUsername);
        loginData.put("password", password);

        String loginBody = objectMapper.writeValueAsString(loginData);

        MvcResult loginResult = getMockMvc().perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginBody))
                .andExpect(status().isOk())
                .andReturn();

        String loginResponse = loginResult.getResponse().getContentAsString();
        JsonNode loginJson = objectMapper.readTree(loginResponse);
        
        assertEquals(200, loginJson.get("code").asInt(), "登录应该成功");
        
        JsonNode loginDataNode = loginJson.get("data");
        String token = loginDataNode.get("token").asText();
        String nickname = loginDataNode.get("nickname").asText();
        
        assertEquals(uniqueUsername, nickname, "昵称应该与用户名一致");
        assertNotNull(token, "应该返回 Token");
        
        System.out.println("完整流程测试通过，Token: " + token);
    }

    /**
     * 测试获取当前用户信息
     */
    @Test
    void testGetCurrentUser() throws Exception {
        MvcResult result = getMockMvc().perform(get("/api/auth/me"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        String responseContent = result.getResponse().getContentAsString();
        System.out.println("当前用户响应：" + responseContent);
        
        JsonNode jsonNode = objectMapper.readTree(responseContent);
        assertNotNull(jsonNode);
        assertTrue(jsonNode.has("code"), "响应应该包含 code 字段");
    }

    /**
     * 测试登出接口
     */
    @Test
    void testLogout() throws Exception {
        MvcResult result = getMockMvc().perform(post("/api/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        String responseContent = result.getResponse().getContentAsString();
        System.out.println("登出响应：" + responseContent);
        
        JsonNode jsonNode = objectMapper.readTree(responseContent);
        assertNotNull(jsonNode);
        assertTrue(jsonNode.has("code"), "响应应该包含 code 字段");
    }

    /**
     * 测试 OPTIONS 请求（CORS 预检）
     */
    @Test
    void testOptionsRequest() throws Exception {
        getMockMvc().perform(get("/api/auth/login")
                .with(request -> {
                    request.setMethod("OPTIONS");
                    return request;
                }))
                .andExpect(status().isOk())
                .andReturn();
    }

    /**
     * 测试重复注册（用户名已存在）
     */
    @Test
    void testDuplicateRegister() throws Exception {
        long timestamp = System.currentTimeMillis();
        // 先注册一个用户
        String uniqueUsername = "duptest_" + timestamp;
        registerTestUser(uniqueUsername, "123456", "dup_" + timestamp + "@example.com");
        
        // 等待一小段时间确保时间戳不同
        Thread.sleep(10);
        
        // 尝试用相同的用户名再次注册（使用不同的邮箱）
        Map<String, String> registerData = new HashMap<>();
        registerData.put("username", uniqueUsername);
        registerData.put("password", "123456");
        registerData.put("email", "dup2_" + timestamp + "@example.com");
        registerData.put("nickname", "Dup User");

        String requestBody = objectMapper.writeValueAsString(registerData);

        MvcResult result = getMockMvc().perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andReturn();

        String responseContent = result.getResponse().getContentAsString();
        System.out.println("重复注册响应：" + responseContent);
        
        JsonNode jsonNode = objectMapper.readTree(responseContent);
        // 应该返回错误（用户名已存在）
        assertNotEquals(200, jsonNode.get("code").asInt(), "重复注册应该失败");
    }

    /**
     * 辅助方法：注册测试用户
     */
    private void registerTestUser(String username, String password) throws Exception {
        registerTestUser(username, password, "test@example.com");
    }
    
    /**
     * 辅助方法：注册测试用户
     */
    private void registerTestUser(String username, String password, String email) throws Exception {
        Map<String, String> registerData = new HashMap<>();
        registerData.put("username", username);
        registerData.put("password", password);
        registerData.put("email", email);
        registerData.put("nickname", username);

        String requestBody = objectMapper.writeValueAsString(registerData);

        getMockMvc().perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk());
    }

    private MockMvc getMockMvc() {
        if (mockMvc == null) {
            mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        }
        return mockMvc;
    }
}