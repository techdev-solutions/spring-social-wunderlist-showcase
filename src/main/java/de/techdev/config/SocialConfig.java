/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.techdev.config;

import de.techdev.user.SecurityContext;
import de.techdev.user.SimpleConnectionSignUp;
import de.techdev.user.SimpleSignInAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.social.UserIdSource;
import org.springframework.social.config.annotation.ConnectionFactoryConfigurer;
import org.springframework.social.config.annotation.EnableSocial;
import org.springframework.social.config.annotation.SocialConfigurer;
import org.springframework.social.connect.Connection;
import org.springframework.social.connect.ConnectionFactoryLocator;
import org.springframework.social.connect.ConnectionRepository;
import org.springframework.social.connect.UsersConnectionRepository;
import org.springframework.social.connect.jdbc.JdbcUsersConnectionRepository;
import org.springframework.social.connect.web.ProviderSignInController;
import org.springframework.social.wunderlist.api.Wunderlist;
import org.springframework.social.wunderlist.connect.WunderlistConnectionFactory;

import javax.inject.Inject;
import javax.sql.DataSource;

/**
 * @author Alexander Hanschke
 */
@EnableSocial
@Configuration
public class SocialConfig implements SocialConfigurer {

    @Inject
    private DataSource dataSource;

    @Override
    public void addConnectionFactories(ConnectionFactoryConfigurer configurer, Environment env) {
        String id = env.getProperty("wunderlist.client.id");
        String secret = env.getProperty("wunderlist.client.secret");

        if (id.isEmpty()) {
            throw new IllegalStateException("wunderlist.client.id is not set");
        }

        if (secret.isEmpty()) {
            throw new IllegalStateException("wunderlist.client.secret is not set");
        }

        configurer.addConnectionFactory(new WunderlistConnectionFactory(id, secret));
    }

    @Override
    public UserIdSource getUserIdSource() {
        return () -> SecurityContext.getCurrentUser().getId();
    }

    @Override
    public UsersConnectionRepository getUsersConnectionRepository(ConnectionFactoryLocator locator) {
        JdbcUsersConnectionRepository repo = new JdbcUsersConnectionRepository(dataSource, locator, Encryptors.noOpText());
        repo.setConnectionSignUp(new SimpleConnectionSignUp());
        return repo;
    }

    @Bean
    @Scope(value = "request", proxyMode = ScopedProxyMode.INTERFACES)
    public Wunderlist wunderlist(ConnectionRepository repo) {
        Connection<Wunderlist> wunderlist = repo.findPrimaryConnection(Wunderlist.class);
        return wunderlist != null ? wunderlist.getApi() : null;
    }

    @Bean
    public ProviderSignInController providerSignInController(ConnectionFactoryLocator locator, UsersConnectionRepository repo) {
        return new ProviderSignInController(locator, repo, new SimpleSignInAdapter());
    }
}
