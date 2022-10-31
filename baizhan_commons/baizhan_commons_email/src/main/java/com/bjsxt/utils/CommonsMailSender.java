package com.bjsxt.utils;

import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class CommonsMailSender {
    // Java Mail发送邮件的类
    @Autowired
    private JavaMailSender javaMailSender;
    // Freemarket配置类，可以把Freemarker模板转换为String字符串
    @Autowired
    private FreeMarkerConfigurer freeMarkerConfigurer;
    // 从配置文件中取到发送方。
    @Value("${spring.mail.username}")
    private String from;

    /**
     *
     * @param to 向谁发邮件
     * @param subject 主题
     * @param content 内容，内容中为订单主键
     * @throws MessagingException
     * @throws IOException
     * @throws TemplateException
     */
    public void send(String to,String subject,String content) throws MessagingException, IOException, TemplateException {
        // MimeMessage Java Mail中封装的实体类。包含了邮件的全部信息
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        // 由于MimeMessage提供的API不是很方便，解决MimeMessageHelper，更容易的操作。
        // 操作helper时实际上就是在操作MimeMessage
        // 参数1：实际实体对象 ，参数2：是否有附件 参数3：编码方式
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage,false,"utf-8");
        // 设置接收方
        helper.setTo(to);
        // 设置发送方法
        helper.setFrom(from);
        // 设置主题
        helper.setSubject(subject);
        // 获取模板，把模板内容解析后转换为字符串
        Template template = freeMarkerConfigurer.getConfiguration().getTemplate("order.ftl");
        // 创建Map，根据Map的key设置Freemarket中内容
        Map<String,Object> model = new HashMap<>();
        model.put("orderId",content);
        // 把模板转换为String字符串
        String freemarkString = FreeMarkerTemplateUtils.processTemplateIntoString(template, model);
        // Java Mail中发送内容时支持String 参数
        // 参数1：发送的内容， 参数2：是否解析HTML
        helper.setText(freemarkString,true);
        javaMailSender.send(mimeMessage);
    }
}
