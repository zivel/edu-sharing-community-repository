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
import io.swagger.client.model.Catalog;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * MediacenterProfileExtension
 */
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2021-06-09T17:32:21.273+02:00")
public class MediacenterProfileExtension {
  @SerializedName("id")
  private String id = null;

  @SerializedName("location")
  private String location = null;

  @SerializedName("districtAbbreviation")
  private String districtAbbreviation = null;

  @SerializedName("mainUrl")
  private String mainUrl = null;

  @SerializedName("catalogs")
  private List<Catalog> catalogs = null;

  /**
   * Gets or Sets contentStatus
   */
  @JsonAdapter(ContentStatusEnum.Adapter.class)
  public enum ContentStatusEnum {
    ACTIVATED("Activated"),
    
    DEACTIVATED("Deactivated");

    private String value;

    ContentStatusEnum(String value) {
      this.value = value;
    }

    public String getValue() {
      return value;
    }

    @Override
    public String toString() {
      return String.valueOf(value);
    }

    public static ContentStatusEnum fromValue(String text) {
      for (ContentStatusEnum b : ContentStatusEnum.values()) {
        if (String.valueOf(b.value).equals(text)) {
          return b;
        }
      }
      return null;
    }

    public static class Adapter extends TypeAdapter<ContentStatusEnum> {
      @Override
      public void write(final JsonWriter jsonWriter, final ContentStatusEnum enumeration) throws IOException {
        jsonWriter.value(enumeration.getValue());
      }

      @Override
      public ContentStatusEnum read(final JsonReader jsonReader) throws IOException {
        String value = jsonReader.nextString();
        return ContentStatusEnum.fromValue(String.valueOf(value));
      }
    }
  }

  @SerializedName("contentStatus")
  private ContentStatusEnum contentStatus = null;

  public MediacenterProfileExtension id(String id) {
    this.id = id;
    return this;
  }

   /**
   * Get id
   * @return id
  **/
  @ApiModelProperty(value = "")
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public MediacenterProfileExtension location(String location) {
    this.location = location;
    return this;
  }

   /**
   * Get location
   * @return location
  **/
  @ApiModelProperty(value = "")
  public String getLocation() {
    return location;
  }

  public void setLocation(String location) {
    this.location = location;
  }

  public MediacenterProfileExtension districtAbbreviation(String districtAbbreviation) {
    this.districtAbbreviation = districtAbbreviation;
    return this;
  }

   /**
   * Get districtAbbreviation
   * @return districtAbbreviation
  **/
  @ApiModelProperty(value = "")
  public String getDistrictAbbreviation() {
    return districtAbbreviation;
  }

  public void setDistrictAbbreviation(String districtAbbreviation) {
    this.districtAbbreviation = districtAbbreviation;
  }

  public MediacenterProfileExtension mainUrl(String mainUrl) {
    this.mainUrl = mainUrl;
    return this;
  }

   /**
   * Get mainUrl
   * @return mainUrl
  **/
  @ApiModelProperty(value = "")
  public String getMainUrl() {
    return mainUrl;
  }

  public void setMainUrl(String mainUrl) {
    this.mainUrl = mainUrl;
  }

  public MediacenterProfileExtension catalogs(List<Catalog> catalogs) {
    this.catalogs = catalogs;
    return this;
  }

  public MediacenterProfileExtension addCatalogsItem(Catalog catalogsItem) {
    if (this.catalogs == null) {
      this.catalogs = new ArrayList<Catalog>();
    }
    this.catalogs.add(catalogsItem);
    return this;
  }

   /**
   * Get catalogs
   * @return catalogs
  **/
  @ApiModelProperty(value = "")
  public List<Catalog> getCatalogs() {
    return catalogs;
  }

  public void setCatalogs(List<Catalog> catalogs) {
    this.catalogs = catalogs;
  }

  public MediacenterProfileExtension contentStatus(ContentStatusEnum contentStatus) {
    this.contentStatus = contentStatus;
    return this;
  }

   /**
   * Get contentStatus
   * @return contentStatus
  **/
  @ApiModelProperty(value = "")
  public ContentStatusEnum getContentStatus() {
    return contentStatus;
  }

  public void setContentStatus(ContentStatusEnum contentStatus) {
    this.contentStatus = contentStatus;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    MediacenterProfileExtension mediacenterProfileExtension = (MediacenterProfileExtension) o;
    return Objects.equals(this.id, mediacenterProfileExtension.id) &&
        Objects.equals(this.location, mediacenterProfileExtension.location) &&
        Objects.equals(this.districtAbbreviation, mediacenterProfileExtension.districtAbbreviation) &&
        Objects.equals(this.mainUrl, mediacenterProfileExtension.mainUrl) &&
        Objects.equals(this.catalogs, mediacenterProfileExtension.catalogs) &&
        Objects.equals(this.contentStatus, mediacenterProfileExtension.contentStatus);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, location, districtAbbreviation, mainUrl, catalogs, contentStatus);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class MediacenterProfileExtension {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    location: ").append(toIndentedString(location)).append("\n");
    sb.append("    districtAbbreviation: ").append(toIndentedString(districtAbbreviation)).append("\n");
    sb.append("    mainUrl: ").append(toIndentedString(mainUrl)).append("\n");
    sb.append("    catalogs: ").append(toIndentedString(catalogs)).append("\n");
    sb.append("    contentStatus: ").append(toIndentedString(contentStatus)).append("\n");
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

