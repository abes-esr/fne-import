package fr.fne.batch.services.test;


import com.github.scribejava.core.builder.api.DefaultApi20;
import com.github.scribejava.core.oauth2.clientauthentication.ClientAuthentication;
import com.github.scribejava.core.oauth2.clientauthentication.RequestBodyAuthenticationScheme;

public class MediaWiki20API extends DefaultApi20 {

    protected MediaWiki20API() {
    }

    private static class InstanceHolder {
        private static final MediaWiki20API INSTANCE = new MediaWiki20API();
    }

    public static MediaWiki20API instance() {
        return InstanceHolder.INSTANCE;
    }

    @Override
    public String getAccessTokenEndpoint() {
        return "http://fagonie-dev.v102.abes.fr:8181/w/rest.php/oauth2/access_token";
    }

    @Override
    protected String getAuthorizationBaseUrl() {
        return "http://fagonie-dev.v102.abes.fr:8181/w/rest.php/oauth2/authorize";
    }

    @Override
    public ClientAuthentication getClientAuthentication() {
        return RequestBodyAuthenticationScheme.instance();
    }
}
