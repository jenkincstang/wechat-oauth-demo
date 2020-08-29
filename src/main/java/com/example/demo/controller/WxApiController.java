package com.example.demo.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.example.demo.util.ConstantWxUtils;
import com.google.gson.Gson;
import org.springframework.data.repository.query.Param;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.util.HashMap;

//@CrossOrigin
@Controller  //只是请求地址，不需要返回数据
@RequestMapping("/api/user/wx")
public class WxApiController {

//  @Autowired
//  private UserMemberService memberService;

  @GetMapping("test")
  @ResponseBody
  public String test(){
    return "<h1>Hello World</h1>";
  }


  //1.生成微信扫描二维码
  @GetMapping("login")
  public String getWxCode() {
    //固定地址，后面拼接参数
//        String url = "https://open.weixin.qq.com/" +
//                "connect/oauth2/authorize?appid="+ ConstantWxUtils.WX_OPEN_APP_ID+"&response_type=code";

    // 微信开放平台授权baseUrl  %s相当于?代表占位符
    String baseUrl = "https://open.weixin.qq.com/connect/oauth2/authorize" +
        "?appid=%s" +
        "&redirect_uri=%s"+
        "&response_type=code" +
        "&scope=snsapi_userinfo" +
        "&state=%s" +
        "#wechat_redirect";

    //对redirect_url进行URLEncoder编码
    String redirectUrl = ConstantWxUtils.WX_OPEN_REDIRECT_URL;
    try {
      redirectUrl = URLEncoder.encode(redirectUrl, "utf-8");
    }catch(Exception e) {
    }

    //设置%s里面值
    String url = String.format(
        baseUrl,
        ConstantWxUtils.WX_OPEN_APP_ID,
        redirectUrl,
        "liang"
    );
    //重定向到请求微信地址里面
    return "redirect:"+url;
  }

  //2 获取扫描人信息，添加数据
  @GetMapping("callback")
  public String callback(@Param("code") String code, @Param("state") String state) throws Exception {
    System.out.println("code==>>"+code);
    System.out.println("state==>>"+state);
    try {
      //1 获取code值，临时票据，类似于验证码
      //2 拿着code请求 微信固定的地址，得到两个值 accsess_token 和 openid
      String baseAccessTokenUrl = "https://api.weixin.qq.com/sns/oauth2/access_token" +
              "?appid=%s" +
              "&secret=%s" +
              "&code=%s" +
              "&grant_type=authorization_code";
      //拼接三个参数 ：id  秘钥 和 code值
      String accessTokenUrl = String.format(
              baseAccessTokenUrl,
              ConstantWxUtils.WX_OPEN_APP_ID,
              ConstantWxUtils.WX_OPEN_APP_SECRET,
              code
      );
      //请求这个拼接好的地址，得到返回两个值 accsess_token 和 openid
      //使用httpclient发送请求，得到返回结果
      RestTemplate restTemplate = new RestTemplate();
      ResponseEntity<String> accessTokenInfo = restTemplate.getForEntity(accessTokenUrl,String.class);
      System.out.println(accessTokenInfo.getBody());
      System.out.println(accessTokenInfo.getHeaders());
      System.out.println(accessTokenInfo.getStatusCode());
      System.out.println(accessTokenInfo.getStatusCodeValue());
      System.out.println(accessTokenUrl);
      System.out.println(accessTokenInfo);
      //从accessTokenInfo字符串获取出来两个值 accsess_token 和 openid
      //把accessTokenInfo字符串转换map集合，根据map里面key获取对应值
      //使用json转换工具 Gson
      Gson gson = new Gson();
      HashMap mapAccessToken = gson.fromJson(accessTokenInfo.getBody(), HashMap.class);
      String access_token = (String)mapAccessToken.get("access_token");
      String openid = (String)mapAccessToken.get("openid");


      //https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=APPID&secret=APPSECRET

      String baseTokenUrl = "https://api.weixin.qq.com/cgi-bin/token" +
              "?grant_type=client_credential" +
              "&appid=%s" +
              "&secret=%s";
      String tokenUrlUrl = String.format(
              baseTokenUrl,
              ConstantWxUtils.WX_OPEN_APP_ID,
              ConstantWxUtils.WX_OPEN_APP_SECRET
      );

      ResponseEntity<String> tokenInfo = restTemplate.getForEntity(tokenUrlUrl,String.class);





      //        //获取返回userinfo字符串扫描人信息
      HashMap tokenInfoMap = gson.fromJson(tokenInfo.getBody(), HashMap.class);
      String token = (String)tokenInfoMap.get("access_token");
      System.out.println("Token==>>"+token);
      //https://api.weixin.qq.com/cgi-bin/menu/create?access_token=ACCESS_TOKEN
      String baseCreateButtonUrl = "https://api.weixin.qq.com/cgi-bin/menu/create" +
              "?access_token=%s";
      //拼接三个参数 ：id  秘钥 和 code值
      String createButtonUrl = String.format(
              baseCreateButtonUrl,
              token
      );
      //请求这个拼接好的地址，得到返回两个值 accsess_token 和 openid
      //使用httpclient发送请求，得到返回结果
      String buttonJson = "{\"button\": [{\"type\": \"view\", \"name\": \"OOCL\", \"key\": \"EMPLOAN_HTML_LOAN\",\"url\": \"http://2wcrat.natappfree.cc/api/user/wx/test\"}]}";
      //buttonJson = JSON.toJSONString(buttonJson, SerializerFeature.UseSingleQuotes);
      ResponseEntity<String> createButtonInfo = restTemplate.postForEntity(createButtonUrl,buttonJson,String.class);


      //      //把扫描人信息添加数据库里面
//      //判断数据表里面是否存在相同微信信息，根据openid判断
//      UcenterMember member = memberService.getOpenIdMember(openid);
//      if(member == null) {//memeber是空，表没有相同微信数据，进行添加
//
//        //3 拿着得到accsess_token 和 openid，再去请求微信提供固定的地址，获取到扫描人信息
//        //访问微信的资源服务器，获取用户信息
        String baseUserInfoUrl = "https://api.weixin.qq.com/sns/userinfo" +
                "?access_token=%s" +
                "&openid=%s";
        //拼接两个参数
        String userInfoUrl = String.format(
                baseUserInfoUrl,
                access_token,
                openid
        );

          ResponseEntity<String> userInfo = restTemplate.getForEntity(userInfoUrl,String.class);





          //        //获取返回userinfo字符串扫描人信息
          HashMap userInfoMap = gson.fromJson(userInfo.getBody(), HashMap.class);
          String nickname = (String)userInfoMap.get("nickname");//昵称
          String headimgurl = (String)userInfoMap.get("headimgurl");//头像
          System.out.println(nickname);
          System.out.println(headimgurl);
          System.out.println(userInfoMap);
          System.out.println(userInfo);


//
//        //TODO保存一些用户信息
//        //TODO........
//      }

      //使用jwt根据member对象生成token字符串
//      String jwtToken = JwtUtils.getJwtToken(member.getId(), member.getNickname());
////      //最后：返回首页面，通过路径传递token字符串
////      return "redirect:http://www.baidu.com?token="+jwtToken;

      System.out.println("Button Body:>>"+createButtonInfo.getBody());
      System.out.println("Button Header:>>"+createButtonInfo.getHeaders());

      return "redirect:http://www.baidu.com";
    } catch (Exception e) {
//      throw new GuliException(20001,"登录失败");
      throw new Exception("20001");
    }
  }

}