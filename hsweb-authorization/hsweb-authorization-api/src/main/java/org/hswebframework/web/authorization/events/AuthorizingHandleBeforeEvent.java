package org.hswebframework.web.authorization.events;

import org.hswebframework.web.authorization.define.AuthorizingContext;
import org.hswebframework.web.authorization.define.HandleType;
import org.springframework.context.ApplicationEvent;

/**
 * 权限控制事件,在进行权限控制之前会推送此事件，用于自定义权限控制结果:
 * <pre>{@code
 *   @EventListener
 *   public void handleAuthEvent(AuthorizingHandleBeforeEvent e) {
 *      //admin用户可以访问全部操作
 *      if ("admin".equals(e.getContext().getAuthentication().getUser().getUsername())) {
 *         e.setAllow(true);
 *       }
 *    }
 * }</pre>
 *
 * @author zhouhao
 * @since 4.0
 */
// TODO: 2021/12/21 Reactive支持
public class AuthorizingHandleBeforeEvent extends ApplicationEvent implements AuthorizationEvent {

    private static final long serialVersionUID = -1095765748533721998L;

    private boolean allow = false;

    private boolean execute = true;

    private String message;

    private final HandleType handleType;

    public AuthorizingHandleBeforeEvent(AuthorizingContext context, HandleType handleType) {
        super(context);
        this.handleType = handleType;
    }

    public AuthorizingContext getContext() {
        return ((AuthorizingContext) getSource());
    }

    public boolean isExecute() {
        return execute;
    }

    public boolean isAllow() {
        return allow;
    }

    /**
     * 设置通过当前请求
     *
     * @param allow allow
     */
    public void setAllow(boolean allow) {
        execute = false;
        this.allow = allow;
    }

    public String getMessage() {
        return message;
    }

    /**
     * 设置错误提示消息
     *
     * @param message 消息
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * @return 权限控制类型
     */
    public HandleType getHandleType() {
        return handleType;
    }
}
