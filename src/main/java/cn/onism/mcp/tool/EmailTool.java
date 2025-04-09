package cn.onism.mcp.tool;

import cn.onism.mcp.constants.CodeEnum;
import cn.onism.mcp.exception.CustomException;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.data.MutableDataSet;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.regex.Pattern;

/**
 * 邮件工具
 *
 * @author Onism
 * @date 2025-03-27
 */
@Slf4j
@Component
public class EmailTool {

    private final JavaMailSender mailSender;

    private final String sendMailer;

    @Autowired
    public EmailTool(JavaMailSender mailSender, @Value("${spring.mail.username}") String sendMailer) {
        this.mailSender = mailSender;
        this.sendMailer = sendMailer;
    }

    /**
     * 判断邮箱是否合法
     */
    public static void isValidEmail(String email) {
        if (StringUtils.isBlank(email)) {
            log.error("邮箱为空！");
            throw new CustomException(CodeEnum.EMAIL_EMPTY);
        }
        if (!Pattern.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$", email)) {
            log.error("邮箱格式不合法！");
            throw new CustomException(CodeEnum.EMAIL_FORMAT_ERROR);
        }
    }

    /**
     * 发送邮件
     */
    @Async
    @Tool(description = "给指定邮箱发送邮件消息，email 为收件人邮箱，subject 为邮件标题，message 为邮件的内容")
    public void sendMailMessage(EmailRequest request) {
        // 校验邮箱是否合法
        isValidEmail(request.getEmail());
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true); // true 表示支持附件
            // 发件人邮箱
            helper.setFrom(sendMailer);
            // 收件人邮箱
            helper.setTo(request.getEmail());
            // 邮件标题
            helper.setSubject(request.getSubject());
            // 邮件正文
            helper.setText(convertToHtml(request.getMessage()));

            mailSender.send(mimeMessage);
        } catch (MailException | MessagingException e) {
            log.error("邮箱发送失败，报错：{}", e.getMessage());
            throw new CustomException(CodeEnum.EMAIL_SEND_ERROR);
        }
    }

    /**
     * 转换为 HTML
     *
     * @param markdown Markdown
     * @return {@link String }
     */
    public static String convertToHtml(String markdown) {
        MutableDataSet options = new MutableDataSet();
        Parser parser = Parser.builder(options).build();
        HtmlRenderer renderer = HtmlRenderer.builder(options).build();
        return renderer.render(parser.parse(markdown));
    }

    @Setter
    @Getter
    public static class EmailRequest implements Serializable {
        /**
         * 收件人邮件
         */
        @ToolParam(description = "收件人邮箱")
        private String email;

        /**
         * 主题
         */
        @ToolParam(description = "发送邮件的标题/主题")
        private String subject;

        /**
         * 消息
         */
        @ToolParam(description = "发送邮件的正文消息内容")
        private String message;

    }
}
