/*
 * This file is part of Alpine.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) Steve Springett. All Rights Reserved.
 */
package alpine.model;

import alpine.Config;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.Size;
import javax.jdo.annotations.Column;
import javax.jdo.annotations.Element;
import javax.jdo.annotations.Extension;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.Join;
import javax.jdo.annotations.Order;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.Unique;
import java.io.Serializable;
import java.security.Principal;
import java.util.Date;
import java.util.List;

/**
 * Persistable object representing an ApiKey.
 *
 * @author Steve Springett
 * @since 1.0.0
 */
@PersistenceCapable
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiKey implements Serializable, Principal {

    private static final long serialVersionUID = 1582714693932260365L;

    public static final String PREFIX = Config.getInstance().getProperty(Config.AlpineKey.API_KEY_PREFIX);
    public static final int PREFIX_LENGTH = PREFIX.length();
    public static final int PUBLIC_ID_LENGTH = 5;
    public static final int API_KEY_LENGTH = 32;
    public static final char API_KEY_SEPARATOR = '_';
    public static final int LEGACY_FULL_KEY_LENGTH = API_KEY_LENGTH; // Alpine <2.2.3
    public static final int LEGACY_WITH_PREFIX_FULL_KEY_LENGTH = PREFIX.length() + API_KEY_LENGTH; // Alpine <3.2.0

    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.NATIVE)
    @JsonIgnore
    private long id;

    @Schema(description = "The full, plain-text key. Only set when initially created, or regenerated.", nullable = true)
    private transient String key;

    @JsonIgnore
    private transient String secret;

    /**
     * Hex-encoded SHA3-256 hash of the secret portion of the API key.
     * Nullable to allow for smooth migration from Alpine {@code <3.2.0} to {@code 3.2.0}.
     *
     * @since 3.2.0
     */
    @Persistent
    @Column(name = "SECRET_HASH", length = 64, allowsNull = "true")
    @JsonIgnore
    private String secretHash;

    @Persistent
    @Column(name = "COMMENT")
    @Size(max = 255)
    private String comment;

    @Persistent
    @Column(name = "CREATED")
    private Date created;

    @Persistent
    @Column(name = "LAST_USED")
    private Date lastUsed;

    @Persistent(table = "APIKEYS_TEAMS", defaultFetchGroup = "true")
    @Join(column = "APIKEY_ID")
    @Element(column = "TEAM_ID")
    @Order(extensions = @Extension(vendorName = "datanucleus", key = "list-ordering", value = "name ASC"))
    @JsonIgnore
    private List<Team> teams;

    @Persistent
    @Unique(name = "APIKEY_PUBLIC_IDX")
    @Size(min = PUBLIC_ID_LENGTH, max = PUBLIC_ID_LENGTH)
    @Column(name = "PUBLIC_ID", allowsNull = "true")
    private String publicId;

    @Persistent
    @Column(name = "IS_LEGACY", allowsNull = "false", defaultValue = "false")
    private boolean isLegacy = false;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public String getSecretHash() {
        return secretHash;
    }

    public void setSecretHash(String secretHash) {
        this.secretHash = secretHash;
    }

    /**
     * Masks all key characters except the prefix and the public ID with *.
     *
     * @return Masked key.
     */
    public String getMaskedKey() {
        return PREFIX + publicId + "*".repeat(API_KEY_LENGTH);
    }

    /**
     * Do not use - only here to satisfy Principal implementation requirement.
     *
     * @return a String presentation of the username
     * @deprecated use {@link UserPrincipal#getUsername()}
     */
    @Deprecated
    @JsonIgnore
    public String getName() {
        return getMaskedKey();
    }

    public String getComment() {
        return comment;
    }

    public void setComment(final String comment) {
        this.comment = comment;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(final Date created) {
        this.created = created;
    }

    public Date getLastUsed() {
        return lastUsed;
    }

    public void setLastUsed(final Date lastUsed) {
        this.lastUsed = lastUsed;
    }

    public List<Team> getTeams() {
        return teams;
    }

    public void setTeams(List<Team> teams) {
        this.teams = teams;
    }

    public String getPublicId() {
        return publicId;
    }

    public void setPublicId(String publicID) {
        this.publicId = publicID;
    }

    public boolean isLegacy() {
        return isLegacy;
    }

    public void setLegacy(boolean isLegacy) {
        this.isLegacy = isLegacy;
    }

}
