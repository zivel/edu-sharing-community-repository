package org.edu_sharing.restservices.ltiplatform.v13;

import com.google.gson.Gson;
import io.jsonwebtoken.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionHistory;
import org.alfresco.service.namespace.QName;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.log4j.Logger;
import org.edu_sharing.alfresco.tools.EduSharingNodeHelper;
import org.edu_sharing.alfrescocontext.gate.AlfAppContextGate;
import org.edu_sharing.repository.client.tools.CCConstants;
import org.edu_sharing.repository.client.tools.UrlTool;
import org.edu_sharing.repository.server.tools.ApplicationInfo;
import org.edu_sharing.repository.server.tools.ApplicationInfoList;
import org.edu_sharing.repository.server.tools.forms.DuplicateFinder;
import org.edu_sharing.repository.server.tools.security.AllSessions;
import org.edu_sharing.repository.server.tools.security.Signing;
import org.edu_sharing.restservices.*;
import org.edu_sharing.restservices.lti.v13.ApiTool;
import org.edu_sharing.restservices.ltiplatform.v13.model.*;
import org.edu_sharing.restservices.node.v1.model.NodeEntry;
import org.edu_sharing.restservices.shared.ErrorResponse;
import org.edu_sharing.service.lti13.LTIConstants;
import org.edu_sharing.service.lti13.LTIJWTUtil;
import org.edu_sharing.service.lti13.registration.RegistrationService;
import org.edu_sharing.service.nodeservice.NodeServiceFactory;
import org.edu_sharing.service.version.VersionService;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.jetbrains.annotations.NotNull;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.extensions.surf.util.I18NUtil;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.io.DataInputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.util.*;

@Path("/ltiplatform/v13")
@Consumes({ "text/html" })
@Produces({"text/html"})
@Tag(name="LTI Platform v13")
public class LTIPlatformApi {

    Logger logger = Logger.getLogger(LTIPlatformApi.class);

    ServiceRegistry serviceRegistry = (ServiceRegistry)AlfAppContextGate.getApplicationContext().getBean(ServiceRegistry.SERVICE_REGISTRY);
    NodeService nodeService = serviceRegistry.getNodeService();
    PersonService personService = serviceRegistry.getPersonService();


    @GET
    @Path("/auth")
    @Operation(summary = "LTI Platform oidc endpoint. responds to a login authentication request")
    @Consumes({ "text/html" })
    @Produces({"text/html"})
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode="200", description= RestConstants.HTTP_200, content = @Content(mediaType = "text/html", schema = @Schema(implementation = String.class))),
                    @ApiResponse(responseCode="400", description=RestConstants.HTTP_400, content = @Content(mediaType = "text/html", schema = @Schema(implementation = String.class))),
                    @ApiResponse(responseCode="401", description=RestConstants.HTTP_401, content = @Content(mediaType = "text/html", schema = @Schema(implementation = String.class))),
                    @ApiResponse(responseCode="403", description=RestConstants.HTTP_403, content = @Content(mediaType = "text/html", schema = @Schema(implementation = String.class))),
                    @ApiResponse(responseCode="404", description=RestConstants.HTTP_404, content = @Content(mediaType = "text/html", schema = @Schema(implementation = String.class))),
                    @ApiResponse(responseCode="500", description=RestConstants.HTTP_500, content = @Content(mediaType = "text/html", schema = @Schema(implementation = String.class)))
            })

    public Response auth(
            @Parameter(description = "scope",required=true) @QueryParam("scope") String scope,
            @Parameter(description = "response_type",required=true) @QueryParam("response_type") String responseType,
            @Parameter(description = "optional parameter client_id specifies the client id for the authorization server that should be used to authorize the subsequent LTI message request",required=false) @QueryParam("client_id") String clientId,
            @Parameter(description = "login_hint",required=true) @QueryParam("login_hint") String loginHint,
            @Parameter(description = "state",required=true) @QueryParam("state") String state,
            @Parameter(description = "response_mode",required=true) @QueryParam("response_mode") String responseMode,
            @Parameter(description = "nonce",required=true) @QueryParam("nonce") String nonce,
            @Parameter(description = "prompt",required=true) @QueryParam("prompt") String prompt,
            @Parameter(description = "Similarly to the login_hint parameter, lti_message_hint value is opaque to the tool. If present in the login initiation request, the tool MUST include it back in the authentication request unaltered",required=false) @QueryParam("lti_message_hint") String ltiMessageHint,
            @Parameter(description = "redirect_uri",required=true) @QueryParam("redirect_uri") String redirect_uri,
            @Context HttpServletRequest req){

        try {
            /**
             * validation
             */

            if (isEmpty(scope)) throw new Exception("missing param scope");
            if (isEmpty(responseType)) throw new Exception("missing param response_type");
            if (isEmpty(loginHint)) throw new Exception("missing param login_hint");
            if (isEmpty(responseMode)) throw new Exception("missing param response_mode");
            if (isEmpty(nonce)) throw new Exception("missing param nonce");
            if (isEmpty(prompt)) throw new Exception("missing param prompt");
            if (isEmpty(redirect_uri)) throw new Exception("missing param redirect_uri");
            if (isEmpty(ltiMessageHint)) throw new Exception("missing param lti_message_hint");

            if(!scope.equals("openid")) throw new Exception("invalid scope " +scope);
            if(!responseType.equals("id_token")) throw new Exception("unsupported response_type "+responseType);
            if(!responseMode.equals("form_post")){throw new Exception("invalid response_mode " +responseMode);}

            String username = AuthenticationUtil.getFullyAuthenticatedUser();

            if(!username.equals(loginHint)){
                throw new Exception("wrong login_hint. does not match session login");
            }


            Map<String,LoginInitiationSessionObject> loginInitiationSessionObjectMap = (Map<String,LoginInitiationSessionObject>)req.
                    getSession().
                    getAttribute(LTIPlatformConstants.LOGIN_INITIATIONS_SESSIONOBJECT);

            if(loginInitiationSessionObjectMap == null){
                throw new Exception(LTIPlatformConstants.ERROR_MISSING_SESSIONOBJECTS);
            }


            LoginInitiationSessionObject loginInitiationSessionObject = loginInitiationSessionObjectMap.get(ltiMessageHint);

            if(loginInitiationSessionObject == null){
                throw new Exception(LTIPlatformConstants.ERROR_MISSING_SESSIONOBJECT+" "+ltiMessageHint);
            }

            /**
             * we use the tool nonce for resolving the LoginInitiationSessionObject later when no lti_message_hint is available anymore
             * TODO check find better solution
             */
            loginInitiationSessionObject.setToolNonce(nonce);
            //we have to reset the session object cause of redisson cache management
            req.getSession().setAttribute(LTIPlatformConstants.LOGIN_INITIATIONS_SESSIONOBJECT, loginInitiationSessionObjectMap);


            ApplicationInfo appInfo = ApplicationInfoList.getRepositoryInfoById(loginInitiationSessionObject.getAppId());
            if(appInfo == null){
                throw new Exception("invalid request: application");
            }

            if(!appInfo.getLtiClientId().equals(clientId)){
                throw new Exception("unauthorized_client");
            }

            if(!Arrays.asList(appInfo.getLtitoolRedirectUrls().split(",")).contains(redirect_uri)){
                throw new Exception("invalid request: redirect_url");
            }

            if(responseMode == null || !responseMode.equals("form_post")){
                throw new Exception("invalid request: response_mode");
            }

            /**
             * build LTI Message
             */

            ApplicationInfo homeApp = ApplicationInfoList.getHomeRepository();


            Map<String,Object> context = null;
            NodeRef contextNodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, loginInitiationSessionObject.getContextId());
            try {
                context = new HashMap<>();
                context.put("id", loginInitiationSessionObject.getContextId());
                context.put("label", nodeService
                        .getProperty(contextNodeRef, ContentModel.PROP_NAME));
            }catch (AccessDeniedException e){
                // user has no permission on context node ( parent folder)
            }


            Map<String,Object> launchPresentation = new HashMap<>();
            launchPresentation.put("locale", I18NUtil.getLocale());

            JwtBuilder jwtBuilder = getBasicLtiMessage(clientId, nonce, username, appInfo, homeApp, context);

            if(LoginInitiationSessionObject.MessageType.deeplink.equals(loginInitiationSessionObject.getMessageType())){
                Map<String,Object> deepLinkingSettings = new HashMap<>();
                deepLinkingSettings.put("accept_types",Arrays.asList(new String[]{"ltiResourceLink"}));
                deepLinkingSettings.put("accept_presentation_document_targets",Arrays.asList(new String[]{"iframe","window"}));
                deepLinkingSettings.put("accept_copy_advice",false);
                deepLinkingSettings.put("accept_multiple",true);
                deepLinkingSettings.put("accept_unsigned",false);
                deepLinkingSettings.put("auto_create",false);
                deepLinkingSettings.put("can_confirm",false);
                deepLinkingSettings.put("deep_link_return_url",homeApp.getClientBaseUrl()+"/rest/ltiplatform/v13/deeplinking-response/");
                deepLinkingSettings.put("title",homeApp.getAppCaption());

                jwtBuilder = jwtBuilder
                        .claim(LTIConstants.LTI_TARGET_LINK_URI,appInfo.getLtitoolTargetLinkUri())
                        .claim(LTIConstants.LTI_LAUNCH_PRESENTATION, launchPresentation)
                        .claim(LTIConstants.DEEP_LINKING_SETTINGS,deepLinkingSettings)
                        .claim(LTIConstants.LTI_MESSAGE_TYPE, LTIConstants.LTI_MESSAGE_TYPE_DEEP_LINKING);

            }else if(LoginInitiationSessionObject.MessageType.resourcelink.equals(loginInitiationSessionObject.getMessageType())){
                NodeRef nodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,loginInitiationSessionObject.getResourceLinkNodeId());
                Map<String,String> claimResourceLink = new HashMap<>();
                claimResourceLink.put("title",(String)nodeService.getProperty(nodeRef,ContentModel.PROP_NAME));
                claimResourceLink.put("id",loginInitiationSessionObject.getResourceLinkNodeId());

                String presentation = (loginInitiationSessionObject.getLaunchPresentation() != null)
                        ? loginInitiationSessionObject.getLaunchPresentation()
                        : "window";
                launchPresentation.put("document_target",presentation);
                launchPresentation.put("return_url",homeApp.getClientBaseUrl()+"/components/workspace?id=" + loginInitiationSessionObject.getContextId() + "&mainnav=true&displayType=0");


                String resourceLink = (String)nodeService.getProperty(nodeRef, QName.createQName(CCConstants.CCM_PROP_LTITOOL_NODE_RESOURCELINK));
                jwtBuilder = jwtBuilder
                        .claim(LTIConstants.LTI_TARGET_LINK_URI, resourceLink)
                        .claim(LTIConstants.LTI_CLAIM_RESOURCE_LINK,claimResourceLink)
                        .claim(LTIConstants.LTI_LAUNCH_PRESENTATION, launchPresentation)
                        .claim(LTIConstants.LTI_MESSAGE_TYPE, LTIConstants.LTI_MESSAGE_TYPE_RESOURCE_LINK);

            }else{
                throw new Exception("unknown lti messagetype:" +loginInitiationSessionObject.getMessageType());
            }


            if(appInfo.hasLtiToolCustomContentOption() && loginInitiationSessionObject.getContentUrlNodeId() != null){
                AccessStatus accessStatus = serviceRegistry.getPermissionService()
                        .hasPermission(new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,loginInitiationSessionObject.getContentUrlNodeId()),
                                PermissionService.WRITE_CONTENT);
                Map<String,String> custom = new HashMap<>();
                custom.put(LTIPlatformConstants.CUSTOM_CLAIM_APP_ID,appInfo.getAppId());
                custom.put(LTIPlatformConstants.CUSTOM_CLAIM_NODEID,loginInitiationSessionObject.getContentUrlNodeId());
                custom.put(LTIPlatformConstants.CUSTOM_CLAIM_USER,username);
                if(loginInitiationSessionObject.getVersion() != null){
                    custom.put(LTIPlatformConstants.CUSTOM_CLAIM_VERSION,loginInitiationSessionObject.getVersion());
                }
                if(loginInitiationSessionObject.getToken() != null){
                    custom.put(LTIPlatformConstants.CUSTOM_CLAIM_TOKEN, loginInitiationSessionObject.getToken());
                }
                if(loginInitiationSessionObject.getContentUrlNodeId() != null) {
                    custom.put(LTIPlatformConstants.CUSTOM_CLAIM_FILENAME, (String)nodeService.getProperty(new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
                            loginInitiationSessionObject.getContentUrlNodeId()),
                            ContentModel.PROP_NAME));
                }
                custom.put(LTIPlatformConstants.CUSTOM_CLAIM_GET_CONTENTAPIURL,homeApp.getClientBaseUrl()+"/rest/ltiplatform/v13/content");
                custom.put(LTIPlatformConstants.CUSTOM_CLAIM_DETAILSSNIPPETURL,homeApp.getClientBaseUrl()+"/rest/lti/v13/details");
                if(accessStatus != null && accessStatus.equals(AccessStatus.ALLOWED)
                        && loginInitiationSessionObject.isResourceLinkEditMode()
                        && (loginInitiationSessionObject.getVersion() == null || "".equals(loginInitiationSessionObject.getVersion()))){
                    custom.put(LTIPlatformConstants.CUSTOM_CLAIM_POST_CONTENTAPIURL,homeApp.getClientBaseUrl()+"/rest/ltiplatform/v13/content");
                }else{
                    logger.info("user "+username +" has no writeContent Permissions");
                }
                jwtBuilder = jwtBuilder.claim(LTIConstants.LTI_CLAIM_CUSTOM,custom);
            }


            Key platformPrivateKey = new Signing().getPemPrivateKey(homeApp.getPrivateKey(), CCConstants.SECURITY_KEY_ALGORITHM);
            String jwt = jwtBuilder.signWith(platformPrivateKey,SignatureAlgorithm.RS256).compact();
            /**
             * @TODO compare lti message hint with session value
             */


            /**
             * @TODO build id_token and send it to redirect_uri
             */

            HashMap<String,String> formParams = new HashMap<>();
            formParams.put("id_token",jwt);
            formParams.put("state",state);
            return  Response.ok(ApiTool.getHTML(redirect_uri,formParams)).build();

        } catch(Throwable e){
            logger.error(e.getMessage(),e);
            return ApiTool.processError(req,e,"LTI_ERROR");
        }
    }

    private JwtBuilder getBasicLtiMessage(String clientId, String nonce, String username, ApplicationInfo appInfo, ApplicationInfo homeApp, Map<String, Object> context) throws Exception {
        Map<String,Object> toolPlatform = new HashMap<>();
        toolPlatform.put("product_family_code","edu-sharing");
        toolPlatform.put("version",VersionService.getVersion(VersionService.Type.REPOSITORY));
        toolPlatform.put("guid", homeApp.getAppId());
        toolPlatform.put("name", homeApp.getAppCaption());
        toolPlatform.put("description", homeApp.getAppCaption());


        String firstName = (String)nodeService.getProperty(personService.getPerson(username),ContentModel.PROP_FIRSTNAME);
        String lastName = (String)nodeService.getProperty(personService.getPerson(username),ContentModel.PROP_LASTNAME);
        String email = (String)nodeService.getProperty(personService.getPerson(username),ContentModel.PROP_EMAIL);

        Date now = new Date();
        JwtBuilder jwtBuilder = Jwts.builder()
                .setHeaderParam(LTIConstants.TYP, LTIConstants.JWT)
                .setHeaderParam(LTIConstants.KID, homeApp.getLtiKid())
                .setHeaderParam(LTIConstants.ALG, LTIConstants.RS256)
                .claim("nonce", nonce)
                .setIssuer(RegistrationService.getLtiPlatformOpenIdConfiguration().getIssuer())
                .setIssuedAt(now)
                .setExpiration(new Date((now.getTime() + 10000)))
                .setAudience(clientId)
                .setSubject(username)
                .claim(LTIConstants.LTI_DEPLOYMENT_ID, appInfo.getLtiDeploymentId())
                .claim("given_name",firstName)
                .claim("family_name",lastName)
                .claim("email",email)
                .claim(LTIConstants.LTI_TOOL_PLATFORM,toolPlatform)
                .claim(LTIConstants.LTI_VERSION, LTIConstants.LTI_VERSION_3)
                .claim("https://purl.imsglobal.org/spec/lti/claim/roles",new ArrayList<>());
        if(context != null){
            jwtBuilder = jwtBuilder.claim(LTIConstants.CONTEXT, context);
        }
        return jwtBuilder;
    }

    private boolean isEmpty( String value){
        if(value == null || value.trim().isEmpty()){
            return true;
        }
        return false;
    }


    @POST
    @Path("/start-dynamic-registration" )
    @Operation(summary = "starts lti dynamic registration.", description = "start dynmic registration")

    @Consumes({ "application/x-www-form-urlencoded" })
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode="200", description=RestConstants.HTTP_200, content = @Content(mediaType = "text/html", schema = @Schema(implementation = String.class))),
                    @ApiResponse(responseCode="400", description=RestConstants.HTTP_400, content = @Content(mediaType = "text/html", schema = @Schema(implementation = String.class))),
                    @ApiResponse(responseCode="401", description=RestConstants.HTTP_401, content = @Content(mediaType = "text/html", schema = @Schema(implementation = String.class))),
                    @ApiResponse(responseCode="403", description=RestConstants.HTTP_403, content = @Content(mediaType = "text/html", schema = @Schema(implementation = String.class))),
                    @ApiResponse(responseCode="404", description=RestConstants.HTTP_404, content = @Content(mediaType = "text/html", schema = @Schema(implementation = String.class))),
                    @ApiResponse(responseCode="500", description=RestConstants.HTTP_500, content = @Content(mediaType = "text/html", schema = @Schema(implementation = String.class)))
            })
    public Response startDynamicRegistration(@Parameter(description = "url",required=true) @FormParam("url") String url,
                                           @Context HttpServletRequest req){
            return startDynamicRegistrationBase(url);
    }

    @GET
    @Path("/start-dynamic-registration" )
    @Operation(summary = "starts lti dynamic registration.", description = "start dynmic registration")

    @Consumes({ "application/x-www-form-urlencoded" })
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode="200", description=RestConstants.HTTP_200, content = @Content(mediaType = "text/html", schema = @Schema(implementation = String.class))),
                    @ApiResponse(responseCode="400", description=RestConstants.HTTP_400, content = @Content(mediaType = "text/html", schema = @Schema(implementation = String.class))),
                    @ApiResponse(responseCode="401", description=RestConstants.HTTP_401, content = @Content(mediaType = "text/html", schema = @Schema(implementation = String.class))),
                    @ApiResponse(responseCode="403", description=RestConstants.HTTP_403, content = @Content(mediaType = "text/html", schema = @Schema(implementation = String.class))),
                    @ApiResponse(responseCode="404", description=RestConstants.HTTP_404, content = @Content(mediaType = "text/html", schema = @Schema(implementation = String.class))),
                    @ApiResponse(responseCode="500", description=RestConstants.HTTP_500, content = @Content(mediaType = "text/html", schema = @Schema(implementation = String.class)))
            })
    public Response startDynamicRegistrationGet(@Parameter(description = "url",required=true) @QueryParam("url") String url,
                                             @Context HttpServletRequest req){
        return startDynamicRegistrationBase(url);
    }

    public Response startDynamicRegistrationBase(String url){
        //generate client id to allow multiple tool deployments
        String clientId = RegistrationService.generateNewClientId();

        Calendar cal = Calendar.getInstance();
        Date now = cal.getTime();
        cal.add(Calendar.HOUR,1);
        Date exp = cal.getTime();

        ApplicationInfo homeApp = ApplicationInfoList.getHomeRepository();

        try {
            String registrationToken = Jwts.builder()
                    .setSubject(clientId)
                    .setIssuedAt(now)
                    .setExpiration(exp)
                    .claim("scope",LTIConstants.LTI_REGISTRATION_SCOPE_NEW)
                    .signWith(new Signing().getPemPrivateKey(homeApp.getPrivateKey(), CCConstants.SECURITY_KEY_ALGORITHM))
                    .setHeaderParam(LTIConstants.KID, homeApp.getLtiKid())
                    .compact();
            String openIdConfigurationUrl = homeApp.getClientBaseUrl()+"/rest/ltiplatform/v13/openid-configuration/";
            url = UrlTool.setParam(url,"openid_configuration", openIdConfigurationUrl);
            url = UrlTool.setParam(url,"registration_token", registrationToken);
            return Response.seeOther(new URI(url)).build();
        } catch (GeneralSecurityException | URISyntaxException e) {
            logger.error(e.getMessage(),e);
            return ErrorResponse.createResponse(e);
        }
    }


    @GET
    @Path("/openid-configuration")
    @Operation(summary = "LTIPlatform openid configuration")
    @Consumes({ "*/*"})
    @Produces({"application/json"})
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode="200", description= RestConstants.HTTP_200, content = @Content(schema = @Schema(implementation = OpenIdConfiguration.class))),
                    @ApiResponse(responseCode="400", description=RestConstants.HTTP_400, content = @Content(schema = @Schema(implementation = String.class))),
                    @ApiResponse(responseCode="401", description=RestConstants.HTTP_401, content = @Content(schema = @Schema(implementation = String.class))),
                    @ApiResponse(responseCode="403", description=RestConstants.HTTP_403, content = @Content(schema = @Schema(implementation = String.class))),
                    @ApiResponse(responseCode="404", description=RestConstants.HTTP_404, content = @Content(schema = @Schema(implementation = String.class))),
                    @ApiResponse(responseCode="500", description=RestConstants.HTTP_500, content = @Content(schema = @Schema(implementation = String.class)))
            })
    public Response openidConfiguration(){
        OpenIdConfiguration oidconf = RegistrationService.getLtiPlatformOpenIdConfiguration();
        return Response.status(Response.Status.OK).entity(oidconf).build();
    }


    @POST
    @Path("/openid-registration")

    @Operation(summary = "registration endpoint the tool uses to register at platform.", description = "tool registration")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode="200", description=RestConstants.HTTP_200, content = @Content(schema = @Schema(implementation = OpenIdRegistrationResult.class))),
                    @ApiResponse(responseCode="400", description=RestConstants.HTTP_400, content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode="401", description=RestConstants.HTTP_401, content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode="403", description=RestConstants.HTTP_403, content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode="404", description=RestConstants.HTTP_404, content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode="409", description=RestConstants.HTTP_409, content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode="500", description=RestConstants.HTTP_500, content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            })

    public Response openIdRegistration(
            @Parameter(description = "registrationpayload" ,required=true ) String registrationpayload,
            @Context HttpServletRequest req) {
        try{

            String authorizationHeader = req.getHeader("Authorization");
            if(authorizationHeader == null || !authorizationHeader.substring(0,7).equals("Bearer ")){
                throw new Exception("missing_registration_token");
            }

            logger.debug("registrationpayload:"+registrationpayload);
            logger.debug("authorizationHeader:" +authorizationHeader);

            String registrationToken = authorizationHeader.substring(7);

            //validate registration token originally send by us
            //should not be reusable cause of exp date
            Jwt jwt = LTIJWTUtil.validateJWT(registrationToken, ApplicationInfoList.getHomeRepository());

            JSONParser jsonParser = new JSONParser();
            JSONObject registrationPayload =  (JSONObject)jsonParser.parse(registrationpayload);
            ApplicationInfo appInfo = AuthenticationUtil.runAsSystem(() -> {
               return new RegistrationService().ltiDynamicToolRegistration(registrationPayload, jwt);
            });


            OpenIdRegistrationResult ors = new OpenIdRegistrationResult();
            ors.setClient_id(appInfo.getLtiClientId());
            ors.setApplication_type("web");
            ors.setClient_name(appInfo.getAppCaption());
            ors.setInitiate_login_uri(appInfo.getLtitoolLoginInitiationsUrl());
            ors.setRedirect_uris(Arrays.asList(appInfo.getLtitoolRedirectUrls().split(",")));
            ors.setToken_endpoint_auth_method((String)registrationPayload.get("token_endpoint_auth_method"));
            ors.setLogo_uri(appInfo.getLogo());
            ors.setScope((String)registrationPayload.get("scope"));

            OpenIdRegistrationResult.LTIToolConfiguration ltiToolConfiguration = new OpenIdRegistrationResult.LTIToolConfiguration();
            //ltiToolConfiguration.setVersion();
            ltiToolConfiguration.setDeployment_id(appInfo.getLtiDeploymentId());
            ltiToolConfiguration.setTarget_link_uri(appInfo.getLtitoolTargetLinkUri());
            ltiToolConfiguration.setDomain(appInfo.getDomain());
            //ltiToolConfiguration.setDescription();
            //ltiToolConfiguration.setClaims();
            ors.setLtiToolConfiguration(ltiToolConfiguration);
            return Response.ok().entity(ors).build();
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
            return ErrorResponse.createResponse(e);
        }
    }


    @GET
    @Path("/token")
    @Operation(summary = "LTIPlatform auth token endpoint")
    @Consumes({ "*/*"})
    @Produces({"application/json"})
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode="200", description= RestConstants.HTTP_200, content = @Content(schema = @Schema(implementation = Void.class))),
                    @ApiResponse(responseCode="400", description=RestConstants.HTTP_400, content = @Content(schema = @Schema(implementation = String.class))),
                    @ApiResponse(responseCode="401", description=RestConstants.HTTP_401, content = @Content(schema = @Schema(implementation = String.class))),
                    @ApiResponse(responseCode="403", description=RestConstants.HTTP_403, content = @Content(schema = @Schema(implementation = String.class))),
                    @ApiResponse(responseCode="404", description=RestConstants.HTTP_404, content = @Content(schema = @Schema(implementation = String.class))),
                    @ApiResponse(responseCode="500", description=RestConstants.HTTP_500, content = @Content(schema = @Schema(implementation = String.class)))
            })
    public Response authTokenEndpoint(){

        try {
            if (true) {
                throw new Exception("not implemented yet");
            }
        }catch (Exception e){
            return ErrorResponse.createResponse(e);
        }

        return Response.status(Response.Status.OK).build();
    }


    @POST
    @Path("/manual-registration")

    @Operation(summary = "manual registration endpoint for registration of tools.", description = "tool registration")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode="200", description=RestConstants.HTTP_200, content = @Content(schema = @Schema(implementation = Void.class))),
                    @ApiResponse(responseCode="400", description=RestConstants.HTTP_400, content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode="401", description=RestConstants.HTTP_401, content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode="403", description=RestConstants.HTTP_403, content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode="404", description=RestConstants.HTTP_404, content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode="409", description=RestConstants.HTTP_409, content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode="500", description=RestConstants.HTTP_500, content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            })

    public Response manualRegistration(
            @Parameter(description = "registrationData" ,required=true ) ManualRegistrationData registrationData,
            @Context HttpServletRequest req) {
        try {
            new RegistrationService().registerTool(
                    RegistrationService.generateNewClientId(),
                    registrationData.getLoginInitiationUrl(),
                    registrationData.getKeysetUrl(),
                    null,
                    StringUtils.join(registrationData.getRedirectionUrls(), ","),
                    registrationData.getLogoUrl(),
                    (registrationData.getCustomParameters() != null) ? StringUtils.join(registrationData.getCustomParameters(),",") : null,
                    registrationData.getToolDescription(),
                    registrationData.getClientName(),
                    registrationData.getTargetLinkUriDeepLink(),
                    registrationData.getToolUrl());
            return Response.ok().build();
        }catch (Exception e){
            return ErrorResponse.createResponse(e);
        }
    }

    @GET
    @Path("/tools")
    @Operation(summary = "List of tools registered")
    @Consumes({ "application/json"})
    @Produces({"application/json"})
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode="200", description= RestConstants.HTTP_200, content = @Content(schema = @Schema(implementation = Tools.class))),
                    @ApiResponse(responseCode="400", description=RestConstants.HTTP_400, content = @Content(schema = @Schema(implementation = String.class))),
                    @ApiResponse(responseCode="401", description=RestConstants.HTTP_401, content = @Content(schema = @Schema(implementation = String.class))),
                    @ApiResponse(responseCode="403", description=RestConstants.HTTP_403, content = @Content(schema = @Schema(implementation = String.class))),
                    @ApiResponse(responseCode="404", description=RestConstants.HTTP_404, content = @Content(schema = @Schema(implementation = String.class))),
                    @ApiResponse(responseCode="500", description=RestConstants.HTTP_500, content = @Content(schema = @Schema(implementation = String.class)))
            })
    public Response tools(){

        try {
            Tools tools = new Tools();
            for(ApplicationInfo appInfo : ApplicationInfoList.getApplicationInfos().values()){
                if(appInfo.isLtiTool()){
                    Tool tool = new Tool();
                    tool.setAppId(appInfo.getAppId());
                    tool.setDescription(appInfo.getLtitoolDescription());
                    try {
                        URI uri = new URI(appInfo.getLtitoolLoginInitiationsUrl());
                        tool.setDomain(uri.getHost());
                    }catch ( java.net.URISyntaxException e){}
                    tool.setName(appInfo.getAppCaption());
                    tool.setLogo(appInfo.getLogo());
                    tool.setCustomContentOption(appInfo.hasLtiToolCustomContentOption());
                    tools.getTools().add(tool);

                }
            }
            return Response.ok(tools).build();
        }catch (Exception e){
            return ErrorResponse.createResponse(e);
        }
    }


    @GET
    @Path("/generateLoginInitiationForm")
    @Operation(summary = "generate a form used for Initiating Login from a Third Party. Use thes endpoint when starting a lti deeplink flow.")
    @Consumes({ "text/html"})
    @Produces({"text/html"})
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode="200", description= RestConstants.HTTP_200, content = @Content(schema = @Schema(implementation = String.class))),
                    @ApiResponse(responseCode="400", description=RestConstants.HTTP_400, content = @Content(schema = @Schema(implementation = String.class))),
                    @ApiResponse(responseCode="401", description=RestConstants.HTTP_401, content = @Content(schema = @Schema(implementation = String.class))),
                    @ApiResponse(responseCode="403", description=RestConstants.HTTP_403, content = @Content(schema = @Schema(implementation = String.class))),
                    @ApiResponse(responseCode="404", description=RestConstants.HTTP_404, content = @Content(schema = @Schema(implementation = String.class))),
                    @ApiResponse(responseCode="500", description=RestConstants.HTTP_500, content = @Content(schema = @Schema(implementation = String.class)))
            })
    public Response generateLoginInitiationForm(@Parameter(description = "appId of the tool",required=true) @QueryParam("appId") String appId,
                                                @Parameter(description = "the folder id the lti node will be created in. is required for lti deeplink.",required=true) @QueryParam("parentId") String parentId,
                                                @Parameter(description = "the nodeId when tool has custom content option.",required=false) @QueryParam("nodeId") String nodeId,
                                                @Context HttpServletRequest req){


        try {
            RepositoryDao repoDao = RepositoryDao.getHomeRepository();

            if ("-userhome-".equals(parentId)) {
                parentId = repoDao.getUserHome();
            }
            if ("-inbox-".equals(parentId)) {
                parentId =repoDao.getUserInbox(true);
            }
            if ("-saved_search-".equals(parentId)) {
                parentId = repoDao.getUserSavedSearch(true);
            }

            for(ApplicationInfo appInfo : ApplicationInfoList.getApplicationInfos().values()){
                if(appInfo.isLtiTool() && appInfo.getAppId().equals(appId)){
                    String form = prepareLoginInitiation(parentId,null, null, nodeId, null, appInfo, LoginInitiationSessionObject.MessageType.deeplink,null, req);
                    return Response.status(Response.Status.OK).entity(form).build();
                }
            }
            throw new Exception("no lti tool found for "+ appId);
        }catch (Exception e){
             return ApiTool.processError(req,e,"LTI_ERROR");
        }
    }

    @GET
    @Path("/generateLoginInitiationFormResourceLink")
    @Operation(summary = "generate a form used for Initiating Login from a Third Party. Use thes endpoint when starting a lti resourcelink flow.")
    @Consumes({ "text/html"})
    @Produces({"text/html"})
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode="200", description= RestConstants.HTTP_200, content = @Content(schema = @Schema(implementation = String.class))),
                    @ApiResponse(responseCode="400", description=RestConstants.HTTP_400, content = @Content(schema = @Schema(implementation = String.class))),
                    @ApiResponse(responseCode="401", description=RestConstants.HTTP_401, content = @Content(schema = @Schema(implementation = String.class))),
                    @ApiResponse(responseCode="403", description=RestConstants.HTTP_403, content = @Content(schema = @Schema(implementation = String.class))),
                    @ApiResponse(responseCode="404", description=RestConstants.HTTP_404, content = @Content(schema = @Schema(implementation = String.class))),
                    @ApiResponse(responseCode="500", description=RestConstants.HTTP_500, content = @Content(schema = @Schema(implementation = String.class)))
            })
    public Response generateLoginInitiationFormResourceLink(@Parameter(description = "the nodeid of a node that contains a lti resourcelink. is required for lti resourcelink",required=true) @QueryParam("nodeId") String nodeId,
                                                @Parameter(description = "for tools with content option, this param sends changeContentUrl (true) else contentUrl will be excluded",required = false,schema = @Schema(defaultValue = "true")) @QueryParam("editMode") Boolean editMode,
                                                @Parameter(description = "the version. for tools with contentoption.", required = false) @QueryParam("version") String version,
                                                @Parameter(description = "launchPresentation. how the resourcelink will be embedded. valid values: window,iframe", required = false) @QueryParam("launchPresentation") String launchPresentation,
                                                @Context HttpServletRequest req){
        try{
            //@TODO find out why defaultvalue of swagger definition does not work
            if(editMode == null){
                editMode = true;
            }
            NodeRef nodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, nodeId);

            if(!nodeService.hasAspect(nodeRef, QName.createQName(CCConstants.CCM_ASPECT_LTITOOL_NODE))){
                throw new Exception("not an lti resoucelink:"+nodeId);
            }

            String resourceLink = (String)nodeService.getProperty(nodeRef, QName.createQName(CCConstants.CCM_PROP_LTITOOL_NODE_RESOURCELINK));
            if(resourceLink == null){
                throw new Exception("lti resoucelink is null:"+nodeId);
            }

            String toolUrl = (String)nodeService.getProperty(nodeRef, QName.createQName(CCConstants.CCM_PROP_LTITOOL_NODE_TOOLURL));
            if(toolUrl == null){
                throw new Exception("lti toolUrl is null:"+nodeId);
            }

            for(ApplicationInfo appInfo : ApplicationInfoList.getApplicationInfos().values()){
                if(appInfo.isLtiTool() && toolUrl.equals(appInfo.getLtitoolUrl())){
                    String form = prepareLoginInitiation(nodeService.getPrimaryParent(nodeRef).getParentRef().getId(),
                            nodeId,
                            editMode,
                            nodeId,
                            version,
                            appInfo,
                            LoginInitiationSessionObject.MessageType.resourcelink,
                            launchPresentation,
                            req);
                    return Response.status(Response.Status.OK).entity(form).build();
                }
            }
            throw new Exception("no lti tool found for toolUrl:"+ toolUrl);


        }catch (Exception e){
            return ApiTool.processError(req,e,"LTI_ERROR");
        }
    }

    /**
     * builds form and session data
     * @param contextId
     * @param appInfo
     * @param messageType
     * @param req
     * @return
     */
    private String prepareLoginInitiation(String contextId,
                                          String resourceLinkNodeId,
                                          Boolean resourceLinkEditMode,
                                          String contentUrlNodeId,
                                          String version,
                                          ApplicationInfo appInfo,
                                          LoginInitiationSessionObject.MessageType messageType,
                                          String launchPresentation,
                                          HttpServletRequest req) {
        Map<String,String> params = new HashMap<>();
        params.put("iss",ApplicationInfoList.getHomeRepository().getClientBaseUrl());
        String targetLinkUrl = appInfo.getLtitoolTargetLinkUri();
        if(messageType.equals(LoginInitiationSessionObject.MessageType.resourcelink)){
            targetLinkUrl = (String)nodeService.getProperty(new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,resourceLinkNodeId),
                    QName.createQName(CCConstants.CCM_PROP_LTITOOL_NODE_RESOURCELINK));
        }
        params.put("target_link_uri", targetLinkUrl);
        params.put("login_hint", AuthenticationUtil.getFullyAuthenticatedUser());

        //allow multiple lti windows
        String ltiSessionScope = UUID.randomUUID().toString();
        params.put("lti_message_hint", ltiSessionScope);
        params.put("client_id", appInfo.getLtiClientId());
        params.put("lti_deployment_id", appInfo.getLtiDeploymentId());
        String form = ApiTool.getHTML(appInfo.getLtitoolLoginInitiationsUrl(),params);

        LoginInitiationSessionObject loginInitiationSessionObject = new LoginInitiationSessionObject();
        loginInitiationSessionObject.setAppId(appInfo.getAppId());
        loginInitiationSessionObject.setClientId(appInfo.getLtiClientId());
        loginInitiationSessionObject.setContextId(contextId);
        loginInitiationSessionObject.setResourceLinkNodeId(resourceLinkNodeId);
        if(resourceLinkEditMode != null) loginInitiationSessionObject.setResourceLinkEditMode(resourceLinkEditMode);
        loginInitiationSessionObject.setVersion(version);
        loginInitiationSessionObject.setMessageType(messageType);
        loginInitiationSessionObject.setContentUrlNodeId(contentUrlNodeId);
        loginInitiationSessionObject.setLaunchPresentation(launchPresentation);
        //remember session in userLTISessions map to reuse in later backend call
        if(contentUrlNodeId != null){
            Map<String,String> map = new HashMap<>();
            map.put(LTIPlatformConstants.CUSTOM_CLAIM_APP_ID,appInfo.getAppId());
            map.put(LTIPlatformConstants.CUSTOM_CLAIM_USER,AuthenticationUtil.getFullyAuthenticatedUser());
            map.put(LTIPlatformConstants.CUSTOM_CLAIM_NODEID,contentUrlNodeId);
            map.put("ts",System.currentTimeMillis()+"");
            String json = new Gson().toJson(map);
            String encryptedToken = ApiTool.encrpt(json);
            loginInitiationSessionObject.setToken(encryptedToken);
            AllSessions.userLTISessions.put(encryptedToken, req.getSession());
        }

        Map<String,LoginInitiationSessionObject> loginInitiationSessionObjectMap = (Map<String,LoginInitiationSessionObject>)req
                .getSession()
                .getAttribute(LTIPlatformConstants.LOGIN_INITIATIONS_SESSIONOBJECT);
        if(loginInitiationSessionObjectMap == null){
            logger.info("");
            loginInitiationSessionObjectMap = new HashMap<>();
        }
        loginInitiationSessionObjectMap.put(ltiSessionScope,loginInitiationSessionObject);
        req.getSession().setAttribute(LTIPlatformConstants.LOGIN_INITIATIONS_SESSIONOBJECT, loginInitiationSessionObjectMap);

        return form;
    }


    @POST
    @Path("/deeplinking-response")

    @Operation(summary = "receiving deeplink response messages.", description = "deeplink response")
    @Consumes({"application/x-www-form-urlencoded"})
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode="200", description=RestConstants.HTTP_200, content = @Content(schema = @Schema(implementation = String.class))),
                    @ApiResponse(responseCode="400", description=RestConstants.HTTP_400, content = @Content(schema = @Schema(implementation = String.class))),
                    @ApiResponse(responseCode="401", description=RestConstants.HTTP_401, content = @Content(schema = @Schema(implementation = String.class))),
                    @ApiResponse(responseCode="403", description=RestConstants.HTTP_403, content = @Content(schema = @Schema(implementation = String.class))),
                    @ApiResponse(responseCode="404", description=RestConstants.HTTP_404, content = @Content(schema = @Schema(implementation = String.class))),
                    @ApiResponse(responseCode="409", description=RestConstants.HTTP_409, content = @Content(schema = @Schema(implementation = String.class))),
                    @ApiResponse(responseCode="500", description=RestConstants.HTTP_500, content = @Content(schema = @Schema(implementation = String.class)))
            })

    public Response deepLinkingResponse(
            @Parameter(description = "JWT",required=true) @FormParam("JWT") String jwt,
            @Context HttpServletRequest req) {
        try{

            Map<String,LoginInitiationSessionObject> loginInitiationSessionObjectMap = (Map<String,LoginInitiationSessionObject>)req.getSession().getAttribute(LTIPlatformConstants.LOGIN_INITIATIONS_SESSIONOBJECT);
            if(loginInitiationSessionObjectMap == null){
                throw new Exception(LTIPlatformConstants.ERROR_MISSING_SESSIONOBJECTS);
            }
            /**
             * we don't hat lti_message_hint here so we have to resolve the object by nonce
             */
            String nonce = LTIJWTUtil.getValue(jwt,"nonce");
            if(nonce == null) throw new Exception("missing nonce");
            LoginInitiationSessionObject sessionObject = loginInitiationSessionObjectMap.entrySet().stream()
                    .filter(e -> e.getValue().getToolNonce().equals(nonce))
                    .findFirst()
                    .orElseThrow(() -> new Exception(LTIPlatformConstants.ERROR_MISSING_SESSIONOBJECT)).getValue();

            LTIJWTUtil jwtUtil = new LTIJWTUtil();
            //find out clientid/deploymentid
            ApplicationInfo appInfoTool = ApplicationInfoList.getRepositoryInfoById(sessionObject.getAppId());
            Jws<Claims> claims = jwtUtil.validateJWT(jwt,appInfoTool);

            /**
             * @ToDo more validation?
             */
            if(!appInfoTool.isLtiTool()){
                throw new Exception("application is no lti tool");
            }


            if(sessionObject == null){
                throw new Exception("missing login initiation session object");
            }


            List<Map<String,Object>> contentItems = (List<Map<String,Object>>)claims.getBody().get(LTIConstants.LTI_CONTENT_ITEMS);
            if(contentItems == null || contentItems.size() == 0){
                throw new Exception("missing lti content items");
            }


            List<String> nodeIds = new ArrayList<>();
            List<String> titles = new ArrayList<>();
            for(Map<String,Object> contentItem : contentItems){
                HashMap<String, String[]> properties = new HashMap<>();
                String type = (String)contentItem.get("type");
                if(!LTIConstants.DEEP_LINK_LTIRESOURCELINK.equals(type)){
                    throw new Exception("unsupported lti type:"+type);
                }

                String url = (String)contentItem.get("url");
                if(url == null){
                    throw new Exception("missing resourcelink url");
                }

                String title = (String)contentItem.get("title");
                title = title != null ? title : url;
                String name = EduSharingNodeHelper.cleanupCmName(title);
                name = new DuplicateFinder().getUniqueValue(sessionObject.getContextId(),CCConstants.CM_NAME,name);
                properties.put(CCConstants.CM_NAME,new String[]{name} );
               // properties.put(CCConstants.LOM_PROP_GENERAL_TITLE,new String[]{title});

                if(contentItem.containsKey("icon")){
                    Map<String,Object> icon = (Map<String,Object>)contentItem.get("icon");
                    String iconUrl = (String)icon.get("url");
                    if(iconUrl != null){
                        properties.put(CCConstants.CCM_PROP_IO_THUMBNAILURL,new String[]{iconUrl});
                    }
                }

                org.edu_sharing.service.nodeservice.NodeService eduNodeService = NodeServiceFactory.getLocalService();


                String nodeId = sessionObject.getContentUrlNodeId();
                if(nodeId != null) {
                    eduNodeService.updateNode(sessionObject.getContentUrlNodeId(), properties);
                }else{
                    nodeId = eduNodeService.createNode(sessionObject.getContextId(), CCConstants.CCM_TYPE_IO, properties);
                }

                updateToResourceLink(nodeId, appInfoTool, url);
                nodeIds.add(nodeId);
                titles.add(name);
            }


            String nodeIdsJS = (nodeIds.size() > 1) ? StringUtils.join(nodeIds.toArray(),"','") : nodeIds.get(0);
            nodeIdsJS = "['"+nodeIdsJS+"']";

            String titlesJS = (titles.size() > 1) ? StringUtils.join(titles.toArray(),"','") : titles.get(0);
            titlesJS = "['"+titlesJS+"']";

            String closeAndInformAngular =
                    "function callAngularFunction(nodeIds, titles) {" +
                            "window.opener.angularComponentReference.zone.run(() => { window.opener.angularComponentReference.loadAngularFunction(nodeIds,titles); });" +
                    "}"
                    + "window.onload = function() {\n" +
                            " nodeIdArr="+nodeIdsJS+";"+
                            " titlesArr="+titlesJS+";"+
                        " callAngularFunction(nodeIdArr,titlesArr);\n" +
                            "window.close();\n" +
                    "};";

            //cleanup session object to prevent wrong context message

            return Response.ok().entity(ApiTool.getHTML(null,null,"no js active. please close tab.",closeAndInformAngular)).build();
        } catch (Throwable e) {
            return ApiTool.processError(req,e,"LTI_ERROR");
        }
    }

    @NotNull
    private String updateToResourceLink(String nodeId, ApplicationInfo appInfoTool, String resourceLink) throws Throwable {

        NodeRef nodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,nodeId);
        QName aspectLti = QName.createQName(CCConstants.CCM_ASPECT_LTITOOL_NODE);

        if(!nodeService.hasAspect(nodeRef,aspectLti)){
            Map<QName, Serializable> ltiAspectProps = new HashMap<>();
            ltiAspectProps.put(QName.createQName(CCConstants.CCM_PROP_LTITOOL_NODE_RESOURCELINK), resourceLink);
            ltiAspectProps.put(QName.createQName(CCConstants.CCM_PROP_LTITOOL_NODE_TOOLURL), appInfoTool.getLtitoolUrl());
            nodeService.addAspect(nodeRef,aspectLti,ltiAspectProps);
        }
        return nodeId;
    }


    @POST
    @Path("/convert2resourcelink")

    @Operation(summary = "manual convertion of an io to an resource link without deeplinking", description = "io conversion to resourcelink")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode="200", description=RestConstants.HTTP_200, content = @Content(schema = @Schema(implementation = Void.class))),
                    @ApiResponse(responseCode="400", description=RestConstants.HTTP_400, content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode="401", description=RestConstants.HTTP_401, content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode="403", description=RestConstants.HTTP_403, content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode="404", description=RestConstants.HTTP_404, content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode="409", description=RestConstants.HTTP_409, content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode="500", description=RestConstants.HTTP_500, content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            })

    public Response convertToResourcelink(
            @Parameter(description = "nodeId", required=true ) @QueryParam("nodeId") String nodeId,
            @Parameter(description = "appId of a lti tool", required=true ) @QueryParam("appId") String appId,
            @Context HttpServletRequest req) {
        RepositoryDao repoDao = null;
        try {
            repoDao = RepositoryDao.getHomeRepository();
            NodeDao nodeDao = NodeDao.getNode(repoDao, nodeId);
            if(!nodeDao.getType().equals("ccm:io")){
                throw new Exception("wrong type:" + nodeDao.getType());
            }

            ApplicationInfo appInfo = ApplicationInfoList.getRepositoryInfoById(appId);
            if(appInfo == null || !appInfo.isLtiTool()){
                throw new Exception("application "+ appId + " is no tool");
            }

            String resourceLink = appInfo.getLtitoolTargetLinkUri();
            if(resourceLink == null){
                String redirectUrls = appInfo.getLtitoolRedirectUrls();
                if(redirectUrls != null && !redirectUrls.trim().isEmpty()){
                    resourceLink = redirectUrls.split(",")[0];
                }
            }
            if(resourceLink == null){
                resourceLink =appInfo.getLtitoolUrl();
            }

            updateToResourceLink(nodeId,appInfo,resourceLink);
            return Response.ok().build();

        } catch (DAOValidationException t) {

            logger.warn(t.getMessage(), t);
            return Response.status(Response.Status.BAD_REQUEST).entity(new ErrorResponse(t)).build();

        } catch (DAOSecurityException t) {

            logger.warn(t.getMessage(), t);
            return Response.status(Response.Status.FORBIDDEN).entity(new ErrorResponse(t)).build();

        } catch (DAOMissingException t) {

            logger.warn(t.getMessage(), t);
            return Response.status(Response.Status.NOT_FOUND).entity(new ErrorResponse(t)).build();

        }catch(DAOVirusDetectedException t){
            logger.warn(t.getMessage(),t);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new ErrorResponse(t)).build();
        } catch (Throwable t) {

            logger.error(t.getMessage(), t);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new ErrorResponse(t)).build();
        }

    }




    @POST
    @Path("/content")
    @Consumes({ "multipart/form-data" })
    @Produces({"application/json"})

    @Operation(summary = "Custom edu-sharing endpoint to change content of node.", description = "Change content of node.")

    @ApiResponses(
            value = {
                    @ApiResponse(responseCode="200", description=RestConstants.HTTP_200, content = @Content(schema = @Schema(implementation = NodeEntry.class))),
                    @ApiResponse(responseCode="400", description=RestConstants.HTTP_400, content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode="401", description=RestConstants.HTTP_401, content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode="403", description=RestConstants.HTTP_403, content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode="404", description=RestConstants.HTTP_404, content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode="500", description=RestConstants.HTTP_500, content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            })

    public Response changeContent(
            @Parameter(description = "jwt containing the claims appId, nodeId, user previously send with ResourceLinkRequest or DeeplinkRequest. Must be signed by tool", required=true ) @QueryParam("jwt")  String jwt,
            @Parameter(description = "comment, leave empty = no new version, otherwise new version is generated", required=false ) @QueryParam("versionComment")  String versionComment,
            @Parameter(description = "MIME-Type", required=true ) @QueryParam("mimetype")  String mimetype,
            //@Parameter(description = "file upload", schema = @Schema(description = "hello 123", name = "file", type = "string", format = "binary")) InputStream inputStream,
            @Parameter(description = "file upload", schema = @Schema( name = "file", type = "string", format = "binary")) @FormDataParam("file") InputStream inputStream,
            @Context HttpServletRequest req) {

        try {

            Jws<Claims>  jwtObj = new LTIJWTUtil().validateForCustomContent(jwt,true);
            String user = jwtObj.getBody().get(LTIPlatformConstants.CUSTOM_CLAIM_USER, String.class);
            String nodeId = jwtObj.getBody().get(LTIPlatformConstants.CUSTOM_CLAIM_NODEID, String.class);



            NodeEntry resp =  AuthenticationUtil.runAs(() -> {
                RepositoryDao repoDao = RepositoryDao.getHomeRepository();
                NodeDao nodeDao = NodeDao.getNode(repoDao, nodeId);
                NodeDao newNode = nodeDao.changeContent(inputStream, mimetype, versionComment);
                NodeEntry response = new NodeEntry();
                response.setNode(newNode.asNode());
                return response;
            },user);


            return Response.status(Response.Status.OK).entity(resp).build();

        } catch (DAOValidationException t) {

            logger.warn(t.getMessage(), t);
            return Response.status(Response.Status.BAD_REQUEST).entity(new ErrorResponse(t)).build();

        } catch (DAOSecurityException t) {

            logger.warn(t.getMessage(), t);
            return Response.status(Response.Status.FORBIDDEN).entity(new ErrorResponse(t)).build();

        } catch (DAOMissingException t) {

            logger.warn(t.getMessage(), t);
            return Response.status(Response.Status.NOT_FOUND).entity(new ErrorResponse(t)).build();

        }catch(DAOVirusDetectedException t){
            logger.warn(t.getMessage(),t);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new ErrorResponse(t)).build();
        }catch(ValidationException e){
            logger.warn(e.getMessage(),e);
            return Response.status(Response.Status.FORBIDDEN).entity(e.getMessage()).build();
        }  catch (Throwable t) {

            logger.error(t.getMessage(), t);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new ErrorResponse(t)).build();
        }

    }


    @GET
    @Path("/content")
    @Consumes({ "application/json" })
    @Produces({ "*/*" })
    @Operation(summary = "Custom edu-sharing endpoint to get content of node.", description = "Get content of node.")

    @ApiResponses(
            value = {
                    @ApiResponse(responseCode="200", description=RestConstants.HTTP_200, content = @Content(schema = @Schema(implementation = byte[].class))),
                    @ApiResponse(responseCode="400", description=RestConstants.HTTP_400, content = @Content(mediaType = "text/html", schema = @Schema(implementation = String.class))),
                    @ApiResponse(responseCode="401", description=RestConstants.HTTP_401, content = @Content(mediaType = "text/html", schema = @Schema(implementation = String.class))),
                    @ApiResponse(responseCode="403", description=RestConstants.HTTP_403, content = @Content(mediaType = "text/html", schema = @Schema(implementation = String.class))),
                    @ApiResponse(responseCode="404", description=RestConstants.HTTP_404, content = @Content(mediaType = "text/html", schema = @Schema(implementation = String.class))),
                    @ApiResponse(responseCode="500", description=RestConstants.HTTP_500, content = @Content(mediaType = "text/html", schema = @Schema(implementation = String.class)))
            })
    public Response getContent(
            @Parameter(description = "jwt containing the claims appId, nodeId, user previously send with ResourceLinkRequest or DeeplinkRequest. Must be signed by tool", required=true ) @QueryParam("jwt")  String jwt,
            @Context HttpServletRequest req,
            @Context HttpServletResponse resp){
        try {
            Jws<Claims> jwtObj = new LTIJWTUtil().validateForCustomContent(jwt,true);
            String nodeId = jwtObj.getBody().get(LTIPlatformConstants.CUSTOM_CLAIM_NODEID, String.class);


            String appId = jwtObj.getBody().get(LTIPlatformConstants.CUSTOM_CLAIM_APP_ID, String.class);

            String version = jwtObj.getBody().get(LTIPlatformConstants.CUSTOM_CLAIM_VERSION, String.class);

            ApplicationInfo appInfo = ApplicationInfoList.getRepositoryInfoById(appId);

            AuthenticationUtil.runAsSystem(() -> {

                NodeRef nodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,nodeId);
                if(version != null && !version.trim().equals("")){
                    VersionHistory versionHistory = serviceRegistry.getVersionService().getVersionHistory(nodeRef);
                    if(versionHistory != null){
                        Version version1 = versionHistory.getVersion(version);
                        if(version1 != null)
                            nodeRef = version1.getFrozenStateNodeRef();
                        else
                            logger.error("unknown version");
                    }
                }

                String toolUrl = (String)serviceRegistry.getNodeService().getProperty(nodeRef,QName.createQName(CCConstants.CCM_PROP_LTITOOL_NODE_TOOLURL));
                if(toolUrl == null || !toolUrl.equals(appInfo.getLtitoolUrl())){
                    throw new ValidationException("tool is not allowed to access this node");
                }

                ContentReader reader = serviceRegistry.getContentService().getReader(nodeRef, ContentModel.PROP_CONTENT);
                if (reader == null) {
                    logger.info("no content found for " + nodeRef);
                    return Response.status(Response.Status.NOT_FOUND).entity("no content found").build();
                }

                String mimetype = reader.getMimetype();

                resp.setContentType((mimetype != null) ? mimetype : "application/octet-stream");
                resp.setContentLength((int) reader.getContentData().getSize());

                int length = 0;
                byte[] bbuf = new byte[1024];
                DataInputStream in = new DataInputStream(reader.getContentInputStream());
                ServletOutputStream op = resp.getOutputStream();
                while ((in != null) && ((length = in.read(bbuf)) != -1)) {
                    op.write(bbuf, 0, length);
                }

                in.close();
                op.flush();
                op.close();
                return null;
            });


            return null;
        }catch(ValidationException e){
            logger.warn(e.getMessage(),e);
            return Response.status(Response.Status.FORBIDDEN).entity(e.getMessage()).build();
        }catch (RuntimeException e){
            if(e.getCause() != null && e.getCause() instanceof ValidationException){
                logger.warn(e.getMessage(),e);
                return Response.status(Response.Status.FORBIDDEN).entity(e.getCause().getMessage()).build();
            }
            logger.error(e.getMessage(),e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }catch (Exception e){
            logger.error(e.getMessage(),e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }




    @PUT
    @Path("/testToken")
    @Operation(summary = "test creates a token signed with homeapp.", description = "test token.")
    @Consumes({ "application/json"})
    @Produces({"application/json"})
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode="200", description=RestConstants.HTTP_200, content = @Content(schema = @Schema(implementation = String.class))),
                    @ApiResponse(responseCode="400", description=RestConstants.HTTP_400, content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode="401", description=RestConstants.HTTP_401, content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode="403", description=RestConstants.HTTP_403, content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode="404", description=RestConstants.HTTP_404, content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode="409", description=RestConstants.HTTP_409, content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode="500", description=RestConstants.HTTP_500, content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            })

    public Response testToken(
            @Parameter(description = "properties" ,required=true ) HashMap<String, String> properties,
            @Context HttpServletRequest req) {



        try {
            if(!"admin".equals(AuthenticationUtil.getFullyAuthenticatedUser())){
                throw new Exception("admin required");
            }

            String kid = ApplicationInfoList.getHomeRepository().getLtiKid();
            if(kid == null){
                throw new Exception("missing kid for homeApp");
            }


            Key issPrivateKey = new Signing().getPemPrivateKey(ApplicationInfoList.getHomeRepository().getPrivateKey(), CCConstants.SECURITY_KEY_ALGORITHM);
            JwtBuilder builder = Jwts.builder()
                    .setHeaderParam("kid", ApplicationInfoList.getHomeRepository().getLtiKid())  // The key id used to sign this
                    .setHeaderParam("typ", "JWT") // The type
                    .setIssuer("ltiStarter")  //This is our own identifier, to know that we are the issuer.
                    .setExpiration(DateUtils.addSeconds(new Date(), 360000000));

            properties.entrySet().stream().forEach(e -> builder.claim(e.getKey(),e.getValue()));

            String result = builder.signWith(SignatureAlgorithm.RS256, issPrivateKey)  //We sign it
                    .compact();


            return Response.status(Response.Status.OK).entity(result).build();

        } catch (Throwable t) {
            return ErrorResponse.createResponse(t);
        }
    }

}