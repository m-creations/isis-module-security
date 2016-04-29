/*
 *  Copyright 2014 Dan Haywood
 *
 *  Licensed under the Apache License, Version 2.0 (the
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
package org.isisaddons.module.security.dom.user;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.isisaddons.module.security.dom.permission.ApplicationPermissionModeTest;

public class ApplicationUserStatusTest {

    public static class ToString extends ApplicationPermissionModeTest {

        @Test
        public void happyCase() throws Exception {
            assertThat(ApplicationUserStatus.DISABLED.toString(), is("DISABLED"));
        }
    }

    public static class Parse extends ApplicationPermissionModeTest {

        @Test
        public void whenTrue() throws Exception {
            assertThat(ApplicationUserStatus.parse(Boolean.TRUE), is(ApplicationUserStatus.ENABLED));
        }

        @Test
        public void whenFalse() throws Exception {
            assertThat(ApplicationUserStatus.parse(Boolean.FALSE), is(ApplicationUserStatus.DISABLED));
        }

        @Test
        public void whenNull() throws Exception {
            assertThat(ApplicationUserStatus.parse(Boolean.FALSE), is(ApplicationUserStatus.DISABLED));
        }
    }

}