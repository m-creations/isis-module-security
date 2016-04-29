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

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.VersionStrategy;

import com.google.common.collect.Lists;

import org.apache.isis.applib.DomainObjectContainer;
import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.BookmarkPolicy;
import org.apache.isis.applib.annotation.Collection;
import org.apache.isis.applib.annotation.CollectionLayout;
import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.DomainObjectLayout;
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
import org.apache.isis.applib.annotation.ViewModel;
import org.apache.isis.applib.annotation.ViewModelLayout;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.util.ObjectContracts;

import org.isisaddons.module.security.SecurityModule;
import org.isisaddons.module.security.dom.user.ApplicationUser;
import org.isisaddons.module.security.dom.user.ApplicationUserRepository;
import org.isisaddons.module.security.dom.user.ApplicationUserV;

/*@SuppressWarnings("UnusedDeclaration")
@javax.jdo.annotations.PersistenceCapable(
        identityType = IdentityType.APPLICATION,
        schema = "isissecurity",
        table = "ApplicationTenancy")
@javax.jdo.annotations.Inheritance(
        strategy = InheritanceStrategy.NEW_TABLE)
@javax.jdo.annotations.DatastoreIdentity(
        strategy = IdGeneratorStrategy.NATIVE, column = "id")
@javax.jdo.annotations.Version(
        strategy = VersionStrategy.VERSION_NUMBER,
        column = "version")
@javax.jdo.annotations.Uniques({
        @javax.jdo.annotations.Unique(
                name = "ApplicationTenancy_name_UNQ", members = { "name" })
})
@javax.jdo.annotations.Queries( {
        @javax.jdo.annotations.Query(
                name = "findByPath", language = "JDOQL",
                value = "SELECT "
                        + "FROM org.isisaddons.module.security.dom.tenancy.ApplicationTenancyV "
                        + "WHERE path == :path"),
        @javax.jdo.annotations.Query(
                name = "findByName", language = "JDOQL",
                value = "SELECT "
                        + "FROM org.isisaddons.module.security.dom.tenancy.ApplicationTenancyV "
                        + "WHERE name == :name"),
        @javax.jdo.annotations.Query(
                name = "findByNameOrPathMatching", language = "JDOQL",
                value = "SELECT "
                        + "FROM org.isisaddons.module.security.dom.tenancy.ApplicationTenancyV "
                        + "WHERE name.matches(:regex) || path.matches(:regex) ")})
@DomainObject(
        objectType = "isissecurity.ApplicationTenancy",
        autoCompleteRepository = ApplicationTenancyRepository.class,
        autoCompleteAction = "autoComplete"
)
@DomainObjectLayout(
        bookmarking = BookmarkPolicy.AS_ROOT
)*/
@ViewModel
@ViewModelLayout(
        bookmarking = BookmarkPolicy.AS_ROOT
)
public class ApplicationTenancyV implements Comparable<ApplicationTenancyV> {

    public static abstract class PropertyDomainEvent<T> extends SecurityModule.PropertyDomainEvent<ApplicationTenancy, T> {}

    public static abstract class CollectionDomainEvent<T> extends SecurityModule.CollectionDomainEvent<ApplicationTenancy, T> {}

    public static abstract class ActionDomainEventV extends SecurityModule.ActionDomainEvent<ApplicationTenancy> {}

    // //////////////////////////////////////

    public static final int MAX_LENGTH_PATH = 255;
    public static final int MAX_LENGTH_NAME = 40;
    public static final int TYPICAL_LENGTH_NAME = 20;

    //region > name (property, title)

    public static class NameDomainEventV extends PropertyDomainEvent<String> {}

    private String name;

    //@javax.jdo.annotations.Column(allowsNull="false", length = MAX_LENGTH_NAME)
    @Title
    @Property(
            domainEvent = NameDomainEventV.class,
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

//    public static class UpdateNameDomainEventV extends ActionDomainEvent {}

    @Action(
//            domainEvent =UpdateNameDomainEventV.class,
            semantics = SemanticsOf.IDEMPOTENT
    )
    @MemberOrder(name="name", sequence = "1")
    public ApplicationTenancyV updateName(
            @Parameter(maxLength = MAX_LENGTH_NAME)
            @ParameterLayout(named="Name", typicalLength=TYPICAL_LENGTH_NAME)
            final String name) {
        setName(name);
        return this;
    }

    public String xdefault0UpdateName() {
        return getName();
    }
    //endregion

    //region > path

    public static class PathDomainEventV extends PropertyDomainEvent<String> {}

    private String path;

    //@javax.jdo.annotations.PrimaryKey
    //@javax.jdo.annotations.Column(length = MAX_LENGTH_PATH, allowsNull = "false")
    @Property(
            domainEvent = PathDomainEventV.class,
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

//    public static class UsersDomainEventV extends CollectionDomainEvent<ApplicationUserV> {}

    //@javax.jdo.annotations.Persistent(mappedBy = "tenancy")
    private SortedSet<ApplicationUserV> users = new TreeSet<>();

    @Collection(
//            domainEvent = UsersDomainEventV.class,
            editing = Editing.DISABLED
    )
    @CollectionLayout(
            render = RenderType.EAGERLY
    )
    @MemberOrder(sequence = "10")
    public SortedSet<ApplicationUserV> getUsers() {
        return users;
    }

    public void setUsers(final SortedSet<ApplicationUserV> users) {
        this.users = users;
    }

    // necessary for integration tests
    public void addToUsers(final ApplicationUserV applicationUser) {
        getUsers().add(applicationUser);
    }
    // necessary for integration tests
    public void removeFromUsers(final ApplicationUserV applicationUser) {
        getUsers().remove(applicationUser);
    }
    //endregion

    //region > addUser (action)

//    public static class AddUserDomainEventV extends ActionDomainEvent {}

    @Action(
//           domainEvent = AddUserDomainEventV.class,
            semantics = SemanticsOf.IDEMPOTENT
    )
    @ActionLayout(
            named="Add"
    )
    @MemberOrder(name="Users", sequence = "1")
    public ApplicationTenancyV addUser(final ApplicationUserV applicationUser) {
        applicationUser.setTenancy(this);
        // no need to add to users set, since will be done by JDO/DN.
        return this;
    }

    /*public List<ApplicationUserV> autoComplete0AddUser(final String search) {
        final List<ApplicationUserV> matchingSearch = applicationUserRepository.find(search);
        final List<ApplicationUserV> list = Lists.newArrayList(matchingSearch);
        list.removeAll(getUsers());
        return list;
    }*/

    //endregion

    //region > removeUser (action)

//    public static class RemoveUserDomainEventV extends ActionDomainEvent {}

    @Action(
//            domainEvent = RemoveUserDomainEventV.class,
            semantics = SemanticsOf.IDEMPOTENT
    )
    @ActionLayout(
            named="Remove"
    )
    @MemberOrder(name="Users", sequence = "2")
    public ApplicationTenancyV removeUser(final ApplicationUserV applicationUser) {
        applicationUser.setTenancy(null);
        // no need to add to users set, since will be done by JDO/DN.
        return this;
    }
    public java.util.Collection<ApplicationUserV> xchoices0RemoveUser() {
        return getUsers();
    }
    public String disableRemoveUser(final ApplicationUserV applicationUser) {
        return xchoices0RemoveUser().isEmpty()? "No users to remove": null;
    }

    //endregion

    //region > parent (property)

    public static class ParentDomainEventV extends PropertyDomainEvent<ApplicationTenancy> {}

    private ApplicationTenancyV parent;

    //@javax.jdo.annotations.Column(name = "parentPath", allowsNull = "true")
    @Property(
            domainEvent = ParentDomainEventV.class,
            editing = Editing.DISABLED
    )
    @PropertyLayout(
            hidden = Where.PARENTED_TABLES
    )
    public ApplicationTenancyV getParent() {
        return parent;
    }

    public void setParent(final ApplicationTenancyV parent) {
        this.parent = parent;
    }

    //endregion

    //region > updateParent (action)

//    public static class UpdateParentDomainEventV extends ActionDomainEvent {}

    @Action(
//            domainEvent = UpdateParentDomainEventV.class,
            semantics = SemanticsOf.IDEMPOTENT
    )
    @MemberOrder(name="parent", sequence = "1")
    public ApplicationTenancyV updateParent(
            @Parameter(optionality = Optionality.OPTIONAL)
            final ApplicationTenancyV tenancy
    ) {
        // no need to add to children set, since will be done by JDO/DN.
        setParent(tenancy);
        return this;
    }

    public ApplicationTenancyV xdefault0UpdateParent() {
        return getParent();
    }
    //endregion


    //region > children

    public static class ChildrenDomainEventV extends CollectionDomainEvent<ApplicationTenancy> {}

    //@javax.jdo.annotations.Persistent(mappedBy = "parent")
    private SortedSet<ApplicationTenancyV> children = new TreeSet<>();

    @Collection(
            domainEvent = ChildrenDomainEventV.class,
            editing = Editing.DISABLED
    )
    @CollectionLayout(
            render = RenderType.EAGERLY
    )
    public SortedSet<ApplicationTenancyV> getChildren() {
        return children;
    }

    public void setChildren(final SortedSet<ApplicationTenancyV> children) {
        this.children = children;
    }

    // necessary for integration tests
    public void addToChildren(final ApplicationTenancyV applicationTenancy) {
        getChildren().add(applicationTenancy);
    }
    // necessary for integration tests
    public void removeFromChildren(final ApplicationTenancyV applicationTenancy) {
        getChildren().remove(applicationTenancy);
    }
    //endregion

    //region > addChild (action)

//    public static class AddChildDomainEventV extends ActionDomainEvent {}

    @Action(
//            domainEvent = AddChildDomainEventV.class,
            semantics = SemanticsOf.IDEMPOTENT
    )
    @ActionLayout(
            named="Add"
    )
    @MemberOrder(name="Children", sequence = "1")
    public ApplicationTenancyV addChild(final ApplicationTenancyV applicationTenancy) {
        applicationTenancy.setParent(this);
        // no need to add to children set, since will be done by JDO/DN.
        return this;
    }

    //endregion

    //region > removeChild (action)

//    public static class RemoveChildDomainEventV extends ActionDomainEvent {}

    @Action(
//            domainEvent = RemoveChildDomainEventV.class,
            semantics = SemanticsOf.IDEMPOTENT
    )
    @MemberOrder(name="Children", sequence = "2")
    public ApplicationTenancyV removeChild(final ApplicationTenancyV applicationTenancy) {
        applicationTenancy.setParent(null);
        // no need to remove from children set, since will be done by JDO/DN.
        return this;
    }
    public java.util.Collection<ApplicationTenancyV> xchoices0RemoveChild() {
        return getChildren();
    }
    public String disableRemoveChild(final ApplicationTenancyV applicationTenancy) {
        return xchoices0RemoveChild().isEmpty()? "No children to remove": null;
    }

    //endregion


    //region > delete (action)
//    public static class DeleteDomainEventV extends ActionDomainEvent {}

    @Action(
//            domainEvent = DeleteDomainEventV.class,
            semantics = SemanticsOf.NON_IDEMPOTENT
    )
    @MemberOrder(sequence = "1")
    public List<? extends ApplicationTenancy> delete(
            @Parameter(optionality = Optionality.OPTIONAL)
            @ParameterLayout(named="Are you sure?")
            final Boolean areYouSure) {
        for (final ApplicationUserV user : getUsers()) {
            user.updateTenancy(null);
        }
        container.removeIfNotAlready(this);
        container.flush();
        return applicationTenancyRepository.allTenancies();
    }

    public String validateDelete(final Boolean areYouSure) {
        return not(areYouSure) ? "Please confirm this action": null;
    }

    public Boolean xdefault0Delete() {
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
    public int compareTo(final ApplicationTenancyV o) {
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
