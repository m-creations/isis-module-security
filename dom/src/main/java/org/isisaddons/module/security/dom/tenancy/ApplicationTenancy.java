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
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.isis.applib.DomainObjectContainer;
import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.Collection;
import org.apache.isis.applib.annotation.CollectionLayout;
import org.apache.isis.applib.annotation.Editing;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.Optionality;
import org.apache.isis.applib.annotation.Parameter;
import org.apache.isis.applib.annotation.ParameterLayout;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.annotation.PropertyLayout;
import org.apache.isis.applib.annotation.RenderType;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.annotation.Title;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.util.ObjectContracts;
import org.isisaddons.module.security.SecurityModule;
import org.isisaddons.module.security.dom.user.ApplicationUser;
import org.isisaddons.module.security.dom.user.ApplicationUserRepository;

import com.google.common.collect.Lists;

@SuppressWarnings("UnusedDeclaration")

public class ApplicationTenancy implements Comparable<ApplicationTenancy> {

    public static abstract class PropertyDomainEvent<T> extends SecurityModule.PropertyDomainEvent<ApplicationTenancy, T> {}

    public static abstract class CollectionDomainEvent<T> extends SecurityModule.CollectionDomainEvent<ApplicationTenancy, T> {}

    public static abstract class ActionDomainEvent extends SecurityModule.ActionDomainEvent<ApplicationTenancy> {}

    // //////////////////////////////////////

    public static final int MAX_LENGTH_PATH = 255;
    public static final int MAX_LENGTH_NAME = 40;
    public static final int TYPICAL_LENGTH_NAME = 20;

    //region > name (property, title)

    public static class NameDomainEvent extends PropertyDomainEvent<String> {}

    protected String name;


    @Title
    @Property(
            domainEvent = NameDomainEvent.class,
            editing = Editing.DISABLED
    )
    @PropertyLayout(
            typicalLength=TYPICAL_LENGTH_NAME
    )
    @MemberOrder(sequence = "1")
    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    //endregion

    //region > updateName (action)

    public static class UpdateNameDomainEvent extends ActionDomainEvent {}

    @Action(
            domainEvent =UpdateNameDomainEvent.class,
            semantics = SemanticsOf.IDEMPOTENT
    )
    @MemberOrder(name="name", sequence = "1")
    public ApplicationTenancy updateName(
            @Parameter(maxLength = MAX_LENGTH_NAME)
            @ParameterLayout(named="Name", typicalLength=TYPICAL_LENGTH_NAME)
            final String name) {
        setName(name);
        return this;
    }

    public String default0UpdateName() {
        return getName();
    }
    //endregion

    //region > path

    public static class PathDomainEvent extends PropertyDomainEvent<String> {}

    protected String path;



    @Property(
            domainEvent = PathDomainEvent.class,
            editing = Editing.DISABLED
    )
    public String getPath() {
        return path;
    }

    public void setPath(final String path) {
        this.path = path;
    }

    //endregion

    //region > users (collection)

    public static class UsersDomainEvent extends CollectionDomainEvent<ApplicationUser> {}


    
    //region > parent (property)

    public static class ParentDomainEvent extends PropertyDomainEvent<ApplicationTenancy> {}

    protected JdoApplicationTenancy parent;


    @Property(
            domainEvent = ParentDomainEvent.class,
            editing = Editing.DISABLED
    )
    @PropertyLayout(
            hidden = Where.PARENTED_TABLES
    )
    public JdoApplicationTenancy getParent() {
        return parent;
    }

    public void setParent(final ApplicationTenancy parent) {
        this.parent = (JdoApplicationTenancy) parent;
    }

    //endregion

    //region > updateParent (action)

    public static class UpdateParentDomainEvent extends ActionDomainEvent {}

    @Action(
            domainEvent = UpdateParentDomainEvent.class,
            semantics = SemanticsOf.IDEMPOTENT
    )
    @MemberOrder(name="parent", sequence = "1")
    public ApplicationTenancy updateParent(
            @Parameter(optionality = Optionality.OPTIONAL)
            final ApplicationTenancy tenancy
    ) {
        // no need to add to children set, since will be done by JDO/DN.
        setParent(tenancy);
        return this;
    }

    public ApplicationTenancy default0UpdateParent() {
        return getParent();
    }
    //endregion


    //region > children

    public static class ChildrenDomainEvent extends CollectionDomainEvent<ApplicationTenancy> {}





   


    //region > delete (action)
    public static class DeleteDomainEvent extends ActionDomainEvent {}

    @Action(
            domainEvent = DeleteDomainEvent.class,
            semantics = SemanticsOf.NON_IDEMPOTENT
    )
    @MemberOrder(sequence = "1")
    public List<? extends ApplicationTenancy> delete(
            @Parameter(optionality = Optionality.OPTIONAL)
            @ParameterLayout(named="Are you sure?")
            final Boolean areYouSure) {
        /*for (final ApplicationUser user : getUsers()) {
            user.updateTenancy(null);
        }*/
        container.removeIfNotAlready(this);
        container.flush();
        return applicationTenancyRepository.allTenancies();
    }

    public String validateDelete(final Boolean areYouSure) {
        return not(areYouSure) ? "Please confirm this action": null;
    }

    public Boolean default0Delete() {
        return Boolean.FALSE;
    }

    static boolean not(final Boolean areYouSure) {
        return areYouSure == null || !areYouSure;
    }
    //endregion

    //region > compareTo


    @Override
    public String toString() {
        return ObjectContracts.toString(this, "path,name");
    }

    @Override
    public int compareTo(final ApplicationTenancy o) {
        return ObjectContracts.compare(this, o, "path");
    }
    //endregion

    //region  >  (injected)
    @javax.inject.Inject
    ApplicationUserRepository applicationUserRepository;
    @javax.inject.Inject
    ApplicationTenancyRepository applicationTenancyRepository;
    @javax.inject.Inject
    DomainObjectContainer container;
    //endregion
}
