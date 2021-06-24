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
 * Create
 */
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2021-06-09T17:32:21.273+02:00")
public class Create {
  @SerializedName("onlyMetadata")
  private Boolean onlyMetadata = false;

  public Create onlyMetadata(Boolean onlyMetadata) {
    this.onlyMetadata = onlyMetadata;
    return this;
  }

   /**
   * Get onlyMetadata
   * @return onlyMetadata
  **/
  @ApiModelProperty(value = "")
  public Boolean isOnlyMetadata() {
    return onlyMetadata;
  }

  public void setOnlyMetadata(Boolean onlyMetadata) {
    this.onlyMetadata = onlyMetadata;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Create create = (Create) o;
    return Objects.equals(this.onlyMetadata, create.onlyMetadata);
  }

  @Override
  public int hashCode() {
    return Objects.hash(onlyMetadata);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Create {\n");
    
    sb.append("    onlyMetadata: ").append(toIndentedString(onlyMetadata)).append("\n");
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

