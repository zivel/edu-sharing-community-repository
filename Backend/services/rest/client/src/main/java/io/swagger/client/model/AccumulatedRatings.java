/*
 * edu-sharing Repository REST API
 * The public restful API of the edu-sharing repository.
 *
 * OpenAPI spec version: 1.1
 * 
 *
 * NOTE: This class is auto generated by the swagger code generator program.
 * https://github.com/swagger-api/swagger-codegen.git
 * Do not edit the class manually.
 */


package io.swagger.client.model;

import java.util.Objects;
import java.util.Arrays;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.client.model.RatingData;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AccumulatedRatings
 */
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2020-11-30T10:38:59.102+01:00")
public class AccumulatedRatings {
  @SerializedName("overall")
  private RatingData overall = null;

  @SerializedName("user")
  private Double user = null;

  @SerializedName("affiliation")
  private Map<String, RatingData> affiliation = null;

  public AccumulatedRatings overall(RatingData overall) {
    this.overall = overall;
    return this;
  }

   /**
   * Get overall
   * @return overall
  **/
  @ApiModelProperty(value = "")
  public RatingData getOverall() {
    return overall;
  }

  public void setOverall(RatingData overall) {
    this.overall = overall;
  }

  public AccumulatedRatings user(Double user) {
    this.user = user;
    return this;
  }

   /**
   * Get user
   * @return user
  **/
  @ApiModelProperty(value = "")
  public Double getUser() {
    return user;
  }

  public void setUser(Double user) {
    this.user = user;
  }

  public AccumulatedRatings affiliation(Map<String, RatingData> affiliation) {
    this.affiliation = affiliation;
    return this;
  }

  public AccumulatedRatings putAffiliationItem(String key, RatingData affiliationItem) {
    if (this.affiliation == null) {
      this.affiliation = new HashMap<String, RatingData>();
    }
    this.affiliation.put(key, affiliationItem);
    return this;
  }

   /**
   * Get affiliation
   * @return affiliation
  **/
  @ApiModelProperty(value = "")
  public Map<String, RatingData> getAffiliation() {
    return affiliation;
  }

  public void setAffiliation(Map<String, RatingData> affiliation) {
    this.affiliation = affiliation;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AccumulatedRatings accumulatedRatings = (AccumulatedRatings) o;
    return Objects.equals(this.overall, accumulatedRatings.overall) &&
        Objects.equals(this.user, accumulatedRatings.user) &&
        Objects.equals(this.affiliation, accumulatedRatings.affiliation);
  }

  @Override
  public int hashCode() {
    return Objects.hash(overall, user, affiliation);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AccumulatedRatings {\n");
    
    sb.append("    overall: ").append(toIndentedString(overall)).append("\n");
    sb.append("    user: ").append(toIndentedString(user)).append("\n");
    sb.append("    affiliation: ").append(toIndentedString(affiliation)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }

}

