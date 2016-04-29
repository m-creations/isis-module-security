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
package org.isisaddons.module.security.dom.permission;

import java.util.List;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.DomainServiceLayout;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.RestrictTo;
import org.apache.isis.applib.annotation.SemanticsOf;

import org.isisaddons.module.security.SecurityModule;

@DomainService(
        nature = NatureOfService.VIEW_MENU_ONLY
)
@DomainServiceLayout(
        named="Security",
        menuBar = DomainServiceLayout.MenuBar.SECONDARY,
        menuOrder = "100.50"
)
public class ApplicationPermissionMenu {

        //region > domain event classes
        public static abstract class PropertyDomainEvent<T> extends SecurityModule.PropertyDomainEvent<ApplicationPermissions, T> {}

        public static abstract class CollectionDomainEvent<T> extends SecurityModule.CollectionDomainEvent<ApplicationPermissions, T> {}

        public static abstract class ActionDomainEvent extends SecurityModule.ActionDomainEvent<ApplicationPermissions> {}
        //endregion

        //region > iconName
        public String iconName() {
            return "applicationPermission";
        }
        //endregion

        //region > allPermission (action)
        public static class AllPermissionsDomainEvent extends ActionDomainEvent {}

        @Action(
                domainEvent=AllPermissionsDomainEvent.class,
                semantics = SemanticsOf.SAFE,
                restrictTo = RestrictTo.PROTOTYPING
        )
        @MemberOrder(sequence = "100.50.1")
        public List<?  extends ApplicationPermission> allPermissions() {
                return applicationPermissionRepository.allPermissions();
        }
        //endregion

        //region > inject
        @Inject
        private ApplicationPermissionRepository applicationPermissionRepository;
        //endregion

}
