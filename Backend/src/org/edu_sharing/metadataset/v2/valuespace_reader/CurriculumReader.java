package org.edu_sharing.metadataset.v2.valuespace_reader;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.log4j.Logger;
import org.edu_sharing.metadataset.v2.MetadataKey;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CurriculumReader extends ValuespaceReader{
    private static final String ASSOCIATION_IS_CHILD_OF = "isChildOf";
    private static final String ASSOCIATION_PRECEDES = "precedes";
    private String url;
    private static Logger logger = Logger.getLogger(CurriculumReader.class);

    public CurriculumReader(String valuespaceUrl) {
        // e.g. http://localhost:8000/api/v1/curricula/metadatasets
        String openSaltRegex="(https?:\\/\\/.*\\/)api\\/v1\\/curricula\\/metadatasets";
        Pattern pattern = Pattern.compile(openSaltRegex);
        Matcher matched = pattern.matcher(valuespaceUrl);
        if(matched.matches()){
            url = valuespaceUrl;
            logger.info("matched Curriculum at "+matched.group(1));
        }
    }

    public List<MetadataKey> getValuespace() throws Exception {
        List<MetadataKey> result = new ArrayList<>();
        JSONArray list = getApi();
        for (int i = 0; i < list.length(); i++) {
            JSONArray subList = list.getJSONArray(i);
            for (int j = 0; j < subList.length(); j++) {
                JSONObject entry = subList.getJSONObject(j);
                result.add(convertEntry(entry));
            }
        }
        return result;
    }

    private MetadataKey convertEntry(JSONObject entry) throws JSONException {
        MetadataKey key = new MetadataKey();
        key.setCaption(entry.getString("title"));
        key.setKey(entry.getString("id"));
        key.setParent(entry.isNull("parent_id") ? null : entry.getString("parent_id"));
        return key;
    }

    private List<String> getAssocs(String key, String assocName, JSONArray associations) throws IOException, JSONException {
        List<String> assocs=new ArrayList<>();
        for(int i=0;i<associations.length();i++){
            if(associations.getJSONObject(i).getJSONObject("originNodeURI").getString("identifier").equals(key) && associations.getJSONObject(i).getString("associationType").equals(assocName)){
                String assoc = associations.getJSONObject(i).getJSONObject("destinationNodeURI").getString("identifier");
                if(!assoc.equals(key))
                    assocs.add(assoc);
            }
        }
        return assocs;
    }

    private String getParentId(String key, String mainParent, JSONArray associations) throws IOException, JSONException {
        for(String assoc : getAssocs(key,ASSOCIATION_IS_CHILD_OF,associations)){
            if(!assoc.equals(mainParent))
                return assoc;
        }
        return null;
    }

    private JSONArray getApi() throws IOException, JSONException {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpUriRequest request=new HttpGet(getApiUrl());
        CloseableHttpResponse result = httpclient.execute(request);
        String data=StreamUtils.copyToString(result.getEntity().getContent(), StandardCharsets.UTF_8);
        result.close();
        return new JSONArray(data);
    }
    private String getApiUrl() {
        return url;
    }

    @Override
    protected boolean supportsUrl() {
        return url != null;
    }
}
