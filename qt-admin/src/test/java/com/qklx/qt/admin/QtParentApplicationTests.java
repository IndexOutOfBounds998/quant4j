//package com.qklx.qt.admin;
//
//import com.qklx.qt.admin.entity.User;
//import com.qklx.qt.common.config.RedisUtil;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.context.junit4.SpringRunner;
//
//@RunWith(SpringRunner.class)
//@SpringBootTest
//public class QtParentApplicationTests {
//
//    @Autowired
//    RedisUtil redisUtil;
//
//
//    @Test
//    public void test() throws Exception {
//
//        redisUtil.sSet("testSet", 1, 2, 3);
//
//        boolean testSet1 = redisUtil.sHasKey("testSet", 1);
//        boolean testSet2 = redisUtil.sHasKey("testSet", 4);
//        System.out.println("testSet1" + testSet1);
//        System.out.println("testSet2" + testSet2);
//    }
//
//
//}
