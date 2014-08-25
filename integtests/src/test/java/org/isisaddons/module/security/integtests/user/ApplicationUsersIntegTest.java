/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.isisaddons.module.security.integtests.user;

import java.util.List;
import javax.inject.Inject;
import javax.jdo.JDODataStoreException;
import org.isisaddons.module.security.dom.actor.ApplicationUser;
import org.isisaddons.module.security.dom.actor.ApplicationUsers;
import org.isisaddons.module.security.fixture.scripts.SecurityModuleAppTearDown;
import org.isisaddons.module.security.integtests.SecurityModuleAppIntegTest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.core.Is.is;

public class ApplicationUsersIntegTest extends SecurityModuleAppIntegTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUpData() throws Exception {
        scenarioExecution().install(new SecurityModuleAppTearDown());
    }

    @Inject
    ApplicationUsers applicationUsers;

    public static class NewUser extends ApplicationUsersIntegTest {

        @Test
        public void happyCase() throws Exception {

            // given
            final List<ApplicationUser> before = applicationUsers.allUsers();
            Assert.assertThat(before.size(), is(0));

            // when
            final ApplicationUser applicationUser = applicationUsers.newUser("fred");
            Assert.assertThat(applicationUser.getName(), is("fred"));

            // then
            final List<ApplicationUser> after = applicationUsers.allUsers();
            Assert.assertThat(after.size(), is(1));
        }

        @Test
        public void alreadyExists() throws Exception {

            // then
            expectedExceptions.expect(JDODataStoreException.class);

            // given
            applicationUsers.newUser("fred");

            // when
            applicationUsers.newUser("fred");
        }
    }

    public static class FindByName extends ApplicationUsersIntegTest {

        @Test
        public void happyCase() throws Exception {

            // given
            applicationUsers.newUser("fred");
            applicationUsers.newUser("mary");

            // when
            final ApplicationUser fred = applicationUsers.findUserByName("fred");

            // then
            Assert.assertThat(fred, is(not(nullValue())));
            Assert.assertThat(fred.getName(), is("fred"));
        }

        @Test
        public void whenDoesntMatch() throws Exception {

            // given
            applicationUsers.newUser("fred");
            applicationUsers.newUser("mary");

            // when
            final ApplicationUser nonExistent = applicationUsers.findUserByName("bill");

            // then
            Assert.assertThat(nonExistent, is(nullValue()));
        }
    }

    public static class AutoComplete extends ApplicationUsersIntegTest {

        @Test
        public void happyCase() throws Exception {

            // given
            applicationUsers.newUser("fred");
            applicationUsers.newUser("mary");
            applicationUsers.newUser("bill");

            // when
            final List<ApplicationUser> after = applicationUsers.autoComplete("r");

            // then
            Assert.assertThat(after.size(), is(2)); // fred and mary
        }
    }

    public static class AllTenancies extends ApplicationUsersIntegTest {

        @Test
        public void happyCase() throws Exception {

            // given
            applicationUsers.newUser("fred");
            applicationUsers.newUser("mary");

            // when
            final List<ApplicationUser> after = applicationUsers.allUsers();

            // then
            Assert.assertThat(after.size(), is(2));
        }
    }


}