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
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.util.ObjectContracts;

import org.isisaddons.module.security.SecurityModule;
import org.isisaddons.module.security.dom.tenancy.ApplicationTenancy.ActionDomainEvent;
import org.isisaddons.module.security.dom.tenancy.ApplicationTenancy.UsersDomainEvent;
import org.isisaddons.module.security.dom.user.ApplicationUser;
import org.isisaddons.module.security.dom.user.ApplicationUserRepository;
import org.isisaddons.module.security.dom.user.JdoApplicationUser;

@SuppressWarnings("UnusedDeclaration")
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
                        + "FROM org.isisaddons.module.security.dom.tenancy.ApplicationTenancy "
                        + "WHERE path == :path"),
        @javax.jdo.annotations.Query(
                name = "findByName", language = "JDOQL",
                value = "SELECT "
                        + "FROM org.isisaddons.module.security.dom.tenancy.ApplicationTenancy "
                        + "WHERE name == :name"),
        @javax.jdo.annotations.Query(
                name = "findByNameOrPathMatching", language = "JDOQL",
                value = "SELECT "
                        + "FROM org.isisaddons.module.security.dom.tenancy.ApplicationTenancy "
                        + "WHERE name.matches(:regex) || path.matches(:regex) ")})
@DomainObject(
        objectType = "isissecurity.ApplicationTenancy",
        autoCompleteRepository = ApplicationTenancyRepository.class,
        autoCompleteAction = "autoComplete"
)
@DomainObjectLayout(
        bookmarking = BookmarkPolicy.AS_ROOT
)
public class JdoApplicationTenancy extends ApplicationTenancy {


    @javax.jdo.annotations.Column(allowsNull="false", length = MAX_LENGTH_NAME)
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

    //endregion

    //region > updateName (action)



    @Action(
            domainEvent =UpdateNameDomainEvent.class,
            semantics = SemanticsOf.IDEMPOTENT
    )
    @MemberOrder(name="name", sequence = "1")
    public JdoApplicationTenancy updateName(
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



    @javax.jdo.annotations.PrimaryKey
    @javax.jdo.annotations.Column(length = MAX_LENGTH_PATH, allowsNull = "false")
    @Property(
            domainEvent = PathDomainEvent.class,
            editing = Editing.DISABLED
    )
    public String getPath() {
        return path;
    }

    //endregion

    //region > users (collection)



    @javax.jdo.annotations.Persistent(mappedBy = "tenancy")
    @Collection(
            domainEvent = UsersDomainEvent.class,
            editing = Editing.DISABLED
    )
    @CollectionLayout(
            render = RenderType.EAGERLY
    )
    @MemberOrder(sequence = "10")
    protected SortedSet<JdoApplicationUser> users = new TreeSet<>();
    public SortedSet<JdoApplicationUser> getUsers() {
        return users;
    }

    public void setUsers(final SortedSet<JdoApplicationUser> users) {
        this.users = users;
    }

    // necessary for integration tests
    public void addToUsers(final JdoApplicationUser applicationUser) {
        getUsers().add(applicationUser);
    }
    // necessary for integration tests
    public void removeFromUsers(final JdoApplicationUser applicationUser) {
        getUsers().remove(applicationUser);
    }
    //endregion

    //region > addUser (action)

    public static class AddUserDomainEvent extends ActionDomainEvent {}

    @Action(
           domainEvent = AddUserDomainEvent.class,
            semantics = SemanticsOf.IDEMPOTENT
    )
    @ActionLayout(
            named="Add"
    )
    @MemberOrder(name="Users", sequence = "1")
    public ApplicationTenancy addUser(final JdoApplicationUser applicationUser) {
        applicationUser.setTenancy(this);
        // no need to add to users set, since will be done by JDO/DN.
        return this;
    }

    public List<?  extends ApplicationUser> autoComplete0AddUser(final String search) {
        final List<?  extends ApplicationUser> matchingSearch = applicationUserRepository.find(search);
        final List<?  extends ApplicationUser> list = Lists.newArrayList(matchingSearch);
        list.removeAll(getUsers());
        return list;
    }

    //endregion

    //region > removeUser (action)

    public static class RemoveUserDomainEvent extends ActionDomainEvent {}

    @Action(
            domainEvent = RemoveUserDomainEvent.class,
            semantics = SemanticsOf.IDEMPOTENT
    )
    @ActionLayout(
            named="Remove"
    )
    @MemberOrder(name="Users", sequence = "2")
    public ApplicationTenancy removeUser(final JdoApplicationUser applicationUser) {
        applicationUser.setTenancy(null);
        // no need to add to users set, since will be done by JDO/DN.
        return this;
    }
    public java.util.Collection<JdoApplicationUser> choices0RemoveUser() {
        return getUsers();
    }
    public String disableRemoveUser(final JdoApplicationUser applicationUser) {
        return choices0RemoveUser().isEmpty()? "No users to remove": null;
    }

    //endregion


    //region > parent (property)




    @javax.jdo.annotations.Column(name = "parentPath", allowsNull = "true")
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

    //endregion

    //region > updateParent (action)



    @Action(
            domainEvent = UpdateParentDomainEvent.class,
            semantics = SemanticsOf.IDEMPOTENT
    )
    @MemberOrder(name="parent", sequence = "1")
    public JdoApplicationTenancy updateParent(
            @Parameter(optionality = Optionality.OPTIONAL)
            final JdoApplicationTenancy tenancy
    ) {
        // no need to add to children set, since will be done by JDO/DN.
        setParent(tenancy);
        return this;
    }

    public JdoApplicationTenancy default0UpdateParent() {
        return getParent();
    }
    //endregion


    //region > children

    @javax.jdo.annotations.Persistent(mappedBy = "parent")
    private SortedSet<JdoApplicationTenancy> children = new TreeSet<>();



    @Collection(
            domainEvent = ChildrenDomainEvent.class,
            editing = Editing.DISABLED
    )
    @CollectionLayout(
            render = RenderType.EAGERLY
    )
    public SortedSet<JdoApplicationTenancy> getChildren() {
        return children;
    }
    
    public void setChildren(SortedSet<JdoApplicationTenancy> children) {        
        this.children = children;
    }
    
    // necessary for integration tests
    public void addToChildren(JdoApplicationTenancy applicationTenancy) {        
            getChildren().add(applicationTenancy);
        
    }
    // necessary for integration tests
    public void removeFromChildren(final JdoApplicationTenancy applicationTenancy) {
        getChildren().remove(applicationTenancy);
    }
    //endregion


    //region > addChild (action)

    public static class AddChildDomainEvent extends ActionDomainEvent {}

    @Action(
            domainEvent = AddChildDomainEvent.class,
            semantics = SemanticsOf.IDEMPOTENT
    )
    @ActionLayout(
            named="Add"
    )
    @MemberOrder(name="Children", sequence = "1")
    public ApplicationTenancy addChild(final JdoApplicationTenancy applicationTenancy) {
        applicationTenancy.setParent(this);
        // no need to add to children set, since will be done by JDO/DN.
        return this;
    }

    //endregion

    //region > removeChild (action)

    public static class RemoveChildDomainEvent extends ActionDomainEvent {}

    @Action(
            domainEvent = RemoveChildDomainEvent.class,
            semantics = SemanticsOf.IDEMPOTENT
    )
    @MemberOrder(name="Children", sequence = "2")
    public ApplicationTenancy removeChild(final JdoApplicationTenancy applicationTenancy) {
        applicationTenancy.setParent(null);
        // no need to remove from children set, since will be done by JDO/DN.
        return this;
    }
    public java.util.Collection<JdoApplicationTenancy> choices0RemoveChild() {
        return getChildren();
    }
    public String disableRemoveChild(final JdoApplicationTenancy applicationTenancy) {
        return choices0RemoveChild().isEmpty()? "No children to remove": null;
    }

    //endregion


  

    //region > delete (action)


    @Action(
            domainEvent = DeleteDomainEvent.class,
            semantics = SemanticsOf.NON_IDEMPOTENT
    )
    @MemberOrder(sequence = "1")
    public List<? extends ApplicationTenancy> delete(
            @Parameter(optionality = Optionality.OPTIONAL)
            @ParameterLayout(named="Are you sure?")
            final Boolean areYouSure) {
        for (final ApplicationUser user : getUsers()) {
            user.updateTenancy(null);
        }
        container.removeIfNotAlready(this);
        container.flush();
        List<? extends ApplicationTenancy> allTenancies = applicationTenancyRepository.allTenancies();
        
        return allTenancies;
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



    //region  >  (injected)
    @javax.inject.Inject
    ApplicationUserRepository applicationUserRepository;
    @javax.inject.Inject
    ApplicationTenancyRepository applicationTenancyRepository;
    @javax.inject.Inject
    DomainObjectContainer container;
    //endregion
}
