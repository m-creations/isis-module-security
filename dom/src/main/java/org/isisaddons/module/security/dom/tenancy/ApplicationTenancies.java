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
package org.isisaddons.module.security.dom.tenancy;

import java.util.List;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.Optionality;
import org.apache.isis.applib.annotation.Parameter;
import org.apache.isis.applib.annotation.ParameterLayout;
import org.apache.isis.applib.annotation.RestrictTo;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.annotation.Where;

import org.isisaddons.module.security.SecurityModule;

/**
 * @deprecated - replaced by {@link ApplicationTenancyMenu}
 */
@Deprecated
@DomainService(nature = NatureOfService.DOMAIN)
public class ApplicationTenancies {

    //region > domain event classes
    public static abstract class PropertyDomainEvent<T> extends SecurityModule.PropertyDomainEvent<ApplicationTenancies, T> {}

    public static abstract class CollectionDomainEvent<T> extends SecurityModule.CollectionDomainEvent<ApplicationTenancies, T> {}

    public static abstract class ActionDomainEvent extends SecurityModule.ActionDomainEvent<ApplicationTenancies> {}
    //endregion

    //region > iconName
    public String iconName() {
        return "applicationTenancy";
    }
    //endregion

    //region > findTenancyByName
    public static class FindTenancyByNameDomainEvent extends ActionDomainEvent {
    }

    /**
     * @deprecated - use {@link ApplicationTenancyMenu#findTenancies(String)} instead.
     */
    @Deprecated
    @Action(
            domainEvent = FindTenancyByNameDomainEvent.class,
            semantics = SemanticsOf.SAFE,
            hidden = Where.EVERYWHERE // since deprecated
    )
    public ApplicationTenancy findTenancyByName(
            @Parameter(maxLength = ApplicationTenancy.MAX_LENGTH_NAME)
            @ParameterLayout(named = "Name", typicalLength = ApplicationTenancy.TYPICAL_LENGTH_NAME)
            final String name) {
        return applicationTenancyRepository.findByName(name);
    }
    //endregion

    //region > findTenancyByPath

    public static class FindTenancyByPathDomainEvent extends ActionDomainEvent {}

    /**
     * @deprecated - use {@link ApplicationTenancyMenu#findTenancies(String)} instead.
     */
    @Deprecated
    @Action(
            domainEvent = FindTenancyByPathDomainEvent.class,
            semantics = SemanticsOf.SAFE,
            hidden = Where.EVERYWHERE // since deprecated
    )
    public ApplicationTenancy findTenancyByPath(
            @Parameter(maxLength = ApplicationTenancy.MAX_LENGTH_PATH)
            @ParameterLayout(named = "Path")
            final String path) {
        return applicationTenancyRepository.findByPath(path);
    }
    //endregion

    //region > newTenancy
    public static class NewTenancyDomainEvent extends ActionDomainEvent {
    }

    /**
     * @deprecated - use {@link ApplicationTenancyMenu#newTenancy(String, String, ApplicationTenancy)} instead.
     */
    @Action(
            domainEvent = NewTenancyDomainEvent.class,
            semantics = SemanticsOf.IDEMPOTENT,
            hidden = Where.EVERYWHERE
    )
    public ApplicationTenancy newTenancy(
            @Parameter(maxLength = ApplicationTenancy.MAX_LENGTH_NAME)
            @ParameterLayout(named = "Name", typicalLength = ApplicationTenancy.TYPICAL_LENGTH_NAME)
            final String name,
            @Parameter(maxLength = ApplicationTenancy.MAX_LENGTH_PATH)
            @ParameterLayout(named = "Path")
            final String path,
            @Parameter(optionality = Optionality.OPTIONAL)
            @ParameterLayout(named = "Parent")
            final ApplicationTenancy parent) {
        return applicationTenancyRepository.newTenancy(name, path, parent);
    }
    //endregion

    //region > allTenancies
    public static class AllTenanciesDomainEvent extends ActionDomainEvent {
    }

    /**
     * @deprecated - use {@link ApplicationTenancyMenu#allTenancies()} instead.
     */
    @Action(
            domainEvent = AllTenanciesDomainEvent.class,
            semantics = SemanticsOf.SAFE,
            restrictTo = RestrictTo.PROTOTYPING,
            hidden = Where.EVERYWHERE
    )
    @MemberOrder(sequence = "100.30.4")
    public List<? extends ApplicationTenancy> allTenancies() {
        return applicationTenancyRepository.allTenancies();
    }
    //endregion


    //region > injected
    @Inject
    ApplicationTenancyRepository applicationTenancyRepository;
    //endregion

}
