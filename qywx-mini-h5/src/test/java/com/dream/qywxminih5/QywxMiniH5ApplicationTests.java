package com.dream.qywxminih5;

import com.dream.qywxminih5.util.QywxH5Util;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class QywxMiniH5ApplicationTests {

    @Autowired
    private QywxH5Util qywxMiniUtil;

    @Test
    void testToken() {
        qywxMiniUtil.getQywxToken();
    }

}
