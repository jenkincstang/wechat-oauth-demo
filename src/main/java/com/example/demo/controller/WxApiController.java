package com.example.demo.controller;

import com.example.demo.util.ConstantWxUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.net.URLEncoder;

//@CrossOrigin
@Controller  //只是请求地址，不需要返回数据
@RequestMapping("/api/user/wx")
public class WxApiController {

//  @Autowired
//  private UserMemberService memberService;


  //1.生成微信扫描二维码
  @GetMapping("login")
  public String getWxCode() {
    //固定地址，后面拼接参数
//        String url = "https://open.weixin.qq.com/" +
//                "connect/qrconnect?appid="+ ConstantWxUtils.WX_OPEN_APP_ID+"&response_type=code";

    // 微信开放平台授权baseUrl  %s相当于?代表占位符
    String baseUrl = "https://open.weixin.qq.com/connect/qrconnect" +
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
}