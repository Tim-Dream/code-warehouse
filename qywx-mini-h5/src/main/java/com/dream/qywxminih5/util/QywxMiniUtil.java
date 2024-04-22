package com.dream.qywxminih5.util;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
public class QywxMiniUtil {

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
    public Map getQywxSession(String code) {
        try {
            String sessionUrl = "https://qyapi.weixin.qq.com/cgi-bin/miniprogram/jscode2session?access_token=" + getQywxToken() + "&js_code=" + code + "&grant_type=authorization_code";
            String sessionRst = HttpUtil.get(sessionUrl);
            Map result = JSONObject.parseObject(sessionRst, Map.class);
            String errcode = String.valueOf(result.get("errcode"));
            //        token 无效,需要重新获取token
            if("40014".equals(errcode)){
//            TODO 删除Redis中的 token的缓存
                sessionUrl = "https://qyapi.weixin.qq.com/cgi-bin/miniprogram/jscode2session?access_token=" + getQywxToken() + "&js_code=" + code + "&grant_type=authorization_code";
                sessionRst = HttpUtil.get(sessionUrl);
                result = JSONObject.parseObject(sessionRst, Map.class);
            }
            return result;
        } catch (Exception e) {
            throw new RuntimeException("企业微信获取Session失败");
        }
    }


    /**
     *  发送消息通知
     * @param wxid 发送用户的userId
     */
    public void sendMsg(String wxid) {
        String url = "https://qyapi.weixin.qq.com/cgi-bin/message/send?access_token="+ getQywxToken();
        //封装发送消息请求JSON
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("{");
        stringBuffer.append("\"touser\":" + "\"" + wxid + "\",");
        stringBuffer.append("\"toparty\": \"\",");
        stringBuffer.append("\"totag\": \"\",");
        stringBuffer.append("\"msgtype\":\"miniprogram_notice\",");
        stringBuffer.append("\"miniprogram_notice\" : {");
        stringBuffer.append("\"appid\": \""+appId+"\",");
        stringBuffer.append("\"page\": \"/pages/taskDetails/taskDetails?detailsData="+"id"+"\",");
        stringBuffer.append("\"title\": \""+"title"+"\",");
        stringBuffer.append("\"emphasis_first_item\": false,");
        stringBuffer.append("\"description\": \""+"date"+" \",");
        stringBuffer.append("\"content_item\": [");
        stringBuffer.append("{");
        stringBuffer.append("\"key\": \"事项类型\",");
        stringBuffer.append("\"value\": \""+"type"+"\"");
        stringBuffer.append("},");
        stringBuffer.append("{");
        stringBuffer.append("\"key\": \"发起人\",");
        stringBuffer.append("\"value\": \""+"user"+"\"");
        stringBuffer.append("},");
        stringBuffer.append("{");
        stringBuffer.append("\"key\": \"所属部门\",");
        stringBuffer.append("\"value\": \""+"dept"+"\"");
        stringBuffer.append("},");
        stringBuffer.append("{");
        stringBuffer.append("\"key\": \"所属企业\",");
        stringBuffer.append("\"value\": \""+"org"+"\"");
        stringBuffer.append("}");
        stringBuffer.append("]");
        stringBuffer.append("},");
        stringBuffer.append("\"enable_id_trans\": 0,");
        stringBuffer.append("\"enable_duplicate_check\": 0,");
        stringBuffer.append("\"duplicate_check_interval\": 1800,");
        stringBuffer.append("}");

        HttpUtil.post(url, stringBuffer.toString());
    }
}
