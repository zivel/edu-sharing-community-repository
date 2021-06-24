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
import java.util.ArrayList;
import java.util.List;

/**
 * AvailableMds
 */
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2021-06-09T17:32:21.273+02:00")
public class AvailableMds {
  @SerializedName("repository")
  private String repository = null;

  @SerializedName("mds")
  private List<String> mds = null;

  public AvailableMds repository(String repository) {
    this.repository = repository;
    return this;
  }

   /**
   * Get repository
   * @return repository
  **/
  @ApiModelProperty(value = "")
  public String getRepository() {
    return repository;
  }

  public void setRepository(String repository) {
    this.repository = repository;
  }

  public AvailableMds mds(List<String> mds) {
    this.mds = mds;
    return this;
  }

  public AvailableMds addMdsItem(String mdsItem) {
    if (this.mds == null) {
      this.mds = new ArrayList<String>();
    }
    this.mds.add(mdsItem);
    return this;
  }

   /**
   * Get mds
   * @return mds
  **/
  @ApiModelProperty(value = "")
  public List<String> getMds() {
    return mds;
  }

  public void setMds(List<String> mds) {
    this.mds = mds;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AvailableMds availableMds = (AvailableMds) o;
    return Objects.equals(this.repository, availableMds.repository) &&
        Objects.equals(this.mds, availableMds.mds);
  }

  @Override
  public int hashCode() {
    return Objects.hash(repository, mds);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AvailableMds {\n");
    
    sb.append("    repository: ").append(toIndentedString(repository)).append("\n");
    sb.append("    mds: ").append(toIndentedString(mds)).append("\n");
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

