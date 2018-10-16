package org.vaadin.scp.auth;

import com.google.gson.Gson;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.Page;
import com.vaadin.server.RequestHandler;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinResponse;
import com.vaadin.server.VaadinServletResponse;
import com.vaadin.server.VaadinSession;
import com.vaadin.spring.annotation.SpringComponent;
import com.vaadin.spring.annotation.UIScope;
import com.vaadin.ui.Link;
import com.vaadin.ui.themes.ValoTheme;
import java.io.IOException;
import org.scribe.builder.ServiceBuilder;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.model.Verifier;
import org.scribe.oauth.OAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.vaadin.scp.SessionService;
import org.vaadin.viritin.label.RichText;
import org.vaadin.viritin.layouts.MVerticalLayout;

/**
 *
 * @author Matti Tahvonen
 */
@SpringComponent
@UIScope
public class LoginView extends MVerticalLayout implements RequestHandler {

    @Autowired
    Environment environment;
    
    @Autowired
    SessionService sessionService;

    Link loginLink;
    private String gpluskey;
    private String gplussecret;

    private OAuthService service;
    private String redirectUrl;

    public LoginView() {
    }

    @Override
    public void attach() {
        super.attach();
        
        if(sessionService.getEmail() != null) {
            System.out.println("Login successfull, redirect to listing UI");
            return;
        }

        redirectUrl = Page.getCurrent().getLocation().toString();

        gpluskey = environment.getProperty("gpluskey");
        gplussecret = environment.getProperty("gplussecret");

        service = createService();
        String url = service.getAuthorizationUrl(null);

        loginLink = new Link("Login with Google", new ExternalResource(url));
        loginLink.addStyleName(ValoTheme.LINK_LARGE);

        setCaption("Login");
        add(new RichText().withMarkDownResource("/splash.md"),loginLink);

        VaadinSession.getCurrent().addRequestHandler(this);

    }

    private OAuthService createService() {
        ServiceBuilder sb = new ServiceBuilder();
        sb.provider(Google2Api.class);
        sb.apiKey(gpluskey);
        sb.apiSecret(gplussecret);
        sb.scope("email");
        String callBackUrl = Page.getCurrent().getLocation().toString();
        if (callBackUrl.contains("#")) {
            callBackUrl = callBackUrl.substring(0, callBackUrl.indexOf("#"));
        }
        sb.callback(callBackUrl);
        return sb.build();
    }

    @Override
    public boolean handleRequest(VaadinSession session, VaadinRequest request,
            VaadinResponse response) throws IOException {
        if (request.getParameter("code") != null) {
            String code = request.getParameter("code");
            Verifier v = new Verifier(code);
            Token t = service.getAccessToken(null, v);

            OAuthRequest r = new OAuthRequest(Verb.GET,
                    "https://www.googleapis.com/plus/v1/people/me");
            service.signRequest(t, r);
            Response resp = r.send();
            final String body = resp.getBody();

            System.err.println(body);
            GooglePlusAnswer answer = new Gson().fromJson(body,
                    GooglePlusAnswer.class);
            
            System.err.println(answer.toString());

            setUser(answer.emails[0].value, answer.displayName);

            VaadinSession.getCurrent().removeRequestHandler(this);

            ((VaadinServletResponse) response).getHttpServletResponse().
                    sendRedirect(redirectUrl);
            return true;
        }

        return false;
    }

    public void setUser(String email, String displayName) {
        sessionService.setEmail(email);
    }

}
