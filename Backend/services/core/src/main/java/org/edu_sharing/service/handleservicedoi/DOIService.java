package org.edu_sharing.service.handleservicedoi;

import com.typesafe.config.Config;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.edu_sharing.alfresco.lightbend.LightbendConfigLoader;
import org.edu_sharing.metadataset.v2.MetadataKey;
import org.edu_sharing.metadataset.v2.MetadataSet;
import org.edu_sharing.metadataset.v2.MetadataWidget;
import org.edu_sharing.metadataset.v2.tools.MetadataHelper;
import org.edu_sharing.repository.server.jobs.quartz.MigrateMetadataValuespaceJob;
import org.edu_sharing.repository.server.tools.ApplicationInfoList;
import org.edu_sharing.service.handleservice.HandleService;
import org.edu_sharing.service.handleservice.HandleServiceNotConfiguredException;
import org.edu_sharing.service.handleservicedoi.model.DOI;
import org.edu_sharing.service.handleservicedoi.model.Data;
import org.edu_sharing.repository.client.tools.CCConstants;
import org.edu_sharing.repository.server.tools.VCardConverter;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

public class DOIService implements HandleService {

    Logger logger = Logger.getLogger(DOIService.class);

    //https://api.test.datacite.org/
    String baseUrl = "https://api.datacite.org";
    String accountId;
    String prefix;
    String password;
    boolean enabled = false;

    RestTemplate template = new RestTemplate(new HttpComponentsClientHttpRequestFactory());

    public DOIService() throws HandleServiceNotConfiguredException {
        Config config = LightbendConfigLoader.get().getConfig("repository.doiservice");
        baseUrl = config.getString("baseUrl");
        accountId = config.getString("accountId");
        prefix = config.getString("prefix");
        password = config.getString("password");
        enabled = config.getBoolean("enabled");
    }

    @Override
    public boolean enabled() {
        return enabled;
    }

    @Override
    public boolean available() {
        HttpHeaders headers = new HttpHeaders();
        headers.put("Accept", Arrays.asList("text/plain"));
        HttpEntity<DOI> entity = new HttpEntity<>(headers);
        ResponseEntity<String> exchange = template.exchange(baseUrl + "/heartbeat", HttpMethod.GET, entity, String.class);
        logger.debug("heartbeat check response: " + exchange.getBody());
        return exchange.getStatusCode() == HttpStatusCode.valueOf(200) ? true : false;
    }

    public DOI getDOI(String id){

        HttpEntity<DOI> entity = new HttpEntity<>(getHttpHeaders());
        ResponseEntity<DOI> result = template.exchange(baseUrl+"/dois/" + id, HttpMethod.GET, entity, DOI.class);

        return result.getBody();
    }

    @Override
    public String create(String id, Map<QName, Serializable> properties) throws Exception {
        return update(id,properties);
    }


    @Override
    public String update(String id, Map<QName, Serializable> properties) throws Exception {
        logger.debug("Updating DOI: " + id);
        DOI doi = mapForPublishing(properties);
        HttpEntity<DOI> entity = new HttpEntity<>(doi,getHttpHeaders());
        ResponseEntity<DOI> doiResponseEntity = template.exchange(baseUrl+"/dois/"+id, HttpMethod.PUT, entity, DOI.class);
        if(!doiResponseEntity.getStatusCode().is2xxSuccessful()){
            throw new DOIServiceException("update id:" + id + " failed. api returned: " + doiResponseEntity.getStatusCode());
        }
        return doiResponseEntity.getBody().getData().getId();
    }

    @Override
    public String delete(String id) throws Exception {
        //only works for drafts
        HttpEntity<Void> entity = new HttpEntity<>(getHttpHeaders());
        ResponseEntity<Void> doiResponseEntity = template.exchange(baseUrl+"/dois/"+id, HttpMethod.DELETE, entity,Void.class);
        if(!doiResponseEntity.getStatusCode().is2xxSuccessful()){
            throw new DOIServiceException("delete id:" + id + " failed. api returned: " + doiResponseEntity.getStatusCode());
        }
        return id;
    }



    @Override
    public String generateId() throws Exception {
        DOI doi = DOI.builder()
                .data(Data.builder()
                        .type("dois")
                        .attributes(Data.Attributes.builder()
                                .prefix(prefix).build())
                        .build())
                .build();

        HttpEntity<DOI> entity = new HttpEntity<>(doi,getHttpHeaders());

        ResponseEntity<DOI> doiResponseEntity = template.exchange(baseUrl+"/dois/", HttpMethod.POST, entity, DOI.class);
        if(!doiResponseEntity.getStatusCode().is2xxSuccessful()){
            throw new Exception("generate id failed. api returned: " + doiResponseEntity.getStatusCode());
        }
        logger.debug("generated DOI: " + doiResponseEntity.getBody().getData().getId());
        return doiResponseEntity.getBody().getData().getId();
    }

    @Override
    public String getHandleIdProperty() {
        return CCConstants.CCM_PROP_PUBLISHED_DOI_ID;
    }

    private DOI mapForPublishing(Map<QName, Serializable> properties) throws Exception {
        DOI doi = DOI.builder()
                .data(Data.builder()
                        .type("dois")
                        .attributes(Data.Attributes.builder()
                                .creators(new ArrayList<>())
                                .titles(new ArrayList<>())
                                .event("publish")
                                .build())
                        .build())
                .build();

        //creator
        //the main researchers involved working on the data, or the authors of the publication in priority order. May be a corporate/institutional or personal name.
        List<String> author = (List<String>) properties.get(QName.createQName(CCConstants.CCM_PROP_IO_REPL_LIFECYCLECONTRIBUTER_AUTHOR));
        List<String> authorFreetext = (List<String>) properties.get(QName.createQName(CCConstants.CCM_PROP_AUTHOR_FREETEXT));
        if(author == null || author.isEmpty()){
            if(authorFreetext == null || authorFreetext.isEmpty()){
                throw new DOIServiceMissingAttributeException(CCConstants.getValidLocalName(CCConstants.CCM_PROP_IO_REPL_LIFECYCLECONTRIBUTER_AUTHOR),"Creator");
            }
        }
        if(author != null && !author.isEmpty()) {
            author.stream().forEach(a -> doi.getData().getAttributes().getCreators()
                    .add(Data.Creator.builder().name(VCardConverter.getNameForVCardString(a)).build()));
        }
        if(authorFreetext != null && !authorFreetext.isEmpty()){
            authorFreetext.stream().forEach(a -> doi.getData().getAttributes().getCreators()
                    .add(Data.Creator.builder().name(a).build()));
        }


        //title
        String title = (String) properties.get(QName.createQName(CCConstants.LOM_PROP_GENERAL_TITLE));

        if(StringUtils.isEmpty(title)){
            throw new DOIServiceMissingAttributeException(CCConstants.getValidLocalName(CCConstants.LOM_PROP_GENERAL_TITLE),"Title");
        }
        doi.getData().getAttributes().getTitles().add(Data.Title.builder().title(title).build());

        //publisher
        List<String> publisherList = (List<String>)properties.get(QName.createQName(CCConstants.CCM_PROP_IO_REPL_LIFECYCLECONTRIBUTER_PUBLISHER));
        String publisher;
        if(publisherList == null || publisherList.isEmpty()){
            throw new DOIServiceMissingAttributeException(CCConstants.getValidLocalName(CCConstants.CCM_PROP_IO_REPL_LIFECYCLECONTRIBUTER_PUBLISHER),"Publisher");
        }else publisher = publisherList.get(0);
        doi.getData().getAttributes().setPublisher(VCardConverter.getNameForVCardString(publisher));

        //published year
        Date d = (Date) properties.get(QName.createQName(CCConstants.CCM_PROP_PUBLISHED_DATE));
        if(d == null){
            throw new DOIServiceMissingAttributeException(CCConstants.getValidLocalName(CCConstants.CCM_PROP_PUBLISHED_DATE),"PublicationYear");
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(d);
        doi.getData().getAttributes().setPublicationYear(calendar.get(Calendar.YEAR));


        //learning resourcetype
        List<String> lrts = (List<String>) properties.get(QName.createQName(CCConstants.CCM_PROP_IO_REPL_EDUCATIONAL_LEARNINGRESSOURCETYPE));
        if(lrts == null || lrts.size() == 0){
            throw new DOIServiceMissingAttributeException(CCConstants.getValidLocalName(CCConstants.CCM_PROP_IO_REPL_LIFECYCLECONTRIBUTER_PUBLISHER), "resourceTypeGeneral");
        }

        String mapping = getMapping(properties,CCConstants.getValidLocalName(CCConstants.CCM_PROP_IO_REPL_LIFECYCLECONTRIBUTER_PUBLISHER),lrts.get(0));
        if(mapping == null) mapping = "Other";
        Data.Types t = Data.Types.builder().resourceTypeGeneral(mapping).build();
        doi.getData().getAttributes().setTypes(t);


        //url
        doi.getData().getAttributes().setUrl(getContentLink(properties));
        return doi;
    }

    private HttpHeaders getHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.put("Authorization",Arrays.asList(DOIService.getBasicAuthenticationHeader(accountId,password)));
        headers.put("Content-Type", Arrays.asList("application/vnd.api+json"));
        headers.put("Accept", Arrays.asList("*/*"));
        return headers;
    }

    public static final String getBasicAuthenticationHeader(String username, String password) {
        String valueToEncode = username + ":" + password;
        return "Basic " + Base64.getEncoder().encodeToString(valueToEncode.getBytes());
    }

    public String getMapping(Map<QName,Serializable> properties, String mdsWidgetId, String key){
        try {
            String mdsSet = (String)properties.get(QName.createQName(CCConstants.CM_PROP_METADATASET_EDU_METADATASET));
            if(mdsSet==null || mdsSet.isEmpty())
                mdsSet=CCConstants.metadatasetdefault_id;

            MetadataSet mds = MetadataHelper.getMetadataset(ApplicationInfoList.getHomeRepository(),mdsSet);
            MetadataWidget widget = mds.findWidget(mdsWidgetId);
            Map<String, Collection<MetadataKey.MetadataKeyRelated>> valuespaceMappingByRelation = widget.getValuespaceMappingByRelation(MetadataKey.MetadataKeyRelated.Relation.closeMatch);
            Collection<MetadataKey.MetadataKeyRelated> metadataKeyRelateds = valuespaceMappingByRelation.get(key);
            if(metadataKeyRelateds != null && metadataKeyRelateds.size() > 0){
               return metadataKeyRelateds.iterator().next().getKey();
            }
        }catch (Exception e){
            logger.debug(e.getMessage(),e);
        }
        return null;
    }

}