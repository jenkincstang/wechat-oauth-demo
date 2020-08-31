package com.example.demo.controller;

import com.auth0.jwt.exceptions.AlgorithmMismatchException;
import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.demo.dto.UserDto;
import com.example.demo.entity.SysUser;
import com.example.demo.service.SysUserService;
import com.example.demo.util.TokenUtils;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import java.util.HashMap;

import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import com.example.demo.util.ConstantWxUtils;

import com.google.gson.Gson;

// @CrossOrigin
@Controller
@RequestMapping("/api/user/wx")
public class WxApiController {

  @Autowired
  private SysUserService sysUserService;

  @GetMapping("login")
  public String getWxCode() throws UnsupportedEncodingException {
    String baseAuthorizeUrl = getBaseAuthorizeUrl();
    String redirectUrl = getEncodeRedirectUrl();
    // 设置%s值
    String authorizeUrl = getAuthorizeUrl(baseAuthorizeUrl, redirectUrl, "jenkin");
    // 重定向到请求微信地址里面
    return "redirect:" + authorizeUrl;
  }

  @PostMapping("user/login")
  @ResponseBody
  public Map<String,Object> getWxCode(UserDto userDto) throws UnsupportedEncodingException {
    Map<String,Object> map = new HashMap<>();
    Map<String,String> payload = new HashMap<>();
    try{
      SysUser sysUser = sysUserService.findSysUserByUsername(userDto.getUsername());
      BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
      String tmp = passwordEncoder.encode(userDto.getPassword());

      if (passwordEncoder.matches(userDto.getPassword(),tmp)){
        payload.put("userId",sysUser.getId()+"");
        payload.put("username",sysUser.getUsername());
        payload.put("email",sysUser.getEmail());
        String token = TokenUtils.getToken(payload);
        map.put("state",true);
        map.put("msg","login successful!");
        map.put("token",token);
      }else {
        map.put("state",false);
        map.put("msg","wrong password！");
      }
    }catch (Exception e){
      map.put("state",false);
      map.put("msg","user not exist!");
    }
    return map;
  }


  @PostMapping("user/test")
  @ResponseBody
  public Map<String,Object> getWxCode(String token) throws UnsupportedEncodingException {
    Map<String,Object> map = new HashMap<>();
    try{
      DecodedJWT jwt = TokenUtils.verify(token);
      map.put("state",true);
      map.put("msg","request successful!");
      return map;
    }catch (SignatureVerificationException e){
      map.put("state",false);
      map.put("msg","SignatureVerificationException");
    }catch (TokenExpiredException e){
      map.put("state",false);
      map.put("msg","TokenExpiredException");
    }catch (AlgorithmMismatchException e){
      map.put("state",false);
      map.put("msg","AlgorithmMismatchException");
    }catch (Exception e){
      map.put("state",false);
      map.put("msg","Exception");
    }
    return map;
  }


  @RequestMapping(value = "register", method = RequestMethod.POST)
  @ResponseBody
  public String register(UserDto userDto){
    System.out.println(userDto);
    SysUser sysUser = null;
    try{
      sysUser = sysUserService.findSysUserByUsername(userDto.getUsername());
    }catch (Exception e){
      sysUser = new SysUser();
      sysUser.setUsername(userDto.getUsername());
      BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
      String tmp = passwordEncoder.encode(userDto.getPassword());
      sysUser.setPassword(tmp);
//      SysRole adminRole = sysRoleService.getSysRoleByName("ROLE_ADMIN");
//      SysRole userRole = sysRoleService.getSysRoleByName("ROLE_USER");
//      List<SysRole> roles = new ArrayList<>();
//      roles.add(adminRole);
//      roles.add(userRole);
//      sysUser.setSysRoles(roles);
      sysUserService.saveSysUser(sysUser);
    }
    return "register successful!";
  }

  private String getAuthorizeUrl(String baseAuthorizeUrl, String redirectUrl, String state) {
    return String.format(baseAuthorizeUrl, ConstantWxUtils.WX_OPEN_APP_ID, redirectUrl, state);
  }

  private String getEncodeRedirectUrl() throws UnsupportedEncodingException {
    // 对redirect_url进行URLEncoder编码
    String redirectUrl = ConstantWxUtils.WX_OPEN_REDIRECT_URL;
    redirectUrl = URLEncoder.encode(redirectUrl, "utf-8");
    return redirectUrl;
  }

  private String getBaseAuthorizeUrl() {
    return "https://open.weixin.qq.com/connect/oauth2/authorize"
        + "?appid=%s"
        + "&redirect_uri=%s"
        + "&response_type=code"
        + "&scope=snsapi_userinfo"
        + "&state=%s"
        + "#wechat_redirect";
  }

  @GetMapping("test")
  @ResponseBody
  public String test() {
    return "<h1>Hello World</h1>";
  }

  @GetMapping("callback")
  public String callback(@Param("code") String code, @Param("state") String state)
      throws Exception {
    RestTemplate restTemplate = new RestTemplate();
    try {
      /** 将code值作为请求参数，发起获取accsess_token和openid的请求 */
      String baseAccessTokenUrl = getBaseAccessTokenUrl();
      String accessTokenUrl =
          getAccessTokenUrl(
              baseAccessTokenUrl, ConstantWxUtils.WX_OPEN_APP_ID,
              ConstantWxUtils.WX_OPEN_APP_SECRET, code
          );
      ResponseEntity<String> accessTokenInfo =
          restTemplate.getForEntity(accessTokenUrl, String.class);
      System.out.println(accessTokenInfo.getHeaders()+"\n"+accessTokenInfo.getBody());
      HashMap accessTokenMap = getInfoMap(accessTokenInfo.getBody());
      String access_token = (String) accessTokenMap.get("access_token");
      String openid = (String) accessTokenMap.get("openid");
      /** 将accsess_token和openid作为参数，发起获取用户信息的请求 */
      String baseUserInfoUrl = getBaseUserInfoUrl();
      String userInfoUrl = getUserInfoUrl(baseUserInfoUrl, access_token, openid);
      ResponseEntity<String> userInfo = restTemplate.getForEntity(userInfoUrl, String.class);
      System.out.println(userInfo.getHeaders()+"\n"+userInfo.getBody());
      HashMap userInfoMap = getInfoMap(userInfo.getBody());
      String nickname = (String) userInfoMap.get("nickname");
      String headimgurl = (String) userInfoMap.get("headimgurl");
      /** 获取access_token，用于发起创建公众号自定义菜单请求，此处的access_token与上面的不一样 */
      String baseTokenUrl = getBaseTokenUrl();
      String tokenUrl =
          getTokenUrl(
              baseTokenUrl, ConstantWxUtils.WX_OPEN_APP_ID, ConstantWxUtils.WX_OPEN_APP_SECRET);
      ResponseEntity<String> tokenInfo = restTemplate.getForEntity(tokenUrl, String.class);
      System.out.println(tokenInfo.getHeaders()+"\n"+tokenInfo.getBody());
      HashMap tokenInfoMap = getInfoMap(tokenInfo.getBody());
      String token = (String) tokenInfoMap.get("access_token");
      /** 发起创建公众号自定义菜单请求 */
      String baseCreateButtonUrl = getBaseCreateButtonUrl();
      String createButtonUrl = String.format(baseCreateButtonUrl, token);
      String buttonJson = getButtonJson();
      ResponseEntity<String> createButtonInfo =
          restTemplate.postForEntity(createButtonUrl, buttonJson, String.class);
      System.out.println(createButtonInfo.getHeaders()+"\n"+createButtonInfo.getBody());

      /**
       * 可能要在这里生成token，并将token放入response中
       * 方法回调结束后的跳转地址 */
      return "redirect:http://www.baidu.com";
    } catch (Exception e) {
      throw new Exception("方法回调出现错误");
    }
  }

  private String getBaseUserInfoUrl() {
    return "https://api.weixin.qq.com/sns/userinfo" + "?access_token=%s" + "&openid=%s";
  }

  private String getUserInfoUrl(String baseUserInfoUrl, String accessToken, String openId) {
    return String.format(baseUserInfoUrl, accessToken, openId);
  }

  private String getButtonJson() {
    return "{\"button\": [{\"type\": \"view\", \"name\": \"OOCL\", \"key\": \"EMPLOAN_HTML_LOAN\",\"url\": \"http://2wcrat.natappfree.cc/api/user/wx/test\"}]}";
  }

  private String getBaseCreateButtonUrl() {
    return "https://api.weixin.qq.com/cgi-bin/menu/create" + "?access_token=%s";
  }

  private HashMap getInfoMap(String infoBody) {
    Gson gson = new Gson();
    HashMap infoMap = gson.fromJson(infoBody, HashMap.class);
    return infoMap;
  }

  private String getAccessTokenUrl(
      String baseAccessTokenUrl, String appId, String appSecret, String code) {
    return String.format(baseAccessTokenUrl, appId, appSecret, code);
  }

  private String getBaseAccessTokenUrl() {
    return "https://api.weixin.qq.com/sns/oauth2/access_token"
        + "?appid=%s"
        + "&secret=%s"
        + "&code=%s"
        + "&grant_type=authorization_code";
  }

  private String getTokenUrl(String baseTokenUrl, String wxOpenAppId, String wxOpenAppSecret) {
    return String.format(baseTokenUrl, wxOpenAppId, wxOpenAppSecret);
  }

  private String getBaseTokenUrl() {
    return "https://api.weixin.qq.com/cgi-bin/token"
        + "?grant_type=client_credential"
        + "&appid=%s"
        + "&secret=%s";
  }
}
