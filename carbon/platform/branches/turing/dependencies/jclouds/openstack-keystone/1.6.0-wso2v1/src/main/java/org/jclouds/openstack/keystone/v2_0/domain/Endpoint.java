/*
 * Licensed to jclouds, Inc. (jclouds) under one or more
 * contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  jclouds licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jclouds.openstack.keystone.v2_0.domain;

import static com.google.common.base.Preconditions.checkNotNull;

import java.beans.ConstructorProperties;
import java.net.URI;

import org.jclouds.javax.annotation.Nullable;

import com.google.common.base.Objects;
import com.google.common.base.Objects.ToStringHelper;

/**
 * An network-accessible address, usually described by URL, where a service may be accessed. If
 * using an extension for templates, you can create an endpoint template, which represents the
 * templates of all the consumable services that are available across the regions.
 *
 * @author AdrianCole
 * @see <a href="http://docs.openstack.org/api/openstack-identity-service/2.0/content/Identity-Endpoint-Concepts-e1362.html"
/>
 */
public class Endpoint {

   public static Builder<?> builder() {
      return new ConcreteBuilder();
   }

   public Builder<?> toBuilder() {
      return new ConcreteBuilder().fromEndpoint(this);
   }

   public static abstract class Builder<T extends Builder<T>>  {
      protected abstract T self();

      protected String versionId;
      protected String region;
      protected URI publicURL;
      protected URI internalURL;
      protected URI adminURL;
      protected String tenantId;
      protected URI versionInfo;
      protected URI versionList;

      /**
       * @see Endpoint#getVersionId()
       */
      public T versionId(String versionId) {
         this.versionId = versionId;
         return self();
      }

      /**
       * @see Endpoint#getRegion()
       */
      public T region(String region) {
         this.region = region;
         return self();
      }

      /**
       * @see Endpoint#getPublicURL()
       */
      public T publicURL(URI publicURL) {
         this.publicURL = publicURL;
         return self();
      }

      /**
       * @see Endpoint#getInternalURL()
       */
      public T internalURL(URI internalURL) {
         this.internalURL = internalURL;
         return self();
      }

      /**
       * @see Endpoint#getAdminURL()
       */
      public T adminURL(URI adminURL) {
         this.adminURL = adminURL;
         return self();
      }

      /**
       * @see Endpoint#getVersionInfo()
       */
      public T versionInfo(URI versionInfo) {
         this.versionInfo = versionInfo;
         return self();
      }

      /**
       * @see Endpoint#getVersionList()
       */
      public T versionList(URI versionList) {
         this.versionList = versionList;
         return self();
      }

      /**
       * @see Endpoint#getTenantId()
       */
      public T tenantId(String tenantId) {
         this.tenantId = tenantId;
         return self();
      }

      public Endpoint build() {
         return new Endpoint(null, versionId, region, publicURL, internalURL, adminURL, versionInfo, versionList, null, tenantId);
      }

      public T fromEndpoint(Endpoint in) {
         return this
               .versionId(in.getVersionId())
               .region(in.getRegion())
               .publicURL(in.getPublicURL())
               .internalURL(in.getInternalURL())
               .adminURL(in.getAdminURL())
               .versionInfo(in.getVersionInfo())
               .versionList(in.getVersionList())
               .tenantId(in.getTenantId());
      }
   }

   private static class ConcreteBuilder extends Builder<ConcreteBuilder> {
      @Override
      protected ConcreteBuilder self() {
         return this;
      }
   }

   private final String versionId;
   private final String region;
   private final URI publicURL;
   private final URI internalURL;
   private final URI adminURL;
   private final URI versionInfo;
   private final URI versionList;
   private final String tenantId;

   @ConstructorProperties({
         "id", "versionId", "region", "publicURL", "internalURL", "adminURL", "versionInfo", "versionList", "tenantName", "tenantId"
   })
   protected Endpoint(@Nullable String id, @Nullable String versionId, @Nullable String region, @Nullable URI publicURL,
                      @Nullable URI internalURL, @Nullable URI adminURL, @Nullable URI versionInfo, @Nullable URI versionList,
                      @Nullable String tenantName, @Nullable String tenantId) {
      this.versionId = versionId != null ? versionId : null;
      this.tenantId = tenantId != null ? tenantId : tenantName;
      this.region = region;
      this.publicURL = publicURL;
      this.internalURL = internalURL;
      this.adminURL = adminURL;
      this.versionInfo = versionInfo;
      this.versionList = versionList;
   }

   /**
    * When providing an ID, it is assumed that the endpoint exists in the current OpenStack
    * deployment
    *
    * @return the versionId of the endpoint in the current OpenStack deployment, or null if not specified
    */
   @Nullable
   public String getVersionId() {
      return this.versionId;
   }

   /**
    * @return the region of the endpoint, or null if not specified
    */
   @Nullable
   public String getRegion() {
      return this.region;
   }

   /**
    * @return the public url of the endpoint
    */
   @Nullable
   public URI getPublicURL() {
      return this.publicURL;
   }

   /**
    * @return the internal url of the endpoint
    */
   @Nullable
   public URI getInternalURL() {
      return this.internalURL;
   }

   /**
    * @return the admin url of the endpoint
    */
   @Nullable
   public URI getAdminURL() {
      return this.adminURL;
   }

   @Nullable
   public URI getVersionInfo() {
      return this.versionInfo;
   }

   @Nullable
   public URI getVersionList() {
      return this.versionList;
   }

   /**
    * @return the tenant versionId of the endpoint or null
    */
   @Nullable
   public String getTenantId() {
      return this.tenantId;
   }

   @Override
   public int hashCode() {
      return Objects.hashCode(versionId, region, publicURL, internalURL, adminURL, versionInfo, versionList, tenantId);
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) return true;
      if (obj == null || getClass() != obj.getClass()) return false;
      Endpoint that = Endpoint.class.cast(obj);
      return Objects.equal(this.versionId, that.versionId)
            && Objects.equal(this.region, that.region)
            && Objects.equal(this.publicURL, that.publicURL)
            && Objects.equal(this.internalURL, that.internalURL)
            && Objects.equal(this.adminURL, that.adminURL)
            && Objects.equal(this.versionInfo, that.versionInfo)
            && Objects.equal(this.versionList, that.versionList)
            && Objects.equal(this.tenantId, that.tenantId);
   }

   protected ToStringHelper string() {
      return Objects.toStringHelper(this)
            .add("versionId", versionId).add("region", region).add("publicURL", publicURL).add("internalURL", internalURL)
            .add("adminURL", adminURL).add("versionInfo", versionInfo).add("versionList", versionList).add("tenantId", tenantId);
   }

   @Override
   public String toString() {
      return string().toString();
   }

}
