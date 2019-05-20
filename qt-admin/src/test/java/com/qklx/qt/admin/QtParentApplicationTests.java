package com.qklx.qt.admin;

import com.qklx.qt.admin.entity.User;

//@RunWith(SpringRunner.class)
//@SpringBootTest
public class QtParentApplicationTests {

//    @Autowired
//    private IMailService iMailService;

    //    @Test
    public void testMail() throws Exception {


        User user = new User();
        user = user.selectById(1);
        System.out.println("获取到user" + user);
    }


}
