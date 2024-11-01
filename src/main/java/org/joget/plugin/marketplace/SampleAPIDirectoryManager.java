package org.joget.plugin.marketplace;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.workflow.security.WorkflowUserDetails;
import org.joget.commons.util.LogUtil;
import org.joget.commons.util.ResourceBundleUtil;
import org.joget.directory.dao.RoleDao;
import org.joget.directory.dao.UserDao;
import org.joget.directory.ext.DirectoryManagerAuthenticatorImpl;
import org.joget.directory.model.Role;
import org.joget.directory.model.User;
import org.joget.directory.model.service.DirectoryManager;
import org.joget.directory.model.service.DirectoryManagerAuthenticator;
import org.joget.directory.model.service.DirectoryManagerProxyImpl;
import org.joget.directory.model.service.UserSecurityFactory;
import org.joget.plugin.base.PluginManager;
import org.joget.plugin.directory.SecureDirectoryManager;
import org.joget.plugin.directory.SecureDirectoryManagerImpl;
import org.joget.workflow.model.dao.WorkflowHelper;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import org.json.JSONObject;

public class SampleAPIDirectoryManager extends SecureDirectoryManager {

    public SecureDirectoryManagerImpl dirManager;

    @Override
    public String getName() {
        return "Sample API Directory Manager";
    }

    @Override
    public String getDescription() {
        return "Sample Directory Manager with authentication to external API";
    }

    @Override
    public String getVersion() {
        return "8.0.0";
    }

    @Override
    public DirectoryManager getDirectoryManagerImpl(Map properties) {
        if (dirManager == null) {
            dirManager = new ExtSecureDirectoryManagerImpl(properties);
        } else {
            dirManager.setProperties(properties);
        }

        return dirManager;
    }

    @Override
    public String getPropertyOptions() {
        UserSecurityFactory f = (UserSecurityFactory) new SecureDirectoryManagerImpl(null);
        String usJson = f.getUserSecurity().getPropertyOptions();
        usJson = usJson.replaceAll("\\n", "\\\\n");

        String addOnJson = "";
        if (SecureDirectoryManagerImpl.NUM_OF_DM > 1) {
            for (int i = 2; i <= SecureDirectoryManagerImpl.NUM_OF_DM; i++) {
                addOnJson += ",{\nname : 'dm" + i + "',\n label : '@@app.edm.label.addon@@',\n type : 'elementselect',\n";
                addOnJson += "options_ajax : '[CONTEXT_PATH]/web/json/plugin/org.joget.plugin.directory.SecureDirectoryManager/service',\n";
                addOnJson += "url : '[CONTEXT_PATH]/web/property/json/getPropertyOptions'\n}";
            }
        }

        String json = AppUtil.readPluginResource(getClass().getName(), "/properties/app/SampleAPIDirectoryManager.json", new String[]{usJson, addOnJson}, true, "messages/open-id-authentication");
        return json;
    }

    @Override
    public String getLabel() {
        return "Sample API Directory Manager";
    }

    @Override
    public String getClassName() {
        return getClass().getName();
    }

    @Override
    public void webService(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String errorUrl = request.getContextPath() + "/web/login?login_error=1";
        String action = request.getParameter("action");
        try {
            if ("dmOptions".equals(action)) {
                super.webService(request, response);
            } else if (request.getParameter("sampleUsername") != null && request.getParameter("sampleUsername").length() > 0) {
                String username = request.getParameter("sampleUsername");
                String password = request.getParameter("samplePassword");
                
                if(authenticateExternal(username, password)){
                    doLogin(username, request, response);
                } else {
                    LogUtil.info(SampleAPIDirectoryManager.class.getName(), "User [" + username + "] authentication : false");
                    request.getSession().setAttribute("SPRING_SECURITY_LAST_EXCEPTION", new Exception(ResourceBundleUtil.getMessage("AbstractUserDetailsAuthenticationProvider.badCredentials")));
                    response.sendRedirect(errorUrl);
                }
            }
        } catch (Exception ex) {
            LogUtil.error(SampleAPIDirectoryManager.class.getName(), ex, "Error");
            request.getSession().setAttribute("SPRING_SECURITY_LAST_EXCEPTION", "Error");
            response.sendRedirect(errorUrl);
        }
    }
    
    private boolean authenticateExternal(String username, String password){
        boolean result = false;
        
        LogUtil.info(SampleAPIDirectoryManager.class.getName(), "username: "+ username);
        LogUtil.info(SampleAPIDirectoryManager.class.getName(), "password: "+ password);
        
        //sample mock call to external API
        //please modify these codes
        String loginUrl = "https://jogettestapi.free.beeceptor.com/loginUser";
        
        try{
            URL apiURL = new URL(loginUrl);
            HttpURLConnection conn = (HttpURLConnection) apiURL.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setDoOutput(true);

            String params = "userName=" + username +
                    "&password=" + password;

            try (OutputStream os = conn.getOutputStream()) {
                os.write(params.getBytes());
                os.flush();
            }

            //please modify the logic to determine if external API returns "authenticated" or not
            int responseCode = conn.getResponseCode();
            
            if (responseCode == HttpURLConnection.HTTP_OK) {
                
                String content = "";
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String line;
                while ((line = in.readLine()) != null) {
                    content += line;
                    LogUtil.info(getClass().getName(),line);
                }
                
                JSONObject contentObj = new JSONObject(content);
                
                // Check if the response indicates successful authentication
                if (contentObj.has("authenticated") && contentObj.getBoolean("authenticated")) {
                    LogUtil.info(getClass().getName(), "External API returns true");
                    return true;
                } else {
                    LogUtil.info(getClass().getName(), "External API returns false");
                    return false;
                }
            }else{
                LogUtil.info(getClass().getName(), conn.getResponseMessage());
            }
            
        }catch(Exception ex){
            LogUtil.error(SampleAPIDirectoryManager.class.getName(), ex, "External API Call Failed");
        }
        
        return result;
    }
    
    private void doLogin(String username, HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            // read from properties
            DirectoryManagerProxyImpl dm = (DirectoryManagerProxyImpl) AppUtil.getApplicationContext().getBean("directoryManager");
            SecureDirectoryManagerImpl dmImpl = (SecureDirectoryManagerImpl) dm.getDirectoryManagerImpl();

            //String certificate = dmImpl.getPropertyString("certificate");
            boolean userProvisioningEnabled = Boolean.parseBoolean(dmImpl.getPropertyString("userProvisioning"));

            // get user
            User user = dmImpl.getUserByUsername(username);
            if (user == null && userProvisioningEnabled) {
                // user does not exist, provision
                LogUtil.info(getClass().getName(), "User does not exist, user provisioning enabled, creating user");
                
                user = new User();
                user.setId(username);
                user.setUsername(username);
                user.setTimeZone("0");
                user.setActive(1);
//                if (userInfo.getEmailAddress() != null && !userInfo.getEmailAddress().isEmpty()) {
//                    user.setEmail(userInfo.getEmailAddress());
//                }
//
//                if (userInfo.getGivenName() != null && !userInfo.getGivenName().isEmpty()) {
//                    user.setFirstName(userInfo.getGivenName());
//                }
//
//                if (userInfo.getFamilyName() != null && !userInfo.getFamilyName().isEmpty()) {
//                    user.setLastName(userInfo.getFamilyName());
//                }
//
//                if (userInfo.getLocale() != null && !userInfo.getLocale().isEmpty()) {
//                    user.setLocale(userInfo.getLocale());
//                }

                // set role
                RoleDao roleDao = (RoleDao) AppUtil.getApplicationContext().getBean("roleDao");
                Set roleSet = new HashSet();
                Role r = roleDao.getRole("ROLE_USER");
                if (r != null) {
                    roleSet.add(r);
                }
                user.setRoles(roleSet);
                // add user
                UserDao userDao = (UserDao) AppUtil.getApplicationContext().getBean("userDao");
                userDao.addUser(user);
            } else if (user == null && !userProvisioningEnabled) {
                LogUtil.info(getClass().getName(), "User does not exist, user provisioning disabled, deny login");
                request.getSession().setAttribute("SPRING_SECURITY_LAST_EXCEPTION", new Exception(ResourceBundleUtil.getMessage("AbstractUserDetailsAuthenticationProvider.badCredentials")));
                response.sendRedirect(request.getContextPath() + "/web/login?login_error=1");
                return;
            }


            PluginManager pluginManager = (PluginManager) AppUtil.getApplicationContext().getBean("pluginManager");
            DirectoryManagerAuthenticator authenticator = (DirectoryManagerAuthenticator) pluginManager.getPlugin(DirectoryManagerAuthenticatorImpl.class.getName());
            DirectoryManager wrapper = new DirectoryManagerWrapper(dmImpl, true);
            if (user != null) {
                authenticator.authenticate(wrapper, user.getUsername(), user.getPassword());
            }
            // get authorities
            Collection<Role> roles = dm.getUserRoles(username);
            List<GrantedAuthority> gaList = new ArrayList<>();
            if (roles != null && !roles.isEmpty()) {
                for (Role role : roles) {
                    GrantedAuthority ga = new SimpleGrantedAuthority(role.getId());
                    gaList.add(ga);
                }
            }

            // login user
            UserDetails details = new WorkflowUserDetails(user);
            UsernamePasswordAuthenticationToken result = new UsernamePasswordAuthenticationToken(username, "", gaList);
            result.setDetails(details);
            SecurityContextHolder.getContext().setAuthentication(result);

            // add audit trail
            WorkflowHelper workflowHelper = (WorkflowHelper) AppUtil.getApplicationContext().getBean("workflowHelper");
            workflowHelper.addAuditTrail(this.getClass().getName(), "authenticate", "Authentication for user [" + username + "] : " + true);

            // redirect
            String relayState = request.getParameter("RelayState");
            if (relayState != null && !relayState.isEmpty()) {
                response.sendRedirect(relayState);
            } else {
                SavedRequest savedRequest = new HttpSessionRequestCache().getRequest(request, response);
                String savedUrl = "";
                if (savedRequest != null) {
                    savedUrl = savedRequest.getRedirectUrl();
                } else {
                    savedUrl = request.getContextPath();
                }
                response.sendRedirect(savedUrl);
            }
        } catch (IOException | RuntimeException ex) {
            LogUtil.error(getClass().getName(), ex, "Error in Sample API Directory login");
            request.getSession().setAttribute("SPRING_SECURITY_LAST_EXCEPTION", new Exception(ResourceBundleUtil.getMessage("AbstractUserDetailsAuthenticationProvider.badCredentials")));
            String url = request.getContextPath() + "/web/login?login_error=1";
            response.sendRedirect(url);
        }
    }
}
