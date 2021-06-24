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
import java.io.IOException;

/**
 * ColumnV2
 */
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2021-06-09T17:32:21.273+02:00")
public class ColumnV2 {
  @SerializedName("id")
  private String id = null;

  @SerializedName("format")
  private String format = null;

  @SerializedName("showDefault")
  private Boolean showDefault = false;

  public ColumnV2 id(String id) {
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

  public ColumnV2 format(String format) {
    this.format = format;
    return this;
  }

   /**
   * Get format
   * @return format
  **/
  @ApiModelProperty(value = "")
  public String getFormat() {
    return format;
  }

  public void setFormat(String format) {
    this.format = format;
  }

  public ColumnV2 showDefault(Boolean showDefault) {
    this.showDefault = showDefault;
    return this;
  }

   /**
   * Get showDefault
   * @return showDefault
  **/
  @ApiModelProperty(value = "")
  public Boolean isShowDefault() {
    return showDefault;
  }

  public void setShowDefault(Boolean showDefault) {
    this.showDefault = showDefault;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ColumnV2 columnV2 = (ColumnV2) o;
    return Objects.equals(this.id, columnV2.id) &&
        Objects.equals(this.format, columnV2.format) &&
        Objects.equals(this.showDefault, columnV2.showDefault);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, format, showDefault);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ColumnV2 {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    format: ").append(toIndentedString(format)).append("\n");
    sb.append("    showDefault: ").append(toIndentedString(showDefault)).append("\n");
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

