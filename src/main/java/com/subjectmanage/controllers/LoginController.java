package com.subjectmanage.controllers;

import com.subjectmanage.beans.Student;
import com.subjectmanage.beans.Teacher;
import com.subjectmanage.services.StudentServiceImpl;
import com.subjectmanage.services.TeacherServiceimpl;
import com.subjectmanage.utils.CheckNumber;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.imageio.ImageIO;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.*;

@Controller
@RequestMapping("/login")
public class LoginController {

    @Autowired
    private StudentServiceImpl studentService;

    @Autowired
    private TeacherServiceimpl teacherService;


    @RequestMapping("/toLogin")
    public String toLogin(){
        return "login";
    }


    @RequestMapping(value = "/getCheckNumber", produces = {"text/html;charset=UTF-8"})
    public void getCheckNumber(HttpServletResponse response, HttpSession session ) throws IOException {
        //创建对象
        CheckNumber checkNumber = new CheckNumber();
        //获取图片对象
        BufferedImage image = checkNumber.getImage();
        //获得图片的文本内容
        String text = checkNumber.getText();
        // 将系统生成的文本内容保存到session中
        session.setAttribute("checkNumber",text);
        //把图片返回前端
        ImageIO.write(image, "JPEG", response.getOutputStream());
    }

    @PostMapping(value = "/userLogin")
    public String Login(@RequestParam("user_num") String user_num, @RequestParam("password") String password,
                        @RequestParam("captcha") String captcha,
                        @RequestParam("user_type") String user_type,
                         boolean rememberMe,
                        Model model, HttpSession session,HttpServletRequest request,HttpServletResponse response){
        Student student = null;
        Teacher teacher = null;
        System.out.println(rememberMe);
        Object user = session.getAttribute("loginUser");
        if(user!=null){
            model.addAttribute("msg","你已经登陆过了！不要重复登陆");
            return "login";
        }

        String checkNumber = (String) session.getAttribute("checkNumber");
        if(checkNumber==null||!checkNumber.equals(captcha)) {
            model.addAttribute("msg", "验证码错误！请重新输入");
            return "login";
        }

        if(user_type.equals("学生")){
            student = studentService.selectLoginStudent(user_num,password);
        }else if(user_type.equals("老师")){
            teacher = teacherService.selectLoginTeacher(user_num,password);
        }

        //业务调用：如果验证正确，返回一个登陆的用户对象，并存入session
        if(student!=null){
            session.setAttribute("loginUser",student);
            session.setAttribute("user_type",user_type);
            rememberMe(rememberMe,request,response,student.getStudent_num(),student.getPassword());
            return "redirect:/index";
        }else if(teacher!=null){
            session.setAttribute("loginUser",teacher);
            session.setAttribute("user_type",user_type);
            rememberMe(rememberMe,request,response,teacher.getTeacher_num(),teacher.getPassword());
            return "redirect:/index";
        }
        else{
            model.addAttribute("msg","用户名或密码错误！请重新输入");
            return "login";
        }

    }

    public void rememberMe(boolean rememberMe,HttpServletRequest request,HttpServletResponse response,String username,String password){
        if (rememberMe){
            //记住用户
            Cookie nameCookie = new Cookie("name",username);
            Cookie pwdCookie = new Cookie("pwd",password);
            //3天
            nameCookie.setMaxAge(3*24*60*60);
            pwdCookie.setMaxAge(3*24*60*60);
            response.addCookie(nameCookie);
            response.addCookie(pwdCookie);
        }else{//清除cookie
            Cookie[] cookies = request.getCookies();
            for(Cookie cookie:cookies ){
                if (cookie.getName().equals("name")||cookie.getName().equals("pwd")) {
                    cookie.setValue(null);
                    cookie.setMaxAge(0);// 立即销毁cookie
                    System.out.println("被删除的cookie名字为:" + cookie.getName());
                    response.addCookie(cookie);
                }
            }
        }
    }

    @RequestMapping(value = "/initMenu")
    @ResponseBody
    public Map<String,Object> initMenu(HttpSession session, HttpServletRequest request){
        String user_type = (String)session.getAttribute("user_type");
        Map<String,Object> map=new HashMap<>();
        if(user_type!=null&&user_type.equals("学生")){
            Student loginUser =(Student) session.getAttribute("loginUser");
            Map<String,Object>homeInfo=new HashMap<>();
            homeInfo.put("title","首页");
            homeInfo.put("icon","fa fa-home");
            homeInfo.put("href","/toWelcome");
            map.put("homeInfo",homeInfo);
            Map<String,Object>logoInfo=new HashMap<>();
            logoInfo.put("title","综设管理系统");
            logoInfo.put("image","images/logo.jpg");
            logoInfo.put("href","/index");
            map.put("logoInfo",logoInfo);
            List<Map<String,Object>> menuInfo=new ArrayList<>();//保证菜单的顺序
            List<Map<String,Object>>childInfo=new ArrayList<>();



            Map<String,Object>aInfo=new HashMap<>();
            aInfo.put("title","个人信息修改");
            aInfo.put("icon","fa fa-home");
            aInfo.put("href","/userDetails");
            childInfo.add(aInfo);

            Map<String,Object>bInfo=new HashMap<>();
            bInfo.put("title","查看所有课题");
            bInfo.put("icon","fa fa-window-maximize");
            bInfo.put("href","/topic/toTopicList");
            childInfo.add(bInfo);

            Map<String,Object>dInfo=new HashMap<>();
            dInfo.put("title","查看已选课题");
            dInfo.put("icon","fa fa-window-maximize");
            dInfo.put("href","/topic/chosenTopic");
            childInfo.add(dInfo);

            Map<String,Object>eInfo=new HashMap<>();
            eInfo.put("title","查看课题成绩");
            eInfo.put("icon","fa fa-window-maximize");
            eInfo.put("href","/topic/topicScore");
            childInfo.add(eInfo);



            Map<String,Object>cInfo=new HashMap<>();
            cInfo.put("title","欢迎你");
            cInfo.put("icon","fa fa-address-book");
            cInfo.put("href","");
            cInfo.put("target","_self");
            cInfo.put("child",childInfo);

            menuInfo.add(cInfo);


            map.put("menuInfo",menuInfo);
            return map;
        }
        else if(user_type!=null&&user_type.equals("老师")){
            Teacher loginUser = (Teacher)session.getAttribute("loginUser");
            Map<String,Object>homeInfo=new HashMap<>();
            homeInfo.put("title","首页");
            homeInfo.put("icon","fa fa-home");
            homeInfo.put("href","/toWelcome");
            map.put("homeInfo",homeInfo);
            Map<String,Object>logoInfo=new HashMap<>();
            logoInfo.put("title","综设管理系统");
            logoInfo.put("image","images/logo.jpg");
            logoInfo.put("href","/index");
            map.put("logoInfo",logoInfo);

            List<Map<String,Object>> menuInfo=new ArrayList<>();//保证菜单的顺序
            List<Map<String,Object>>childInfo=new ArrayList<>();



            Map<String,Object>aInfo=new HashMap<>();
            aInfo.put("title","个人信息修改");
            aInfo.put("icon","fa fa-home");
            aInfo.put("href","/userDetails");
            childInfo.add(aInfo);

            Map<String,Object>bInfo=new HashMap<>();
            bInfo.put("title","查看所有课题");
            bInfo.put("icon","fa fa-window-maximize");
            bInfo.put("href","/topic/toTopicList");
            childInfo.add(bInfo);

            Map<String,Object>dInfo=new HashMap<>();
            dInfo.put("title","查看个人发布课题");
            dInfo.put("icon","fa fa-window-maximize");
            dInfo.put("href","/topic/teach/toTopicList");
            childInfo.add(dInfo);

            Map<String,Object>eInfo=new HashMap<>();
            eInfo.put("title","课题小组确认管理");
            eInfo.put("icon","fa fa-window-maximize");
            eInfo.put("href","/topic/teach/groupCheck");
            childInfo.add(eInfo);

            Map<String,Object>fInfo=new HashMap<>();
            fInfo.put("title","学生课题小组管理");
            fInfo.put("icon","fa fa-window-maximize");
            fInfo.put("href","/topic/teach/groupManage");
            childInfo.add(fInfo);


            Map<String,Object>cInfo=new HashMap<>();
            cInfo.put("title","欢迎你");
            cInfo.put("icon","fa fa-address-book");
            cInfo.put("href","");
            cInfo.put("target","_self");
            cInfo.put("child",childInfo);

            menuInfo.add(cInfo);


            map.put("menuInfo",menuInfo);
            return map;
        }


        return map;

    }
}
