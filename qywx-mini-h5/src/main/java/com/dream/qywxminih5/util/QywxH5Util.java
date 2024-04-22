package com.dream.qywxminih5.util;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
public class QywxH5Util {

    @Value("${qywx.corpid}")
    private String corpid;

    @Value("${qywx.appId}")
    private String appId;

    @Value("${qywx.corpsecret}")
    private String corpsecret;

    /**
     * 获取企业微信token、
     * token有效期2小时，无需每次都拿新token，缓存即可
     */
    public String getQywxToken(){
//      TODO 先从Redis中读取token，如果为空，再获取token

        String tokenUrl = "https://qyapi.weixin.qq.com/cgi-bin/gettoken?corpid=" + corpid + "&corpsecret=" + corpsecret;
        String result = HttpUtil.get(tokenUrl);
//        String result = "{\"errcode\":0,\"errmsg\":\"ok\",\"access_token\":\"yynxudttHdOf5u7CNm8diQU4ven8NSLfXE7O-mgdWZJTNz85iZSIhk5tuU6ff5-EP0jekhq4yBEDPPajjdLA3efFS9V348xsiSPNB32qYsRGCjLFORVrM7IncvFQ6C0xtaiO50QL_xrLZhmgR1ETCWK_JbRZd7hdk77QTeYbVQ4vpzA0fnR6YIRjV1pkDA2mSE5UdwlBR-tOX4Xf-c0_iQ\",\"expires_in\":7200}";
        Map map = JSONObject.parseObject(result, Map.class);

        log.info("获取token请求结果{}",result);

        String errcode =  String.valueOf(map.get("errcode"));
        String token =  String.valueOf(map.get("access_token"));
        if("0".equals(errcode)){
//            TODO 将code存入 Redis
            return token;
        }
        return null;
    }

    /**
     * 企业微信，根据用户code，获取用户ID等信息
     * @param code 获取页面code
     * @return
     */
    public Map getQywxSession(String code){
        String sessionUrl = "https://qyapi.weixin.qq.com/cgi-bin/auth/getuserinfo?access_token=" + getQywxToken() + "&code=" + code;
        String sessionRst = HttpUtil.get(sessionUrl);
        Map map = JSONObject.parseObject(sessionRst, Map.class);
        String errcode = String.valueOf(map.get("errcode"));
//        token 无效,需要重新获取token
        if("40014".equals(errcode)){
//            TODO 删除Redis中的 token的缓存
            sessionUrl = "https://qyapi.weixin.qq.com/cgi-bin/auth/getuserinfo?access_token=" + getQywxToken() + "&code=" + code;
            sessionRst = HttpUtil.get(sessionUrl);
            map = JSONObject.parseObject(sessionRst, Map.class);
        }
        return map;
    }

    /**
     * 发送消息通知
     * @param wxid 发送用户的userId
     */
    public void sendMsg(String wxid){
        String sb = "{" + "    \"touser\" : \"" +
                wxid + "\"," +
                "    \"toparty\" : \"\",\n" +
                "    \"totag\" : \"\",\n" +
                "    \"msgtype\" : \"template_card\",\n" +
                "    \"agentid\" : " + appId +
                ",\n" +
                "    \"template_card\" : {\n" +
                "        \"card_type\" : \"text_notice\",\n" +
                "        \"source\" : {\n" +
                "            \"icon_url\": \"\",\n" +
                "            \"desc\": \"审计管理系统\",\n" +
                "            \"desc_color\": 1\n" +
                "        },\n" +
                "        \"task_id\": \"\",\n" +
                "        \"main_title\" : {\n" +
                "            \"title\" : \"" +
                "标题" + "\",\n" +
                "            \"desc\" : \"" +
                "描述" + "\"\n" +
                "        },\n" +
                "        \"horizontal_content_list\" : [\n" +
                "            {\n" +
                "                \"keyname\": \"事项类型\",\n" +
                "                \"value\": \"" +
                "type" +
                "\"\n" +
                "            },\n" +
                "            {\n" +
                "                \"keyname\": \"发起人\",\n" +
                "                \"value\": \"" +
                "user" + "\"\n" +
                "            },\n" +
                "            {\n" +
                "                \"keyname\": \"所属部门\",\n" +
                "                \"value\": \"" +
                "dept" + "\"\n" +
                "            },\n" +
                "            {\n" +
                "                \"keyname\": \"所属企业\",\n" +
                "                \"value\": \"" +
                "org" +
                "\"\n" +
                "            }\n" +
                "        ],\n" +
                "        \"card_action\": {\n" +
                "            \"type\": 1,\n" +
                "            \"url\": \"" +
                "url"+ "\",\n" +
                "            \"appid\": \"\",\n" +
                "            \"pagepath\": \"\"\n" +
                "        }\n" +
                "    },\n" +
                "    \"enable_id_trans\": 0,\n" +
                "    \"enable_duplicate_check\": 0,\n" +
                "    \"duplicate_check_interval\": 1800\n" +
                "}";
        String realUrl = "https://qyapi.weixin.qq.com/cgi-bin/message/send?access_token="+ getQywxToken();
        HttpUtil.post(realUrl, sb);
    }

}
