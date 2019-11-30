package com.malong.community.controller;

import com.malong.community.dto.AccesstakenDTO;
import com.malong.community.dto.GithupUser;
import com.malong.community.mapper.UserMapper;
import com.malong.community.model.User;
import com.malong.community.provider.GithupProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

@Controller
public class AuthorizeController {

    @Autowired
    private GithupProvider githupProvider;

    @Value("${githup.client.id}")
    private String clientId;

    @Value("${githup.client.secret}")
    private String clientSecret;

    @Value("${githup.redirect.uri}")
    private String redirectUri;

    @Autowired
    private UserMapper userMapper;

    @GetMapping("/callback")
    public String callbook(@RequestParam(name="code") String code,
                           @RequestParam(name="state") String state,
                           HttpServletResponse response){
        AccesstakenDTO accesstakenDTO = new AccesstakenDTO();
        accesstakenDTO.setCode(code);
        accesstakenDTO.setRedirect_uri(redirectUri);
        accesstakenDTO.setState(state);
        accesstakenDTO.setClient_id(clientId);
        accesstakenDTO.setClient_secret(clientSecret);
        String accessToken = githupProvider.getAccesstoken(accesstakenDTO);
        GithupUser githupUser = githupProvider.getUser(accessToken);
        if(githupUser != null && githupUser.getId() != null){
            //登录成功，写session和cookie
            User user = new User();
            String token = UUID.randomUUID().toString();
            user.setAvatarUrl(githupUser.getAvatar_url());
            user.setToken(token);
            user.setName(githupUser.getName());
            user.setAccountId(String.valueOf(githupUser.getId()));
            user.setGmtCreate(System.currentTimeMillis());
            user.setGmtModified(user.getGmtCreate());
            userMapper.insert(user);
            response.addCookie(new Cookie("token",token));
            return "redirect:/";
        }
        else{
            //登录失败，重写登录
            return "redirect:/";
        }
    }

}
